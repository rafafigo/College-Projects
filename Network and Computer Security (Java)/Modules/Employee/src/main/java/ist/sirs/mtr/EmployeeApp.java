package ist.sirs.mtr;

import ist.sirs.mtr.api.EmployeeFrontend;
import ist.sirs.mtr.cmd.CommandReader;

public class EmployeeApp {

  public static void main(String[] args) {
    System.out.println(EmployeeApp.class.getSimpleName());

    // Print Arguments
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("Arg[%d] = %s%n", i, args[i]);
    }

    // Check Arguments
    if (args.length != 3) {
      System.err.println("Invalid Number Of Arguments");
      System.err.println("Arguments: Employee Host, Employee Port, CA Certificate Path");
      return;
    }

    String eHost = args[0];
    int ePort = parsePort(args[1]);
    String caCrtPath = args[2];

    EmployeeFrontend frontend = new EmployeeFrontend(eHost, ePort, caCrtPath);
    CommandReader commandReader = new CommandReader(frontend);

    commandReader.run();
    frontend.shutdown();
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
