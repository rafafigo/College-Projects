package pt.ulisboa.tecnico.cnv.scaling.loadbalancer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** User request, which keeps all the information associated with a request. */
public class UserRequest {

  private final Strategy strategy;
  private final String imageName;
  private final Point startingPoint;
  private final Rectangle viewPort;

  public UserRequest(
      Strategy strategy, String imageName, int xS, int yS, int x0, int x1, int y0, int y1) {
    this.strategy = strategy;
    this.imageName = imageName;
    this.startingPoint = new Point(xS, yS);
    this.viewPort = new Rectangle(x0, y0, x1 - x0, y1 - y0);
  }

  /**
   * Parses a string of a strategy to a Strategy.
   *
   * @return The strategy associated with the request.
   */
  public static Strategy parseStrategy(String strategy) {
    switch (strategy) {
      case "GRID_SCAN":
        return Strategy.GRID;
      case "PROGRESSIVE_SCAN":
        return Strategy.PROGRESSIVE;
      case "GREEDY_RANGE_SCAN":
        return Strategy.GREEDY;
      default:
        return null;
    }
  }

  /**
   * Parses an http query received from a client to a User request.
   *
   * @return The UserRequest associated with the query.
   */
  public static UserRequest parseFromQuery(String query) {

    Map<String, String> keyValues = new HashMap<>();
    for (final String p : query.split("&")) {
      final String[] pSplit = p.split("=");
      keyValues.put(pSplit[0], pSplit[1]);
    }

    if (!isValidScanRequest(keyValues)) return null;
    Strategy s = UserRequest.parseStrategy(keyValues.get("s"));
    if (s == null) return null;

    try {
      return new UserRequest(
          s,
          keyValues.get("i"),
          Integer.parseInt(keyValues.get("xS")),
          Integer.parseInt(keyValues.get("yS")),
          Integer.parseInt(keyValues.get("x0")),
          Integer.parseInt(keyValues.get("x1")),
          Integer.parseInt(keyValues.get("y0")),
          Integer.parseInt(keyValues.get("y1")));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Verifies if the query of the request has all needed keys of a scan request.
   *
   * @return If it is a scan request.
   */
  private static boolean isValidScanRequest(Map<String, String> keyValue) {
    return keyValue.containsKey("w")
        && keyValue.containsKey("h")
        && keyValue.containsKey("x0")
        && keyValue.containsKey("x1")
        && keyValue.containsKey("y0")
        && keyValue.containsKey("y1")
        && keyValue.containsKey("xS")
        && keyValue.containsKey("yS")
        && keyValue.containsKey("s")
        && keyValue.containsKey("i");
  }
  /**
   * Gets the image name associated with the request.
   *
   * @return If it is a scan request.
   */
  public String getImageName() {
    return this.imageName;
  }

  /**
   * Gets the starting point.
   *
   * @return The starting point.
   */
  public Point getStartingPoint() {
    return this.startingPoint;
  }

  /**
   * Gets the viewport.
   *
   * @return The view port.
   */
  public Rectangle getViewPort() {
    return this.viewPort;
  }

  public Strategy getStrategy() {
    return this.strategy;
  }

  public Double getArea() {
    return this.getViewPort().getWidth() * this.viewPort.getHeight();
  }

  /**
   * Verifies if the object is an UserRequest and if all fields are equal.
   *
   * @return If they are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserRequest other = (UserRequest) o;
    return this.strategy.equals(other.strategy)
        && this.imageName.equals(other.imageName)
        && this.startingPoint.equals(other.startingPoint)
        && this.viewPort.equals(other.viewPort);
  }

  /**
   * Creates an hash of all fields.
   *
   * @return The hash of all fields.
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.strategy, this.imageName, this.startingPoint, this.viewPort);
  }

  /**
   * Gets all the important information of a UserRequest.
   *
   * @return The UserRequest as string.
   */
  @Override
  public String toString() {
    return String.format(
        "UserRequest{ Strategy=%s, ImageName=%s, StartingPoint=%s, ViewPort=%s }",
        strategy, imageName, startingPoint, viewPort);
  }

  public enum Strategy {
    GRID,
    PROGRESSIVE,
    GREEDY
  }
}
