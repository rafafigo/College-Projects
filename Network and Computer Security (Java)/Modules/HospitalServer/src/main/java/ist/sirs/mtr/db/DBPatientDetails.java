package ist.sirs.mtr.db;

public class DBPatientDetails {
  // Patient Id
  private final int pid;
  // Patient NIF
  private final int nif;
  // Patient Name
  private final String name;

  public DBPatientDetails(int pid, int nif, String name) {
    this.pid = pid;
    this.nif = nif;
    this.name = name;
  }

  public int getPid() {
    return pid;
  }

  public int getNif() {
    return nif;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("%d%d%s", pid, nif, name);
  }
}
