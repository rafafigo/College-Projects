package pt.tecnico.sauron.silo;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.domain.request.*;
import pt.tecnico.sauron.silo.exceptions.SauronException;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.replica.VecTimestamp;

import java.util.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_REQUEST;
import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.INVALID_TYPE;

/*
 * This Class is used like a Library:
 *  - Convert Domain Objects to Silo Objects using 'toSilo{ObjectName}' Methods
 *  - Convert Silo Objects to Domain Objects using 'toDomain{ObjectName}' Methods
 *  - Throws SauronException where the Object given to be converted is invalid
 */
public class SiloConverter {

    public static ObservedObject.Type toDomainType(Silo.Type reqType) throws SauronException {
        switch (reqType.toString()) {
            case "PERSON":
                return ObservedObject.Type.PERSON;
            case "CAR":
                return ObservedObject.Type.CAR;
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
        }
    }

    public static Camera toDomainCamera(Silo.Camera reqCamera) throws SauronException {

        if (!reqCamera.hasLocation()) {
            throw new SauronException(INVALID_ARGUMENT, INVALID_REQUEST, reqCamera.getName());
        }

        Silo.Location reqLocation = reqCamera.getLocation();
        Location location = new Location(reqLocation.getLatitude(), reqLocation.getLongitude());
        return new Camera(reqCamera.getName(), location);
    }

    public static List<Camera> toDomainCameras(List<Silo.Camera> reqCameras) throws SauronException {

        List<Camera> cameras = new ArrayList<>();

        for (Silo.Camera reqCamera : reqCameras) {
            cameras.add(toDomainCamera(reqCamera));
        }
        return cameras;
    }

    public static ObservedObject toDomainObject(Silo.ObservedObject reqObject) throws SauronException {

        switch (reqObject.getType().toString()) {
            case "PERSON":
                return new Person(reqObject.getId());
            case "CAR":
                return new Car(reqObject.getId());
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
        }
    }

    public static List<ObservedObject> toDomainObjects(List<Silo.ObservedObject> reqObjects) throws SauronException {

        List<ObservedObject> objects = new ArrayList<>();

        for (Silo.ObservedObject reqObject : reqObjects) {
            objects.add(toDomainObject(reqObject));
        }
        return objects;
    }

    public static Observation toDomainObservation(Silo.Observation reqObservation) throws SauronException {

        ObservedObject observedObject = toDomainObject(reqObservation.getObject());
        Camera camera = toDomainCamera(reqObservation.getCamera());
        Long seconds = reqObservation.getTs().getSeconds();

        return new Observation(observedObject, camera, seconds);
    }

    public static List<Observation> toDomainObservations(List<Silo.Observation> reqObservations) throws SauronException {

        List<Observation> observations = new ArrayList<>();

        for (Silo.Observation reqObservation : reqObservations) {
            observations.add(toDomainObservation(reqObservation));
        }
        return observations;
    }

    public static Silo.Location toSiloLocation(Location location) {

        return Silo.Location.newBuilder()
                .setLatitude(location.getLatitude())
                .setLongitude(location.getLongitude())
                .build();
    }

    public static Silo.Camera toSiloCamera(Camera camera) {

        return Silo.Camera.newBuilder()
                .setName(camera.getName())
                .setLocation(toSiloLocation(camera.getLocation()))
                .build();
    }

    public static List<Silo.Camera> toSiloCameras(List<Camera> cameras) {

        List<Silo.Camera> resCameras = new ArrayList<>();

        for (Camera camera : cameras) {
            resCameras.add(toSiloCamera(camera));
        }
        return resCameras;
    }

    public static Silo.ObservedObject toSiloObject(ObservedObject object) throws SauronException {

        switch (object.getType()) {
            case PERSON:
                Person person = (Person)object;
                return Silo.ObservedObject.newBuilder()
                        .setType(Silo.Type.PERSON)
                        .setId(person.getId().toString())
                        .build();
            case CAR:
                Car car = (Car)object;
                return Silo.ObservedObject.newBuilder()
                        .setType(Silo.Type.CAR)
                        .setId(car.getId())
                        .build();
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
        }
    }

    public static List<Silo.ObservedObject> toSiloObjects(List<ObservedObject> objects) throws SauronException {

        List<Silo.ObservedObject> resObjects = new ArrayList<>();

        for (ObservedObject object : objects) {
            resObjects.add(toSiloObject(object));
        }
        return resObjects;
    }

