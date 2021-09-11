package pt.tecnico.ulisboa.hds.hdlt.user;

import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToUserFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.Location;

import java.util.Map;
import java.util.Scanner;

public class CommandReader {

  private static final Scanner scanner = new Scanner(System.in);
  private final UserToServerFrontend usFrontend;
  private final UserToUserFrontend uuFrontend;

  public CommandReader(UserToServerFrontend usFrontend, UserToUserFrontend uuFrontend) {
    this.usFrontend = usFrontend;
    this.uuFrontend = uuFrontend;
  }

  public void run() {
    int option = -1;
    do {
      try {
        displayMenu();
        System.out.print(">>> ");
        option = Integer.parseInt(scanner.nextLine());
        System.out.println(parse(option));
      } catch (NumberFormatException e) {
        System.out.println("Error: Invalid Command!");
      } catch (UserRuntimeException e) {
        System.out.printf("Error: %s%n", e.getMessage());
      }
    } while (option != 3);
  }

  private String parse(int option) {
    return switch (option) {
      case 1 -> this.submitULReport();
      case 2 -> this.obtainUL();
      case 3 -> "CommandReader: Shutting Down (...)";
      default -> "Error: Command Not Available!";
    };
  }

  private String submitULReport() {
    System.out.print("Epoch: ");
    return this.doSubmitULReport(Integer.parseInt(scanner.nextLine()));
  }

  public String doSubmitULReport(Integer epoch) {
    Map<String, byte[]> authProofs = this.uuFrontend.getAuthProofs(epoch);
    if (authProofs == null) {
      return String.format("Not Enough Valid Users to Send Location Report at Epoch %d!", epoch);
    }
    this.usFrontend.submitULReport(epoch, authProofs);
    return String.format("User Location Report at Epoch %d Submitted Successfully!", epoch);
  }

  private String obtainUL() {
    System.out.print("Epoch: ");
    return this.doObtainUL(Integer.parseInt(scanner.nextLine()));
  }

  private String doObtainUL(Integer epoch) {
    Location location = this.usFrontend.obtainUL(epoch);
    return String.format(
        "User Location at Epoch %d: (%d, %d)!", epoch, location.getX(), location.getY());
  }

  private void displayMenu() {
    System.out.println("============== Menu ===============");
    System.out.println("| 1 - Submit User Location Report |");
    System.out.println("| 2 - Obtain User Location        |");
    System.out.println("| 3 - Exit                        |");
    System.out.println("===================================");
  }
}
