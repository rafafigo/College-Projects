package pt.tecnico.ulisboa.hds.hdlt.ha;

import pt.tecnico.ulisboa.hds.hdlt.ha.api.HACrypto;
import pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.ha.session.Session;

public class HAApp {

  public static void main(String[] args) {
    System.out.println(HAApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 6) {
      System.err.println("Invalid Number Of Arguments");
      System.out.println(
          "Usage: [Username] [Server Host] [Server Port] [Session Time] [HA Private Key Path] "
              + "[Server Public Key Path]");
      return;
    }

    String uname = args[0];
    String sHost = args[1];
    int sPort = parsePort(args[2]);
    Session session = new Session(Integer.parseInt(args[3]));
    HACrypto crypto = new HACrypto(args[4], args[5], session);

    HAToDHServerFrontend dhFrontend =
        new HAToDHServerFrontend(uname, crypto, sHost, sPort, session);
    HAToServerFrontend hsFrontend =
        new HAToServerFrontend(uname, crypto, dhFrontend, sHost, sPort, session);
    CommandReader commandReader = new CommandReader(hsFrontend);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(dhFrontend, hsFrontend)));

    commandReader.run();
    shutdown(dhFrontend, hsFrontend);
  }

  private static void shutdown(HAToDHServerFrontend dhFrontend, HAToServerFrontend hsFrontend) {
    System.out.printf("%nGoodbye!%n");
    hsFrontend.shutdown();
    dhFrontend.shutdown();
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
}
