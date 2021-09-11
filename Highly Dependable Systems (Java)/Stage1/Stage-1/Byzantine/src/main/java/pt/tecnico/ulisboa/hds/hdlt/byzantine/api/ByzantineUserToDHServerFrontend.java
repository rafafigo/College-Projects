package pt.tecnico.ulisboa.hds.hdlt.byzantine.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHReq;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHReqPayload;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.Header;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;

public class ByzantineUserToDHServerFrontend {
  private final String uname;
  private final UserCrypto uCrypto;

  private final ManagedChannel channel;
  private final DHServicesGrpc.DHServicesBlockingStub stub;

  public ByzantineUserToDHServerFrontend(
      String uname, UserCrypto uCrypto, String sHost, int sPort) {
    this.uname = uname;
    this.uCrypto = uCrypto;
    this.channel = ManagedChannelBuilder.forAddress(sHost, sPort).usePlaintext().build();
    this.stub = DHServicesGrpc.newBlockingStub(this.channel);
  }

  public String dHNoFreshness() {
    KeyPair kPair;
    try {
      kPair = Crypto.doDHFirstPhase(null);
    } catch (AssertError e) {
      throw new UserRuntimeException("Invalid Crypto Arguments!");
    }
    DHPublicKey dhPubKey = (DHPublicKey) kPair.getPublic();
    DHParameterSpec dhParamSpec = dhPubKey.getParams();
    BigInteger nonce = Crypto.generateRandomNonce();

    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .build();

    DHReqPayload payload =
        DHReqPayload.newBuilder()
            .setP(ByteString.copyFrom(dhParamSpec.getP().toByteArray()))
            .setG(ByteString.copyFrom(dhParamSpec.getG().toByteArray()))
            .setY(ByteString.copyFrom(dhPubKey.getY().toByteArray()))
            .build();

    ByteString signature =
        ByteString.copyFrom(
            this.uCrypto.signPayload(
                Bytes.concat(header.toByteArray(), payload.toByteArray(), nonce.toByteArray())));

    DHReq req =
        DHReq.newBuilder().setHeader(header).setPayload(payload).setSignature(signature).build();
    this.stub.dH(req);
    try {
      // Double Nonce
      this.stub.dH(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
  }

  public String dHNoValidSignature() {
    KeyPair kPair;
    try {
      kPair = Crypto.doDHFirstPhase(null);
    } catch (AssertError e) {
      throw new UserRuntimeException("Invalid Crypto Arguments!");
    }
    DHPublicKey dhPubKey = (DHPublicKey) kPair.getPublic();
    DHParameterSpec dhParamSpec = dhPubKey.getParams();
    BigInteger nonce = Crypto.generateRandomNonce();

    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .build();

    DHReqPayload payload =
        DHReqPayload.newBuilder()
            .setP(ByteString.copyFrom(dhParamSpec.getP().toByteArray()))
            .setG(ByteString.copyFrom(dhParamSpec.getG().toByteArray()))
            .setY(ByteString.copyFrom(dhPubKey.getY().toByteArray()))
            .build();
    // Invalid Signature (Added a Nonce 2 Times to the signature)
    ByteString signature =
        ByteString.copyFrom(
            this.uCrypto.signPayload(Bytes.concat(header.toByteArray(), nonce.toByteArray())));

    DHReq req =
        DHReq.newBuilder().setHeader(header).setPayload(payload).setSignature(signature).build();
    try {
      this.stub.dH(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return "Should Not Happen!";
  }

  public void shutdown() {
    channel.shutdown();
  }
}
