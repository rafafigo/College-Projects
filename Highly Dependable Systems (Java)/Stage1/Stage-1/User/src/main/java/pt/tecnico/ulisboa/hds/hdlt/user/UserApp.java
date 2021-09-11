package pt.tecnico.ulisboa.hds.hdlt.user;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.user.api.*;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class UserApp {

  public static void main(String[] args) {
    System.out.println(UserApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 12) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println(
          "Usage: [Username] [Server Host] [Server Port] [Session Time] [Server Public Key Path] [Users Private Key Path] "
              + "[Users Public Key Dir Path] [Number Of Byzantine Users] [Maximum Distance Between Users] "
              + "[Grid Path] [Users URLs Path] [Epoch Life Time]");
      return;
    }

    String uname = args[0];
    String sHost = args[1];
    int sPort = parsePort(args[2]);
    Session session = new Session(Integer.parseInt(args[3]));
    UserCrypto uCrypto = new UserCrypto(args[4], args[5], args[6], session);
    Integer nByzantineUsers = Integer.parseInt(args[7]);
    Integer uMaxDistance = Integer.parseInt(args[8]);
    GridManager grid;
    Map<String, String> uURLs;
    try {
      grid = new GridManager(uname, args[9]);
      uURLs = parseUsersURLs(args[10]);
    } catch (FileNotFoundException e) {
      System.err.printf("Invalid Argument(s) Grid | URLs!%n");
      return;
    }
    int uPort = parsePort(uURLs.get(uname).split(":")[1]);
    long epochLifeTime = Integer.parseInt(args[11]);

    UserToDHServerFrontend dhFrontend =
        new UserToDHServerFrontend(uname, uCrypto, sHost, sPort, session);
    UserToServerFrontend usFrontend =
        new UserToServerFrontend(uname, grid, uCrypto, dhFrontend, sHost, sPort, session);
    UserToUserFrontend uuFrontend =
        new UserToUserFrontend(uname, grid, uCrypto, nByzantineUsers, uMaxDistance, uURLs);
    CommandReader commandReader = new CommandReader(usFrontend, uuFrontend);

    // Start UserServer
    final Server uServer =
        ServerBuilder.forPort(uPort)
            .addService(new UserUserServicesImpl(uname, grid, uCrypto, uMaxDistance))
            .build();

    try {
      uServer.start();
    } catch (IOException e) {
      throw new AssertError(UserApp.class.getSimpleName(), "main", e);
    }
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> shutdown(uServer, dhFrontend, usFrontend, uuFrontend)));

    if (epochLifeTime > 0) submitULReports(uname, grid, commandReader, epochLifeTime);
    commandReader.run();
    shutdown(uServer, dhFrontend, usFrontend, uuFrontend);
  }

  private static void shutdown(
      Server uServer,
      UserToDHServerFrontend dhFrontend,
      UserToServerFrontend usFrontend,
      UserToUserFrontend uuFrontend) {
    System.out.printf("%nGoodbye!%n");
    uServer.shutdownNow();
    dhFrontend.shutdown();
    usFrontend.shutdown();
    uuFrontend.shutdown();
  }

  public static int parsePort(String arg) {
    int p = -1;
    try {
      p = Integer.parseInt(arg);
    } catch (NumberFormatException ignored) {
    }
    if (p < 0 || p > 65535) {
      System.err.printf("Invalid Argument '%s'!%n", arg);
      System.exit(1);
    }
    return p;
  }

  public static Map<String, String> parseUsersURLs(String uURLsPath) throws FileNotFoundException {
    Map<String, String> uURLs = new HashMap<>();
    Scanner scanner = new Scanner(new File(uURLsPath));
    while (scanner.hasNextLine()) {
      String[] uURL = scanner.nextLine().split(";");
      if (uURL.length < 2) {
        System.err.printf("Invalid Argument '%s'!%n", uURLsPath);
      }
      uURLs.put(uURL[0].trim(), uURL[1].trim());
    }
    scanner.close();
    return uURLs;
  }

  private static void submitULReports(
      String uname, GridManager grid, CommandReader commandReader, long epochLifeTime) {
    grid.getEpochs(uname).stream()
        .sorted()
        .forEach(
            epoch -> {
              try {
                System.out.println(commandReader.doSubmitULReport(epoch));
              } catch (UserRuntimeException e) {
                System.out.printf("Error: %s%n", e.getMessage());
              }
              sleep(epochLifeTime);
            });
  }
}
