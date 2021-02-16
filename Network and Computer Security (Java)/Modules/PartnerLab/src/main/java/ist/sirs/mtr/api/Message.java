package ist.sirs.mtr.api;

public enum Message {
  TOKEN_NOT_AVAILABLE("Token Not Available! Login Must be Performed!"),
  INVALID_CERTIFICATE("Invalid Hospital Server Certificate!"),
  LOGIN_SUCCESS("Login Successful!"),
  TEST_RES_WRITTEN("Test Result Write Successful!"),
  TEST_RES_SUBMITTED("Test Results Submission Successful!"),
  LOGOUT_SUCCESS("Logout Successful!");

  public final String lbl;

  Message(String lbl) {
    this.lbl = lbl;
  }
}
