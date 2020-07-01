package pt.tecnico.sauron.silo.replica;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.SiloServerApp;
import pt.tecnico.sauron.silo.domain.request.*;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.UNAVAILABLE;
import static pt.tecnico.sauron.silo.SiloConverter.*;

public class ReplicaFrontend {

    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
    private static final Integer timeout = Integer.parseInt(SiloServerApp.getProps().getProperty("timeout"));
    private static final Integer retries = Integer.parseInt(SiloServerApp.getProps().getProperty("retries"));
    private int retry = 0;
    private final ManagedChannel channel;
    private final SauronGrpc.SauronFutureStub stub;
    private final ReplicaManager repManager;

    /***
     * Creates a replicaFrontend to send gossip messages to a certain replica.
     * Uses FutureStubs in order to not block while waiting for the response.
     * @param repManager the replicaManager of the Replica that sends the request.
     * @param target of the replica that we want to exchange messages.
     ***/
    public ReplicaFrontend(ReplicaManager repManager, String target) {
        this.repManager = repManager;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SauronGrpc.newFutureStub(channel);
        debug("Connection Established");
    }

    /***
     * This function prints a debug message only prints if the debug flag is on
     * @param msg a message
     ***/
    private static void debug(String msg) {
        if (DEBUG_FLAG) System.out.println("Debug: " + msg);
    }


    /***
     * The replica sends its valueTS to the replica established in the constructor.
     * We create a listener in order to continue to send updates to another replicas without blocking.
     * When a response is completed then the executable (command) is called and executed.
     * If response does not throw exceptions then does the replicas updates.
     * When an error associated with connection issues is thrown, we try to send again (max:3 times).
     * @param valueTS of the Replica that sends the request.
     ***/
    public void updateReplica(VecTimestamp valueTS) {

        Silo.UpdateRequest request = Silo.UpdateRequest.newBuilder()
                .addAllValueTS(toSiloVecTS(valueTS))
                .build();

        ListenableFuture<Silo.UpdateResponse> listener = stub
                .withDeadlineAfter(timeout, TimeUnit.SECONDS)
                .updateReplica(request);

        debug("Sending a non-blocking request");

        listener.addListener(() -> {
            try {
                List<Update> updates = toDomainUpdates(listener.get().getUpdatesList());
                this.repManager.doUpdates(updates);
                this.channel.shutdown();
            } catch (SauronException e) {
                /*
                 * Never Thrown
                 * Because updateLog has no Updates that throws Exceptions
                 */
                System.err.println("Exception in updateReplica (Unexpected)");
                System.exit(1);
            } catch (StatusRuntimeException e) {
                Status.Code code = e.getStatus().getCode();
                if (code == UNAVAILABLE || code == DEADLINE_EXCEEDED) {
                    Thread.currentThread().interrupt();
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }, command -> {
            command.run();
            if (Thread.currentThread().isInterrupted() && this.retry++ < retries) {
                debug("Non-Blocking Request failed due to connection issues");
                debug(String.format("Try %d, of sending our TS to this replica", this.retry + 1));
                updateReplica(valueTS);
            }
        });
    }
}
