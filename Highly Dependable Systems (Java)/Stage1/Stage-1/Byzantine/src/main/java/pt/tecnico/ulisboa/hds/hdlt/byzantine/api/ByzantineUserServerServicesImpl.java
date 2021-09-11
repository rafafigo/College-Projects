package pt.tecnico.ulisboa.hds.hdlt.byzantine.api;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesGrpc.UserServerServicesImplBase;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.ObtainULRep;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.ObtainULReq;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.SubmitULReportRep;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.SubmitULReportReq;
import pt.tecnico.ulisboa.hds.hdlt.server.api.ServerCrypto;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;

import java.math.BigInteger;

import static io.grpc.Status.UNKNOWN;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class ByzantineUserServerServicesImpl extends UserServerServicesImplBase {

  public static final int UNSIGNED_EXCEPTION = 0;
  public static final int NEVER_SIGN_EXCEPTION = 1;
  public static final int SIGN_EXCEPTION = 2;
  private final ServerCrypto sCrypto;
  private int mode = UNSIGNED_EXCEPTION;

  public ByzantineUserServerServicesImpl(ServerCrypto sCrypto) {
    super();
    this.sCrypto = sCrypto;
  }

  public void setMode(int mode) {
    this.mode = mode;
  }

  @Override
  public void submitULReport(SubmitULReportReq req, StreamObserver<SubmitULReportRep> resObs) {

    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());
    switch (this.mode) {
      case UNSIGNED_EXCEPTION:
        sleep(50);
        resObs.onError(Status.UNKNOWN.withDescription("Exception Unsigned!").asRuntimeException());
        break;
      case NEVER_SIGN_EXCEPTION:
        return;
      case SIGN_EXCEPTION:
        throw new ServerStatusRuntimeException(this.sCrypto, UNKNOWN, "Exception Signed!", nonce);
    }
  }

  @Override
  public void obtainUL(ObtainULReq req, StreamObserver<ObtainULRep> resObs) {
    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());
    switch (this.mode) {
      case UNSIGNED_EXCEPTION:
        sleep(50);
        resObs.onError(Status.UNKNOWN.withDescription("Exception unsigned!").asRuntimeException());
        break;
      case NEVER_SIGN_EXCEPTION:
        return;
      case SIGN_EXCEPTION:
        throw new ServerStatusRuntimeException(this.sCrypto, UNKNOWN, "Exception Signed!", nonce);
    }
  }
}
