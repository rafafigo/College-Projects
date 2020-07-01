package pt.tecnico.sauron.silo.replica;

import io.grpc.Status;
import pt.tecnico.sauron.silo.Sauron;
import pt.tecnico.sauron.silo.SiloServerApp;
import pt.tecnico.sauron.silo.domain.request.Update;
import pt.tecnico.sauron.silo.exceptions.ErrorMessage;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.*;
import java.util.stream.Collectors;

public class ReplicaManager {

    private static final boolean DEBUG_FLAG = System.getProperty("debug") != null;
    private static final Integer timeout = Integer.parseInt(SiloServerApp.getProps().getProperty("timeout"));
    private final Integer ID;
    private final VecTimestamp valueTS;
    private final Set<Update> updateLog;
    private final Sauron sauron;

    /* Debug Messages */
    private static void debug(String msg) {
        if (DEBUG_FLAG) System.out.println("Debug: " + msg);
    }

    public ReplicaManager(Sauron sauron, String zooHost, String zooPort, String rootPath, Integer ID, Long sync) {
        this.ID = ID;
        this.valueTS = new VecTimestamp();
        this.updateLog = new HashSet<>();
        this.sauron = sauron;
        /* Creates a thread to exchange Gossip Messages every sync seconds */
        new Timer().schedule(new UpdateSender(zooHost, zooPort, rootPath), 0, sync * 1000);
    }

    /***
     * This Method:
     *  - Updates the Update.prevTS
     *  - Executes the Update (in the case of a non duplicate one)
     * @param update Update requested
     * @return New Update.prevTS
     * @throws SauronException In the case of the Update didn't executed well
     ***/
    public VecTimestamp update(Update update) throws SauronException {

        synchronized (this.valueTS) {
            update.getPrevTS().update(this.ID, this.valueTS.get(this.ID) + 1);
        }
        debug(String.format("Received %s", update));

        if (this.execUpdate(update) < 0) {
            update.getPrevTS().decrement(this.ID);
        }
        return update.getPrevTS();
    }

    /***
     * This Method:
     *  - Searches on UpdateLog for the relevant Updates to a given replica
     *  i.e. Update.prevTS <= valueTS ? Not relevant : relevant
     * @param valueTS Timestamp of a given replica
     * @return List of recent Updates that the replica does not have
     ***/
    public List<Update> getUpdates(VecTimestamp valueTS) {

        synchronized (this.updateLog) {
            return this.updateLog.stream()
                    .filter(entry -> !entry.getPrevTS().isLess(valueTS, true))
                    .collect(Collectors.toList());
        }
    }

    /***
     * This Method:
     *  - Sorts the given UpdateLog by Update.prevTS
     *  - Executes non duplicate Updates
     * @param updateLog List of recent Updates given from other replicas
     ***/
    public void doUpdates(List<Update> updateLog) throws SauronException {

        Collections.sort(updateLog);

        for (Update update : updateLog) {
            debug(String.format("Received %s", update));
            this.execUpdate(update);
        }
    }

    /***
     * This Method:
     *  - Update is duplicate ? returns : Add it to UpdateLog
     *  - Executes the Update (in the case of a non duplicate one)
     *  - Increments valueTS[Update.ID]
     *  - Notify all threads that are waiting for Updates on readAwait Method
     * @param update Update to be executed
     * @return Update is duplicate ? -1 : 0
     * @throws SauronException In the case of the Update didn't executed well
     ***/
    private int execUpdate(Update update) throws SauronException {

        boolean added;
        synchronized (this.updateLog) { added = this.updateLog.add(update); }
        if (!added) { /* Update is duplicate */ return -1; }
        debug("Executing Update...");

        try {
            update.execute(this.sauron);
        } catch (SauronException e) {
            synchronized (this.updateLog) { this.updateLog.remove(update); }
            debug(String.format("Update execution thrown %s", e.getClass().getSimpleName()));
            throw e;
        }

        synchronized (this.valueTS) { this.valueTS.increment(update.getID()); }
        debug(String.format("Updated Value TS: %s", update.getPrevTS()));
        synchronized (this) { notifyAll(); }
        return 0;
    }

    /***
     * This Method:
     *  - valueTS < prevTS ? Waits for Updates : returns
     * @param prevTS Timestamp of the client who desires to execute a Query
     * @return valueTS Timestamp of the current replica
     * @throws SauronException In case the wait takes too long
     ***/
    public VecTimestamp readAwait(VecTimestamp prevTS) throws SauronException {

        synchronized (this) {
            while (this.valueTS.isLess(prevTS, false)) {
                debug(String.format("%s < %s", this.valueTS, prevTS));
                debug(String.format("Waiting %d seconds...", timeout));
                try {
                    wait(timeout * (long)1000, 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SauronException(Status.DEADLINE_EXCEEDED, ErrorMessage.TIMEOUT);
                }
            }
        }
        return this.getValueTS();
    }

    private VecTimestamp getValueTS() {
        synchronized (this.valueTS) {
            return this.valueTS;
        }
    }

    private class UpdateSender extends TimerTask {
        private final String rootPath;
        private final ZKNaming zkNaming;

        public UpdateSender(String zooHost, String zooPort, String rootPath) {
            this.rootPath = rootPath;
            this.zkNaming = new ZKNaming(zooHost, zooPort);
        }

        /***
         * This Method:
         *  - Searches for all replicas registered in ZooKeeper
         *  - For each one, invokes sendTo Method
         ***/
        @Override
        public void run() {

            ZKRecord[] records;
            try { records = this.zkNaming.listRecords(this.rootPath).toArray(ZKRecord[]::new); }
            catch (ZKNamingException e) { return; }

            /* Foreach Replica */
            for (ZKRecord record : records) {
                this.sendTo(record);
            }
        }

        /***
         * This Method:
         *  - record of current replica ? returns : Exchanges async Gossip Messages with it
         *  Gossip Messages:
         *   - Uses ReplicaFrontend to exchange them
         *   - Request Updates for the current replica by sending its valueTS
         *   - ReplicaFrontend call doUpdates Method to execute the Updates received
         * @param record ZKRecord of a replica
         ***/
        private void sendTo(ZKRecord record) {

            String path = record.getPath();
            String strID = path.substring(path.lastIndexOf('/') + 1);
            if (strID.equals(ReplicaManager.this.ID.toString())) return;

            debug(String.format("Trying to Connect To Replica %s", strID));
            ReplicaFrontend frontend = new ReplicaFrontend(ReplicaManager.this, record.getURI());
            debug(String.format("Sending Gossip Message to Replica %s", strID));
            frontend.updateReplica(getValueTS());
        }
    }
}
