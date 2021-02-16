package ist.sirs.mtr.crypto;

import java.security.Key;

public class CryptoAESKeys {

  private Key secKey;

  public Key getSecKey() {
    synchronized (this) {
      return secKey;
    }
  }

  public void setSecKey(Key secKey) {
    synchronized (this) {
      this.secKey = secKey;
    }
  }
}
