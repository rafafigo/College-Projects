package ist.sirs.mtr.crypto;

import ist.sirs.mtr.error.AssertError;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class Crypto {

  private static final int IV_SIZE = 16;
  private static final int DH_KEY_SIZE = 3072;
  private static final Map<String, CryptoRSAKeys> cryptoRSAKeys = new HashMap<>();
  private static final Map<String, CryptoAESKeys> cryptoAESKeys = new HashMap<>();
  private static final SecureRandom random = new SecureRandom();

  /*============
  | DH Setters |
  ============*/

  public static KeyPair doDHFirstPhase(DHParameterSpec dhParamSpec) {
    // Generates a KeyPair to Diffie-Hellman
    try {
      KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("DH");
      if (dhParamSpec == null) kPairGen.initialize(DH_KEY_SIZE);
      else kPairGen.initialize(dhParamSpec);
      return kPairGen.generateKeyPair();
    } catch (GeneralSecurityException e) {
      return null;
    }
  }

  public static boolean doDHSecondPhase(
      String o, DHParameterSpec dhParamSpec, KeyPair kPair, BigInteger y) {
    // Generates an AES Key to Diffie-Hellman
    addCryptoAESKeys(o);
    try {
      KeyAgreement kAgree = KeyAgreement.getInstance("DH");
      kAgree.init(kPair.getPrivate());
      KeyFactory kFactory = KeyFactory.getInstance("DH");
      DHPublicKeySpec pubKeySpec = new DHPublicKeySpec(y, dhParamSpec.getP(), dhParamSpec.getG());
      PublicKey pubKey = kFactory.generatePublic(pubKeySpec);
      kAgree.doPhase(pubKey, true);
      Key secKey = new SecretKeySpec(kAgree.generateSecret(), 0, 16, "AES");
      cryptoAESKeys.get(o).setSecKey(secKey);
      return true;
    } catch (GeneralSecurityException e) {
      return false;
    }
  }

  /*=============
  | RSA Setters |
  =============*/

  public static boolean addCrt(String o, File file) {
    addCryptoRSAKeys(o);
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] crtBytes = fis.readAllBytes();
      fis.close();
      addCrt(o, crtBytes);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static boolean addCrt(String o, byte[] crtBytes) {
    addCryptoRSAKeys(o);
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate crt =
          (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(crtBytes));
      cryptoRSAKeys.get(o).setCrtBytes(crtBytes);
      cryptoRSAKeys.get(o).setCrt(crt);
      cryptoRSAKeys.get(o).setPubKey(crt.getPublicKey());
      return true;
    } catch (CertificateException e) {
      return false;
    }
  }

  public static boolean addPrivKey(String o, File file) {
    addCryptoRSAKeys(o);
    try {
      FileInputStream fis = new FileInputStream(file);
      PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(fis.readAllBytes());
      KeyFactory kf = KeyFactory.getInstance("RSA");
      cryptoRSAKeys.get(o).setPrivKey(kf.generatePrivate(privSpec));
      fis.close();
      return true;
    } catch (GeneralSecurityException | IOException e) {
      return false;
    }
  }

  /*============
  | RSA Crypto |
  ============*/

  public static byte[] cipherBytesRSAPriv(String o, byte[] plainBytes) {
    return cryptoRSA(Cipher.ENCRYPT_MODE, cryptoRSAKeys.get(o).getPrivKey(), plainBytes);
  }

  public static byte[] decipherBytesRSAPub(String o, byte[] encryptedBytes) {
    return cryptoRSA(Cipher.DECRYPT_MODE, cryptoRSAKeys.get(o).getPubKey(), encryptedBytes);
  }

  public static byte[] cipherBytesRSAPub(String o, byte[] plainBytes) {
    return cryptoRSA(Cipher.ENCRYPT_MODE, cryptoRSAKeys.get(o).getPubKey(), plainBytes);
  }

  public static byte[] decipherBytesRSAPriv(String o, byte[] encryptedBytes) {
    return cryptoRSA(Cipher.DECRYPT_MODE, cryptoRSAKeys.get(o).getPrivKey(), encryptedBytes);
  }

  private static byte[] cryptoRSA(int mode, Key key, byte[] bytes) {
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(mode, key);
      return cipher.doFinal(bytes);
    } catch (GeneralSecurityException e) {
      throw new AssertError(Crypto.class.getTypeName(), "cryptoRSA", e);
    }
  }

  /*============
  | AES Crypto |
  ============*/

  public static byte[] cipherBytesAES(String o, IvParameterSpec iv, byte[] plainBytes) {
    return cryptoAES(Cipher.ENCRYPT_MODE, cryptoAESKeys.get(o).getSecKey(), iv, plainBytes);
  }

  public static byte[] decipherBytesAES(String o, IvParameterSpec iv, byte[] encryptedBytes) {
    return cryptoAES(Cipher.DECRYPT_MODE, cryptoAESKeys.get(o).getSecKey(), iv, encryptedBytes);
  }

  public static byte[] cryptoAES(int mode, Key key, IvParameterSpec iv, byte[] bytes) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(mode, key, iv);
      return cipher.doFinal(bytes);
    } catch (GeneralSecurityException e) {
      throw new AssertError(Crypto.class.getTypeName(), "cryptoAES", e);
    }
  }

  /*============
  | Converters |
  ============*/

  public static String convertBytesToString(byte[] bytes) {
    return new String(bytes);
  }

  public static int convertBytesToInt(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static BigInteger convertBytesToBigInt(byte[] bytes) {
    return new BigInteger(1, bytes);
  }

  public static long convertBytesToLong(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getLong();
  }

  public static byte[] convertIntToBytes(int intToBytes) {
    return ByteBuffer.allocate(Integer.BYTES).putInt(intToBytes).array();
  }

  public static byte[] convertLongToBytes(long longToBytes) {
    return ByteBuffer.allocate(Long.BYTES).putLong(longToBytes).array();
  }

  /*========
  | Hashes |
  ========*/

  public static byte[] hmac(String o, byte[] msgBytes) {
    try {
      Mac mac = Mac.getInstance("HmacSHA512");
      mac.init(cryptoAESKeys.get(o).getSecKey());
      return mac.doFinal(msgBytes);
    } catch (GeneralSecurityException e) {
      throw new AssertError(Crypto.class.getTypeName(), "hmac", e);
    }
  }

  public static byte[] hash(String stringToHash) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      return md.digest(stringToHash.getBytes());
    } catch (NoSuchAlgorithmException e) {
      throw new AssertError(Crypto.class.getTypeName(), "hash", e);
    }
  }

  /*===================
  | Random Generators |
  ===================*/

  public static byte[] generateRandomIV() {
    byte[] iv = new byte[IV_SIZE];
    random.nextBytes(iv);
    return iv;
  }

  public static BigInteger generateRandomBigInt() {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return new BigInteger(1, bytes);
  }

  /*=============
  | RSA Getters |
  =============*/

  public static byte[] getCrtBytes(String o) {
    return cryptoRSAKeys.get(o).getCrtBytes();
  }

  public static X509Certificate getCrt(String o) {
    return cryptoRSAKeys.get(o).getCrt();
  }

  public static PublicKey getPubKey(String o) {
    return cryptoRSAKeys.get(o).getPubKey();
  }

  public static PrivateKey getPrivKey(String o) {
    return cryptoRSAKeys.get(o).getPrivKey();
  }

  /*=============
  | Auxiliaries |
  =============*/

  public static boolean addCryptoAESKeys(String o) {
    synchronized (cryptoAESKeys) {
      if (!cryptoAESKeys.containsKey(o)) {
        cryptoAESKeys.put(o, new CryptoAESKeys());
        return true;
      }
    }
    return false;
  }

  public static boolean addCryptoRSAKeys(String o) {
    synchronized (cryptoRSAKeys) {
      if (!cryptoRSAKeys.containsKey(o)) {
        cryptoRSAKeys.put(o, new CryptoRSAKeys());
        return true;
      }
    }
    return false;
  }

  public static boolean removeCryptoAESKeys(String o) {
    synchronized (cryptoAESKeys) {
      if (cryptoAESKeys.containsKey(o)) {
        cryptoAESKeys.remove(o);
        return true;
      }
    }
    return false;
  }

  public static boolean removeCryptoRSAKeys(String o) {
    synchronized (cryptoRSAKeys) {
      if (cryptoRSAKeys.containsKey(o)) {
        cryptoRSAKeys.remove(o);
        return true;
      }
    }
    return false;
  }

  public static boolean hasRSACrt(String o) {
    synchronized (cryptoRSAKeys) {
      return cryptoRSAKeys.containsKey(o) && cryptoRSAKeys.get(o).getCrt() != null;
    }
  }

  public static boolean hasAESKey(String o) {
    synchronized (cryptoAESKeys) {
      return cryptoAESKeys.containsKey(o) && cryptoAESKeys.get(o).getSecKey() != null;
    }
  }

  public static boolean checkCrtValidity(String o, String ca) {
    try {
      synchronized (cryptoRSAKeys) {
        if (cryptoRSAKeys.containsKey(o) && cryptoRSAKeys.containsKey(ca)) {
          Crypto.getCrt(o).verify(Crypto.getPubKey(ca));
          Crypto.getCrt(o).checkValidity();
        } else return false;
      }
      return true;
    } catch (GeneralSecurityException e) {
      return false;
    }
  }
}