    public static Silo.Observation toSiloObservation(Observation observation) throws SauronException {

        Silo.Camera resCamera = toSiloCamera(observation.getCamera());
        Timestamp resTimestamp = Timestamp.newBuilder()
                .setSeconds(observation.getSeconds())
                .build();
        return Silo.Observation.newBuilder()
                .setObject(toSiloObject(observation.getObservedObject()))
                .setCamera(resCamera)
                .setTs(resTimestamp)
                .build();
    }

    public static List<Silo.Observation> toSiloObservations(List<Observation> observations) throws SauronException {

        List<Silo.Observation> resObservations = new ArrayList<>();

        for (Observation observation : observations) {
            resObservations.add(toSiloObservation(observation));
        }
        return resObservations;
    }

    public static VecTimestamp toDomainVecTS(List<Silo.Entry> prevTS) {

        VecTimestamp vecTS = new VecTimestamp();
        for (Silo.Entry entry : prevTS) {
            vecTS.update(entry.getKey(), entry.getValue());
        }
        return vecTS;
    }

    public static List<Silo.Entry> toSiloVecTS(VecTimestamp prevTS) {

        List<Silo.Entry> vecTS = new ArrayList<>();
        while (prevTS.hasNext()) {
            Map.Entry<Integer, Integer> entry = prevTS.next();
            vecTS.add(Silo.Entry.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(entry.getValue())
                    .build());
        }
        return vecTS;
    }

    public static Update toDomainUpdate(Silo.Update update) throws SauronException {

        if (update.hasCamJoin()) return toDomainCamJoin(update.getID(), update.getPrevTSList(), update.getCamJoin());
        if (update.hasReport()) return toDomainReport(update.getID(), update.getPrevTSList(), update.getReport());
        throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
    }

    public static List<Update> toDomainUpdates(List<Silo.Update> updates) {

        List<Update> resUpdates = new ArrayList<>();

        for (Silo.Update update : updates) {
            try {
                resUpdates.add(toDomainUpdate(update));
            } catch (SauronException e) {
                /*
                 * Never Thrown
                 * Because updateLog has no Updates that throws Exceptions
                 */
                System.err.println("Exception in toDomainUpdates (Unexpected)");
                System.exit(1);
            }
        }
        return resUpdates;
    }

    public static CamJoinUpdate toDomainCamJoin(Integer ID, List<Silo.Entry> prevTS, Silo.CamJoinRequest request) throws SauronException {

        Camera camera = toDomainCamera(request.getCamera());
        return new CamJoinUpdate(ID, toDomainVecTS(prevTS), camera);
    }

    public static ReportUpdate toDomainReport(Integer ID, List<Silo.Entry> prevTS, Silo.ReportRequest request) throws SauronException {

        String camName = request.getCamName();
        List<ObservedObject> objects = toDomainObjects(request.getObjectsList());
        return new ReportUpdate(ID, toDomainVecTS(prevTS), camName, objects, request.getTs().getSeconds());
    }

    public static List<Silo.Update> toSiloUpdates(List<Update> updates) {

        List<Silo.Update> reqUpdates = new ArrayList<>();

        for (Update update : updates) {
            try {
                reqUpdates.add(toSiloUpdate(update));
            } catch (SauronException e) {
                /*
                * Never Thrown
                * Because updateLog has no Updates that throws Exceptions
                */
                System.err.println("Exception in toSiloUpdates (Unexpected)");
                System.exit(1);
            }
        }
        return reqUpdates;
    }

    public static Silo.Update toSiloUpdate(Update update) throws SauronException {

        Silo.Update.Builder reqUpdate = Silo.Update.newBuilder()
                .setID(update.getID())
                .addAllPrevTS(toSiloVecTS(update.getPrevTS()));

        switch (update.getType()) {
            case CAM_JOIN:
                return reqUpdate.setCamJoin(toSiloCamJoin((CamJoinUpdate)update)).build();
            case CAM_REPORT:
                return reqUpdate.setReport(toSiloReport((ReportUpdate)update)).build();
            default: throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
        }
    }

    public static Silo.CamJoinRequest toSiloCamJoin(CamJoinUpdate update) {

        Silo.Camera camera = toSiloCamera(update.getCamera());
        return Silo.CamJoinRequest.newBuilder().setCamera(camera).build();
    }

    public static Silo.ReportRequest toSiloReport(ReportUpdate update) throws SauronException {

        String camName = update.getCamName();
        List<Silo.ObservedObject> objects = toSiloObjects(update.getObjects());
        Timestamp TS = Timestamp.newBuilder().setSeconds(update.getSeconds()).build();
        return Silo.ReportRequest.newBuilder().setCamName(camName).addAllObjects(objects).setTs(TS).build();
    }
}
