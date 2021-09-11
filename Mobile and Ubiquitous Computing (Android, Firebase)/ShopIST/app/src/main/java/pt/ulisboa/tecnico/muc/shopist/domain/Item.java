package pt.ulisboa.tecnico.muc.shopist.domain;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Item extends DomainComparator {

  private final Map<String, PantryItem> pantryItems;
  private final Map<String, ShoppingItem> shoppingItems;
  private final List<Picture> pictures;
  private final List<Integer> ratings;
  private String crowdItemId;
  private String name;
  private String barcode;
  private int totalInNeed;
  private int totalInCart;
  private boolean hasPrompted = false;

  public Item() {
    this(null, null, null, null, null);
  }

  public Item(String id, Timestamp timestamp) {
    this(id, null, null, null, timestamp);
  }

  public Item(String id, String crowdItemId, String name, String barcode, Timestamp timestamp) {
    super(id, timestamp);
    this.crowdItemId = crowdItemId;
    this.name = name;
    this.barcode = barcode;
    this.pantryItems = new HashMap<>();
    this.shoppingItems = new HashMap<>();
    this.pictures = new ArrayList<>();
    this.ratings = new ArrayList<>();
    this.totalInNeed = 0;
    this.totalInCart = 0;
  }

  public String getCrowdItemId() {
    return this.crowdItemId;
  }

  public void setCrowdItemId(String crowdItemId) {
    this.crowdItemId = crowdItemId;
  }

  public boolean hasPrompted() {
    boolean hasPrompted = this.hasPrompted;
    this.hasPrompted = true;
    return hasPrompted;
  }

  /* Name */

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /* Barcode */

  public String getBarcode() {
    return this.barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public boolean hasBarcode() {
    return this.barcode != null;
  }

  /* Total in Need */

  public int getTotalInNeed() {
    return this.totalInNeed;
  }

  /* Total in Cart */

  public int getTotalInCart() {
    return this.totalInCart;
  }

  public boolean hasInCart() {
    return this.totalInCart > 0;
  }

  /* Pantry Items */

  public Map<String, PantryItem> getPantryItems() {
    return this.pantryItems;
  }

  public List<PantryItem> getInNeedPantryItems() {
    List<PantryItem> inNeedPantryItems = new ArrayList<>();
    for (PantryItem pantryItem : this.pantryItems.values()) {
      if (pantryItem.getInNeed() > 0) {
        inNeedPantryItems.add(pantryItem);
      }
    }
    return inNeedPantryItems;
  }

  public List<PantryItem> getInCartPantryItems() {
    List<PantryItem> inCartPantryItems = new ArrayList<>();
    for (PantryItem pantryItem : this.pantryItems.values()) {
      if (pantryItem.getInCart() > 0) {
        inCartPantryItems.add(pantryItem);
      }
    }
    return inCartPantryItems;
  }

  public PantryItem getPantryItem(String pantryId) {
    return this.pantryItems.get(pantryId);
  }

  public void putAllPantryItems(Collection<PantryItem> pantryItems) {
    for (PantryItem pantryItem : pantryItems) {
      this.putPantryItem(pantryItem);
    }
  }

  public void deductPantryItem(PantryItem pantryItem) {
    this.totalInNeed -= pantryItem.getInNeed();
    this.totalInCart -= pantryItem.getInCart();
  }

  public void incrementPantryItem(PantryItem pantryItem) {
    this.totalInNeed += pantryItem.getInNeed();
    this.totalInCart += pantryItem.getInCart();
  }

  public void putPantryItem(PantryItem pantryItem) {
    String pantryId = pantryItem.getId();

    if (this.pantryItems.containsKey(pantryId)) {
      this.deductPantryItem(Objects.requireNonNull(this.pantryItems.get(pantryId)));
    }
    this.pantryItems.put(pantryItem.getId(), pantryItem);
    if (pantryItem.getInPantry() != null && pantryItem.getInNeed() != null) {
      this.incrementPantryItem(pantryItem);
    }
  }

  public void removePantryItem(String pantryId) {
    PantryItem pantryItem = this.pantryItems.remove(pantryId);
    if (pantryItem != null) this.deductPantryItem(pantryItem);
  }

  public boolean isItemInNeed() {
    for (PantryItem pantryItem : this.pantryItems.values()) if (pantryItem.hasInNeed()) return true;
    return false;
  }

  /* Shopping Items */

  public Map<String, ShoppingItem> getShoppingItems() {
    return this.shoppingItems;
  }

  public ShoppingItem getShoppingItem(String crowdShoppingId) {
    return this.shoppingItems.get(crowdShoppingId);
  }

  public Float getPrice(String crowdShoppingId) {
    return Objects.requireNonNull(this.shoppingItems.get(crowdShoppingId)).getPrice();
  }

  public void putShoppingItem(ShoppingItem shoppingItem) {
    this.shoppingItems.put(shoppingItem.getId(), shoppingItem);
  }

  public void putAllPrices(Map<String, Float> prices) {
    for (Map.Entry<String, Float> price : prices.entrySet()) {
      this.shoppingItems.put(price.getKey(), new ShoppingItem(price.getKey(), price.getValue()));
    }
  }

  /* Cart */

  public void setCart(Map<String, Integer> inCartPantries) {
    for (Map.Entry<String, Integer> inCartPantry : inCartPantries.entrySet()) {
      PantryItem pantryItem = Objects.requireNonNull(this.pantryItems.get(inCartPantry.getKey()));
      this.totalInNeed += (pantryItem.getInCart() - inCartPantry.getValue());
      this.totalInCart += (inCartPantry.getValue() - pantryItem.getInCart());
      pantryItem.setInCartInNeed(inCartPantry.getValue());
    }
  }

  /* Pictures */

  public List<Picture> getPictures() {
    return this.pictures;
  }

  public Picture getPicture() {
    return this.pictures.isEmpty() ? null : this.pictures.get(0);
  }

  public void putAllPictureUris(List<String> pictureUris) {
    this.pictures.clear();
    this.addAllPictureUris(pictureUris);
  }

  public void addAllPictureUris(List<String> pictureUris) {
    for (String pictureUri : pictureUris) {
      this.pictures.add(new Picture(pictureUri));
    }
  }

  public float getTotalPrice(String crowdShoppingId) {
    return this.totalInCart * this.getPrice(crowdShoppingId);
  }

  /* Ratings */

  public List<Integer> getRatings() {
    return ratings;
  }

  public void putAllRatings(List<Integer> ratings) {
    if (ratings.size() != 5) {
      System.out.println("ERROR - INVALID RATINGS SIZE!");
    }

    this.ratings.clear();
    this.ratings.addAll(ratings);
  }
}
