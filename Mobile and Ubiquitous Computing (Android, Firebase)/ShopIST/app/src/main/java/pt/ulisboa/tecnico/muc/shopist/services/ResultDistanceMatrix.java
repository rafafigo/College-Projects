package pt.ulisboa.tecnico.muc.shopist.services;

import java.util.List;

public class ResultDistanceMatrix {

  public String status;
  public List<InfoDistanceMatrix> rows;

  public static class InfoDistanceMatrix {

    public List<DistanceElement> elements;

    public static class DistanceElement {

      public String status;
      public ValueItem duration;

      public static class ValueItem {

        public Long value;
      }
    }
  }
}
