package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.request.*;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import pt.tecnico.sauron.silo.grpc.SauronGrpc.SauronImplBase;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.replica.ReplicaManager;
import pt.tecnico.sauron.silo.replica.VecTimestamp;

import static pt.tecnico.sauron.silo.SiloConverter.*;

public class SauronServiceImpl extends SauronImplBase {

    private static final boolean DEBUG_FLAG = System.getProperty("debug") != null;
    private final Sauron sauron;
    private final Integer ID;
    private final ReplicaManager repManager;

    /* Debug Messages */
    private static void debug(String msg) {
        if (DEBUG_FLAG) System.out.println("Debug: " + msg);
    }

    public SauronServiceImpl(String zooHost, String zooPort, String rootPath, Integer ID, Long sync) {
        this.sauron = new Sauron();
        this.ID = ID;
        this.repManager = new ReplicaManager(this.sauron, zooHost, zooPort, rootPath, ID, sync);
    }

    /***
     * Requests to Join a Camera to the Server
     * @param request contains Camera and a VecTimestamp
     * @param responseObserver contains the updated VecTimestamp
     ***/
    @Override
    public void camJoin(Silo.CamJoinRequest request, StreamObserver<Silo.CamJoinResponse> responseObserver) {
        try {
            Camera camera = toDomainCamera(request.getCamera());
            VecTimestamp prevTS = toDomainVecTS(request.getPrevTSList());
            VecTimestamp newTS = this.repManager.update(new CamJoinUpdate(this.ID, prevTS, camera));

            debug("Responding to a 'camJoin' Request");
            responseObserver.onNext(Silo.CamJoinResponse.newBuilder()
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'camJoin'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests the Server for the Info of a Camera
     * @param request contains the Camera name and a VecTimestamp
     * @param responseObserver contains the requested Camera and the updated VecTimestamp
     ***/
    @Override
    public void camInfo(Silo.CamInfoRequest request, StreamObserver<Silo.CamInfoResponse> responseObserver) {
        try {
            debug("Received 'camInfo' Query");
            VecTimestamp vecTS = toDomainVecTS(request.getPrevTSList());
            VecTimestamp newTS = this.repManager.readAwait(vecTS);
            Silo.Location location = toSiloLocation(this.sauron.cam_info(request.getName()));

            debug("Responding to a 'camInfo' Request");
            responseObserver.onNext(Silo.CamInfoResponse.newBuilder()
                    .setLocation(location)
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'camInfo'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests to Report Observations to the Server
     * @param request contains the Camera name, a list of Observed Objects and a VecTimestamp
     * @param responseObserver contains the updated VecTimestamp
     ***/
    @Override
    public void report(Silo.ReportRequest request, StreamObserver<Silo.ReportResponse> responseObserver) {
        try {
            List<ObservedObject> objects = toDomainObjects(request.getObjectsList());
            VecTimestamp prevTS = toDomainVecTS(request.getPrevTSList());
            Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            ReportUpdate report = new ReportUpdate(this.ID, prevTS, request.getCamName(), objects, instant.getEpochSecond());
            VecTimestamp newTS = this.repManager.update(report);

            debug("Responding to a 'report' Request");
            responseObserver.onNext(Silo.ReportResponse.newBuilder()
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'report'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests the Server to Track an Observed Object by an identifier
     * @param request contains the Observed Object to Track and a VecTimestamp
     * @param responseObserver contains the most recent Observation of the Object and the updated VecTimestamp
     ***/
    @Override
    public void track(Silo.TrackRequest request, StreamObserver<Silo.TrackResponse> responseObserver) {
        try {
            debug("Received 'track' Query");
            VecTimestamp vecTS = toDomainVecTS(request.getPrevTSList());
            VecTimestamp newTS = this.repManager.readAwait(vecTS);
            Observation observation = this.sauron.track(toDomainObject(request.getObject()));

            debug("Responding to a 'track' Request");
            responseObserver.onNext(Silo.TrackResponse.newBuilder()
                    .setObservation(toSiloObservation(observation))
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'track'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests the Server to Track Match an Observed Object by a partial identifier
     * @param request contains the Observed Object to Track Match and a VecTimestamp
     * @param responseObserver contains the most recent Observation of the Object and the updated VecTimestamp
     ***/
    @Override
    public void trackMatch(Silo.TrackMatchRequest request, StreamObserver<Silo.TrackMatchResponse> responseObserver) {
        try {
            debug("Received 'trackMatch' Query");
            VecTimestamp vecTS = toDomainVecTS(request.getPrevTSList());
            VecTimestamp newTS = this.repManager.readAwait(vecTS);
            Silo.ObservedObject object = request.getObject();
            List<Observation> observations = this.sauron.trackMatch(object.getId(), toDomainType(object.getType()));

            debug("Responding to a 'trackMatch' Request");
            responseObserver.onNext(Silo.TrackMatchResponse.newBuilder()
                    .addAllObservations(toSiloObservations(observations))
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'trackMatch'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests the Server to Trace an Observed Object by an identifier
     * @param request contains the Observed Object to Trace and a VecTimestamp
     * @param responseObserver contains the list of all Observations of the Object and the updated VecTimestamp
     ***/
    @Override
    public void trace(Silo.TraceRequest request, StreamObserver<Silo.TraceResponse> responseObserver) {
        try {
            debug("Received 'trace' Query");
            VecTimestamp vecTS = toDomainVecTS(request.getPrevTSList());
            VecTimestamp newTS = this.repManager.readAwait(vecTS);
            List<Observation> observations = this.sauron.trace(toDomainObject(request.getObject()));

            debug("Responding to a 'trace' Request");
            responseObserver.onNext(Silo.TraceResponse.newBuilder()
                    .addAllObservations(toSiloObservations(observations))
                    .addAllNewTS(toSiloVecTS(newTS))
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'trace'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests the Server all the Cameras and all the Observations
     * @param request is empty
     * @param responseObserver contains a list of Cameras and a list of Observations
     ***/
    @Override
    public void ctrlPing(Silo.CtrlPingRequest request, StreamObserver<Silo.CtrlPingResponse> responseObserver) {
        try {
            debug("Received 'Ctrl_Ping' Query");
            List<Silo.Camera> cameras = toSiloCameras(this.sauron.getCameras());
            List<Silo.Observation> observations = toSiloObservations(this.sauron.getObservations());

            debug("Responding to a 'Ctrl_Ping' Request");
            responseObserver.onNext(Silo.CtrlPingResponse.newBuilder()
                    .addAllCameras(cameras)
                    .addAllObservations(observations)
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'Ctrl_Ping'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests to Initialize the Server with Cameras and Observations
     * @param request contains a list of Cameras and a list of Observations
     * @param responseObserver is empty
     ***/
    @Override
    public void ctrlInit(Silo.CtrlInitRequest request, StreamObserver<Silo.CtrlInitResponse> responseObserver) {
        try {
            debug("Received 'Ctrl_Init' Update");
            List<Camera> cameras = toDomainCameras(request.getCamerasList());
            List<Observation> observations = toDomainObservations(request.getObservationsList());
            debug("Executing 'Ctrl_Init' Update...");
            this.sauron.ctrl_init(cameras, observations);

            debug("Responding to a 'Ctrl_Init' Request");
            responseObserver.onNext(Silo.CtrlInitResponse.newBuilder()
                    .build());
            responseObserver.onCompleted();
        } catch (SauronException e) {
            debug("Exception was thrown while performing 'Ctrl_Init'");
            responseObserver.onError(e.getStatus().asRuntimeException());
        }
    }

    /***
     * Requests to Clean all the information in the Server
     * @param request is empty
     * @param responseObserver is empty
     ***/
    @Override
    public void ctrlClear(Silo.CtrlClearRequest request, StreamObserver<Silo.CtrlClearResponse> responseObserver) {

        debug("Received 'Ctrl_Clear' Update");
        debug("Executing 'Ctrl_Clear' Update...");
        this.sauron.clear();

        debug("Responding to a 'Ctrl_Clear' Request");
        responseObserver.onNext(Silo.CtrlClearResponse.newBuilder()
                .build());
        responseObserver.onCompleted();
    }

    /***
     * Request sent by other Replicas to ask for Updates
     * @param request contains the sender VecTimestamp
     * @param responseObserver contains a list of Updates the sender needs
     ***/
    @Override
    public void updateReplica(Silo.UpdateRequest request, StreamObserver<Silo.UpdateResponse> responseObserver) {

        debug("Received a Gossip message");
        List<Update> updateLog = repManager.getUpdates(toDomainVecTS(request.getValueTSList()));

        debug("Responding to Gossip message from another replica");
        responseObserver.onNext(Silo.UpdateResponse.newBuilder()
                        .addAllUpdates(toSiloUpdates(updateLog))
                        .build());
        responseObserver.onCompleted();
    }
}
