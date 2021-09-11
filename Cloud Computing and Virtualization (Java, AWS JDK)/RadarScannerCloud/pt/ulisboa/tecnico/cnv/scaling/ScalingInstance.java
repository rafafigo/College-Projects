package pt.ulisboa.tecnico.cnv.scaling;

import com.amazonaws.services.ec2.model.Instance;

/** Scaling instance, which keeps track of an AWS instance, its state and current cost. */
public class ScalingInstance {

  private static final int UNHEALTHY_THRESHOLD = 2;
  private static final int HEALTHY_THRESHOLD = 4;
  private final Instance instance;
  private double currentCost;
  private int nHealthy;
  private int nUnhealthy;
  private boolean isUp;

  public ScalingInstance(Instance instance) {
    this.instance = instance;
    this.currentCost = 0.0;
    this.nHealthy = 0;
    this.nUnhealthy = 0;
    this.isUp = false;
  }

  /**
   * Adds the estimated cost of the request that started to its current cost.
   *
   * @param cost Incoming request estimated cost.
   */
  public synchronized void addCost(double cost) {
    this.currentCost += cost;
  }

  /**
   * Removes the estimated cost of the request that finalized to its current cost.
   *
   * @param cost Incoming request estimated Cost.
   */
  public synchronized void removeCost(double cost) {
    this.currentCost -= cost;
  }

  /**
   * Registers a healthy check. If the instance is already healthy it returns. Else it will set the
   * instance as initialized and it will increment the number of healths. When the number of healths
   * is equal or higher to the healthy threshold, the instance is set to healthy.
   */
  public synchronized void registerHealthyCheck() {
    if (this.isHealthy()) return;
    this.isUp = true;
    if (++this.nHealthy >= HEALTHY_THRESHOLD) this.nUnhealthy = 0;
  }

  /**
   * Registers an Unhealthy check. If the instance is not initialized yet it returns. Else it will
   * increment the number of unhealths, and set the number of healths to 0.
   */
  public synchronized void registerUnhealthyCheck() {
    if (!this.isUp) return;
    this.nUnhealthy++;
    this.nHealthy = 0;
  }

  /**
   * Verifies if the instance is healthy. The instance is healthy when it has already initialized
   * and the number of unhealths is 0.
   *
   * @return Whether the the instance is healthy or not.
   */
  public synchronized boolean isHealthy() {
    return this.isUp && this.nUnhealthy == 0;
  }

  /**
   * Verifies if the instance is unhealthy. The instance is unhealthy when the number of unhealths
   * is higher or equal to the unhealthy threshold.
   *
   * @return Whether the instance is unhealthy or not.
   */
  public synchronized boolean isUnhealthy() {
    return this.nUnhealthy >= UNHEALTHY_THRESHOLD;
  }

  /**
   * Gets the public dns name of the instance.
   *
   * @return The public dns name.
   */
  public String getPublicDnsName() {
    return this.instance.getPublicDnsName();
  }

  /**
   * Gets the instance id of the instance.
   *
   * @return The instance id.
   */
  public String getInstanceId() {
    return this.instance.getInstanceId();
  }

  /**
   * Gets the current cost of the instance.
   *
   * @return The current cost.
   */
  public double getCurrentCost() {
    return this.currentCost;
  }

  /**
   * Gets all the important information of a scaling instance.
   *
   * @return The instance state.
   */
  @Override
  public String toString() {
    return String.format(
        "InstanceState{ instance=%s, currentCost=%.1f, nUnhealthy=%d, nHealthy=%d }",
        instance.getInstanceId(), currentCost, nUnhealthy, nHealthy);
  }
}
