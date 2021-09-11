package pt.tecnico.ulisboa.hds.hdlt.server.location;

public class Location {

  private final Integer x;
  private final Integer y;

  public Location(Integer x, Integer y) {
    this.x = x;
    this.y = y;
  }

  public Integer getX() {
    return this.x;
  }

  public Integer getY() {
    return this.y;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Location)) return false;
    Location location = (Location) o;
    return this.x.equals(location.getX()) && this.y.equals(location.getY());
  }
}
