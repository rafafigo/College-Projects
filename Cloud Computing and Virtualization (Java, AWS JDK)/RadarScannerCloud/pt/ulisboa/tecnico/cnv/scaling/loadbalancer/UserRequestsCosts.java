package pt.ulisboa.tecnico.cnv.scaling.loadbalancer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User request Costs, which has the most recently made requests and its costs fetched from the MSS.
 */
public class UserRequestsCosts {

  private static final int DELTA = 20;
  private final Map<UserRequest, Double> cache;

  public UserRequestsCosts() {
    this.cache = new LinkedHashMap<>(LoadBalancer.CAPACITY);
  }

  /**
   * Gets the cost associated with a UserRequest if exists, and puts the request at the start of the
   * cache.
   *
   * @return The cost associated with the UserRequest, or null if it does not exist.
   */
  public synchronized Double get(UserRequest uRequest) {
    Double cost = cache.get(uRequest);

    if (cost != null) {
      cache.remove(uRequest);
      cache.put(uRequest, cost);
    }
    return cost;
  }

  /**
   * Puts a UserRequest and its cost in the cache if it was not present. If the cache has reached
   * its limit it removes the oldest member.
   */
  public synchronized void put(UserRequest uRequest, double cost) {
    if (this.get(uRequest) != null) return;

    if (cache.size() == LoadBalancer.CAPACITY) {
      cache.remove(cache.keySet().iterator().next());
    }
    cache.put(uRequest, cost);
  }

  /** Puts all the UserRequests and its costs to the end of the cache. */
  public void putAll(Map<UserRequest, Double> uRequestsCosts) {
    for (Map.Entry<UserRequest, Double> uRequestsCost : uRequestsCosts.entrySet()) {
      this.cache.remove(uRequestsCost.getKey());
      this.put(uRequestsCost.getKey(), uRequestsCost.getValue());
    }
  }

  /**
   * Gets the estimated cost of an UserRequest. If the request exists in the cache then it will
   * return its cost. Else it will find the most similar UserRequest in the cache.
   *
   * @return The estimated cost.
   */
  public synchronized double getEstimatedCost(UserRequest uRequest) {
    Double cost = this.get(uRequest);
    if (cost != null) return cost;

    UserRequest closestUserRequest = null;
    double closestCost = 0;
    double proximityCost;

    for (UserRequest uR : this.cache.keySet()) {
      proximityCost = uRequest.getStrategy().equals(uR.getStrategy()) ? 0.4 : 0;
      proximityCost += uRequest.getImageName().equals(uR.getImageName()) ? 0.2 : 0;
      proximityCost += uRequest.getViewPort().equals(uR.getViewPort()) ? 0.1 : 0;
      double absArea = Math.abs(uRequest.getArea() - uR.getArea());
      proximityCost += absArea < DELTA ? (DELTA - absArea) / DELTA * 0.2 : 0;
      double absStartingPoint = uRequest.getStartingPoint().distance(uR.getStartingPoint());
      proximityCost += absStartingPoint < DELTA ? (DELTA - absStartingPoint) / DELTA * 0.1 : 0;
      if (closestCost < proximityCost) {
        closestCost = proximityCost;
        closestUserRequest = uR;
      }
    }
    cost = this.cache.get(closestUserRequest);
    return cost != null ? cost : 0;
  }
}
