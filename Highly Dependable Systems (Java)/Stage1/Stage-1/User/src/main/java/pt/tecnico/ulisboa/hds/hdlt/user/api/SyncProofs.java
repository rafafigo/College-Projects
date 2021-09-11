package pt.tecnico.ulisboa.hds.hdlt.user.api;

import java.util.HashMap;
import java.util.Map;

public class SyncProofs {

  private final Map<String, byte[]> authProofs;
  private Integer nReplies;

  public SyncProofs() {
    this.nReplies = 0;
    this.authProofs = new HashMap<>();
  }

  public synchronized void addProof(String uname, byte[] proof) {
    this.authProofs.put(uname, proof);
    this.nReplies++;
    notifyAll();
  }

  public synchronized void newReply() {
    this.nReplies++;
    notifyAll();
  }

  public synchronized void resetNrOfReplies() {
    this.nReplies = 0;
  }

  public Integer getNrOfValidReplies() {
    return this.authProofs.size();
  }

  public Integer getNrOfReplies() {
    return this.nReplies;
  }

  public Map<String, byte[]> getAuthProofs() {
    return this.authProofs;
  }
}
