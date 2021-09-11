package pt.tecnico.ulisboa.hds.hdlt.user.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesGrpc.UserServerServicesBlockingStub;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.*;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.location.Location;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.*;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class UserToServerFrontend {

  private static final Integer TIMEOUT = 5;
  private final String uname;
  private final GridManager grid;
  private final UserCrypto uCrypto;
  private final UserToDHServerFrontend dhFrontend;
  private final ManagedChannel channel;
  private final UserServerServicesBlockingStub stub;
  private Session session;

  public UserToServerFrontend(
      String uname,
      GridManager grid,
      UserCrypto uCrypto,
      UserToDHServerFrontend dhFrontend,
      String sHost,
      int sPort,
      Session session) {
    this.uname = uname;
    this.grid = grid;
    this.uCrypto = uCrypto;
    this.dhFrontend = dhFrontend;
    this.session = session;
    this.channel = ManagedChannelBuilder.forAddress(sHost, sPort).usePlaintext().build();
    this.stub = UserServerServicesGrpc.newBlockingStub(this.channel);
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public void submitULReport(Integer epoch, Map<String, byte[]> authProofs) {

    SubmitULReportReqPayload.Builder payloadBuilder = SubmitULReportReqPayload.newBuilder();
    Location location = this.grid.getLocation(this.uname, epoch);
    payloadBuilder.setX(location.getX());
    payloadBuilder.setY(location.getY());

    for (Map.Entry<String, byte[]> authProof : authProofs.entrySet()) {
      payloadBuilder.addAuthProofs(
          AuthProof.newBuilder()
              .setUname(authProof.getKey())
              .setSignedProof(ByteString.copyFrom(authProof.getValue()))
              .build());
    }
    SubmitULReportReqPayload payload = payloadBuilder.build();
    this.doSubmitULReport(epoch, payload);
  }

  private void doSubmitULReport(Integer epoch, SubmitULReportReqPayload payload) {
    if (!this.session.isValid()) this.dhFrontend.dH();

    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);

    byte[] cipheredPayload =
        Crypto.cipherBytesAES(this.session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), payload.toByteArray()));
    try {
      SubmitULReportRep reply =
          this.stub
              .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
              .submitULReport(
                  SubmitULReportReq.newBuilder()
                      .setHeader(header)
                      .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
                      .setHmac(ByteString.copyFrom(hmac))
                      .build());
      try {
        this.uCrypto.checkAuthHmac(reply.getHmac().toByteArray(), nonce.toByteArray());
      } catch (UserRuntimeException e) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        this.doSubmitULReport(epoch, payload);
      }
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == UNAVAILABLE || e.getStatus().getCode() == DEADLINE_EXCEEDED) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        this.doSubmitULReport(epoch, payload);
        return;
      }
      try {
        this.uCrypto.checkErrorAuth(e, nonce);
      } catch (UserRuntimeException e1) {
        System.out.printf("Exception Validation Failed: %s%nRetrying (...)%n", e1.getMessage());
        sleep(1000);
        this.doSubmitULReport(epoch, payload);
        return;
      }
      if (e.getStatus().getCode() == UNAUTHENTICATED) {
        this.session.invalidate();
        this.doSubmitULReport(epoch, payload);
        return;
      }
      throw new UserRuntimeException(e.getMessage());
    }
  }

  public Location obtainUL(Integer epoch) {
    ObtainULRepPayload payload = this.doObtainUL(epoch);
    return new Location(payload.getX(), payload.getY());
  }

  private ObtainULRepPayload doObtainUL(Integer epoch) {

    if (!this.session.isValid()) this.dhFrontend.dH();

    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);

    byte[] hmac = Crypto.hmac(this.session.getSecKey(), header.toByteArray());

    ObtainULRep reply;
    try {
      reply =
          this.stub
              .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
              .obtainUL(
                  ObtainULReq.newBuilder()
                      .setHeader(header)
                      .setHmac(ByteString.copyFrom(hmac))
                      .build());
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == UNAVAILABLE || e.getStatus().getCode() == DEADLINE_EXCEEDED) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        return this.doObtainUL(epoch);
      }
      try {
        this.uCrypto.checkErrorAuth(e, nonce);
      } catch (UserRuntimeException e1) {
        System.out.printf("Exception Validation Failed: %s%nRetrying (...)%n", e1.getMessage());
        sleep(1000);
        return this.doObtainUL(epoch);
      }
      if (e.getStatus().getCode() == UNAUTHENTICATED) {
        this.session.invalidate();
        return this.doObtainUL(epoch);
      }
      throw new UserRuntimeException(e.getMessage());
    }

    iv = new IvParameterSpec(reply.getIv().toByteArray());
    byte[] payload;
    try {
      payload =
          Crypto.decipherBytesAES(
              this.session.getSecKey(), iv, reply.getCipheredPayload().toByteArray());
    } catch (AssertError e) {
      System.out.printf("Decryption Failed: %s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.doObtainUL(epoch);
    }

    try {
      this.uCrypto.checkAuthHmac(
          reply.getHmac().toByteArray(), Bytes.concat(payload, nonce.toByteArray(), iv.getIV()));
    } catch (UserRuntimeException e) {
      System.out.printf("%s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.doObtainUL(epoch);
    }

    ObtainULRepPayload repPayload;
    try {
      repPayload = ObtainULRepPayload.parseFrom(payload);
    } catch (InvalidProtocolBufferException e) {
      System.out.printf("Invalid Payload: %s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.doObtainUL(epoch);
    }
    return repPayload;
  }

  private Header generateHeader(Integer epoch, BigInteger nonce, IvParameterSpec iv) {
    return Header.newBuilder()
        .setUname(this.uname)
        .setEpoch(epoch)
        .setNonce(ByteString.copyFrom(nonce.toByteArray()))
        .setIv(ByteString.copyFrom(iv.getIV()))
        .build();
  }

  public void shutdown() {
    channel.shutdown();
  }
}
