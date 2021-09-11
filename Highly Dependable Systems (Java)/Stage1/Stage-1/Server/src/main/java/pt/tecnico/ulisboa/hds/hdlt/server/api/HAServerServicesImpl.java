package pt.tecnico.ulisboa.hds.hdlt.server.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.contract.hs.HAServerServicesGrpc.HAServerServicesImplBase;
import pt.tecnico.ulisboa.hds.hdlt.contract.hs.HAServerServicesOuterClass.*;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.server.location.Location;
import pt.tecnico.ulisboa.hds.hdlt.server.repository.DBManager;
import pt.tecnico.ulisboa.hds.hdlt.server.session.Session;
import pt.tecnico.ulisboa.hds.hdlt.server.session.SessionsManager;

import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.util.List;

import static io.grpc.Status.*;

public class HAServerServicesImpl extends HAServerServicesImplBase {

  private final ServerCrypto sCrypto;
  private final DBManager db;

  public HAServerServicesImpl(ServerCrypto sCrypto, DBManager db) {
    super();
    this.sCrypto = sCrypto;
    this.db = db;
  }

  @Override
  public void obtainUL(ObtainULReq req, StreamObserver<ObtainULRep> resObs) {
    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());

    if (!req.getHeader().getUname().equals("HA")) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, PERMISSION_DENIED, "Permission Denied!", nonce);
    }

    Session session = SessionsManager.getSession(req.getHeader().getUname());
    IvParameterSpec iv = new IvParameterSpec(req.getHeader().getIv().toByteArray());

    if (session == null)
      throw new ServerStatusRuntimeException(
          this.sCrypto, UNAUTHENTICATED, "Session not established!", nonce);

    this.sCrypto.checkFreshness(this.db, nonce);

    byte[] payload;
    try {
      payload =
          Crypto.decipherBytesAES(session.getSecKey(), iv, req.getCipheredPayload().toByteArray());
    } catch (AssertionError e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Invalid Session!", nonce);
    }

    this.sCrypto.checkAuthHmac(
        session,
        req.getHmac().toByteArray(),
        Bytes.concat(req.getHeader().toByteArray(), payload),
        nonce);

    ObtainULReqPayload reqPayload;
    try {
      reqPayload = ObtainULReqPayload.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, INVALID_ARGUMENT, "Invalid Payload!", nonce);
    }

    Location location =
        this.db.getUserLocation(reqPayload.getUname(), req.getHeader().getEpoch(), nonce);

    iv = new IvParameterSpec(Crypto.generateRandomIV());
    ObtainULRepPayload repPayload =
        ObtainULRepPayload.newBuilder().setX(location.getX()).setY(location.getY()).build();
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(session.getSecKey(), iv, repPayload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            session.getSecKey(),
            Bytes.concat(repPayload.toByteArray(), nonce.toByteArray(), iv.getIV()));

    resObs.onNext(
        ObtainULRep.newBuilder()
            .setIv(ByteString.copyFrom(iv.getIV()))
            .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
            .setHmac(ByteString.copyFrom(hmac))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void obtainUAtL(ObtainUAtLReq req, StreamObserver<ObtainUAtLRep> resObs) {
    BigInteger nonce = new BigInteger(1, req.getHeader().getNonce().toByteArray());

    if (!req.getHeader().getUname().equals("HA")) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, PERMISSION_DENIED, "Permission Denied!", nonce);
    }

    Session session = SessionsManager.getSession(req.getHeader().getUname());
    IvParameterSpec iv = new IvParameterSpec(req.getHeader().getIv().toByteArray());

    if (session == null)
      throw new ServerStatusRuntimeException(
          this.sCrypto, UNAUTHENTICATED, "Session not established!", nonce);

    this.sCrypto.checkFreshness(this.db, nonce);

    byte[] payload;
    try {
      payload =
          Crypto.decipherBytesAES(session.getSecKey(), iv, req.getCipheredPayload().toByteArray());
    } catch (AssertionError e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, Status.INVALID_ARGUMENT, "Invalid Session!", nonce);
    }

    this.sCrypto.checkAuthHmac(
        session,
        req.getHmac().toByteArray(),
        Bytes.concat(req.getHeader().toByteArray(), payload),
        nonce);

    ObtainUAtLReqPayload reqPayload;
    try {
      reqPayload = ObtainUAtLReqPayload.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      throw new ServerStatusRuntimeException(
          this.sCrypto, INVALID_ARGUMENT, "Invalid Payload!", nonce);
    }

    List<String> unames =
        this.db.getUsersAtLocation(
            req.getHeader().getEpoch(), new Location(reqPayload.getX(), reqPayload.getY()));

    ObtainUAtLRepPayload.Builder repPayload = ObtainUAtLRepPayload.newBuilder();

    for (String uname : unames) {
      repPayload.addUnames(uname);
    }

    iv = new IvParameterSpec(Crypto.generateRandomIV());
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(session.getSecKey(), iv, repPayload.build().toByteArray());
    byte[] hmac =
        Crypto.hmac(
            session.getSecKey(),
            Bytes.concat(repPayload.build().toByteArray(), nonce.toByteArray(), iv.getIV()));

    resObs.onNext(
        ObtainUAtLRep.newBuilder()
            .setIv(ByteString.copyFrom(iv.getIV()))
            .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
            .setHmac(ByteString.copyFrom(hmac))
            .build());
    resObs.onCompleted();
  }
}
