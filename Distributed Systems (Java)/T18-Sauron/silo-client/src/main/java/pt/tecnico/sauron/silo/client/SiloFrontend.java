package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import static pt.tecnico.sauron.silo.client.exception.ErrorMessage.*;

import static io.grpc.Status.Code.*;

import io.grpc.Status;
import io.grpc.Status.Code;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SiloFrontend {

    private static final boolean DEBUG_FLAG = System.getProperty("debug") != null;
    private static final String rootPath = "/grpc/sauron/silo";
    private final Integer timeout;
    private final Integer maxRetry;
    private final String instance;
    private final ZKNaming zkNaming;
    private Integer crrRetry;
    private ManagedChannel channel;
    private SauronGrpc.SauronBlockingStub stub;
    private CamJoinRequest camJoinRequest = null;
    private List<Entry> prevTS = new ArrayList<>();

    /***
     * This Constructor tries to establish a connection with a server, using a random Replica.
     * @param zooHost is the Host of ZooKeeper.
     * @param zooPort is the port of ZooKeeper.
     * @param timeout is the time to wait for a response on a request.
     * @param retries  is the number of times we try to reconnect to the server due to connection issues.
     * @throws SiloFrontendException, that indicates if the connection with the server was successful.
     ***/
    public SiloFrontend(String zooHost, String zooPort, Integer timeout, Integer retries) throws SiloFrontendException {
        debug("Trying to Connect To a Random Replica");
        this.timeout = timeout;
        this.maxRetry = retries;
        this.instance = null;
        this.checkZooPort(zooPort);
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        this.crrRetry = 0;
        this.connectHandler();
    }

    /***
     * This Constructor tries to establish a connection with a server, using a specified Replica.
     * @param zooHost is the Host of ZooKeeper.
     * @param zooPort is the port of ZooKeeper.
     * @param timeout is the time to wait for a response on a request.
     * @param retries  is the number of times we try to reconnect to the server due to connection issues.
     * @param instance is the ID of the replica to connect.
     * @throws SiloFrontendException, that indicates if the connection with the server was successful.
     ***/
    public SiloFrontend(String zooHost, String zooPort, String instance, Integer timeout, Integer retries) throws SiloFrontendException {
        debug("Trying to Connect To Replica " + instance);
        this.timeout = timeout;
        this.maxRetry = retries;
        this.instance = instance;
        this.checkInstance();
        this.checkZooPort(zooPort);
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        this.crrRetry = 0;
        this.connectHandler();
    }

    /***
     * This function prints a debug message only prints if the debug flag is on.
     * @param msg a message
     ***/
    private static void debug(String msg) {
        if (DEBUG_FLAG) System.out.println("Debug: " + msg);
    }

    /***
     * This function tries to send a cam_join request to the server, and saves this cam_join.
     * to resend when a crash happens. Tries to send at max this.retries times.
     * @param request an update to send to the server.
     ***/
    public CamJoinResponse cam_join(CamJoinRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Cam_Join Request", (this.crrRetry + 1)));
            try {
                CamJoinRequest req = CamJoinRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();

                CamJoinResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .camJoin(req);

                debug("Cam_Join Response was received");
                this.prevTS = response.getNewTSList();
                this.camJoinRequest = request;
                return response;
            } catch (StatusRuntimeException e) {
                this.camJoinRequest = null;
                requestHandler(e);
            }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a cam_info request to the server. Tries to send at max this.retries times.
     * @param request a query to send to the server.
     ***/
    public CamInfoResponse cam_info(CamInfoRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Cam_Info Request", (this.crrRetry + 1)));
            try {
                CamInfoRequest req = CamInfoRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();
                CamInfoResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .camInfo(req);

                debug("Cam_Info Response was received");
                this.prevTS = response.getNewTSList();
                return response;
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a report request to the server. Tries to send at max this.retries times.
     * @param request an update to send to the server.
     ***/
    public ReportResponse report(ReportRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Report Request", (this.crrRetry + 1)));
            try {
                ReportRequest req = ReportRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();
                ReportResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .report(req);
                debug("Report Response was received");
                this.prevTS = response.getNewTSList();
                return response;
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a track request to the server. Tries to send at max this.retries  times.
     * @param request a query to send to the server.
     ***/
    public TrackResponse track(TrackRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Report Request", (this.crrRetry + 1)));
            try {
                TrackRequest req = TrackRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();
                TrackResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .track(req);
                debug("Track Response was received");
                this.prevTS = response.getNewTSList();
                return response;
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a trackMatch request to the server. Tries to send at max this.retries times.
     * @param request a query to send to the server.
     ***/
    public TrackMatchResponse trackMatch(TrackMatchRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of trackMatch Request", (this.crrRetry + 1)));
            try {
                TrackMatchRequest req = TrackMatchRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();
                TrackMatchResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .trackMatch(req);
                debug("TrackMatch Response was received");
                this.prevTS = response.getNewTSList();
                return response;
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a trace request to the server. Tries to send at max this.retries times.
     * @param request a query to send to the server.
     ***/
    public TraceResponse trace(TraceRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Trace Request", (this.crrRetry + 1)));
            try {
                TraceRequest req = TraceRequest.newBuilder(request)
                        .addAllPrevTS(this.prevTS)
                        .build();
                TraceResponse response = this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .trace(req);
                debug("TrackMatch Response was received");
                this.prevTS = response.getNewTSList();
                return response;
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a ctrl_ping request to the server. Tries to send  at max this.retries times.
     * @param request a query to send to the server.
     ***/
    public CtrlPingResponse ctrl_ping(CtrlPingRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Ctrl_Ping Request", (this.crrRetry + 1)));
            try {
                return this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .ctrlPing(request);
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a ctrl_clear request to the server. Tries to send at max this.retries times.
     * @param request an update to send to the server.
     ***/
    public CtrlClearResponse ctrl_clear(CtrlClearRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Ctrl_Clear Request", (this.crrRetry + 1)));
            try {
                return this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .ctrlClear(request);
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function tries to send a ctrl_init request to the server. Tries to send at max this.retries times.
     * @param request an update to send to the server.
     ***/
    public CtrlInitResponse ctrl_init(CtrlInitRequest request) {
        for (this.crrRetry = 0; this.crrRetry < this.maxRetry; this.crrRetry++) {
            debug(String.format("Try %d of Ctrl_Init Request", (this.crrRetry + 1)));
            try {
                return this.stub
                        .withDeadlineAfter(this.timeout, TimeUnit.SECONDS)
                        .ctrlInit(request);
            }
            catch (StatusRuntimeException e) { requestHandler(e); }
        }
        throw new StatusRuntimeException(Status.UNAVAILABLE);
    }

    /***
     * This function shuts down the server.
     ***/
    public void shutdown() {
        this.channel.shutdown();
    }

    /***
     * This function handle's StatusRunTimeExceptions, trying to reconnect to the server,
     * if the error was due to connection issues (UNAVAILABLE DEADLINE_EXCEEDED).
     * If the connection was successfully reestablished, then tries to do cam_join first if it was made before.
     * The cam_join is sent in order to keep different replicas with same information about the camera.
     * @param e an exception to handle.
     * @throws StatusRuntimeException when the error was not due to connection issues
     ***/
    public void requestHandler(StatusRuntimeException e) {

        Code code = e.getStatus().getCode();
        if (!(code == UNAVAILABLE || code == DEADLINE_EXCEEDED)) {
            debug("Sending error to Client...");
            throw e;
        }
        this.shutdown();
        debug("Received an error due to connection issues, with code: " + code);
        debug("Reconnecting Client");
        try {
            this.connectHandler();
        } catch(SiloFrontendException s) {
            throw e;
        }

        if (this.camJoinRequest != null) {
            debug("Sending Cam_Join Request again");
            this.cam_join(this.camJoinRequest);
        }
    }

    /***
     * This function, tries to establish a reconnection to the server.
     * @throws SiloFrontendException when can't establish a reconnection to a replica
     ***/
    public void connectHandler() throws SiloFrontendException {
        while (true) {
            debug(String.format("Try %d of Connection with Server", (this.crrRetry + 1)));
            try {
                if (this.instance != null) this.connectRep();
                else this.connectRandRep();
                break;
            } catch (SiloFrontendException s) {
                if ((++this.crrRetry).equals(this.maxRetry)) throw s;
            }
        }
    }

    /***
     * This function tries to establish a connection with a random Replica.
     * @throws SiloFrontendException indicates that the list of records were not found
     ***/
    public void connectRandRep() throws SiloFrontendException {
        ZKRecord[] records;

        try {
            records = this.zkNaming.listRecords(rootPath).toArray(ZKRecord[]::new);
        } catch (ZKNamingException e) {
            throw new SiloFrontendException(RECORDS_NOT_FOUND, rootPath);
        }
        if (records.length == 0) throw new SiloFrontendException(RECORDS_NOT_FOUND, rootPath);
        ZKRecord record = records[new Random().nextInt(records.length)];

        debug(String.format("Selected Random Replica %s",
                record.getPath().substring(record.getPath().lastIndexOf('/') + 1)));

        this.channel = ManagedChannelBuilder.forTarget(record.getURI()).usePlaintext().build();
        this.stub = SauronGrpc.newBlockingStub(this.channel);
        debug("Connection Established");
    }

    /***
     * This function tries to establish a connection with a certain Replica.
     * @throws SiloFrontendException that indicates that the record wasn't found
     ***/
    public void connectRep() throws SiloFrontendException {
        ZKRecord record;
        try {
            record = this.zkNaming.lookup(rootPath + "/" + this.instance);
        } catch (ZKNamingException e) {
            throw new SiloFrontendException(RECORDS_NOT_FOUND, rootPath + "/" + this.instance);
        }
        this.channel = ManagedChannelBuilder.forTarget(record.getURI()).usePlaintext().build();
        this.stub = SauronGrpc.newBlockingStub(this.channel);
        debug("Connection Established");
    }

    /***
     * This function verifies if zooPort is valid.
     * @param zooPort a port.
     * @throws SiloFrontendException, that indicates de zoo port is invalid
     ***/
    private void checkZooPort(String zooPort) throws SiloFrontendException {

        int port;
        try {
            port = Integer.parseInt(zooPort);

        } catch (NumberFormatException e) {
            debug("Sending error to Client...");
            throw new SiloFrontendException(INVALID_PORT, zooPort);
        }
        if (port < 0 || port > 65535) {
            debug("Sending error to Client...");
            throw new SiloFrontendException(INVALID_PORT, zooPort);
        }
    }

    /***
     * This function verifies if instance is valid.
     * @throws SiloFrontendException, that indicates the instance is invalid.
     ***/
    private void checkInstance() throws SiloFrontendException {

        int rep;
        try {
            rep = Integer.parseInt(this.instance);

        } catch (NumberFormatException e) {
            debug("Sending error to Client...");
            throw new SiloFrontendException(INVALID_INSTANCE, this.instance);
        }
        if (rep <= 0) {
            debug("Sending error to Client...");
            throw new SiloFrontendException(INVALID_INSTANCE, this.instance);
        }
    }
}
