package pt.tecnico.ulisboa.hds.hdlt.byzantine.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesGrpc.DHServicesImplBase;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHRep;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHRepPayload;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHReq;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.server.api.ServerCrypto;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.server.session.SessionsManager;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;

public class ByzantineDHServicesImpl extends DHServicesImplBase {

  private final ServerCrypto sCrypto;
  private final Integer expSec;

  public ByzantineDHServicesImpl(ServerCrypto sCrypto, int expSec) {
    super();
    this.sCrypto = sCrypto;
    this.expSec = expSec;
  }

  // DH Without Checking Freshness
  @Override
  public void dH(DHReq req, StreamObserver<DHRep> resObs) {

    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());
    // Getting DH Arguments
    BigInteger p = new BigInteger(1, req.getPayload().getP().toByteArray());
    BigInteger g = new BigInteger(1, req.getPayload().getG().toByteArray());
    BigInteger yUser = new BigInteger(1, req.getPayload().getY().toByteArray());

    KeyPair kPair;
    SecretKey secKey;
    try {
      // Executing Diffie-Hellman
      DHParameterSpec dhParamSpec = new DHParameterSpec(p, g);
      kPair = Crypto.doDHFirstPhase(dhParamSpec);
      secKey = Crypto.doDHSecondPhase(dhParamSpec, kPair, yUser);
    } catch (AssertError e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Invalid Crypto Arguments!", nonce);
    }

    SessionsManager.newSession(req.getHeader().getUname(), this.expSec, secKey);
    BigInteger yServer = ((DHPublicKey) kPair.getPublic()).getY();

    DHRepPayload payload =
        DHRepPayload.newBuilder().setY(ByteString.copyFrom(yServer.toByteArray())).build();

    resObs.onNext(
        DHRep.newBuilder()
            .setPayload(payload)
            .setSignature(
                ByteString.copyFrom(
                    this.sCrypto.signPayload(
                        Bytes.concat(payload.toByteArray(), nonce.toByteArray()))))
            .build());
    resObs.onCompleted();
  }
}
