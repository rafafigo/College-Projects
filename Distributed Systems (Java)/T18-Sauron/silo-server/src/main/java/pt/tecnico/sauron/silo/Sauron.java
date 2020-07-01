package pt.tecnico.sauron.silo;

import static pt.tecnico.sauron.silo.exceptions.ErrorMessage.*;
import static io.grpc.Status.*;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

import pt.tecnico.sauron.silo.domain.*;
import static pt.tecnico.sauron.silo.domain.ObservedObject.Type;
import pt.tecnico.sauron.silo.exceptions.SauronException;

public class Sauron {

    private final Map<String, Camera> cameras = new HashMap<>();
    private final Map<Car, Set<Observation>> cars = new HashMap<>();
    private final Map<Person, Set<Observation>> persons = new HashMap<>();

    /***
     * This Function registers a Camera
     * @param camera Camera to register
     * @throws SauronException When the Camera can not be registered
     ***/
    public void cam_join(Camera camera) throws SauronException {

        final String name = camera.getName();
        final Location reqLocation = camera.getLocation();

        synchronized (this.cameras) {
            if (cameras.containsKey(name)) {
                Location location = cameras.get(name).getLocation();
                if (location.equals(reqLocation)) return;
                throw new SauronException(ALREADY_EXISTS, CAMERA_NAME_DUPLICATE, name, reqLocation.getLatitude(), reqLocation.getLongitude());
            }
            cameras.put(name, camera);
        }
    }

    /***
     * This Function returns a Camera location
     * @param name Identifies a Camera
     * @return location Where the Camera is located
     * @throws SauronException When there is no Camera registered with the given name
     ***/
    public Location cam_info(String name) throws SauronException {

        Camera.checkCam_Name(name);

        synchronized (this.cameras) {
            if (!cameras.containsKey(name)) {
                throw new SauronException(NOT_FOUND, CAMERA_NAME_NOT_FOUND, name);
            }
            return cameras.get(name).getLocation();
        }
    }

    /***
     * This Function that registers the camera's observations
     * @param cam_name Where the Camera is located
     * @param objects list with Camera's observations
     * @param seconds Timestamp of the observation
     * @throws SauronException When the Camera was not previously registered
     ***/
    public void cam_report(String cam_name, List<ObservedObject> objects, Long seconds) throws SauronException {

        Camera.checkCam_Name(cam_name);
        Camera camera;
        synchronized (this.cameras) {
            if (!cameras.containsKey(cam_name)) {
                throw new SauronException(NOT_FOUND, CAMERA_NAME_NOT_FOUND, cam_name);
            }
            camera = cameras.get(cam_name);
        }

        for (ObservedObject object : objects) {
            addObservation(new Observation(object, camera, seconds));
        }
    }

