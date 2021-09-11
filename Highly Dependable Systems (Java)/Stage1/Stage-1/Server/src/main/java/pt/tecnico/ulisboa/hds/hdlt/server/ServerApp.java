package pt.tecnico.ulisboa.hds.hdlt.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.server.api.DHServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.server.api.HAServerServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.server.api.ServerCrypto;
import pt.tecnico.ulisboa.hds.hdlt.server.api.UserServerServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ExceptionHandler;
import pt.tecnico.ulisboa.hds.hdlt.server.repository.DBManager;
import pt.tecnico.ulisboa.hds.hdlt.server.session.SessionsCleanupThread;

import java.io.IOException;

public class ServerApp {

  public static void main(String[] args) {
    System.out.println(ServerApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 9) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println(
          "Usage: [Port] [DB Name] [DB User] [DB Password] [Server Private Key Path] "
              + "[Users Public Key Dir Path] [HA Public Key Path] [Number of Byzantine Users] "
              + "[Session Time]");
      return;
    }
    int sPort = parsePort(args[0]);
    String dbName = args[1];
    String dbUser = args[2];
    String dbPwd = args[3];
    ServerCrypto sCrypto = new ServerCrypto(args[4], args[5], args[6]);
    int nByzantineUsers = Integer.parseInt(args[7]);
    int expSec = Integer.parseInt(args[8]);
    DBManager db = new DBManager(dbName, dbUser, dbPwd, sCrypto);

    // Cleanup in Background
    Thread sessionsCleanup = new Thread(new SessionsCleanupThread());
    sessionsCleanup.start();

    // Start Server
    final Server server =
        ServerBuilder.forPort(sPort)
            .addService(new DHServicesImpl(sCrypto, db, expSec))
            .addService(new UserServerServicesImpl(sCrypto, db, nByzantineUsers))
            .addService(new HAServerServicesImpl(sCrypto, db))
            .intercept(new ExceptionHandler())
            .build();

    try {
      server.start();
    } catch (IOException e) {
      throw new AssertError(ServerApp.class.getSimpleName(), "main", e);
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.printf("%nGoodbye!%n");
                  SessionsCleanupThread.stop(sessionsCleanup);
                  server.shutdownNow();
                  db.closeConnection();
                }));

    // Wait Until Server is Terminated
    try {
      server.awaitTermination();
    } catch (InterruptedException e) {
      throw new AssertError(ServerApp.class.getSimpleName(), "main", e);
    }
  }

  private static int parsePort(String arg) {
    int p = -1;
    try {
      p = Integer.parseInt(arg);
    } catch (NumberFormatException ignored) {
    }
    if (p < 0 || p > 65535) {
      throw new AssertError(String.format("Invalid Argument '%s'!%n", arg));
    }
    return p;
  }
}
