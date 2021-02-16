package ist.sirs.mtr.proto.hspl;

public enum ErrorMessage {
  TOO_MANY_ATTEMPTS("Too Many Invalid Login Attempts! Try Again Later!"),
  INVALID_CREDENTIALS("Invalid Credentials!"),
  INVALID_TOKEN("Invalid Access Token!"),
  PERMISSION_DENIED("Permission Denied!"),
  MISSING_HELLO("Missing Hello Message!"),
  MISSING_DH("Missing Diffie Hellman Handshake!"),
  INVALID_FRESHNESS("Message Freshness Test Failed!"),
  INVALID_INTEGRITY("Message Integrity Test Failed!"),
  MODIFIED_CERTIFICATE("Partner Lab Certificate Modified!"),
  INVALID_CRYPTO_ARGUMENTS("Invalid Cryptographic Arguments!"),
  NO_FIELD_SPECIFIED("No Field Specified!"),
  PID_NOT_FOUND("Patient Id Does Not Exist!");

  public final String lbl;

  ErrorMessage(String lbl) {
    this.lbl = lbl;
  }
}
