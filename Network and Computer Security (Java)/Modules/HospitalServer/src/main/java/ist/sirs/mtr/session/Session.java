package ist.sirs.mtr.session;

import java.time.LocalDateTime;

public class Session {

  private static int renewMin;
  private final String role;
  private String uname;
  private LocalDateTime exp;

  public Session(String uname, String role) {
    this.uname = uname;
    this.role = role;
    renewMin = isActive() ? 5 : 1;
    renew();
  }

  public void renew() {
    this.exp = LocalDateTime.now().plusMinutes(renewMin);
  }

  public boolean isAuthentic() {
    return LocalDateTime.now().isBefore(exp);
  }

  public boolean isActive() {
    return uname != null;
  }

  public void activate(String uname) {
    this.uname = uname;
    renewMin = 5;
    renew();
  }

  public String getUname() {
    return uname;
  }

  public String getRole() {
    return role;
  }
}
