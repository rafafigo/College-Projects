package pt.ulisboa.tecnico.muc.shopist.ui.common;

import android.app.Application;
import android.location.Location;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;

import com.bumptech.glide.Priority;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.MainActivity;
import pt.ulisboa.tecnico.muc.shopist.domain.Area;
import pt.ulisboa.tecnico.muc.shopist.domain.Cart;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireAuth.AuthResponse;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireManager;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;
import timber.log.Timber;

public class CommonViewModel extends AndroidViewModel {

  private static final int MIN_LOCATION_DISTANCE = 500;
  private static CommonViewModel instance;
  private final Map<String, Pantry> pantries;
  private final Map<String, Shopping> shoppings;
  private final Map<String, Item> items;
  private final Cart cart;
  private FireManager fireManager;
  private WeakReference<MainActivity> mainActivity;
  private Area startAppArea;

  public CommonViewModel(Application app) {
    super(app);
    this.pantries = new HashMap<>();
    this.shoppings = new HashMap<>();
    this.items = new HashMap<>();
    this.cart = new Cart();
    this.startAppArea = null;
    instance = this;
  }

  public static CommonViewModel getInstance() {
    return instance;
  }

  public void newDB() {
    this.fireManager = new FireManager(this);
  }

  public void preloadPictures() {
    for (Item item : this.items.values()) {
      Picture picture = item.getPicture();
      if (picture != null) {
        GlideApp.with(getApplication().getApplicationContext())
            .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
            .priority(Priority.LOW)
            .preload();
      }
    }
  }

  public void setMainActivity(MainActivity mainActivity) {
    this.mainActivity = new WeakReference<>(mainActivity);
  }

  /* FireFetch Methods */

  public void putItem(Item item) {
    this.items.put(item.getId(), item);
  }

  public void deleteItem(String itemId) {
    this.cart.removeFromCart(Objects.requireNonNull(this.items.get(itemId)).getTotalInCart());
    this.items.remove(itemId);
  }

  public void putPantry(Pantry pantry, boolean isOldLocation) {
    Pantry oldPantry = this.pantries.get(pantry.getId());
    this.pantries.put(pantry.getId(), pantry);
    if (!isOldLocation) this.onLocationUpdate();
  }

  public void putShopping(Shopping shopping, boolean isOldLocation) {
    Shopping oldShopping = this.shoppings.get(shopping.getId());
    this.shoppings.put(shopping.getId(), shopping);
    if (!isOldLocation) this.onLocationUpdate();
  }

  private void onLocationUpdate() {
    MainActivity mainActivity = this.mainActivity.get();
    if (mainActivity != null) {
      mainActivity.onLocationUpdate();
    }
  }

  public PantryItem putPantryItem(
      String itemId, String pantryId, int inPantry, int inNeed, int inCart, Timestamp timestamp) {
    PantryItem pantryItem;
    Item item = this.items.get(itemId);
    if (item != null && item.getPantryItems().containsKey(pantryId)) {
      pantryItem = Objects.requireNonNull(item.getPantryItems().get(pantryId));
      item.deductPantryItem(pantryItem);
      pantryItem.setInPantry(inPantry);
      pantryItem.setInNeed(inNeed);
      pantryItem.setInCart(inCart);
      pantryItem.setTimestamp(timestamp);
      item.incrementPantryItem(pantryItem);
    } else {
      pantryItem = new PantryItem(pantryId, inPantry, inNeed, inCart, timestamp);
      if (item != null) item.putPantryItem(pantryItem);
    }
    return pantryItem;
  }

  public void deletePantry(String pantryId) {
    this.pantries.remove(pantryId);
    Iterator<Map.Entry<String, Item>> items = this.items.entrySet().iterator();
    while (items.hasNext()) {
      Item item = items.next().getValue();
      if (!item.getPantryItems().containsKey(pantryId)) continue;
      this.cart.removeFromCart(item.getPantryItem(pantryId).getInCart());
      if (item.getPantryItems().size() > 1) {
        item.removePantryItem(pantryId);
      } else items.remove();
    }
  }

