package pt.tecnico.ulisboa.hds.hdlt.server.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesGrpc.UserServerServicesImplBase;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.*;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.Proof;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.server.location.Location;
import pt.tecnico.ulisboa.hds.hdlt.server.repository.DBManager;
import pt.tecnico.ulisboa.hds.hdlt.server.session.Session;
import pt.tecnico.ulisboa.hds.hdlt.server.session.SessionsManager;

import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static io.grpc.Status.UNAUTHENTICATED;

public class UserServerServicesImpl extends UserServerServicesImplBase {

  private final ServerCrypto sCrypto;
  private final DBManager db;
  private final int nByzantineUsers;

  public UserServerServicesImpl(ServerCrypto sCrypto, DBManager db, int nByzantineUsers) {
    super();
    this.sCrypto = sCrypto;
    this.db = db;
    this.nByzantineUsers = nByzantineUsers;
  }

  @Override
  public void submitULReport(SubmitULReportReq req, StreamObserver<SubmitULReportRep> resObs) {

    Session session = SessionsManager.getSession(req.getHeader().getUname());
    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());

    if (session == null) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, UNAUTHENTICATED, "Session not established!", nonce);
    }
    this.sCrypto.checkFreshness(this.db, nonce);

    IvParameterSpec iv = new IvParameterSpec(req.getHeader().getIv().toByteArray());
    byte[] payload;
    try {
      payload =
          Crypto.decipherBytesAES(session.getSecKey(), iv, req.getCipheredPayload().toByteArray());
    } catch (AssertionError e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Invalid Session!", nonce);
    }

    SubmitULReportReqPayload reqPayload;
    try {
      reqPayload = SubmitULReportReqPayload.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Invalid Payload!", nonce);
    }
    this.sCrypto.checkAuthHmac(
        session,
        req.getHmac().toByteArray(),
        Bytes.concat(req.getHeader().toByteArray(), reqPayload.toByteArray()),
        nonce);

    int nAuthProofs = reqPayload.getAuthProofsList().size();
    if (nAuthProofs < this.nByzantineUsers) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Number of Proofs Insufficient!", nonce);
    }

    byte[] proofHash =
        Crypto.hash(
            Proof.newBuilder()
                .setUname(req.getHeader().getUname())
                .setEpoch(req.getHeader().getEpoch())
                .setX(reqPayload.getX())
                .setY(reqPayload.getY())
                .build()
                .toByteArray());

    if (nAuthProofs < this.nByzantineUsers) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Not Enough Valid Proofs!", nonce);
    }
    Set<String> seenUnames = new HashSet<>();
    seenUnames.add(req.getHeader().getUname());
    int nInvalidAuthProofs = 0;
    for (AuthProof authProof : reqPayload.getAuthProofsList()) {
      try {
        byte[] proof =
            this.sCrypto.unsignPayload(
                authProof.getUname(), authProof.getSignedProof().toByteArray());
        if (!Arrays.equals(proof, proofHash) || !seenUnames.add(authProof.getUname())) {
          nInvalidAuthProofs++;
        }
      } catch (AssertError e) {
        nInvalidAuthProofs++;
      }
      if (nAuthProofs - nInvalidAuthProofs < this.nByzantineUsers) {
        throw new ServerStatusRuntimeException(
            this.sCrypto, Status.INVALID_ARGUMENT, "Not Enough Valid Proofs!", nonce);
      }
    }
    this.db.addUserLocation(
        req.getHeader().getUname(),
        req.getHeader().getEpoch(),
        new Location(reqPayload.getX(), reqPayload.getY()),
        nonce);

    byte[] hmac = Crypto.hmac(session.getSecKey(), nonce.toByteArray());
    resObs.onNext(SubmitULReportRep.newBuilder().setHmac(ByteString.copyFrom(hmac)).build());
    resObs.onCompleted();
  }

  @Override
  public void obtainUL(ObtainULReq req, StreamObserver<ObtainULRep> resObs) {

    Session session = SessionsManager.getSession(req.getHeader().getUname());
    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());

    if (session == null) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, UNAUTHENTICATED, "Session not established!", nonce);
    }
    this.sCrypto.checkFreshness(this.db, nonce);
    this.sCrypto.checkAuthHmac(
        session, req.getHmac().toByteArray(), req.getHeader().toByteArray(), nonce);

    Location location =
        this.db.getUserLocation(req.getHeader().getUname(), req.getHeader().getEpoch(), nonce);
    ObtainULRepPayload payload =
        ObtainULRepPayload.newBuilder().setX(location.getX()).setY(location.getY()).build();

    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    byte[] cipheredPayload = Crypto.cipherBytesAES(session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            session.getSecKey(),
            Bytes.concat(payload.toByteArray(), nonce.toByteArray(), iv.getIV()));

    resObs.onNext(
        ObtainULRep.newBuilder()
            .setIv(ByteString.copyFrom(iv.getIV()))
            .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
            .setHmac(ByteString.copyFrom(hmac))
            .build());
    resObs.onCompleted();
  }
}
