package pt.tecnico.ulisboa.hds.hdlt.ha.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.hs.HAServerServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.hs.HAServerServicesGrpc.HAServerServicesBlockingStub;
import pt.tecnico.ulisboa.hds.hdlt.contract.hs.HAServerServicesOuterClass.*;
import pt.tecnico.ulisboa.hds.hdlt.ha.error.HARuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.ha.session.Session;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;

import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.*;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class HAToServerFrontend {

  private static final Integer TIMEOUT = 5;
  private final String uname;
  private final HACrypto hCrypto;
  private final ManagedChannel channel;
  private final HAServerServicesBlockingStub hsStub;
  private final HAToDHServerFrontend dhFrontend;
  private Session session;

  public HAToServerFrontend(
      String uname,
      HACrypto hCrypto,
      HAToDHServerFrontend dhFrontend,
      String sHost,
      int sPort,
      Session session) {
    this.uname = uname;
    this.hCrypto = hCrypto;
    this.dhFrontend = dhFrontend;
    this.session = session;
    this.channel = ManagedChannelBuilder.forAddress(sHost, sPort).usePlaintext().build();
    this.hsStub = HAServerServicesGrpc.newBlockingStub(this.channel);
  }

  public static String parseUnames(Integer x, Integer y, Integer epoch, ProtocolStringList unames) {
    if (unames.size() == 0) {
      return String.format("There are no Users at Location (%d, %d) at Epoch %d", x, y, epoch);
    }
    StringBuilder uL = new StringBuilder();
    uL.append(String.format("Users at Location (%d, %d) at Epoch %d:%n", x, y, epoch));
    for (String uname : unames) {
      uL.append(String.format("- %s%n", uname));
    }
    return uL.substring(0, uL.length() - 1);
  }

  public static String parseLocation(String uname, Integer x, Integer y, Integer epoch) {
    return String.format("%s Location at Epoch %d: (%d, %d)!", uname, epoch, x, y);
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public String obtainUL(String uname, Integer epoch) {
    if (!this.session.isValid()) this.dhFrontend.dH();

    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());

    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .setIv(ByteString.copyFrom(iv.getIV()))
            .build();

    ObtainULReqPayload payload = ObtainULReqPayload.newBuilder().setUname(uname).build();
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(this.session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), payload.toByteArray()));

    ObtainULRep reply;
    try {
      reply =
          this.hsStub
              .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
              .obtainUL(
                  ObtainULReq.newBuilder()
                      .setHeader(header)
                      .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
                      .setHmac(ByteString.copyFrom(hmac))
                      .build());
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == UNAVAILABLE || e.getStatus().getCode() == DEADLINE_EXCEEDED) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        return this.obtainUL(uname, epoch);
      }
      try {
        this.hCrypto.checkErrorAuth(e, nonce);
      } catch (HARuntimeException e1) {
        System.out.printf("Exception Validation Failed: %s%nRetrying (...)%n", e1.getMessage());
        sleep(1000);
        return this.obtainUL(uname, epoch);
      }
      if (e.getStatus().getCode() == UNAUTHENTICATED) {
        this.session.invalidate();
        return this.obtainUL(uname, epoch);
      }
      throw new HARuntimeException(e.getMessage());
    }

    iv = new IvParameterSpec(reply.getIv().toByteArray());
    byte[] repPayload;
    try {
      repPayload =
          Crypto.decipherBytesAES(
              this.session.getSecKey(), iv, reply.getCipheredPayload().toByteArray());
    } catch (AssertError e) {
      System.out.printf("Decryption Failed: %s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.obtainUL(uname, epoch);
    }

    try {
      this.hCrypto.checkAuthHmac(
          reply.getHmac().toByteArray(),
          Bytes.concat(repPayload, nonce.toByteArray(), reply.getIv().toByteArray()));
    } catch (HARuntimeException e) {
      System.out.printf("%s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.obtainUL(uname, epoch);
    }

    ObtainULRepPayload protoPayload;
    try {
      protoPayload = ObtainULRepPayload.parseFrom(repPayload);
    } catch (InvalidProtocolBufferException e) {
      System.out.printf("Invalid Payload: %s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.obtainUL(uname, epoch);
    }
    return parseLocation(uname, protoPayload.getX(), protoPayload.getY(), epoch);
  }

  public String obtainUAtL(Integer epoch, Integer x, Integer y) {
    if (!this.session.isValid()) this.dhFrontend.dH();

    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());

    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .setIv(ByteString.copyFrom(iv.getIV()))
            .build();

    ObtainUAtLReqPayload payload = ObtainUAtLReqPayload.newBuilder().setX(x).setY(y).build();
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(this.session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), payload.toByteArray()));

    ObtainUAtLRep reply;
    try {
      reply =
          this.hsStub
              .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
              .obtainUAtL(
                  ObtainUAtLReq.newBuilder()
                      .setHeader(header)
                      .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
                      .setHmac(ByteString.copyFrom(hmac))
                      .build());
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == UNAVAILABLE || e.getStatus().getCode() == DEADLINE_EXCEEDED) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        return this.obtainUAtL(epoch, x, y);
      }
      try {
        this.hCrypto.checkErrorAuth(e, nonce);
      } catch (HARuntimeException e1) {
        System.out.printf("Exception Validation Failed: %s%nRetrying (...)%n", e1.getMessage());
        sleep(1000);
        return this.obtainUAtL(epoch, x, y);
      }
      if (e.getStatus().getCode() == UNAUTHENTICATED) {
        this.session.invalidate();
        return this.obtainUAtL(epoch, x, y);
      }
      throw new HARuntimeException(e.getMessage());
    }

    iv = new IvParameterSpec(reply.getIv().toByteArray());
    byte[] repPayload =
        Crypto.decipherBytesAES(
            this.session.getSecKey(), iv, reply.getCipheredPayload().toByteArray());

    try {
      this.hCrypto.checkAuthHmac(
          reply.getHmac().toByteArray(),
          Bytes.concat(repPayload, nonce.toByteArray(), reply.getIv().toByteArray()));
    } catch (HARuntimeException e) {
      System.out.printf("%s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.obtainUAtL(epoch, x, y);
    }

    ObtainUAtLRepPayload protoPayload;
    try {
      protoPayload = ObtainUAtLRepPayload.parseFrom(repPayload);
    } catch (InvalidProtocolBufferException e) {
      System.out.printf("Invalid Payload: %s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      return this.obtainUAtL(epoch, x, y);
    }
    return parseUnames(x, y, epoch, protoPayload.getUnamesList());
  }

  public void shutdown() {
    channel.shutdown();
  }
}
