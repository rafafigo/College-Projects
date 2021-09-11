package pt.tecnico.ulisboa.hds.hdlt.user.api;

import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import pt.tecnico.ulisboa.hds.hdlt.contract.dh.DHServicesOuterClass.Signature;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserCrypto {

  private static final Metadata.Key<Signature> signatureErrorKey =
      ProtoUtils.keyForProto(Signature.getDefaultInstance());

  private final PrivateKey uPrivKey;
  private final PublicKey sPubKey;
  private final Map<String, PublicKey> uPubKeys;
  private Session session;

  public UserCrypto(
      String sPubKeyPath, String uPrivKeyPath, String uPubKeysDirPath, Session session) {
    this.uPrivKey = Crypto.loadPrivKey(new File(uPrivKeyPath));
    this.sPubKey = Crypto.loadPubKey(new File(sPubKeyPath));
    this.session = session;
    File uPubKeysDir = new File(uPubKeysDirPath);
    if (!uPubKeysDir.isDirectory()) {
      throw new AssertError(
          UserCrypto.class.getSimpleName(),
          String.format("[%s] is not a Directory!", uPubKeysDirPath));
    }
    File[] uPubKeyFiles = uPubKeysDir.listFiles();
    if (uPubKeyFiles == null) {
      throw new AssertError(
          UserCrypto.class.getSimpleName(),
          String.format("[%s] Directory is Empty!", uPubKeysDirPath));
    }
    this.uPubKeys = new HashMap<>();
    for (File uPubKeyFile : uPubKeyFiles) {
      String uPubKeyFilename = uPubKeyFile.getName();
      this.uPubKeys.put(
          uPubKeyFilename.substring(0, uPubKeyFilename.lastIndexOf('.')),
          Crypto.loadPubKey(uPubKeyFile));
    }
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public byte[] signPayload(byte[] payload) {
    return Crypto.cipherBytesRSAPriv(this.uPrivKey, Crypto.hash(payload));
  }

  public byte[] unsignPayload(byte[] signedPayload) {
    return this.doUnsignPayload(sPubKey, signedPayload);
  }

  public byte[] unsignPayload(String uname, byte[] signedPayload) {
    return this.doUnsignPayload(uPubKeys.get(uname), signedPayload);
  }

  private byte[] doUnsignPayload(PublicKey pubKey, byte[] signedPayload) {
    return Crypto.decipherBytesRSAPub(pubKey, signedPayload);
  }

  public void checkAuthSignature(byte[] signature, byte[] payload) {
    if (!Arrays.equals(this.unsignPayload(signature), Crypto.hash(payload))) {
      throw new UserRuntimeException("Authenticity Tests Failed!");
    }
  }

  public void checkAuthSignature(String uname, byte[] signature, byte[] payload) {
    if (!Arrays.equals(this.unsignPayload(uname, signature), Crypto.hash(payload))) {
      throw new UserRuntimeException("Authenticity Tests Failed!");
    }
  }

  public void checkAuthHmac(byte[] hmac, byte[] payload) {
    if (!Arrays.equals(Crypto.hmac(this.session.getSecKey(), payload), hmac)) {
      throw new UserRuntimeException("Authenticity Tests Failed!");
    }
  }

  public void checkErrorAuth(StatusRuntimeException e, BigInteger nonce) {
    this.checkErrorAuth(null, e, nonce);
  }

  public void checkErrorAuth(String uname, StatusRuntimeException e, BigInteger nonce) {
    Metadata metadata = e.getTrailers();
    if (metadata == null || !metadata.containsKey(signatureErrorKey)) {
      throw new UserRuntimeException("Authenticity Checks Failed!");
    }
    Signature signature = metadata.get(signatureErrorKey);
    if (signature == null) {
      throw new UserRuntimeException("Authenticity Checks Failed!");
    }
    String payload =
        String.format(
            "%d%s%d", e.getStatus().getCode().value(), e.getStatus().getDescription(), nonce);
    if (uname == null) {
      this.checkAuthSignature(signature.getByteString().toByteArray(), payload.getBytes());
    } else {
      this.checkAuthSignature(uname, signature.getByteString().toByteArray(), payload.getBytes());
    }
  }
}