    /***
     * This Function adds an Observation to the map of cars or persons
     * @param observation list with Camera's observations
     * @throws SauronException When the type was not set as PERSON or CAR
     ***/
    private void addObservation(Observation observation) throws SauronException {

        switch (observation.getObservedObject().getType()) {
            case PERSON:
                Person person = (Person)observation.getObservedObject();
                synchronized (this.persons) {
                    if (!persons.containsKey(person)) persons.put(person, new HashSet<>());
                    persons.get(person).add(observation);
                }
                break;
            case CAR:
                Car car = (Car)observation.getObservedObject();
                synchronized (this.cars) {
                    if (!cars.containsKey(car)) cars.put(car, new HashSet<>());
                    cars.get(car).add(observation);
                }
                break;
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, "UNKNOWN");
        }
    }

    /***
     * Given an Observed Object, containing Type and ID, returns the most recent
     *      Observation of that object, if there's one in the server to present
     * @param observedObject Observed Object, contains Type and ID
     * @return Observation of the said Object, the most recent one
     * @throws SauronException when there's no observation to present
     ***/
    public synchronized Observation track(ObservedObject observedObject) throws SauronException {

        Set<Observation> observations = getObjectObservations(observedObject);

        return observations.stream()
                .max(Comparator.comparingLong(Observation::getSeconds))
                .orElseThrow(() -> new SauronException(NOT_FOUND, ID_NOT_FOUND,
                        getObjectId(observedObject), observedObject.getType().toString()));
    }

    /***
     * Given an Observed Object, containing Type and a partial ID, returns the most
     *      recent Observation for each Observed Object in the Server which the ID
     *      matches the partial ID in the given Object
     * @param id partial ID of an Observed Object
     * @param type Type of the Observed Object
     * @return List of Observations, the most recent one for each Observed Object
     * @throws SauronException when the Observed Object Type doesn't exist
     ***/
    public List<Observation> trackMatch(String id, Type type) throws SauronException {

        String pattern;
        Set<ObservedObject> observedObjects;

        switch (type) {
            case PERSON:
                pattern = "[0-9]*";
                synchronized (this.persons) {
                    observedObjects = persons.values().stream()
                            .flatMap(Collection::stream)
                            .map(Observation::getObservedObject)
                            .collect(Collectors.toSet());
                }
                break;
            case CAR:
                pattern = "[A-Z0-9]*";
                synchronized (this.cars) {
                    observedObjects = cars.values().stream()
                            .flatMap(Collection::stream)
                            .map(Observation::getObservedObject)
                            .collect(Collectors.toSet());
                }
                break;
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, type.toString());
        }
        return patternMatches(observedObjects, id, type, pattern);
    }

    /***
     * Given a List of Observed Objects, returns the most recent Observations of
     *      each Observed Object which ID matches the partial ID, present in the
     *      Observed Object given as inout
     * @param objects List of Observed Objects to be parsed
     * @param id partial ID of an Observed Object
     * @param type Type of the Observed Object
     * @param pattern String based on the Type to be replaced in the partial ID
     * @return List of Observations, the most recent one for each Observed Object
     * @throws SauronException when there's no Observed Object that matches the partial ID
     ***/
    private synchronized List<Observation> patternMatches(Set<ObservedObject> objects, String id, Type type, String pattern) throws SauronException {

        String regex = id.replace("*", pattern);

        Set<ObservedObject> objectMatches = objects.stream()
                .filter(entry -> Pattern.matches(regex, getObjectId(entry)))
                .collect(Collectors.toSet());

        if (objectMatches.isEmpty()) {
            throw new SauronException(NOT_FOUND, ID_NOT_FOUND, id, type.toString());
        }

        List<Observation> observations = new ArrayList<>();
        for (ObservedObject obj : objectMatches) {
            observations.add(track(obj));
        }
        return observations;
    }

    /***
     * Given an Observed Object returns all Observations present in the Server
     *      of that Observed Object, ordered by most recent to oldest
     * @param observedObject Observed Object, contains Type and ID
     * @return List of all Observations of said Object ordered by most recent to oldest
     * @throws SauronException when there's no Observations to present
     ***/
    public synchronized List<Observation> trace(ObservedObject observedObject) throws SauronException {

        Set<Observation> observations = getObjectObservations(observedObject);

        List<Observation> observationTrace = observations.stream()
                .filter(entry -> getObjectId(observedObject).equals(getObjectId(entry.getObservedObject())))
                .sorted()
                .collect(Collectors.toList());

        if (observationTrace.isEmpty()) {
            throw new SauronException(NOT_FOUND, ID_NOT_FOUND, getObjectId(observedObject), observedObject.getType().toString());
        }
        return observationTrace;
    }

    /***
     * Given an Observed Object returns all Observations of that Observed Object
     * @param object Observed Object, contains Type and ID
     * @return Set of Observations of the Observed Object
     * @throws SauronException when the Observed Object Type doesn't exist
     *                         or there's no Observations to present
     ***/
    private Set<Observation> getObjectObservations(ObservedObject object) throws SauronException {

        switch (object.getType()) {

            case PERSON:
                Person person = (Person)object;
                synchronized (this.persons) {
                    if (persons.containsKey(person)) return persons.get(person);
                }
                break;
            case CAR:
                Car car = (Car)object;
                synchronized (this.cars) {
                    if (cars.containsKey(car)) return cars.get(car);
                }
                break;
            default:
                throw new SauronException(INVALID_ARGUMENT, INVALID_TYPE, object.getType().toString());
        }
        throw new SauronException(NOT_FOUND, ID_NOT_FOUND, getObjectId(object), object.getType().toString());
    }

    /***
     * Initializes Server with data, in this case, with Cameras and Observations
     * @param camerasList List of Cameras to add
     * @param observationsList List of Observations to add
     * @throws SauronException when one of the observations contains a Camera name that doesn't exist
     ***/
    public void ctrl_init(List<Camera> camerasList, List<Observation> observationsList) throws SauronException {

        for (Camera camera : camerasList) {
            cam_join(camera);
        }

        for (Observation observation : observationsList) {
            synchronized (this.cameras) {
                if (!cameras.containsKey(observation.getCamera().getName())) {
                    throw new SauronException(NOT_FOUND, CAMERA_NAME_NOT_FOUND, observation.getCamera().getName());
                }
            }
            addObservation(observation);
        }
    }

    /***
     * Gets all cameras
     * @return a list of cameras
     ***/
    public List<Camera> getCameras() {
        synchronized (this.cameras) { return new ArrayList<>(this.cameras.values()); }
    }

    /***
     * Gets all Observations Cars and Persons
     * @return a list of observations
     ***/
    public List<Observation> getObservations() {
        List<Observation> observations;
        synchronized (this.cars) {
            observations = this.cars.values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList());
        }
        synchronized (this.persons) {
            observations.addAll(this.persons.values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList()));
        }
        return observations;
    }

    /***
     * Cleans cameras and observations
     ***/
    public synchronized void clear() {
        this.cameras.clear();
        this.cars.clear();
        this.persons.clear();
    }

    /***
     * Returns the id of an Observed Object in String
     * @param object Observed Object
     * @return id of the Object in a String
     ***/
    private synchronized String getObjectId(ObservedObject object) {

        switch (object.getType()) {
            case PERSON: return ((Person)object).getId().toString();
            case CAR: return ((Car)object).getId();
            default: return "";
        }
    }
}
