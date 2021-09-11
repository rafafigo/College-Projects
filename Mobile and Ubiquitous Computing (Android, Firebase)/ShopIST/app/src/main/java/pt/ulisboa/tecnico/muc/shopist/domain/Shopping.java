package pt.ulisboa.tecnico.muc.shopist.domain;

import com.google.firebase.Timestamp;

import java.util.Comparator;
import java.util.Map;

public class Shopping extends Area implements Comparator<Item> {

  private String crowdShoppingId;
  private Long queueTime;
  private Map<String, Map<String, Integer>> smartSort;

  public Shopping(String id, String name, Timestamp timestamp) {
    super(id, name, timestamp);
  }

  public String getQueueTime() {
    return Area.formatTime(this.queueTime);
  }

  public void setQueueTime(Long queueTime) {
    this.queueTime = queueTime;
  }

  public void setSmartSort(Map<String, Map<String, Integer>> smartSort) {
    this.smartSort = smartSort;
  }

  public String getCrowdShoppingId() {
    return this.crowdShoppingId;
  }

  public void setCrowdShoppingId(String crowdShoppingId) {
    this.crowdShoppingId = crowdShoppingId;
  }

  public int compare(Item a, Item b) {
    if (this.smartSort == null) return a.compareTo(b);
    Map<String, Integer> rowA = this.smartSort.get(a.getCrowdItemId());
    Map<String, Integer> rowB = this.smartSort.get(b.getCrowdItemId());
    if (rowA == null && rowB == null) return a.compareTo(b);
    else if (rowA == null) return 1;
    else if (rowB == null) return -1;
    Integer bA = rowA.get(b.getCrowdItemId());
    Integer bB = rowB.get(a.getCrowdItemId());
    if (bA == null && bB == null) return a.compareTo(b);
    else if (bA == null) return 1;
    else if (bB == null) return -1;
    return bA.equals(bB) ? a.compareTo(b) : bB.compareTo(bA);
  }
}
