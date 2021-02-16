package ist.sirs.mtr.db;

import java.util.Arrays;

public class DBTResult {
  // Patient Id Encrypted
  private final int pid;
  // Test Result TS
  private final long ts;
  // Test Result Content
  private final String content;
  // Partner Lab Signature
  private final byte[] signature;

  public DBTResult(int pid, long ts, String content, byte[] signature) {
    this.pid = pid;
    this.ts = ts;
    this.content = content;
    this.signature = signature;
  }

  public int getPid() {
    return pid;
  }

  public long getTs() {
    return ts;
  }

  public String getContent() {
    return content;
  }

  public byte[] getSignature() {
    return signature;
  }

  @Override
  public String toString() {
    return String.format("%d%d%s%s", pid, ts, content, Arrays.toString(signature));
  }
}
