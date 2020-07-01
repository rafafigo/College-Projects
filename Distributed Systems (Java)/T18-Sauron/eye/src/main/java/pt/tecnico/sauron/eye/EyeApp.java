package pt.tecnico.sauron.eye;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import static io.grpc.Status.Code.UNAVAILABLE;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.exception.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.Camera;
import pt.tecnico.sauron.silo.grpc.Silo.Location;
import pt.tecnico.sauron.silo.grpc.Silo.Type;
import pt.tecnico.sauron.silo.grpc.Silo.ObservedObject;
import pt.tecnico.sauron.silo.grpc.Silo.CamJoinRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CamInfoRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;


public class EyeApp {

	private static final String EYE_PROP_FILE = "/main.properties";
	private static final boolean DEBUG_FLAG = System.getProperty("debug") != null;
	private static SiloFrontend frontend;
	private static String name;
	private static final Set<ObservedObject> stack = new HashSet<>();

	/* Debug Messages */
	private static void debug(String msg) {
		if (DEBUG_FLAG) System.out.println("Debug: " + msg);
	}
	/* Error Messages */
	private static void error(String msg) {
		System.err.println("Error: " + msg);
	}

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());

		/* Receive & Print Arguments */
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
        /* Create Frontend to call Server */
        try {
			Properties props = new Properties();
			props.load(EyeApp.class.getResourceAsStream(EYE_PROP_FILE));
			Integer timeout = Integer.parseInt(props.getProperty("timeout"));
			Integer retries = Integer.parseInt(props.getProperty("retries"));
            if (args.length == 5) {
                frontend = new SiloFrontend(args[0], args[1], timeout, retries);
            } else if (args.length == 6) {
                frontend = new SiloFrontend(args[0], args[1], args[5], timeout, retries);
            } else {
                error("Invalid Number Of Arguments!%n" + EyeApp.class.getSimpleName() +
                        "Arguments: {Zoo Host, Zoo Port, Camera Name, Latitude, Longitude, <Optional> Instance}");
                return;
            }
		} catch (IOException e) {
			error(String.format("Could not load properties file %s", EYE_PROP_FILE));
			return;
		} catch (NumberFormatException e) {
			error("Timeout and Retries must be Integers!");
			return;
		} catch (SiloFrontendException e) {
			error(e.getMessage());
			return;
		}

		/* Check Latitude Argument */
		double latitude;
		try { latitude = Double.parseDouble(args[3]); }
		catch (NumberFormatException e) { error("Invalid Latitude!"); return; }

		/* Check Longitude Argument */
		double longitude;
		try { longitude = Double.parseDouble(args[4]); }
		catch (NumberFormatException e) { error("Invalid Longitude!"); return; }

		/* Create Camera on Server */
		name = args[2];
		register(latitude, longitude);
		/* Parse Standard Input Commands */
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			parseLine(scanner.nextLine().trim());
		}
		scanner.close();
		report();
		frontend.shutdown();
	}

	private static void parseLine(String line) {
		/* Empty Line -> Report Observations */
		if (line.length() == 0) {
			report();
			return;
		}
		/* '#' -> Comment */
		if (line.startsWith("#")) return;
		/* 'zzz' -> Pause in Data Processing */
		if (line.startsWith("zzz")) {
			sleep(line.substring(3).trim());
			return;
		}
		/* 'info' -> Display Camera Information */
		if (line.startsWith("info")) {
			infoDisplay();
			return;
		}
		/* 'stack' -> Display Stack */
		if (line.startsWith("stack")) {
			stackDisplay();
			return;
		}
		/* 'help' -> Display Eye Commands */
		if (line.startsWith("help")) {
			displayHelp();
			return;
		}
		/* <Type>, <ID> -> Capture Observed Object */
		String[] arr = line.split(",");
		if (arr.length == 2) {
			/* Parse & Push Observed Object */
			parseObservedObject(arr[0].trim(), arr[1].trim());
			return;
		}
		error(String.format("Command: '%s' not found!%n" +
				"For +Details type 'help'!", line));
	}

	private static void register(double latitude, double longitude) {

		Location location = Location.newBuilder()
				.setLatitude(latitude)
				.setLongitude(longitude)
				.build();
		Camera camera = Camera.newBuilder()
				.setName(name)
				.setLocation(location)
				.build();
		try {
			/* Camera Registration */
			debug(String.format("Registering Camera '%s' at Location (%f, %f)",
					name, location.getLatitude(), location.getLongitude()));
			frontend.cam_join(CamJoinRequest.newBuilder()
					.setCamera(camera)
					.build());
			debug("Camera Registration Succeed!");
		} catch (StatusRuntimeException e) {
			if (e.getStatus().getCode() == UNAVAILABLE) {
				error("Server Offline!");
			} else {
				error(e.getStatus().getDescription());
				error("Camera Registration Failed!");
			}
			frontend.shutdown();
			System.exit(1);
		}
	}

	private static void sleep(String value) {

		try {
			long millis = Long.parseLong(value);
			debug(String.format("Sleep(%d)", millis));
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			error("Making Thread sleep!");
		} catch (NumberFormatException e) {
			error("Invalid Milliseconds!");
		}
	}

	private static void infoDisplay() {

		try {
			Location location = frontend.cam_info(CamInfoRequest.newBuilder()
					.setName(name)
					.build())
					.getLocation();
			System.out.printf("info: Camera '%s' with Location (%f, %f)%n",
					name, location.getLatitude(), location.getLongitude());
		} catch (StatusRuntimeException e) {
			if (e.getStatus().getCode() == UNAVAILABLE) {
				error("Server Offline!");
				frontend.shutdown();
				System.exit(1);
			}
			error(e.getStatus().getDescription());
			error("Getting Camera information!");
		}
	}

	private static void stackDisplay() {

		for (ObservedObject object : stack) {
			System.out.printf("stack: %s '%s'%n",
					object.getType(), object.getId());
		}
	}

	private static void displayHelp() {

		System.out.println("|------------------|EyeApp Commands|------------------|");
		System.out.println("| >>> person, <ID>       : Capture a person           |");
		System.out.println("| >>> car, <ID>          : Capture a car              |");
		System.out.println("| >>> # <Text>           : Comments                   |");
		System.out.println("| >>> zzz <Milliseconds> : Pause in Data Processing   |");
		System.out.println("| Empty Line             : Report Pendent Captures    |");
		System.out.println("| >>> stack              : List Pendent Captures      |");
		System.out.println("| >>> info               : Display Camera Information |");
		System.out.println("|-----------------------------------------------------|");
	}

	private static void parseObservedObject(String type, String id) {

		switch (type.toUpperCase()) {
			case "PERSON":
				/* Check Person ID */
				if (!id.matches("\\d+")) {
					error(String.format("Invalid %s ID: '%s'!", Type.PERSON, id));
					break;
				}
				try { Long.parseLong(id); }
				catch (NumberFormatException e) {
					error(String.format("'%s' ID: %s is out of bounds!", Type.PERSON, id));
					break;
				}
				debug(String.format("Scanned %s '%s'!", Type.PERSON, id));
				/* ObservedObject Push */
				if (stack.add(ObservedObject.newBuilder()
						.setType(Type.PERSON)
						.setId(id)
						.build())) debug("Pushed!");
				break;
			case "CAR":
				/* Check Car ID */
				if (!id.matches("\\d{2}[A-Z]{4}" +
						"|\\d{4}[A-Z]{2}" +
						"|[A-Z]{2}\\d{4}" +
						"|[A-Z]{4}\\d{2}" +
						"|[A-Z]{2}\\d{2}[A-Z]{2}" +
						"|\\d{2}[A-Z]{2}\\d{2}")) {
					error(String.format("Invalid %s ID: '%s'!", Type.CAR, id));
					break;
				}
				debug(String.format("Scanned %s '%s'!", Type.CAR, id));
				/* ObservedObject Push */
				if (stack.add(ObservedObject.newBuilder()
						.setType(Type.CAR)
						.setId(id)
						.build())) debug("Pushed!");
				break;
			default:
				error(String.format("Invalid Object with Type: '%s'!%n" +
						"For +Details type 'help'!", type));
		}
	}

	private static void report() {

		if (stack.isEmpty()) return;
		try {
			frontend.report(ReportRequest.newBuilder()
					.setCamName(name)
					.addAllObjects(stack)
					.build());
			debug("Report Succeed!");
			stack.clear();
		} catch (StatusRuntimeException e) {
			if (e.getStatus().getCode() == UNAVAILABLE) {
				error("Server Offline!");
				frontend.shutdown();
				System.exit(1);
			}
			error(e.getStatus().getDescription());
			error("Report Failed!");
		}
	}
}
