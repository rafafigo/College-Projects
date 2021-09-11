package pt.ulisboa.tecnico.muc.shopist.domain;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Pantry extends Area {

  private List<String> uids;

  public Pantry(String id, String name, Timestamp timestamp) {
    super(id, name, timestamp);
    this.uids = new ArrayList<>();
  }

  public List<String> getUids() {
    return this.uids;
  }

  public void setUids(List<String> uids) {
    this.uids = uids;
  }
}
