package pt.tecnico.ulisboa.hds.hdlt.user.session;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

public class Session {

  public final int expSec;
  private LocalDateTime exp = null;
  private SecretKey secKey = null;

  public Session(int expSec) {
    this.expSec = expSec;
  }

  public void newSession(SecretKey secKey) {
    this.secKey = secKey;
    this.exp = LocalDateTime.now().plusSeconds(expSec);
  }

  public void invalidate() {
    this.exp = null;
  }

  public boolean isValid() {
    return this.exp != null && LocalDateTime.now().isBefore(this.exp);
  }

  public SecretKey getSecKey() {
    return this.secKey;
  }
}
