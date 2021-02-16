package ist.sirs.mtr;

import ist.sirs.mtr.api.PartnerLabFrontend;
import ist.sirs.mtr.cmd.CommandReader;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.crypto.CryptoCleanupThread;

import java.io.File;

public class PartnerLabApp {

  public static void main(String[] args) {
    System.out.println(PartnerLabApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 5) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println(
          "Arguments: Hospital Server Host, Hospital Server Port, Crt Path, PrivJavaKey Path, CA Crt Path");
      return;
    }

    String hsHost = args[0];
    int hsPort = parsePort(args[1]);
    String crtPath = args[2];
    String privPkcs8KeyPath = args[3];
    String caCrtPath = args[4];

    Crypto.addCrt("CA", new File(caCrtPath));
    Crypto.addCrt("PL", new File(crtPath));
    Crypto.addPrivKey("PL", new File(privPkcs8KeyPath));

    PartnerLabFrontend frontend = new PartnerLabFrontend(hsHost, hsPort);
    CommandReader commandReader = new CommandReader(frontend);

    Thread cryptoCleanup = new Thread(new CryptoCleanupThread());
    cryptoCleanup.start();
    commandReader.run();
    frontend.shutdown();
    CryptoCleanupThread.stop(cryptoCleanup);
  }

  private static int parsePort(String arg) {
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
