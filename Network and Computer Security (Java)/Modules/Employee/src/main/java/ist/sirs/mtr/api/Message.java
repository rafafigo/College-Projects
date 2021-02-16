package ist.sirs.mtr.api;

public enum Message {
  TOKEN_NOT_AVAILABLE("Token Not Available! Login Must be Performed!"),
  LOGIN_SUCCESS("Login Successful!"),
  WRITE_SUCCESS("Write Successful!"),
  LOGOUT_SUCCESS("Logout Successful!"),
  VALID_TEST_RESULT("Test Result Is Valid!"),
  INVALID_TEST_RESULT("Test Result Is Invalid!"),
  TEST_RESULT_NOT_FOUND("Test Result Not Found!"),
  CREATE_USER_SUCCESS("Creation Of User Successful!"),
  CHANCE_MODE_SUCCESS("Change Of Mode Successful!");

  public final String lbl;

  Message(String lbl) {
    this.lbl = lbl;
  }
}
