package pt.tecnico.ulisboa.hds.hdlt.user.error;

public class UserRuntimeException extends RuntimeException {

  public UserRuntimeException(String errorMsg) {
    super(errorMsg);
  }
}
