package pt.tecnico.ulisboa.hds.hdlt.byzantine.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.Header;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.ObtainULReq;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.SubmitULReportReq;
import pt.tecnico.ulisboa.hds.hdlt.contract.us.UserServerServicesOuterClass.SubmitULReportReqPayload;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;

public class ByzantineUserToServerFrontend {

  private final String uname;
  private final UserToDHServerFrontend dhFrontend;
  private final Session session;
  private final ManagedChannel channel;
  private final UserServerServicesGrpc.UserServerServicesBlockingStub stub;

  public ByzantineUserToServerFrontend(
      String uname, UserToDHServerFrontend dhFrontend, String sHost, int sPort, Session session) {
    this.uname = uname;
    this.dhFrontend = dhFrontend;
    this.session = session;
    this.channel = ManagedChannelBuilder.forAddress(sHost, sPort).usePlaintext().build();
    this.stub = UserServerServicesGrpc.newBlockingStub(this.channel);
  }

  public String submitULReportNoFreshness(Integer epoch) {
    SubmitULReportReqPayload payload = SubmitULReportReqPayload.newBuilder().build();
    if (!this.session.isValid()) this.dhFrontend.dH();
    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(this.session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), payload.toByteArray()));
    SubmitULReportReq req =
        SubmitULReportReq.newBuilder()
            .setHeader(header)
            .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
            .setHmac(ByteString.copyFrom(hmac))
            .build();
    try {
      this.stub.submitULReport(req);
    } catch (StatusRuntimeException ignored) {
    }
    try {
      // Double Nonce
      this.stub.submitULReport(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
  }

  public String submitULReportNoValidHMAC(Integer epoch) {
    SubmitULReportReqPayload payload = SubmitULReportReqPayload.newBuilder().build();
    if (!this.session.isValid()) this.dhFrontend.dH();
    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);
    byte[] cipheredPayload =
        Crypto.cipherBytesAES(this.session.getSecKey(), iv, payload.toByteArray());
    byte[] hmac =
        // Invalid HMAC (Added Nonce 2 Times to the hmac)
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), nonce.toByteArray()));
    SubmitULReportReq req =
        SubmitULReportReq.newBuilder()
            .setHeader(header)
            .setCipheredPayload(ByteString.copyFrom(cipheredPayload))
            .setHmac(ByteString.copyFrom(hmac))
            .build();
    try {
      this.stub.submitULReport(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
  }

  public String obtainULNoFreshness(Integer epoch) {
    if (!this.session.isValid()) this.dhFrontend.dH();

    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);
    byte[] hmac = Crypto.hmac(this.session.getSecKey(), header.toByteArray());

    ObtainULReq req =
        ObtainULReq.newBuilder().setHeader(header).setHmac(ByteString.copyFrom(hmac)).build();
    try {
      this.stub.obtainUL(req);
    } catch (StatusRuntimeException ignored) {
    }
    try {
      // Double Nonce
      this.stub.obtainUL(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
  }

  public String obtainULNoValidHMAC(Integer epoch) {
    if (!this.session.isValid()) this.dhFrontend.dH();
    BigInteger nonce = Crypto.generateRandomNonce();
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    Header header = this.generateHeader(epoch, nonce, iv);
    // Invalid HMAC (Added Nonce 2 Times to the hmac)
    byte[] hmac =
        Crypto.hmac(
            this.session.getSecKey(), Bytes.concat(header.toByteArray(), nonce.toByteArray()));

    ObtainULReq req =
        ObtainULReq.newBuilder().setHeader(header).setHmac(ByteString.copyFrom(hmac)).build();
    try {
      this.stub.obtainUL(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
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
