package ist.sirs.mtr.proto.hse;

public enum ErrorMessage {
  TOO_MANY_ATTEMPTS("Too Many Invalid Login Attempts! Try Again Later!"),
  INVALID_CREDENTIALS("Invalid Credentials!"),
  INVALID_RECORD_TYPE("Invalid Record Type!"),
  CERTIFICATE_NOT_FOUND("Lab Certificate Not Found"),
  ALREADY_AUTH("Already Logged in!"),
  INVALID_TOKEN("Access Token has Expired!"),
  PERMISSION_DENIED("Permission Denied!"),
  NO_UID("Employee Not Registered!"),
  NO_RECORD("Record Not Available!"),
  INVALID_NIF("Invalid Nif!"),
  INVALID_UNAME("Invalid Username!"),
  NO_FIELD_SPECIFIED("No Field Specified!"),
  PID_NOT_FOUND("Patient Id Does Not Exist!"),
  NIF_ALREADY_EXISTS("Nif Already Exists!");

  public final String lbl;

  ErrorMessage(String lbl) {
    this.lbl = lbl;
  }
}
