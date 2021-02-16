package ist.sirs.mtr.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class CryptoRSAKeys {

  private X509Certificate crt;
  private PublicKey pubKey;
  private PrivateKey privKey;
  private byte[] crtBytes;

  public byte[] getCrtBytes() {
    synchronized (this) {
      return crtBytes;
    }
  }

  public void setCrtBytes(byte[] crtBytes) {
    synchronized (this) {
      this.crtBytes = crtBytes;
    }
  }

  public X509Certificate getCrt() {
    synchronized (this) {
      return crt;
    }
  }

  public void setCrt(X509Certificate crt) {
    synchronized (this) {
      this.crt = crt;
    }
  }

  public PublicKey getPubKey() {
    synchronized (this) {
      return pubKey;
    }
  }

  public void setPubKey(PublicKey pubKey) {
    synchronized (this) {
      this.pubKey = pubKey;
    }
  }

  public PrivateKey getPrivKey() {
    synchronized (this) {
      return privKey;
    }
  }

  public void setPrivKey(PrivateKey privKey) {
    synchronized (this) {
      this.privKey = privKey;
    }
  }
}
