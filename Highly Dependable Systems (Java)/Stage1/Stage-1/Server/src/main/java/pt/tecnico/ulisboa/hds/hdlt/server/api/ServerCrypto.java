package pt.tecnico.ulisboa.hds.hdlt.server.api;

import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.server.repository.DBManager;
import pt.tecnico.ulisboa.hds.hdlt.server.repository.InMemoryDB;
import pt.tecnico.ulisboa.hds.hdlt.server.session.Session;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServerCrypto {

  private final PrivateKey sPrivKey;
  private final Map<String, PublicKey> uPubKeys;

  public ServerCrypto(String sPrivKeyPath, String uPubKeysDirPath, String haPubKeyPath) {
    this.sPrivKey = Crypto.loadPrivKey(new File(sPrivKeyPath));
    File uPubKeysDir = new File(uPubKeysDirPath);
    if (!uPubKeysDir.isDirectory()) {
      throw new AssertError(
          ServerCrypto.class.getSimpleName(),
          String.format("[%s] is not a Directory!", uPubKeysDirPath));
    }
    File[] uPubKeyFiles = uPubKeysDir.listFiles();
    if (uPubKeyFiles == null) {
      throw new AssertError(
          ServerCrypto.class.getSimpleName(),
          String.format("[%s] Directory is Empty!", uPubKeysDirPath));
    }
    this.uPubKeys = new HashMap<>();
    this.uPubKeys.put("HA", Crypto.loadPubKey(new File(haPubKeyPath)));
    for (File uPubKeyFile : uPubKeyFiles) {
      String uPubKeyFilename = uPubKeyFile.getName();
      this.uPubKeys.put(
          uPubKeyFilename.substring(0, uPubKeyFilename.lastIndexOf('.')),
          Crypto.loadPubKey(uPubKeyFile));
    }
  }

  public byte[] signPayload(byte[] payload) {
    return Crypto.cipherBytesRSAPriv(this.sPrivKey, Crypto.hash(payload));
  }

  public byte[] unsignPayload(String uname, byte[] signedPayload) {
    return Crypto.decipherBytesRSAPub(this.uPubKeys.get(uname), signedPayload);
  }

  public byte[] generateHmac(Session session, byte[] payload) {
    return Crypto.hmac(session.getSecKey(), payload);
  }

  public void checkFreshness(DBManager db, BigInteger nonce) {
    if (!InMemoryDB.addNonce(db, nonce)) {
      throw new ServerStatusRuntimeException(
          this, INVALID_ARGUMENT, "Freshness Tests Failed!", nonce);
    }
  }

  public void checkAuthSignature(String uname, byte[] signature, byte[] payload, BigInteger nonce) {
    if (!Arrays.equals(this.unsignPayload(uname, signature), Crypto.hash(payload))) {
      throw new ServerStatusRuntimeException(
          this, INVALID_ARGUMENT, "Authenticity Tests Failed!", nonce);
    }
  }

  public void checkAuthHmac(Session session, byte[] hmac, byte[] payload, BigInteger nonce) {
    if (!Arrays.equals(this.generateHmac(session, payload), hmac)) {
      throw new ServerStatusRuntimeException(
          this, INVALID_ARGUMENT, "Authenticity Tests Failed!", nonce);
    }
  }
}
