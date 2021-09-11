package pt.tecnico.ulisboa.hds.hdlt.ha;

import pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.ha.error.HARuntimeException;

import java.util.Scanner;

public class CommandReader {

  private static final Scanner scanner = new Scanner(System.in);
  private final HAToServerFrontend hsFrontend;

  public CommandReader(HAToServerFrontend hsFrontend) {
    this.hsFrontend = hsFrontend;
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
      } catch (HARuntimeException e) {
        System.out.printf("Error: %s%n", e.getMessage());
      }
    } while (option != 3);
  }

  private String parse(int option) {
    return switch (option) {
      case 1 -> this.obtainUL();
      case 2 -> this.obtainUAtL();
      case 3 -> "CommandReader: Shutting Down (...)";
      default -> "Error: Command Not Available!";
    };
  }

  private String obtainUL() {
    System.out.print("Username: ");
    String uname = scanner.nextLine();
    System.out.print("Epoch: ");
    Integer epoch = Integer.parseInt(scanner.nextLine());
    return hsFrontend.obtainUL(uname, epoch);
  }

  private String obtainUAtL() {
    System.out.println("(X, Y)");
    System.out.print("Coordinate X: ");
    Integer x = Integer.parseInt(scanner.nextLine());
    System.out.print("Coordinate Y: ");
    Integer y = Integer.parseInt(scanner.nextLine());
    System.out.print("Epoch: ");
    Integer epoch = Integer.parseInt(scanner.nextLine());
    return hsFrontend.obtainUAtL(epoch, x, y);
  }

  private void displayMenu() {
    System.out.println("============== Menu ==============");
    System.out.println("| 1 - Obtain User Location       |");
    System.out.println("| 2 - Obtain Users At Location   |");
    System.out.println("| 3 - Exit                       |");
    System.out.println("==================================");
  }
}
