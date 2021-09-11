package pt.ulisboa.tecnico.muc.shopist.domain;

import com.google.firebase.Timestamp;

public class DomainComparator implements Comparable<DomainComparator> {

  private String id;
  private Timestamp timestamp;

  protected DomainComparator(String id, Timestamp timestamp) {
    this.id = id;
    this.timestamp = timestamp;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Timestamp getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public int compareTo(DomainComparator o) {
    return o.getTimestamp().compareTo(this.getTimestamp());
  }
}
