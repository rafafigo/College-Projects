package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Properties;

public class SiloServerApp {

	private static final String SERVER_PROP_FILE = "/main.properties";
	private static final Properties props = new Properties();
	private static final String rootPath = "/grpc/sauron/silo";
	private static ZKNaming zkNaming;
	private static Server server;
	private static String instance;
	private static String host;
	private static String port;

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(SiloServerApp.class.getSimpleName());

		/* Receive & Print Arguments */
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		/* Check Arguments */
		if (args.length < 5 || args.length > 6) {
		    System.err.println("Invalid Number Of Arguments");
			System.err.println("Arguments: Zoo Host, Zoo Port, Instance, Server Host, Server Port, <Optional> Seconds to Sync");
			return;
		}
		try {
			props.load(SiloServerApp.class.getResourceAsStream(SERVER_PROP_FILE));
		} catch (IOException e) {
			System.err.println(String.format("Could not load properties file %s", SERVER_PROP_FILE));
			return;
		}
		/* Class Attr Init */
		instance = args[2];
		host = args[3];
		port = args[4];
		/* Check Port */
		int serverPort;
		int zooPort;
		int rep;
		long sync;
		try {
			zooPort = Integer.parseInt(args[1]);
			rep = Integer.parseInt(args[2]);
			serverPort = Integer.parseInt(args[4]);
			sync = Long.parseLong(args.length == 6 ? args[5] : props.getProperty("sync"));

		} catch (NumberFormatException e) {
			System.err.println("Invalid Arguments!");
			System.err.println("Zoo Port, Server Port, Instance and Sync must be Integers!");
			return;
		}
		if (serverPort < 0 || serverPort > 65535 || zooPort < 0 || zooPort > 65535 || rep <= 0) {
			System.err.println("Invalid Arguments!");
			System.err.println("Zoo Port and Server Port must be [0, 65535]");
			System.err.println("Instance must be [0, 9]");
			System.err.println("Sync must be Integer!");
			return;
		}

        try {
			/* Create ZKNaming */
			zkNaming = new ZKNaming(args[0], args[1]);

            /* Publish */
            zkNaming.rebind(rootPath + "/" + instance, host, port);

			/* Set Server Service */
			final BindableService impl = new SauronServiceImpl(args[0], args[1], rootPath, rep, sync);

			/* Create a new Server to listen on Port */
			server = ServerBuilder.forPort(serverPort).addService(impl).build();

            /* Start the Server */
            server.start();

            /* Add Shutdown Hook */
			Runtime.getRuntime().addShutdownHook(new ShutdownServer());

			/* Server Running */
			System.out.println("Server started");

            /* Wait until Server is Terminated */
            server.awaitTermination();

        } catch (ZKNamingException e) {
            System.err.println("Error while Binding the Server!");
        }
	}

	public static Properties getProps() {
		return props;
	}

    private static class ShutdownServer extends Thread {

        @Override
        public void run() {
            try {
                /* Remove */
                zkNaming.unbind(rootPath + "/" + instance, host, port);

                /* Shutdown Server */
                server.shutdown();

            } catch (ZKNamingException e) {
                System.err.println("Error while Unbinding the Server!");
            }
        }
    }
}
