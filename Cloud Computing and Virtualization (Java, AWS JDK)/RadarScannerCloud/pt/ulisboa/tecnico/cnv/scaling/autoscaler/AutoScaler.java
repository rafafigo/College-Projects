package pt.ulisboa.tecnico.cnv.scaling.autoscaler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.scaling.ScalingInstance;
import pt.ulisboa.tecnico.cnv.util.LoggerFormatter;

import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** AutoScaler, which manages the creation and destruction of instances* */
public class AutoScaler implements Runnable {

  private static final Logger logger = Logger.getLogger(AutoScaler.class.getName());
  private static final int COST_THRESHOLD_MIN = 1100000;
  private static final int COST_THRESHOLD_MAX = 3000000;
  private static final int CPU_THRESHOLD_MIN = 30;
  private static final int CPU_THRESHOLD_MAX = 80;
  private static final int MIN_INSTANCES = 2;
  private final String ami;
  private final String keyName;
  private final String securityGroup;
  private final Map<String, ScalingInstance> instances;
  private AmazonEC2 ec2;
  private AmazonCloudWatch cloudWatch;

  public AutoScaler(
      Map<String, ScalingInstance> instances,
      String ami,
      String keyName,
      String securityGroup,
      Level level) {
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    ConsoleHandler loggerHandler = new ConsoleHandler();
    loggerHandler.setFormatter(new LoggerFormatter());
    logger.addHandler(loggerHandler);
    this.ami = ami;
    this.keyName = keyName;
    this.securityGroup = securityGroup;
    this.instances = instances;
    this.newAWS();
  }

  /** Initializes the aws ec2 and cloudwatch with our account aws credentials. */
  private void newAWS() {
    try {
      AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
      this.ec2 =
          AmazonEC2ClientBuilder.standard()
              .withRegion(Regions.US_EAST_1)
              .withCredentials(new AWSStaticCredentialsProvider(credentials))
              .build();
      this.cloudWatch =
          AmazonCloudWatchClientBuilder.standard()
              .withRegion(Regions.US_EAST_1)
              .withCredentials(new AWSStaticCredentialsProvider(credentials))
              .build();
    } catch (Exception e) {
      throw new AmazonClientException("Bad Credentials", e);
    }
  }

  /** Calls the function to create a new timer. */
  @Override
  public void run() {
    this.autoScalerBoot();
    this.newTimer();
  }