  public void putPantryItemCart(String pantryId, String itemId, int inCart) {
    Item item = this.items.get(itemId);
    if (item != null) {
      PantryItem pantryItem =
          Objects.requireNonNull(this.items.get(itemId)).getPantryItem(pantryId);
      inCart -= pantryItem.getInCart();
    }
    this.cart.addToCart(inCart);
  }

  public void deleteShopping(String shoppingId) {
    this.shoppings.remove(shoppingId);
  }

  public Map<String, Pantry> getPantryMap() {
    return this.pantries;
  }

  public List<Pantry> getPantryList() {
    return new ArrayList<>(this.pantries.values());
  }

  public Cart getCart() {
    return this.cart;
  }

  /* Pantries */

  public Pantry getPantry(String pantryId) {
    return this.pantries.get(pantryId);
  }

  public String addPantry(String name, String locationName, double latitude, double longitude) {
    try {
      return this.fireManager.newPantry(name, locationName, latitude, longitude);
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  public int updatePantry(
      String pantryId, String name, String locationName, double latitude, double longitude) {
    try {
      this.fireManager.updatePantry(pantryId, name, locationName, latitude, longitude);
      return 1;
    } catch (ExecutionException | InterruptedException e) {
      return 0;
    }
  }

  public int removePantry(String pantryId) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    this.fireManager.deletePantry(pantryId, writeBatch);
    for (Item item : this.items.values()) {
      if (!item.getPantryItems().containsKey(pantryId)) continue;
      boolean toDeleteItem = item.getPantryItems().size() == 1;
      if (toDeleteItem) this.fireManager.deleteItem(writeBatch, item.getId());
    }
    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  public Map<String, Shopping> getShoppingMap() {
    return this.shoppings;
  }

  public List<Shopping> getShoppingList() {
    return new ArrayList<>(this.shoppings.values());
  }

  /* Shoppings */

  public Shopping getShopping(String shoppingId) {
    return this.shoppings.get(shoppingId);
  }

  public String addShopping(String name, String locationName, double latitude, double longitude) {
    try {
      return this.fireManager.newShopping(name, locationName, latitude, longitude, 0L);
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  public int updateShopping(
      String shoppingId, String name, String locationName, double latitude, double longitude) {
    try {
      return this.fireManager.updateShopping(shoppingId, name, locationName, latitude, longitude);
    } catch (ExecutionException | InterruptedException e) {
      return 0;
    }
  }

  public int removeShopping(String shoppingId) {
    try {
      this.fireManager.deleteShopping(shoppingId);
    } catch (ExecutionException | InterruptedException e) {
      return 0;
    }
    return 1;
  }

  /* Items */

  public Map<String, Item> getItemMap() {
    return this.items;
  }

  public Item getItem(String itemId) {
    return this.items.get(itemId);
  }

  public List<Item> getPantryItems(String pantryId) {
    return this.getPantryItems(pantryId, true);
  }

  public List<Item> getNonPantryItems(String pantryId) {
    return this.getPantryItems(pantryId, false);
  }

  private List<Item> getPantryItems(String pantryId, boolean inPantry) {
    List<Item> items = new ArrayList<>();
    for (Item item : this.items.values()) {
      if (item.getPantryItems().containsKey(pantryId) == inPantry) {
        items.add(item);
      }
    }
    return items;
  }

  public List<Item> getShoppingItems(String crowdShoppingId) {
    List<Item> items = new ArrayList<>();
    for (Item item : this.items.values()) {
      if (item.getShoppingItems().containsKey(crowdShoppingId) && item.isItemInNeed()) {
        items.add(item);
      }
    }
    return items;
  }

  public List<Item> getCartItems() {
    List<Item> cartItems = new ArrayList<>();
    for (Item item : this.items.values()) {
      if (item.hasInCart()) cartItems.add(item);
    }
    return cartItems;
  }

  public int getTotalInCart() {
    return this.cart.getTotalInCart();
  }

  public float getTotalItemPrice(String itemId) {
    return this.getItem(itemId)
        .getPrice(
            Objects.requireNonNull(this.shoppings.get(this.cart.getShoppingId()))
                .getCrowdShoppingId());
  }

  public float getTotalPrice() {
    float price = 0;
    String crowdShoppingId =
        Objects.requireNonNull(this.shoppings.get(this.cart.getShoppingId())).getCrowdShoppingId();
    for (Item item : this.items.values()) {
      if (item.getShoppingItems().containsKey(crowdShoppingId)) {
        price += item.getTotalPrice(crowdShoppingId);
      }
    }
    return price;
  }

  public List<ShoppingItem> getItemShoppingItems(String itemId) {
    List<ShoppingItem> shoppingItems = new ArrayList<>();
    for (ShoppingItem shoppingItem :
        Objects.requireNonNull(this.items.get(itemId)).getShoppingItems().values()) {
      if (shoppingItem.getShopping() != null) {
        shoppingItems.add(shoppingItem);
      }
    }
    return shoppingItems;
  }

  public List<Location> getItemLocations(String itemId) {
    List<Location> locations = new ArrayList<>();
    for (ShoppingItem shoppingItem :
        Objects.requireNonNull(this.items.get(itemId)).getShoppingItems().values()) {
      if (shoppingItem.getShopping() != null) {
        locations.add(shoppingItem.getShopping().getLocation());
      }
    }
    return locations;
  }

  public List<ShoppingItem> newShoppingItems(String itemId) {

    List<ShoppingItem> newShoppingItems = new ArrayList<>();
    Map<String, ShoppingItem> myShoppingItems = null;

    Item item = this.items.get(itemId);
    if (item != null) myShoppingItems = item.getShoppingItems();

    for (Shopping shopping : this.shoppings.values()) {
      if (myShoppingItems != null && myShoppingItems.containsKey(shopping.getCrowdShoppingId())) {
        newShoppingItems.add(
            new ShoppingItem(
                shopping,
                Objects.requireNonNull(myShoppingItems.get(shopping.getCrowdShoppingId()))
                    .getPrice()));
      } else {
        newShoppingItems.add(new ShoppingItem(shopping));
      }
    }
    Collections.sort(newShoppingItems);
    return newShoppingItems;
  }

  public Item addLinkedItem(String crowdItemId, String name) {
    try {
      Item item = this.fireManager.getItemByCrowdItemId(crowdItemId);
      if (item != null) {
        item.setName(name);
        this.items.put(ItemFormFragment.TEMP_ITEM_ID, item);
      }
      return item;
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  public Item addScannedItem(String barcode) {
    try {
      Item item = this.fireManager.getItemByBarcode(barcode);
      if (item == null) {
        item = new Item();
        item.setBarcode(barcode);
      }
      this.items.put(ItemFormFragment.TEMP_ITEM_ID, item);
      return item;
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  public void removeScannedItem() {
    this.items.remove(ItemFormFragment.TEMP_ITEM_ID);
  }

  public String onSaveItem(
      boolean isScanned,
      String itemId,
      String newName,
      String newBarcode,
      PantryItem newPantryItem,
      Set<ShoppingItem> newShoppingItems,
      List<Picture> newPictures) {

    try {
      WriteBatch writeBatch = this.fireManager.getDB().batch();
      Item oldItem;
      if (itemId != null) {
        // Update
        if (isScanned) {
          // Associate To CrowdItem
          String crowdItemId = itemId;
          oldItem = this.getItemByBarcode(newBarcode);
          if (oldItem != null) {
            itemId = oldItem.getId();
            this.fireManager.updateItem(writeBatch, itemId, newName);
          } else {
            itemId = this.fireManager.newItem(writeBatch, newName, crowdItemId);
          }
          this.fireManager.updateCrowdItem(
              writeBatch, crowdItemId, newBarcode, newPictures, newShoppingItems);
        } else {
          this.fireManager.updateItem(writeBatch, itemId, newName);
          this.fireManager.updateCrowdItemByItemId(
              writeBatch, itemId, newBarcode, newPictures, newShoppingItems);
        }
      } else {
        // Add
        String crowdItemId =
            this.fireManager.newCrowdItem(writeBatch, newBarcode, newPictures, newShoppingItems);
        itemId = this.fireManager.newItem(writeBatch, newName, crowdItemId);
      }
      this.fireManager.updatePantryItem(
          writeBatch,
          newPantryItem.getId(),
          itemId,
          newPantryItem.getInPantry(),
          newPantryItem.getInNeed());
      if (!this.commitBatch(writeBatch)) return null;
      return itemId;
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  private Item getItemByBarcode(String newBarcode) {
    for (Item item : this.items.values()) {
      if (newBarcode != null && newBarcode.equals(item.getBarcode())) {
        return item;
      }
    }
    return null;
  }

  public boolean deletePantryItem(String pantryId, String itemId) {

    Item item = Objects.requireNonNull(this.items.get(itemId));
    boolean toDeleteItem = item.getPantryItems().size() == 1;

    WriteBatch writeBatch = this.fireManager.getDB().batch();
    if (toDeleteItem) this.fireManager.deleteItem(writeBatch, itemId);
    else this.fireManager.deletePantryItem(pantryId, itemId, writeBatch);
    boolean success = this.commitBatch(writeBatch);
    if (!toDeleteItem && success) item.removePantryItem(pantryId);
    return success;
  }

  public boolean hasCart() {
    return this.cart.hasCart();
  }

  public boolean canGoToCart(String shoppingId) {
    if (!this.cart.hasCart()) {
      try {
        this.fireManager.updateShoppingCart(shoppingId);
      } catch (InterruptedException | ExecutionException e) {
        return false;
      }
      return true;
    } else return this.cart.getShoppingId().equals(shoppingId);
  }

  public int addToCart(String itemId, Map<String, Integer> inCartPantries) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    Item item = Objects.requireNonNull(this.items.get(itemId));

    for (String pantryId : inCartPantries.keySet()) {
      PantryItem pantryItem = item.getPantryItem(pantryId);
      int inCartAdded = Objects.requireNonNull(inCartPantries.get(pantryId));
      int inNeed = pantryItem.getInNeed() - inCartAdded;
      int inCart = pantryItem.getInCart() + inCartAdded;
      this.updatePantryItemInNeedInCart(itemId, pantryItem, inNeed, inCart, writeBatch);
    }

    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  public int addAllToCart(String itemId) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    Item item = Objects.requireNonNull(this.items.get(itemId));

    for (PantryItem pantryItem : item.getPantryItems().values()) {
      int inCart = pantryItem.getInNeed() + pantryItem.getInCart();
      this.updatePantryItemInNeedInCart(itemId, pantryItem, 0, inCart, writeBatch);
    }

    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  public void updateSmartSort(String crowdShoppingId, Item item, List<Item> inShoppingItems) {
    try {
      if (inShoppingItems.size() == 1) return;

      List<String> aCrowdItemIds = new ArrayList<>();
      for (Item inShoppingItem : inShoppingItems) {
        if (!inShoppingItem.equals(item)) aCrowdItemIds.add(inShoppingItem.getCrowdItemId());
      }
      this.fireManager.updateSmartSort(crowdShoppingId, item.getCrowdItemId(), aCrowdItemIds);
    } catch (ExecutionException | InterruptedException ignored) {
    }
  }

  public int removeFromCart(String itemId, Map<String, Integer> inCartPantries) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    Item item = Objects.requireNonNull(this.items.get(itemId));

    for (String pantryId : inCartPantries.keySet()) {
      PantryItem pantryItem = item.getPantryItem(pantryId);
      int inNeedAdded = Objects.requireNonNull(inCartPantries.get(pantryId));
      int inCart = pantryItem.getInCart() - inNeedAdded;
      int inNeed = pantryItem.getInNeed() + inNeedAdded;
      this.updatePantryItemInNeedInCart(itemId, pantryItem, inNeed, inCart, writeBatch);
    }

    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  public int removeAllFromCart(String itemId) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    Item item = Objects.requireNonNull(this.items.get(itemId));

    for (PantryItem pantryItem : item.getPantryItems().values()) {
      int inNeed = pantryItem.getInNeed() + pantryItem.getInCart();
      this.updatePantryItemInNeedInCart(itemId, pantryItem, inNeed, 0, writeBatch);
    }
    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  public int addAllToPantry(List<Item> items) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    for (Item item : items) {
      this.addAllToPantryItem(item.getId(), writeBatch, item);
    }
    if (!this.commitBatch(writeBatch)) return 0;
    return 1;
  }

  private void addAllToPantryItem(String itemId, WriteBatch writeBatch, Item item) {
    for (PantryItem pantryItem : item.getPantryItems().values()) {
      int inPantry = pantryItem.getInPantry() + pantryItem.getInCart();
      this.updatePantryItemInPantry(itemId, pantryItem, inPantry, writeBatch);
    }
  }

  public boolean updateItemRatings(String itemId, int newRating) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    try {
      this.fireManager.updateItemRatings(writeBatch, itemId, newRating);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    return this.commitBatch(writeBatch);
  }

  public float getItemUserRating(String itemId) {
    try {
      return this.fireManager.getItemUserRating(itemId);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      return 0.0f;
    }
  }

  public List<Integer> getItemRatings(String itemId) {
    return Objects.requireNonNull(this.items.get(itemId)).getRatings();
  }

  public float getItemRatingsAverage(String itemId) {
    int totalRatings = 0;
    float numOfRatings = 0.0f;
    List<Integer> itemRatings = Objects.requireNonNull(this.items.get(itemId)).getRatings();

    for (int i = 0; i < itemRatings.size(); i++) {
      totalRatings += ((i + 1) * itemRatings.get(i));
      numOfRatings += itemRatings.get(i);
    }

    return numOfRatings > 0 ? totalRatings / numOfRatings : 0;
  }

  private void updatePantryItemInNeedInCart(
      String itemId, PantryItem pantryItem, int inNeed, int inCart, WriteBatch writeBatch) {
    this.updatePantryItemCart(
        pantryItem.getId(), itemId, pantryItem.getInPantry(), inNeed, inCart, writeBatch);
  }

  private void updatePantryItemInPantry(
      String itemId, PantryItem pantryItem, int inPantry, WriteBatch writeBatch) {
    this.updatePantryItemCart(
        pantryItem.getId(), itemId, inPantry, pantryItem.getInNeed(), 0, writeBatch);
  }

  public int updatePantryItem(String pantryId, String itemId, int inPantry, int inNeed) {
    WriteBatch writeBatch = this.fireManager.getDB().batch();
    this.fireManager.updatePantryItem(writeBatch, pantryId, itemId, inPantry, inNeed);
    return this.commitBatch(writeBatch) ? 1 : 0;
  }

  private void updatePantryItemCart(
      String pantryId, String itemId, int inPantry, int inNeed, int inCart, WriteBatch writeBatch) {
    this.fireManager.updatePantryItemInCart(pantryId, itemId, inPantry, inNeed, inCart, writeBatch);
  }

  private boolean commitBatch(WriteBatch writeBatch) {
    try {
      Tasks.await(writeBatch.commit());
      return true;
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
  }

  public int countPantryItems(String pantryId) {
    int count = 0;
    for (Item item : this.items.values()) {
      if (item.getPantryItems().containsKey(pantryId)) {
        count++;
      }
    }
    return count;
  }

  public int countShoppingItems(String crowdShoppingId) {
    int count = 0;
    for (Item item : this.items.values()) {
      if (item.getShoppingItems().containsKey(crowdShoppingId)) {
        count++;
      }
    }
    return count;
  }

  public String getPantryToken(String pantryId) {
    try {
      return this.fireManager.getToken(
          "getPantryToken", Collections.singletonMap("pantryId", pantryId));
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  public String getShoppingToken(String shoppingId) {
    try {
      return this.fireManager.getToken(
          "getShoppingToken", Collections.singletonMap("shoppingId", shoppingId));
    } catch (InterruptedException | ExecutionException e) {
      return e.getMessage();
    }
  }

  public int getShared(String token) {
    try {
      if (this.fireManager.getShared(token) == null) return 0;
      return 1;
    } catch (InterruptedException | ExecutionException e) {
      return 0;
    }
  }

  public List<Map.Entry<String, String>> getEmails(String pantryId) {
    List<String> uids = Objects.requireNonNull(this.pantries.get(pantryId)).getUids();
    uids.remove(this.fireManager.getUid());
    if (uids.isEmpty()) return new ArrayList<>();
    try {
      return this.fireManager.getEmails(uids);
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  public String addUserToPantry(String pantryId, String email) {
    try {
      return this.fireManager.addUserToPantry(pantryId, email);
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  public boolean removeUserFromPantry(String pantryId, String uid) {
    try {
      return this.fireManager.removeUserFromPantry(pantryId, uid);
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
  }

  public void checkIn() {
    if (this.cart.hasCart()) {
      try {
        this.fireManager.checkIn(
            Objects.requireNonNull(this.shoppings.get(this.cart.getShoppingId()))
                .getCrowdShoppingId(),
            this.cart.getTotalInCart());
      } catch (ExecutionException | InterruptedException e) {
        Timber.d(e, "CheckIn Failed!");
      }
    }
  }

  public void checkOut() {
    if (this.cart.hasCart()) {
      try {
        this.fireManager.checkOut(
            Objects.requireNonNull(this.shoppings.get(this.cart.getShoppingId()))
                .getCrowdShoppingId(),
            this.cart.getTotalInCart());
      } catch (ExecutionException | InterruptedException e) {
        Timber.d(e, "CheckOut Failed!");
      }
    }
  }

  public boolean isUniqueAreaLocation(Location currLocation) {
    Area areaAtLocation = null;

    for (Pantry pantry : this.pantries.values()) {
      Location location = pantry.getLocation();

      if (location.distanceTo(currLocation) <= MIN_LOCATION_DISTANCE) {
        if (areaAtLocation == null) {
          areaAtLocation = pantry;
        } else {
          return false;
        }
      }
    }

    for (Shopping shopping : this.shoppings.values()) {
      Location location = shopping.getLocation();

      if (location.distanceTo(currLocation) <= MIN_LOCATION_DISTANCE) {
        if (areaAtLocation == null) {
          areaAtLocation = shopping;
        } else {
          return false;
        }
      }
    }

    this.setStartAppArea(areaAtLocation);
    return areaAtLocation != null;
  }

  public Area getStartAppArea() {
    return this.startAppArea;
  }

  public void setStartAppArea(Area startAppArea) {
    this.startAppArea = startAppArea;
  }

  public AuthResponse linkWithGoogle(String idToken) {
    return this.fireManager.linkWithGoogle(idToken);
  }

  public AuthResponse linkWithEmailAndPassword(String email, String pwd) {
    return this.fireManager.linkWithEmailAndPassword(email, pwd);
  }

  public AuthResponse signInWithEmailAndPassword(String email, String pwd) {
    return this.fireManager.signInWithEmailAndPassword(email, pwd);
  }

  public String getAccount() {
    return this.fireManager.getAccount();
  }

  public void logoutAccount() {
    this.fireManager.logoutAccount();
  }

  public Uri getPictureUri(Item item) {
    Picture picture = item.getPicture();
    if (item.getPicture() == null) return null;
    try {
      return this.fireManager.getPictureUri(picture.getPictureUri());
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  public void cleanDomain() {
    this.pantries.clear();
    this.shoppings.clear();
    this.items.clear();
    this.cart.clear();
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    this.fireManager.close();
    if (this.mainActivity.get() != null) {
      this.mainActivity.get().finish();
    }
  }
}
