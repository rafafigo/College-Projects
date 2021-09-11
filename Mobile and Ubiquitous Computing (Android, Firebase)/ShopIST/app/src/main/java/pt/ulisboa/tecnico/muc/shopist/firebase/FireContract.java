package pt.ulisboa.tecnico.muc.shopist.firebase;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

public final class FireContract {
  private FireContract() {}

  protected static final class UserContract {
    public String ShoppingId;
    public String Email;
    public Map<String, Integer> CrowdItemRatings;
  }

  protected static final class PantryContract {
    public String Name;
    public GeoPoint Location;
    public String LocationName;
    public Map<String, Map<String, Integer>> PantryItems;
    public Map<String, Map<String, Integer>> PantryCarts;
    public Timestamp Timestamp;
    public List<String> Users;
  }

  protected static final class ItemContract {
    public String Name;
    public DocumentReference CrowdItem;
    public Timestamp Timestamp;
  }

  protected static final class CrowdItemContract {
    public String Barcode;
    public List<String> Pictures;
    public Map<String, Float> Prices;
    public List<Integer> Ratings;
  }

  protected static final class ShoppingContract {
    public String Name;
    public DocumentReference CrowdShopping;
    public Timestamp Timestamp;
  }

  protected static final class CrowdShoppingContract {
    public GeoPoint Location;
    public String LocationName;
    public Long QueueTime;
    public Map<String, Map<String, Integer>> SmartSort;
  }
}
