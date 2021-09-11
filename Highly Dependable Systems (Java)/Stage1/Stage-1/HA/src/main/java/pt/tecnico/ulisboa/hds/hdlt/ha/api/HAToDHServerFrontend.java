package pt.tecnico.ulisboa.hds.hdlt.ha.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesGrpc.DHServicesBlockingStub;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHRep;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHReq;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.DHReqPayload;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.Header;
import pt.tecnico.ulisboa.hds.hdlt.ha.error.HARuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.ha.session.Session;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.UNAVAILABLE;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class HAToDHServerFrontend {

  private static final Integer TIMEOUT = 5;
  private final String uname;
  private final HACrypto hCrypto;
  private final ManagedChannel channel;
  private final DHServicesBlockingStub dhStub;
  private Session session;

  public HAToDHServerFrontend(
      String uname, HACrypto hCrypto, String sHost, int sPort, Session session) {
    this.uname = uname;
    this.hCrypto = hCrypto;
    this.channel = ManagedChannelBuilder.forAddress(sHost, sPort).usePlaintext().build();
    this.dhStub = DHServicesGrpc.newBlockingStub(this.channel);
    this.session = session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public void dH() {
    KeyPair kPair;
    try {
      kPair = Crypto.doDHFirstPhase(null);
    } catch (AssertError e) {
      throw new HARuntimeException("Invalid Crypto Arguments!");
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
            this.hCrypto.signPayload(
                Bytes.concat(header.toByteArray(), payload.toByteArray(), nonce.toByteArray())));

    DHRep reply;
    try {
      reply =
          this.dhStub
              .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
              .dH(
                  DHReq.newBuilder()
                      .setHeader(header)
                      .setPayload(payload)
                      .setSignature(signature)
                      .build());
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == UNAVAILABLE || e.getStatus().getCode() == DEADLINE_EXCEEDED) {
        System.out.printf("%s%nRetrying (...)%n", e.getMessage());
        sleep(1000);
        this.dH();
        return;
      }
      try {
        this.hCrypto.checkErrorAuth(e, nonce);
      } catch (HARuntimeException e1) {
        System.out.printf("Exception Validation Failed: %s%nRetrying (...)%n", e1.getMessage());
        sleep(1000);
        this.dH();
        return;
      }
      throw new HARuntimeException(e.getMessage());
    }

    try {
      this.hCrypto.checkAuthSignature(
          reply.getSignature().toByteArray(),
          Bytes.concat(reply.getPayload().toByteArray(), nonce.toByteArray()));
    } catch (HARuntimeException e) {
      System.out.printf("%s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      this.dH();
      return;
    }

    BigInteger yServer = new BigInteger(1, reply.getPayload().getY().toByteArray());
    SecretKey secKey;
    try {
      secKey = Crypto.doDHSecondPhase(dhParamSpec, kPair, yServer);
    } catch (AssertError e) {
      System.out.printf("%s%nRetrying (...)%n", e.getMessage());
      sleep(1000);
      this.dH();
      return;
    }
    this.session.newSession(secKey);
  }

  public void shutdown() {
    channel.shutdown();
  }
}
