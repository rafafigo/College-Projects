package ist.sirs.mtr.tres;

public class TRes {

  private final long ts;
  private final String content;

  public TRes(String content, long ts) {
    this.ts = ts;
    this.content = content;
  }

  public long getTs() {
    return ts;
  }

  public String getContent() {
    return content;
  }
}
