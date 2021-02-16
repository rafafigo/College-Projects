package ist.sirs.mtr.tres;

import ist.sirs.mtr.crypto.Crypto;

public class TRes {

  private final int pid;
  private final long ts;
  private final String content;
  private final byte[] signature;

  public TRes(int pid, String content) {
    this.pid = pid;
    this.ts = System.currentTimeMillis();
    this.content = content;
    this.signature =
        Crypto.cipherBytesRSAPriv("PL", Crypto.hash(String.format("%d%s%d", pid, content, ts)));
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
}
