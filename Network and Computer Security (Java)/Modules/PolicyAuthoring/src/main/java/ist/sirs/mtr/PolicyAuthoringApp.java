package ist.sirs.mtr;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import ist.sirs.mtr.api.HSPolicyAuthoringServicesImpl;
import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.pdp.PDP;

import java.io.File;
import java.io.IOException;

public class PolicyAuthoringApp {

  public static void main(String[] args) {
    System.out.println(PolicyAuthoringApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 4) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println("Arguments: Port, Certificate Path, PrivKey Path, PDPConf Path");
      return;
    }

    int paPort = parsePort(args[0]);
    String crtPath = args[1];
    String privKeyPath = args[2];
    String pdpConfPath = args[3];

    PDP pdp = new PDP(pdpConfPath);

    // Start Server
    final Server server =
        NettyServerBuilder.forPort(paPort)
            .useTransportSecurity(new File(crtPath), new File(privKeyPath))
            .addService(new HSPolicyAuthoringServicesImpl(pdp))
            .build();

    try {
      server.start();
    } catch (IOException e) {
      throw new AssertError(PolicyAuthoringApp.class.getSimpleName(), "main", e);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

    // Wait Until Server is Terminated
    try {
      server.awaitTermination();
    } catch (InterruptedException e) {
      throw new AssertError(HSPolicyAuthoringServicesImpl.class.getSimpleName(), "main", e);
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
