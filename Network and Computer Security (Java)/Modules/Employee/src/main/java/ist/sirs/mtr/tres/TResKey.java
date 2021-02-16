package ist.sirs.mtr.tres;

import java.util.Objects;

public class TResKey {

  private final int pid;
  private final int recId;

  public TResKey(int pid, int recId) {
    this.pid = pid;
    this.recId = recId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TResKey tResKey = (TResKey) o;
    return pid == tResKey.pid && recId == tResKey.recId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pid, recId);
  }
}
