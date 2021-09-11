package pt.tecnico.ulisboa.hds.hdlt.server.session;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

public class Session {

  public final int expSec;
  private final SecretKey secKey;
  private final LocalDateTime exp;

  public Session(int expSec, SecretKey secKey) {
    this.expSec = expSec;
    this.secKey = secKey;
    this.exp = LocalDateTime.now().plusSeconds(expSec);
  }

  public boolean isValid() {
    return LocalDateTime.now().isBefore(this.exp);
  }

  public SecretKey getSecKey() {
    return this.secKey;
  }
}
