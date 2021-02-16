package ist.sirs.mtr.cmd;

import ist.sirs.mtr.api.EmployeeFrontend;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CommandReader {

  private static final Scanner scanner = new Scanner(System.in);
  private static final String dateFormatPat = "dd-MM-yyyy HH:mm[:ss]";
  private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(dateFormatPat);
  private final EmployeeFrontend frontend;

  public CommandReader(EmployeeFrontend frontend) {
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
        return read();
      case 2:
        return write();
      case 3:
        return tResAuth();
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

  private String read() {
    displayRead();
    System.out.print(">>> ");
    int opt = Integer.parseInt(scanner.nextLine());

    switch (opt) {
      case 0:
        return readRecord();
      case 1:
        return patientDetails();
      case 2:
        return checkMode();
      default:
        return "Command Not Available!";
    }
  }

  private String readRecord() {
    int pid = getPId();
    int recType = getRecType();

    System.out.print("Timestamp? (Y/N): ");
    LocalDateTime dateTime = null;

    if (scanner.nextLine().equalsIgnoreCase("Y")) {
      System.out.printf("Timestamp (%s): ", dateFormatPat);
      dateTime = LocalDateTime.parse(scanner.nextLine(), dateFormat);
    }
    return frontend.read(pid, recType, dateTime);
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

  private String checkMode() {
    return frontend.checkMode();
  }

  private String write() {
    displayWrite();
    System.out.print(">>> ");
    int opt = Integer.parseInt(scanner.nextLine());

    switch (opt) {
      case 0:
        return writeRecord();
      case 1:
        return createUser();
      case 2:
        return createPatient();
      case 3:
        return changeMode();
      default:
        return "Command Not Available!";
    }
  }

  private String writeRecord() {
    int pid = getPId();
    int recType = getRecType();

    System.out.print("Record Content: ");
    String recCont = scanner.nextLine();
    return frontend.write(pid, recType, recCont);
  }

  private String createUser() {
    System.out.print("Username: ");
    String uname = scanner.nextLine();

    System.out.print("Password: ");
    String pwd = scanner.nextLine();

    displayRoles();
    System.out.print(">>> ");
    int role = Integer.parseInt(scanner.nextLine());
    if (role < 0 || role > 9) return "Invalid Role!";

    return frontend.createUser(uname, pwd, role);
  }

  private String createPatient() {
    System.out.print("Nif: ");
    int nif = Integer.parseInt(scanner.nextLine());

    System.out.print("Name: ");
    String name = scanner.nextLine();

    return frontend.createPatient(nif, name);
  }

  private String changeMode() {
    displayModes();
    System.out.print(">>> ");
    int mode = Integer.parseInt(scanner.nextLine());
    if (mode < 0 || mode > 1) return "Invalid Mode!";

    return frontend.changeMode(mode);
  }

  private String tResAuth() {
    int pid = getPId();

    System.out.print("Record Id: ");
    int recId = Integer.parseInt(scanner.nextLine());
    return frontend.tResAuth(pid, recId);
  }

  private String logout() {
    return frontend.logout();
  }

  private int getPId() {
    System.out.print("Patient Id: ");
    return Integer.parseInt(scanner.nextLine());
  }

  private int getRecType() {
    displayRecTypes();
    System.out.print(">>> ");
    return Integer.parseInt(scanner.nextLine());
  }

  private void displayMenu() {
    System.out.println("============== Menu ==============");
    System.out.println("| 0 - Login                      |");
    System.out.println("| 1 - Read                       |");
    System.out.println("| 2 - Write                      |");
    System.out.println("| 3 - Test Result Auth Check     |");
    System.out.println("| 4 - Logout                     |");
    System.out.println("| 5 - Exit                       |");
    System.out.println("==================================");
  }

  private void displayRead() {
    System.out.println("============== Read ==============");
    System.out.println("| 0 - Record                     |");
    System.out.println("| 1 - Patient Details            |");
    System.out.println("| 2 - Check Mode                 |");
    System.out.println("==================================");
  }

  private void displaySearchBy() {
    System.out.println("=========== Search By ============");
    System.out.println("| 0 - Pid                        |");
    System.out.println("| 1 - Nif                        |");
    System.out.println("| 2 - Name                       |");
    System.out.println("==================================");
  }

  private void displayWrite() {
    System.out.println("============= Write ==============");
    System.out.println("| 0 - Record                     |");
    System.out.println("| 1 - Create User                |");
    System.out.println("| 2 - Create Patient             |");
    System.out.println("| 3 - Change Mode                |");
    System.out.println("==================================");
  }

  private void displayRecTypes() {
    System.out.println("========== Record Types ==========");
    System.out.println("| 0 - Test Results               |");
    System.out.println("| 1 - Housekeeping               |");
    System.out.println("| 2 - Diet                       |");
    System.out.println("| 3 - Transports                 |");
    System.out.println("| 4 - Reception                  |");
    System.out.println("| 5 - Prescription               |");
    System.out.println("==================================");
  }

  private void displayRoles() {
    System.out.println("============= Roles ==============");
    System.out.println("| 0 - Hospital Manager           |");
    System.out.println("| 1 - Laboratory Technician      |");
    System.out.println("| 2 - Ward Clerk                 |");
    System.out.println("| 3 - Porter                     |");
    System.out.println("| 4 - Patient Services Assistant |");
    System.out.println("| 5 - Clinical Assistant         |");
    System.out.println("| 6 - Nurse                      |");
    System.out.println("| 7 - Doctor                     |");
    System.out.println("| 8 - Employee                   |");
    System.out.println("| 9 - Partner Lab                |");
    System.out.println("==================================");
  }

  private void displayModes() {
    System.out.println("============= Modes ==============");
    System.out.println("| 0 - Regular                    |");
    System.out.println("| 1 - Pandemic                   |");
    System.out.println("==================================");
  }
}
