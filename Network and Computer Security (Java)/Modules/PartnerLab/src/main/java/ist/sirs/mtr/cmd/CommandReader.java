package ist.sirs.mtr.cmd;

import ist.sirs.mtr.api.Message;
import ist.sirs.mtr.api.PartnerLabFrontend;
import ist.sirs.mtr.tres.TRes;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommandReader {

  private static final Scanner scanner = new Scanner(System.in);
  private final List<TRes> tResList = new ArrayList<>();
  private final PartnerLabFrontend frontend;

  public CommandReader(PartnerLabFrontend frontend) {
    this.frontend = frontend;
  }

  public void run() {
    int option = -1;
    do {
      try {
        displayMenu();
        System.out.print(">>> ");
        option = Integer.parseInt(scanner.nextLine());
        System.out.println(parse(option));
      } catch (NumberFormatException | DateTimeParseException e) {
        System.out.println("Invalid Command!");
      }
    } while (option != 5);
  }

  private String parse(int option) {
    switch (option) {
      case 0:
        return login();
      case 1:
        return patientDetails();
      case 2:
        return writeTResults();
      case 3:
        return submitTResults();
      case 4:
        return logout();
      case 5:
        return "Goodbye!";
      default:
        return "Command Not Available!";
    }
  }

  private String login() {
    System.out.print("Username: ");
    String uname = scanner.nextLine();

    System.out.print("Password: ");
    String pwd = scanner.nextLine();
    return frontend.login(uname, pwd);
  }

  private String patientDetails() {
    displaySearchBy();
    System.out.print(">>> ");
    int opt = Integer.parseInt(scanner.nextLine());

    switch (opt) {
      case 0:
        System.out.print("Pid: ");
        int pid = Integer.parseInt(scanner.nextLine());
        return frontend.patientDetails(pid, 0, "");
      case 1:
        System.out.print("Nif: ");
        int nif = Integer.parseInt(scanner.nextLine());
        return frontend.patientDetails(0, nif, "");
      case 2:
        System.out.print("Name: ");
        String name = scanner.nextLine();
        return frontend.patientDetails(0, 0, name);
      default:
        return "Command Not Available!";
    }
  }

  private String writeTResults() {
    int pid = getPId();

    System.out.print("Test Result Content: ");
    String content = scanner.nextLine();

    tResList.add(new TRes(pid, content));
    return Message.TEST_RES_WRITTEN.lbl;
  }

  private String submitTResults() {
    String res = frontend.submitTResults(tResList);
    if (res.equals(Message.TEST_RES_SUBMITTED.lbl)) tResList.clear();
    return res;
  }

  private String logout() {
    return frontend.logout();
  }

  private int getPId() {
    System.out.print("Patient Id: ");
    return Integer.parseInt(scanner.nextLine());
  }

  private void displayMenu() {
    System.out.println("============== Menu ==============");
    System.out.println("| 0 - Login                      |");
    System.out.println("| 1 - Patient Details            |");
    System.out.println("| 2 - Write Test Result          |");
    System.out.println("| 3 - Submit Test Results        |");
    System.out.println("| 4 - Logout                     |");
    System.out.println("| 5 - Exit                       |");
    System.out.println("==================================");
  }

  private void displaySearchBy() {
    System.out.println("=========== Search By ============");
    System.out.println("| 0 - Pid                        |");
    System.out.println("| 1 - Nif                        |");
    System.out.println("| 2 - Name                       |");
    System.out.println("==================================");
  }
}
