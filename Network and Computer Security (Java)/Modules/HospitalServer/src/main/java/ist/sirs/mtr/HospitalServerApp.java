package ist.sirs.mtr;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import ist.sirs.mtr.api.HSEmployeeServicesImpl;
import ist.sirs.mtr.api.HSPartnerLabServicesImpl;
import ist.sirs.mtr.api.PEPFrontend;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.crypto.CryptoCleanupThread;
import ist.sirs.mtr.db.HSDatabase;
import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.exception.ExceptionHandler;
import ist.sirs.mtr.session.SessionsCleanupThread;
import ist.sirs.mtr.throttle.ThrottleCleanupThread;

import java.io.File;
import java.io.IOException;

public class HospitalServerApp {

  public static void main(String[] args) {
    System.out.println(HospitalServerApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 12 && args.length != 13) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println(
          "Arguments: Employee Service Port, Partner Lab Service Port, "
              + "Policy Authoring Host, Policy Authoring Port, "
              + "DB Name, DB User, DB Pwd, Crt Path, PrivKey Path, PrivJavaKey Path, "
              + "CA Certificate Path, DB Init, <Records Path>");
      return;
    }
    int sEmployeePort = parsePort(args[0]);
    int sPartnerLabPort = parsePort(args[1]);
    String paHost = args[2];
    int paPort = parsePort(args[3]);
    String dbName = args[4];
    String dbUser = args[5];
    String dbPwd = args[6];
    String crtPath = args[7];
    String privKeyPath = args[8];
    String privPkcs8KeyPath = args[9];
    String caCrtPath = args[10];
    String recordsPath = !args[11].equals("0") ? args[12] : null;

    Crypto.addCrt("CA", new File(caCrtPath));
    Crypto.addCrt("HS", new File(crtPath));
    Crypto.addPrivKey("HS", new File(privPkcs8KeyPath));
    HSDatabase db = new HSDatabase(dbName, dbUser, dbPwd, recordsPath);
    PEPFrontend pepFrontend = new PEPFrontend(paHost, paPort, caCrtPath);

    // Start Server
    final Server serverEmployee =
        NettyServerBuilder.forPort(sEmployeePort)
            .useTransportSecurity(new File(crtPath), new File(privKeyPath))
            .addService(new HSEmployeeServicesImpl(db, pepFrontend))
            .intercept(new ExceptionHandler())
            .build();
    final Server serverPartnerLab =
        ServerBuilder.forPort(sPartnerLabPort)
            .addService(new HSPartnerLabServicesImpl(db, pepFrontend))
            .intercept(new ExceptionHandler())
            .build();

    // Cleanup in Background
    Thread throttleCleanup = new Thread(new ThrottleCleanupThread());
    Thread sessionsCleanup = new Thread(new SessionsCleanupThread());
    Thread cryptoCleanup = new Thread(new CryptoCleanupThread());

    try {
      serverEmployee.start();
      serverPartnerLab.start();
    } catch (IOException e) {
      throw new AssertError(HospitalServerApp.class.getSimpleName(), "main", e);
    }
    throttleCleanup.start();
    sessionsCleanup.start();
    cryptoCleanup.start();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  pepFrontend.shutdown();
                  serverEmployee.shutdownNow();
                  serverPartnerLab.shutdownNow();
                  db.closeConn();
                  ThrottleCleanupThread.stop(throttleCleanup);
                  SessionsCleanupThread.stop(sessionsCleanup);
                  CryptoCleanupThread.stop(cryptoCleanup);
                }));

    // Wait Until Server is Terminated
    try {
      serverEmployee.awaitTermination();
      serverPartnerLab.awaitTermination();
    } catch (InterruptedException e) {
      throw new AssertError(HospitalServerApp.class.getSimpleName(), "main", e);
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
