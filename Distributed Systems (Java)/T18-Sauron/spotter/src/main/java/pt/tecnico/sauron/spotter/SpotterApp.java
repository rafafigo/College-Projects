package pt.tecnico.sauron.spotter;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

import static io.grpc.Status.Code.*;

public class SpotterApp {

    private static final String SPOTTER_PROP_FILE = "/main.properties";
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
    private static SiloFrontend silo;

    /***
     * This function prints a debug message only prints if the debug flag is on
     * @param msg a message
     ***/
    private static void debug(String msg) {
        if (DEBUG_FLAG) System.out.println("Debug: " + msg);
    }

    /***
     * Prints an error message
     * @param msg a error message
     ***/
    private static void error(String msg) {
        System.err.println("Error: " + msg);
        System.err.println("Type help for more details");
    }

    public static void main(String[] args) {
        System.out.println(SpotterApp.class.getSimpleName());

        /* Receive and Print Arguments */
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        /* Create Frontend to call Server */
        try {
            Properties props = new Properties();
            props.load(SpotterApp.class.getResourceAsStream(SPOTTER_PROP_FILE));
            Integer timeout = Integer.parseInt(props.getProperty("timeout"));
            Integer retries = Integer.parseInt(props.getProperty("retries"));
            if (args.length == 2) {
                silo = new SiloFrontend(args[0], args[1], timeout, retries);
            } else if (args.length == 3) {
                silo = new SiloFrontend(args[0], args[1], args[2], timeout, retries);
            } else {
                error("Invalid Number Of Arguments");
                error(String.format("%s Arguments: Zoo Host, Zoo Port, <Optional> Instance", SpotterApp.class.getSimpleName()));
                return;
            }
        } catch (IOException e) {
            error(String.format("Could not load properties file %s", SPOTTER_PROP_FILE));
            return;
        } catch (NumberFormatException e) {
            error("Timeout and Retries must be Integers!");
            return;
        } catch (SiloFrontendException e) {
            error(e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);

        /* Get input*/
        while (scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine();
                String[] params = line.split(" +");

                if (params.length == 1 || params.length == 2) command_control(params);
                else if (params.length == 3) command_search(params[0], params[1], params[2]);
                else error(String.format("Invalid length of parameters: %d", params.length));

            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == NOT_FOUND || e.getStatus().getCode() == INVALID_ARGUMENT) { System.out.println(); }
                else if (e.getStatus().getCode() == UNAVAILABLE) {
                    System.err.println("Server Unavailable");
                    break;
                }
                else { error(e.getStatus().getDescription()); }
            }
        }
        scanner.close();
        silo.shutdown();
    }

    /***
     * This Function finds the correct command of Search
     * @param command spot or trail are accepted
     * @param type person or car are accepted
     * @param id that represents the identifier or a partial identifier
     ***/
    private static void command_search(String command, String type, String id) {
        type = type.toUpperCase();
        debug(String.format("Scanned Command: '%s'!", command));
        switch (command) {
            case "spot":
                spot(type, id);
                break;
            case "trail":
                trail(type, id);
                break;
            default:
                error(String.format("Invalid Command: '%s' with number of parameters: 3", command));
        }
    }

    /***
     * Spot Function that verifies if its of valid type
     * @param type person or car are accepted
     * @param id that represents or a partial identifier
     ***/
    private static void spot(String type, String id) {
        switch (type) {
            case "PERSON":
                spotPerson(id);
                break;
            case "CAR":
                spotCar(id);
                break;
            default:
                error(String.format("spot - type: '%s' Invalid Type", type));
        }
    }

    /***
     * This function finds if we are doing a track match or a track for a car
     * @param id that represents the identifier of a partial identifier
     ***/
    private static void spotCar(String id) {
        if (id.length() > 6) {
            error(String.format("spot - car id: '%s' - Invalid format!", id));

        } else if (checkCarId(id)) {
            debug(String.format("Scanned car with id: '%s'!", id));
            track(ObservedObject.newBuilder()
                    .setId(id)
                    .setType(Type.CAR)
                    .build());

        } else if (id.contains("*")) {
            debug(String.format("Scanned car with partial id: '%s'!", id));
            trackMatch(ObservedObject.newBuilder()
                    .setId(id)
                    .setType(Type.CAR)
                    .build());

        } else {
            error("spot - car id: '%s' - Invalid Match!");
        }
    }

    /***
     * This function finds if we are doing a track match or a track for a person
     * @param id that represents the identifier of a partial identifier
     ***/
    private static void spotPerson(String id) {
        try {
            if (id.matches("\\d+")) {
                Long.parseLong(id);
                debug(String.format("Scanned Person with id: '%s'!", id));
                track(ObservedObject.newBuilder()
                        .setId(id)
                        .setType(Type.PERSON)
                        .build());

            } else if (id.contains("*")) {
                debug(String.format("Scanned Person with partial id: '%s'!", id));
                trackMatch(ObservedObject.newBuilder()
                        .setId(id)
                        .setType(Type.PERSON)
                        .build());

            } else {
                error(String.format("spot - person id: '%s' - Invalid Match!", id));
            }

        } catch (NumberFormatException e) {
            error(String.format("spot - person id: '%s' - Out of bounds!", id));
        }
    }


    /***
     * This function sends a request to the server and calls printObservation to print the response
     * The response has the most recent observation of the object
     * @param object that has a type and a id
     ***/
    private static void track(ObservedObject object) {
        TrackResponse response = silo.track(TrackRequest.newBuilder()
                .setObject(object)
                .build());
        if (response.getObservation() != null)
            printObservation(response.getObservation());
    }


    /***
     * This function sends a request to the server with an object and receives a response
     * The response is then order by ascending id
     * @param object that has a type and a id
     ***/
    private static void trackMatch(ObservedObject object) {
        TrackMatchResponse response = silo.trackMatch(TrackMatchRequest.newBuilder()
                .setObject(object)
                .build());

        /* Orders observations by id */
        List<Observation> observations = response.getObservationsList();
        switch (object.getType()) {
            case CAR:
                observations = observations.stream().sorted((o1,o2) ->
                        compareByIdCar(o1.getObject().getId(), o2.getObject().getId())).collect(Collectors.toList());
                break;
            case PERSON:
                observations = observations.stream().sorted((o1, o2) ->
                        compareByIdPerson(o1.getObject().getId(), o2.getObject().getId())).collect(Collectors.toList());
                break;
        }
        for (Observation observation : observations) {
            printObservation(observation);
        }
    }

    /***
     * This function finds out if the type and the id are valid and call trace method
     * Trail gets all the observations associated it an id order by timestamp
     * @param type person or car are valid
     * @param id that is an identifier of the object
     ***/
    private static void trail(String type, String id) {
        ObservedObject object;
        switch (type) {
            case "PERSON":
                if (!id.matches("\\d+")) {
                    error(String.format("trail - person id: '%s' - Invalid!", id));
                    return;
                }
                try {
                    Long.parseLong(id);
                } catch (NumberFormatException e) {
                    error(String.format("trail - person id: '%s' - Out of bounds!", id));
                    return;
                }
                break;
            case "CAR":
                if (!checkCarId(id)) {
                    error(String.format("trail - car id: '%s' - Invalid!", id));
                    return;
                }
                break;
            default:
                error(String.format("trail - type: '%s' Invalid Type!", type));
                return;
        }
        object = createObservedObject(type, id);
        debug(String.format("Scanned %s with id: '%s'!", type, id));
        trace(object);
    }

    /***
     * This function sends a response to the server and calls printObservation to print the response
     * The response is ordered by (most recent first) timestamp since they all have same id
     * @param object that has a type and a id
     ***/
    private static void trace(ObservedObject object) {
        TraceResponse response = silo.trace(TraceRequest.newBuilder()
                .setObject(object)
                .build());

        List<Observation> observations = response.getObservationsList();

        for (Observation observation : observations) {
            printObservation(observation);
        }
    }

    /***
     * This function verifies if the car id is valid
     * @param carId is the id of the type Car
     ***/
    private static boolean checkCarId(String carId) {
        return carId.matches("\\d{2}[A-Z]{4}|\\d{4}[A-Z]{2}|[A-Z]{2}\\d{4}" +
                "|[A-Z]{4}\\d{2}|[A-Z]{2}\\d{2}[A-Z]{2}|\\d{2}[A-Z]{2}\\d{2}");
    }

    /***
     * Control Commands - ping, clear, init and help;
     * init receives two parameters the command and the file name the rest only one
     * @param params that has a list of the parameters
     ***/
    private static void command_control(String[] params) {
        String command = params[0];
        debug(String.format("Scanned Command: '%s'!", command));

        switch (command) {
            case "ping":
                ctrl_ping();
                return;
            case "clear":
                silo.ctrl_clear(CtrlClearRequest.getDefaultInstance());
                return;
            case "init":
                if (params.length != 2) break;
                ctrl_init(params[1]);
                return;
            case "help":
                ctrl_help();
                return;
        }
        error(String.format("Invalid Command: '%s' with number of parameters: %d", params[0], params.length));
    }

    /***
     * This function pings the server
     * This means that gets all the observations and cameras and prints them
     ***/
    private static void ctrl_ping() {
        CtrlPingResponse response = silo.ctrl_ping(CtrlPingRequest.getDefaultInstance());

        System.out.println("Pinging Server Sauron");
        System.out.println("All Cameras");

        /* Lists all cameras ordered by name*/
        List<Camera> cameras = response.getCamerasList().stream().sorted(Comparator.comparing(Camera::getName)).collect(Collectors.toList());
        for (Camera camera : cameras) {
            System.out.printf("%s,%s,%s%n", camera.getName(), camera.getLocation().getLatitude(), camera.getLocation().getLongitude());
        }
        System.out.println();
        System.out.println("All Observations");

        /* Lists all observations ordered by timestamp*/
        List<Observation> observations = response.getObservationsList().stream().sorted((o1, o2) ->
                compareTs(o1.getTs(),o2.getTs())).collect(Collectors.toList());
        for (Observation obs : observations) {
            printObservation(obs);
        }
    }

    /***
     * This function gets observations and/or cameras to add to the server through a file
     * @param filename is the file that contains the commands accepted for initiate the server
     ***/
    private static void ctrl_init(String filename) {
        Scanner scanner;
        try {
            File file = new File(filename);
            scanner = new Scanner(file);

        } catch (FileNotFoundException e) {
            error(String.format("Init - filename: '%s' not found", filename));
            return;
        }
        debug(String.format("FileName accepted: '%s'!", filename));
        CtrlInitRequest.Builder request = CtrlInitRequest.newBuilder();

        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] params = line.split(" +");

            if (params.length == 0) {
                error("Init - On file - Invalid  number of paremeters");
                continue;
            }
            params[0] = params[0].toLowerCase();

            SetUp(request, params);
        }

        silo.ctrl_init(request.build());
        debug("Successfully Registered Cameras and Observations");
        scanner.close();
    }

    /***
     * This function verifies if we want to add a camera or an observation and adds them to the builder
     * @param request that is a builder with the request of control init that allows to add cameras and or observations
     * @param params that have only the command, camera_name, latitude and longitude if
     *               camera or observation that has this 4 parameters + type, object and timestamp
     ***/
    private static void SetUp(CtrlInitRequest.Builder request, String[] params) {
        try {
            switch (params[0]) {
                case "camera":
                    if (params.length != 4) break;
                    request.addCameras(createCamera(params[1], params[2], params[3]));
                    return;
                case "observation":
                    if (params.length != 7) break;
                    ObservedObject object = createObservedObject(params[1], params[2]);
                    if (object == null) break;
                    Instant time = Instant.parse(params[3]);
                    Timestamp ts = Timestamp.newBuilder()
                            .setSeconds(time.getEpochSecond())
                            .build();
                    Camera camera = createCamera(params[4], params[5], params[6]);
                    Observation observation = Observation.newBuilder()
                            .setCamera(camera)
                            .setTs(ts)
                            .setObject(object)
                            .build();
                    request.addObservations(observation);
                    return;
            }
            error(String.format("Init - On file - Invalid Command : '%s' with number of parameters: %d",params[0], params.length));
        } catch (NumberFormatException e) {error("Init - latitude or longitude: Out of bounds!");}
    }

    /***
     * This function creates a camera
     * @param cameraName that has the name of the camera
     * @param latitudeStr that has a latitude in format string
     * @param longitudeStr that has a longitude in format string
     * @return a camera
     ***/
    private static Camera createCamera(String cameraName, String latitudeStr, String longitudeStr) {
        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);


        Location location = Location.newBuilder()
                .setLatitude(latitude)
                .setLongitude(longitude)
                .build();
        return Camera.newBuilder()
                .setName(cameraName)
                .setLocation(location)
                .build();

    }

    /***
     * Helper Printer
     ***/
    private static void ctrl_help() {
        System.out.println("Help Details");
        System.out.println("> Spotter App Commands");
        System.out.println();
        System.out.println(">> Search Commands");
        System.out.println(">>> spot <type> <id>      : Finds most recent observation for the matching objects");
        System.out.println(">>> trail <type> <id>     : Finds all observations of an object");
        System.out.println();
        System.out.println(">> Control Commands");
        System.out.println(">>> ping                  : Finds all cameras and observations");
        System.out.println(">>> clear                 : Clears all cameras and observations");
        System.out.println(">>> init <fileName>       : Adds initial data");
        System.out.println();
        System.out.println(">> <fileName> Commands");
        System.out.println(">>> camera <cameraName> <latitude> <longitude>                              : Creates a camera");
        System.out.println(">>> observation <type> <id> <timestamp> <cameraName> <latitude> <longitude> : Creates Observation");
        System.out.println();
    }

    /***
     * Orders by most recent first
     * @param ts1 is timestamp to compare with ts2
     * @param ts2 is a timestamp to compare with ts1
     * @return a number that indicates what is bigger
     ***/
    private static int compareTs(Timestamp ts1, Timestamp ts2) {
        if (ts1.getSeconds() == ts2.getSeconds()) return 0;
        return ts1.getSeconds() < ts2.getSeconds() ? 1 : -1;
    }

    /***
     * Orders ids in crescent order as strings
     * @param car1 is id string to compare with car2
     * @param car2 is a id string to compare with car1
     * @return a number that indicates what is bigger
     ***/
    private static int compareByIdCar(String car1, String car2) {
        return car1.compareTo(car2);
    }

    /***
     * Orders ids in crescent order as longs
     * @param person1 is id string to compare with person2
     * @param person2 is a id string to compare with person1
     * @return a number that indicates what is bigger
     ***/
    private static int compareByIdPerson(String person1, String person2) {
        long p1 = Long.parseLong(person1);
        long p2 = Long.parseLong(person2);
        if (p1 == p2) return 0;
        return p1 > p2 ? 1 : -1;
    }

    /***
     * This function creates an Observed Object
     * @param type person or car
     * @param id an identifier
     * @return an object that can be null if type is invalid
     ***/
    private static ObservedObject createObservedObject(String type, String id) {
        ObservedObject object = null;
        switch (type.toUpperCase()) {
            case "PERSON":
                object = ObservedObject.newBuilder()
                        .setId(id)
                        .setType(Type.PERSON)
                        .build();
                break;
            case "CAR":
                object = ObservedObject.newBuilder()
                        .setId(id)
                        .setType(Type.CAR)
                        .build();
        }
        return object;
    }

    /***
     * This function prints an observation in a certain format
     * @param observation observation that we want to print
     ***/
    private static void printObservation(Observation observation) {
        Camera camera = observation.getCamera();
        ObservedObject object = observation.getObject();
        Timestamp timestamp = observation.getTs();
        ZonedDateTime ts = Instant.ofEpochSecond(timestamp.getSeconds()).atZone(ZoneId.systemDefault());
        Location location = camera.getLocation();

        System.out.printf("%s,%s,%s,%s,%s,%s%n", object.getType(), object.getId(), ts.toLocalDateTime().toString(),
                camera.getName(), location.getLatitude(), location.getLongitude());
    }
}