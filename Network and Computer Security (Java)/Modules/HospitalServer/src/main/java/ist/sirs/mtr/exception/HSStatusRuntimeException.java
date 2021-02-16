package ist.sirs.mtr.exception;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import ist.sirs.mtr.api.HSPartnerLabServicesImpl;
import ist.sirs.mtr.proto.hspl.HSPartnerLabContract.SecureMessage;

public class HSStatusRuntimeException extends StatusRuntimeException {

  public HSStatusRuntimeException(HSRuntimeException exception) {
    super(
        exception.getStatus().withDescription(exception.getMessage()),
        generateRSASecureMessage(exception.getStatus(), exception.getMessage()));
  }

  private static Metadata generateRSASecureMessage(Status status, String description) {
    Metadata metadata = new Metadata();
    String msg = String.format("%d%s", status.getCode().value(), description);
    metadata.put(
        ProtoUtils.keyForProto(SecureMessage.getDefaultInstance()),
        HSPartnerLabServicesImpl.generateRSASecureMessage(msg));
    return metadata;
  }
}
