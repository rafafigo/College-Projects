package pt.tecnico.ulisboa.hds.hdlt.user.api;

import com.google.common.primitives.Bytes;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.RequestULProofRep;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;

import java.math.BigInteger;

import static io.grpc.Status.Code.UNAVAILABLE;

public class RequestULStreamObserver implements StreamObserver<RequestULProofRep> {

  private final UserCrypto uCrypto;
  private final String uname;
  private final SyncProofs syncProofs;
  private final byte[] proof;
  private final BigInteger nonce;

  public RequestULStreamObserver(
      UserCrypto uCrypto, String uname, SyncProofs syncProofs, byte[] proof, BigInteger nonce) {
    super();
    this.uCrypto = uCrypto;
    this.uname = uname;
    this.syncProofs = syncProofs;
    this.proof = proof;
    this.nonce = nonce;
  }

  @Override
  public void onNext(RequestULProofRep rep) {
    try {
      this.uCrypto.checkAuthSignature(
          this.uname,
          rep.getSignature().toByteArray(),
          Bytes.concat(this.proof, this.nonce.toByteArray()));
      this.uCrypto.checkAuthSignature(this.uname, rep.getSignedProof().toByteArray(), this.proof);
      this.syncProofs.addProof(this.uname, rep.getSignedProof().toByteArray());
    } catch (UserRuntimeException e) {
      this.syncProofs.newReply();
      System.out.printf("Invalid Response From User '%s'!%n", this.uname);
    }
  }

  @Override
  public void onError(Throwable throwable) {
    this.syncProofs.newReply();
    try {
      this.uCrypto.checkErrorAuth(this.uname, (StatusRuntimeException) throwable, nonce);
    } catch (UserRuntimeException e) {
      if (((StatusRuntimeException) throwable).getStatus().getCode() != UNAVAILABLE) {
        System.out.printf("Invalid Exception From User '%s'!%n", this.uname);
      }
    }
  }

  @Override
  public void onCompleted() {}
}
