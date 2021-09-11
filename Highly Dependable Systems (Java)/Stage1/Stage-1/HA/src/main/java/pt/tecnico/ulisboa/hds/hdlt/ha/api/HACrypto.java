package pt.tecnico.ulisboa.hds.hdlt.ha.api;

import com.google.common.primitives.Bytes;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.Signature;
import pt.tecnico.ulisboa.hds.hdlt.ha.error.HARuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.ha.session.Session;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class HACrypto {

  private static final Metadata.Key<Signature> signatureErrorKey =
      ProtoUtils.keyForProto(Signature.getDefaultInstance());

  private final PrivateKey uPrivKey;
  private final PublicKey sPubKey;
  private Session session;

  public HACrypto(String uPrivKeyPath, String sPubKeyPath, Session session) {
    this.uPrivKey = Crypto.loadPrivKey(new File(uPrivKeyPath));
    this.sPubKey = Crypto.loadPubKey(new File(sPubKeyPath));
    this.session = session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public byte[] signPayload(byte[] payload) {
    return Crypto.cipherBytesRSAPriv(this.uPrivKey, Crypto.hash(Bytes.concat(payload)));
  }

  public byte[] unsignPayload(byte[] signedPayload) {
    return Crypto.decipherBytesRSAPub(this.sPubKey, signedPayload);
  }

  public void checkAuthSignature(byte[] signature, byte[] payload) {
    if (!Arrays.equals(this.unsignPayload(signature), Crypto.hash(payload))) {
      throw new HARuntimeException("Authenticity Tests Failed!");
    }
  }

  public void checkAuthHmac(byte[] hmac, byte[] payload) {
    if (!Arrays.equals(Crypto.hmac(this.session.getSecKey(), payload), hmac)) {
      throw new HARuntimeException("Authenticity Tests Failed!");
    }
  }

  public void checkErrorAuth(StatusRuntimeException e, BigInteger nonce) {
    Metadata metadata = e.getTrailers();
    if (metadata == null || !metadata.containsKey(signatureErrorKey)) {
      throw new HARuntimeException("Authenticity Checks Failed!");
    }
    Signature signature = metadata.get(signatureErrorKey);
    if (signature == null) {
      throw new HARuntimeException("Authenticity Checks Failed!");
    }
    String payload =
        String.format(
            "%d%s%d", e.getStatus().getCode().value(), e.getStatus().getDescription(), nonce);
    this.checkAuthSignature(signature.getByteString().toByteArray(), payload.getBytes());
  }
}
