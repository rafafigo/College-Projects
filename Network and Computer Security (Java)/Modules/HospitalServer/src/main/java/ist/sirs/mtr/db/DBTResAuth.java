package ist.sirs.mtr.db;

public class DBTResAuth {
  // Lab Signature
  private final byte[] labSig;
  // Lab Certificate
  private final byte[] labCrt;

  public DBTResAuth(byte[] labSig, byte[] labCrt) {
    this.labSig = labSig;
    this.labCrt = labCrt;
  }

  public byte[] getLabSig() {
    return labSig;
  }

  public byte[] getLabCrt() {
    return labCrt;
  }
}