  /** Creates the minimum number of Instances for the System to start operating. */
  private void autoScalerBoot() {
    List<Thread> newInstanceThreads = new ArrayList<>();
    for (int i = 0; i < MIN_INSTANCES; i++) {
      Thread newInstanceThread =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  newInstance();
                }
              });
      newInstanceThread.start();
      newInstanceThreads.add(newInstanceThread);
    }
    for (Thread newInstanceThread : newInstanceThreads) {
      try {
        newInstanceThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Creates a timer that runs the autoscaling. Every 30 seconds the timer is called, the auto
   * scaler will check the cpu utilization in aws. Every time the auto scaler takes an action it
   * will have a timeout before another action can be made.
   */
  private void newTimer() {
    new Timer()
        .schedule(
            new TimerTask() {
              private boolean doScale = true;
              private int i = 0;

              @Override
              public synchronized void run() {
                checkUnhealthy();
                if (!doScale) return;
                if (i >= 3) i = 0;
                boolean toCheckCPU = i++ == 0;
                boolean tookAction = autoScale(toCheckCPU);
                if (tookAction) {
                  doScale = false;
                  new Timer()
                      .schedule(
                          new TimerTask() {
                            @Override
                            public void run() {
                              doScale = true;
                              i = 0;
                            }
                          },
                          30000);
                }
              }
            },
            0,
            10000);
  }

  /** Detects instances marked as Unhealthy by the LoadBalancer and replaces them. */
  private void checkUnhealthy() {

    for (ScalingInstance instance : this.instances.values()) {
      if (instance.isUnhealthy()) {
        logger.warning(
            String.format("Instance %s Unhealthy: Replacing it!", instance.getInstanceId()));
        this.instances.remove(instance.getInstanceId());
        this.removeInstance(instance.getInstanceId());
        this.newInstance();
      }
    }
  }

  /**
   * AutoScale, calculates the sum of all instances costs and the instance with minimum cost. When
   * it is needed to check cpu, calculates the sum of instances cpu utilization and the instance
   * with minimum cpu. Can take different actions: Create an instance. Remove the instance with
   * minimum cost or cpu.
   *
   * @param toCheckCPU allows knowing if cloudwatch needs to be used.
   * @return if it took an action
   */
  private boolean autoScale(boolean toCheckCPU) {

    double sumCosts = 0.0;
    Double minCost = null;
    String minCostInstanceId = null;

    for (ScalingInstance instance : this.instances.values()) {
      logger.info(
          String.format(
              "Instance %s: Current Cost of %.1f",
              instance.getInstanceId(), instance.getCurrentCost()));
      sumCosts += instance.getCurrentCost();
      if (minCost == null || minCost > instance.getCurrentCost()) {
        minCostInstanceId = instance.getInstanceId();
        minCost = instance.getCurrentCost();
      }
    }

    if (this.takeAction(sumCosts, COST_THRESHOLD_MAX, COST_THRESHOLD_MIN, minCostInstanceId))
      return true;

    if (toCheckCPU) {
      double sumCPUs = 0.0;
      Double minCPU = null;
      String minCPUInstanceId = null;

      for (ScalingInstance instance : this.instances.values()) {
        Dimension dimension = new Dimension();
        dimension.setName("InstanceId");
        dimension.setValue(instance.getInstanceId());
        GetMetricStatisticsRequest request =
            new GetMetricStatisticsRequest()
                .withStartTime(new Date(new Date().getTime() - 600000))
                .withEndTime(new Date())
                .withPeriod(60)
                .withNamespace("AWS/EC2")
                .withMetricName("CPUUtilization")
                .withStatistics(Statistic.Average)
                .withDimensions(dimension);
        GetMetricStatisticsResult result;
        try {
          result = this.cloudWatch.getMetricStatistics(request);
        } catch (Exception e) {
          logger.warning(
              String.format(
                  "Unable to GetMetricsStatistics of Instance %s", instance.getInstanceId()));
          continue;
        }
        Datapoint latestDp = null;
        for (Datapoint dp : result.getDatapoints()) {
          if (latestDp == null || latestDp.getTimestamp().before(dp.getTimestamp())) {
            latestDp = dp;
          }
        }
        if (latestDp == null) {
          logger.warning("Insufficient CPU Utilization Metrics to Perform Action!");
          return false;
        }
        logger.info(
            String.format(
                "Instance %s: CPU Utilization of %.1f",
                instance.getInstanceId(), latestDp.getAverage()));

        sumCPUs += latestDp.getAverage();
        if (minCPU == null || minCPU > latestDp.getAverage()) {
          minCPUInstanceId = instance.getInstanceId();
          minCPU = latestDp.getAverage();
        }
      }
      return this.takeAction(sumCPUs, CPU_THRESHOLD_MAX, CPU_THRESHOLD_MIN, minCPUInstanceId);
    }
    return false;
  }

  /**
   * Creates an instance if valueSum is higher then valueThresholdMax * number of instances. Removes
   * the instance with minValueInstanceId if valueSum is lower then valueThresholdMin * number of
   * instances, and exists at least on instance.
   *
   * @param valueSum sum of cost or cpu of all instances.
   * @param valueThresholdMax maximum threshold of cost or cpu.
   * @param valueThresholdMin minimum threshold of cost or cpu.
   * @param minValueInstanceId instance with the minimum cost or cpu.
   * @return if it took an action
   */
  private boolean takeAction(
      double valueSum, int valueThresholdMax, int valueThresholdMin, String minValueInstanceId) {

    if (valueSum > valueThresholdMax * this.instances.size()) {
      this.newInstance();
      return true;
    }
    if (valueSum < valueThresholdMin * this.instances.size()
        && this.instances.size() > MIN_INSTANCES) {
      this.instances.remove(minValueInstanceId);
      this.removeInstance(minValueInstanceId);
      return true;
    }
    return false;
  }

  /** Creates a new instance using the image id, the key pair and the security group. */
  private void newInstance() {
    RunInstancesRequest runInstancesRequest =
        new RunInstancesRequest()
            .withImageId(this.ami)
            .withInstanceType(InstanceType.T2Micro)
            .withMinCount(1)
            .withMaxCount(1)
            .withKeyName(this.keyName)
            .withSecurityGroups(this.securityGroup)
            .withMonitoring(true);

    String instanceId =
        ec2.runInstances(runInstancesRequest)
            .getReservation()
            .getInstances()
            .get(0)
            .getInstanceId();

    logger.warning(String.format("Creating new Instance %s!", instanceId));
    this.addRunningInstance(new DescribeInstanceStatusRequest().withInstanceIds(instanceId));
  }

  /**
   * It waits until the instance is running. If the instance is not created, we try to create again.
   *
   * @param request which is a status request associated with the instance created to use to request
   *     its instance status.
   */
  private void addRunningInstance(DescribeInstanceStatusRequest request) {
    while (true) {
      List<InstanceStatus> instanceStatuses =
          ec2.describeInstanceStatus(request).getInstanceStatuses();
      if (!instanceStatuses.isEmpty()) {
        switch (instanceStatuses.get(0).getInstanceState().getName()) {
          case "running":
            String instanceId = request.getInstanceIds().get(0);
            logger.info(String.format("Detected Instance %s is Running!", instanceId));
            this.instances.put(instanceId, new ScalingInstance(this.fetchInstance(instanceId)));
            return;
          case "pending":
            break;
          default:
            this.newInstance();
        }
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  /**
   * Gets the instance based on its instanceId.
   *
   * @param instanceId of the instance we are trying to fetch.
   * @return The instance.
   */
  private Instance fetchInstance(String instanceId) {
    for (Reservation reservation : ec2.describeInstances().getReservations()) {
      for (Instance instance : reservation.getInstances()) {
        if (instance.getInstanceId().equals(instanceId)) return instance;
      }
    }
    return null;
  }

  /**
   * Removes an instance based on its instanceId.
   *
   * @param instanceId of the instance we are trying to remove.
   */
  private void removeInstance(String instanceId) {
    logger.warning(String.format("Removing Instance %s!", instanceId));
    this.ec2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(instanceId));
  }
}
