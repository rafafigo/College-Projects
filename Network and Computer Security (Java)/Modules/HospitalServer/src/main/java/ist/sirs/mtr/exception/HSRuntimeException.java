package ist.sirs.mtr.exception;

import io.grpc.Status;

public class HSRuntimeException extends RuntimeException {

  private final Status status;

  public HSRuntimeException(String error) {
    super(error);
    this.status = Status.ABORTED;
  }

  public HSRuntimeException(Status status, String error) {
    super(error);
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }
}
