package ist.sirs.mtr.db;

import java.time.LocalDateTime;

public class DBPatientRecord {
  // Record Id
  private final int recId;
  // Record Content
  private final String recCont;
  // Record Date
  private final LocalDateTime recTs;

  public DBPatientRecord(int recId, String recCont, LocalDateTime recTs) {
    this.recId = recId;
    this.recCont = recCont;
    this.recTs = recTs;
  }

  public int getRecId() {
    return recId;
  }

  public String getRecCont() {
    return recCont;
  }

  public LocalDateTime getRecTs() {
    return recTs;
  }
}
