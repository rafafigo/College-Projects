package pt.ulisboa.tecnico.muc.shopist.firebase;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireAuth.AuthResponse;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.CrowdItemContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.ItemContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.ShoppingContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.UserContract;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import timber.log.Timber;

public class FireManager {

  private static final String PICTURES_PATH = "Pictures";

  private final CommonViewModel commonViewModel;
  private final FirebaseFirestore firestore;
  private final StorageReference storageReference;
  private final FireAuth fireAuth;
  private final FireFunctions fireFunctions;
  private final FireFetch fireFetch;
  private FirebaseUser user;

  public FireManager(CommonViewModel commonViewModel) {
    this.commonViewModel = commonViewModel;
    this.storageReference = FirebaseStorage.getInstance().getReference();
    this.firestore = FirebaseFirestore.getInstance();
    this.fireFunctions = new FireFunctions();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    this.fireAuth = new FireAuth(this, firebaseAuth);
    this.user = firebaseAuth.getCurrentUser();
    if (this.user == null) this.user = this.fireAuth.signInAnonymously();
    this.fireFetch = new FireFetch(this.firestore, this.user, this.commonViewModel);
    this.dbFetch();
  }

  private void dbFetch() {
    this.fireFetch.remove();
    Thread dbFetchDoneThread = new Thread(this.fireFetch::isFetchDone);
    dbFetchDoneThread.start();

    this.fireFetch.dbFetchCart();
    this.fireFetch.dbFetchPantries();
    this.fireFetch.dbFetchShoppings();

    try {
      dbFetchDoneThread.join();
    } catch (InterruptedException e) {
      Timber.d(e, "Error: Fetching Data From Firebase!");
    }
  }

  public String newPantry(String name, String locationName, double latitude, double longitude)
      throws ExecutionException, InterruptedException {

    Map<String, Object> pantry = new HashMap<>();
    pantry.put("Name", name);
    pantry.put("Location", new GeoPoint(latitude, longitude));
    pantry.put("LocationName", locationName);
    pantry.put("Users", Collections.singletonList(this.user.getUid()));
    pantry.put("PantryItems", new HashMap<String, Object>());
    pantry.put(
        "PantryCarts", Collections.singletonMap(this.user.getUid(), new HashMap<String, Object>()));
    pantry.put("Timestamp", Timestamp.now());

    DocumentReference pantryDR = this.firestore.collection("Pantry").document();
    Tasks.await(pantryDR.set(pantry));

    return pantryDR.getId();
  }

  public String newShopping(
      String name, String locationName, double latitude, double longitude, long queueTime)
      throws ExecutionException, InterruptedException {

    for (Shopping shopping : this.commonViewModel.getShoppingMap().values()) {
      if (shopping.getLatitude() == latitude && shopping.getLongitude() == longitude) {
        return null;
      }
    }

    Map<String, Object> crowdShopping = new HashMap<>();
    crowdShopping.put("LocationName", locationName);
    crowdShopping.put("Location", new GeoPoint(latitude, longitude));
    crowdShopping.put("QueueTime", queueTime);

    DocumentReference crowdShoppingDR;
    QuerySnapshot crowdShoppingsQS =
        Tasks.await(
            this.firestore
                .collection("CrowdShopping")
                .whereEqualTo("Location", new GeoPoint(latitude, longitude))
                .get());
    if (crowdShoppingsQS.isEmpty()) {
      crowdShoppingDR = Tasks.await(this.firestore.collection("CrowdShopping").add(crowdShopping));
    } else {
      crowdShoppingDR = crowdShoppingsQS.iterator().next().getReference();
    }

    Map<String, Object> shopping = new HashMap<>();
    shopping.put("Name", name);
    shopping.put("CrowdShopping", crowdShoppingDR);
    shopping.put("Users", Collections.singletonList(this.user.getUid()));
    shopping.put("Timestamp", Timestamp.now());

    DocumentReference shoppingDR = Tasks.await(this.firestore.collection("Shopping").add(shopping));

    return shoppingDR.getId();
  }

  public String newItem(WriteBatch writeBatch, String name, String crowdItemId) {

    Map<String, Object> item = new HashMap<>();
    item.put("Name", name);
    item.put("CrowdItem", this.firestore.collection("CrowdItem").document(crowdItemId));
    item.put("Timestamp", Timestamp.now());

    DocumentReference itemDR = this.firestore.collection("Item").document();
    writeBatch.set(itemDR, item);

    return itemDR.getId();
  }

  public String newCrowdItem(
      WriteBatch writeBatch,
      String barcode,
      List<Picture> pictures,
      Set<ShoppingItem> shoppingItems)
      throws ExecutionException, InterruptedException {

    List<String> pictureUris = new ArrayList<>();
    for (Picture picture : pictures) {
      String pictureUri = this.newPicture(picture.getPictureBmp());
      picture.setPictureUri(pictureUri);
      pictureUris.add(pictureUri);
    }

    Map<String, Float> shoppingPrices = new HashMap<>();
    for (ShoppingItem shoppingItem : shoppingItems) {
      if (shoppingItem.getPrice() != null) {
        shoppingPrices.put(shoppingItem.getId(), shoppingItem.getPrice());
      }
    }

    Map<String, Object> crowdItem = new HashMap<>();
    crowdItem.put("Barcode", barcode);
    crowdItem.put("Pictures", pictureUris);
    crowdItem.put("Prices", shoppingPrices);
    crowdItem.put("Ratings", Arrays.asList(0, 0, 0, 0, 0));

    DocumentReference crowdItemDR = this.firestore.collection("CrowdItem").document();
    writeBatch.set(crowdItemDR, crowdItem);

    return crowdItemDR.getId();
  }

  private String newPicture(Bitmap pictureBmp) throws ExecutionException, InterruptedException {

    String pictureUri = String.format("%s/%s.jpg", PICTURES_PATH, UUID.randomUUID());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    pictureBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    Tasks.await(this.storageReference.child(pictureUri).putBytes(outputStream.toByteArray()));

    return pictureUri;
  }

  public void updateUser(String uid, String email) {

    Map<String, Object> newUser = new HashMap<>();
    newUser.put("Email", email);
    this.firestore.collection("User").document(uid).update(newUser);
  }

  public void updatePantry(
      String pantryId, String name, String locationName, Double latitude, Double longitude)
      throws ExecutionException, InterruptedException {

    Map<String, Object> pantry = new HashMap<>();
    pantry.put("Name", name);
    pantry.put("LocationName", locationName);
    pantry.put("Location", new GeoPoint(latitude, longitude));
    pantry.put("Timestamp", Timestamp.now());

    Tasks.await(this.firestore.collection("Pantry").document(pantryId).update(pantry));
  }

  public int updateShopping(
      String shoppingId, String name, String locationName, double latitude, double longitude)
      throws ExecutionException, InterruptedException {

    for (Shopping shopping : this.commonViewModel.getShoppingMap().values()) {
      if (!shoppingId.equals(shopping.getId())
          && shopping.getLatitude() == latitude
          && shopping.getLongitude() == longitude) {
        return 0;
      }
    }

    QuerySnapshot crowdShoppingsQS =
        Tasks.await(
            this.firestore
                .collection("CrowdShopping")
                .whereEqualTo("Location", new GeoPoint(latitude, longitude))
                .get());
    DocumentReference crowdShoppingDR;
    if (crowdShoppingsQS.isEmpty()) {
      DocumentSnapshot shoppingDS =
          Tasks.await(this.firestore.collection("Shopping").document(shoppingId).get(Source.CACHE));
      String crowdShoppingId =
          Objects.requireNonNull(shoppingDS.toObject(ShoppingContract.class)).CrowdShopping.getId();
      crowdShoppingDR = this.firestore.collection("CrowdShopping").document(crowdShoppingId);
    } else {
      crowdShoppingDR = crowdShoppingsQS.iterator().next().getReference();
    }

    Map<String, Object> crowdShopping = new HashMap<>();
    crowdShopping.put("LocationName", locationName);
    crowdShopping.put("Location", new GeoPoint(latitude, longitude));

    Map<String, Object> shopping = new HashMap<>();
    shopping.put("Name", name);
    shopping.put("CrowdShopping", crowdShoppingDR);
    shopping.put("Timestamp", Timestamp.now());

    WriteBatch writeBatch = this.firestore.batch();
    writeBatch.update(this.firestore.collection("Shopping").document(shoppingId), shopping);
    writeBatch.update(crowdShoppingDR, crowdShopping);
    Tasks.await(writeBatch.commit());
    return 1;
  }

  public void updatePantryItem(
      WriteBatch writeBatch, String pantryId, String itemId, int newInPantry, int newInNeed) {

    Map<String, Object> newPantryItem = new HashMap<>();
    newPantryItem.put("InPantry", newInPantry);
    newPantryItem.put("InNeed", newInNeed);

    writeBatch.update(
        this.firestore.collection("Pantry").document(pantryId),
        String.format("PantryItems.%s", itemId),
        newPantryItem);
  }

  public void updateItem(WriteBatch writeBatch, String itemId, String newName) {
    Map<String, Object> item = new HashMap<>();
    item.put("Name", newName);
    item.put("Timestamp", Timestamp.now());
    writeBatch.update(this.firestore.collection("Item").document(itemId), item);
  }

  public void updateCrowdItemByItemId(
      WriteBatch writeBatch,
      String itemId,
      String newBarcode,
      List<Picture> newPictures,
      Set<ShoppingItem> newShoppingItems)
      throws ExecutionException, InterruptedException {

    DocumentReference itemDR = this.firestore.collection("Item").document(itemId);
    DocumentSnapshot itemDS = Tasks.await(itemDR.get(Source.CACHE));
    ItemContract itemContract = Objects.requireNonNull(itemDS.toObject(ItemContract.class));

    this.updateCrowdItem(
        writeBatch, itemContract.CrowdItem.getId(), newBarcode, newPictures, newShoppingItems);
  }

  public Uri getPictureUri(String pictureUri) throws ExecutionException, InterruptedException {
    return Tasks.await(this.storageReference.child(pictureUri).getDownloadUrl());
  }

  public void updateCrowdItem(
      WriteBatch writeBatch,
      String crowdItemId,
      String newBarcode,
      List<Picture> newPictures,
      Set<ShoppingItem> newShoppingItems)
      throws ExecutionException, InterruptedException {

    DocumentReference crowdItemDR = this.firestore.collection("CrowdItem").document(crowdItemId);
    DocumentSnapshot crowdItemDS = Tasks.await(crowdItemDR.get(Source.CACHE));
    CrowdItemContract crowdItemContract =
        Objects.requireNonNull(crowdItemDS.toObject(CrowdItemContract.class));

    Map<String, Object> newCrowdItem = new HashMap<>();
    newCrowdItem.put("Barcode", newBarcode);
    newCrowdItem.put("Pictures", this.updatePictures(crowdItemContract.Pictures, newPictures));
    for (ShoppingItem newShoppingItem : newShoppingItems) {
      newCrowdItem.put(
          String.format("Prices.%s", newShoppingItem.getId()),
          newShoppingItem.getPrice() != null ? newShoppingItem.getPrice() : FieldValue.delete());
    }

    writeBatch.update(crowdItemDR, newCrowdItem);
  }

  private List<String> updatePictures(List<String> myPictureUris, List<Picture> newPictures)
      throws ExecutionException, InterruptedException {

    List<String> newPictureUris = new ArrayList<>();
    Set<String> myPictureUrisSet = new HashSet<>(myPictureUris);

    for (Picture newPicture : newPictures) {
      if (!myPictureUrisSet.remove(newPicture.getPictureUri())) {
        // Add
        String newPictureUri = newPicture(newPicture.getPictureBmp());
        newPicture.setPictureUri(newPictureUri);
      }
      newPictureUris.add(newPicture.getPictureUri());
    }
    for (String myPictureUri : myPictureUrisSet) {
      // Remove
      Tasks.await(this.storageReference.child(myPictureUri).delete());
    }
    return newPictureUris;
  }

  public void updateItemRatings(WriteBatch writeBatch, String itemId, int newRating)
      throws ExecutionException, InterruptedException {

    ItemContract itemContract = getItemContract(itemId);

    int lastRating =
        this.updateUserCrowdItemRatings(writeBatch, itemContract.CrowdItem.getId(), newRating);
    this.updateCrowdItemRatings(writeBatch, itemContract.CrowdItem.getId(), lastRating, newRating);
  }

  public ItemContract getItemContract(String itemId)
      throws ExecutionException, InterruptedException {
    DocumentReference itemDR = this.firestore.collection("Item").document(itemId);
    DocumentSnapshot itemDS = Tasks.await(itemDR.get(Source.CACHE));
    return Objects.requireNonNull(itemDS.toObject(ItemContract.class));
  }

  public int updateUserCrowdItemRatings(WriteBatch writeBatch, String crowdItemId, int rating)
      throws ExecutionException, InterruptedException {

    DocumentReference userDR = this.firestore.collection("User").document(user.getUid());
    DocumentSnapshot userDS = Tasks.await(userDR.get(Source.CACHE));
    UserContract userContract = userDS.toObject(UserContract.class);

    Integer lastRating = Objects.requireNonNull(userContract).CrowdItemRatings.get(crowdItemId);

    writeBatch.update(userDR, String.format("CrowdItemRatings.%s", crowdItemId), rating);

    return lastRating == null ? 0 : lastRating;
  }

  public void updateCrowdItemRatings(
      WriteBatch writeBatch, String crowdItemId, int lastRating, int newRating)
      throws ExecutionException, InterruptedException {

    DocumentReference crowdItemDR = this.firestore.collection("CrowdItem").document(crowdItemId);
    DocumentSnapshot crowdItemDS = Tasks.await(crowdItemDR.get(Source.CACHE));
    CrowdItemContract crowdItemContract = crowdItemDS.toObject(CrowdItemContract.class);

    int lastRatingIdx = lastRating - 1;
    int newRatingIdx = newRating - 1;

    List<Integer> currRatings = Objects.requireNonNull(crowdItemContract).Ratings;
    if (newRatingIdx > -1) {
      currRatings.set(newRatingIdx, currRatings.get(newRatingIdx) + 1);
    }
    if (lastRatingIdx > -1) {
      currRatings.set(lastRatingIdx, currRatings.get(lastRatingIdx) - 1);
    }

    Map<String, Object> newCrowdItemRating = new HashMap<>();
    newCrowdItemRating.put("Ratings", currRatings);

    writeBatch.update(crowdItemDR, newCrowdItemRating);
  }

  public float getItemUserRating(String itemId) throws ExecutionException, InterruptedException {

    ItemContract itemContract = getItemContract(itemId);

    DocumentReference userDR = this.firestore.collection("User").document(user.getUid());
    DocumentSnapshot userDS = Tasks.await(userDR.get(Source.CACHE));
    UserContract userContract = userDS.toObject(UserContract.class);

    Integer lastRating =
        Objects.requireNonNull(userContract).CrowdItemRatings.get(itemContract.CrowdItem.getId());

    return lastRating == null ? 0.0f : lastRating;
  }

  public void updatePantryItemInCart(
      String pantryId, String itemId, int inPantry, int inNeed, int inCart, WriteBatch writeBatch) {
    this.updatePantryItem(writeBatch, pantryId, itemId, inPantry, inNeed);
    writeBatch.update(
        this.firestore.collection("Pantry").document(pantryId),
        String.format("PantryCarts.%s.%s", user.getUid(), itemId),
        inCart);
  }

  public void updateShoppingCart(String shoppingId)
      throws ExecutionException, InterruptedException {

    Tasks.await(
        this.firestore.collection("User").document(user.getUid()).update("ShoppingId", shoppingId));
  }

  public String addUserToPantry(String pantryId, String email)
      throws ExecutionException, InterruptedException {

    QuerySnapshot userQS =
        Tasks.await(this.firestore.collection("User").whereEqualTo("Email", email).get());

    if (userQS.isEmpty()) return null;
    String uid = userQS.iterator().next().getId();

    Map<String, Object> newPantry = new HashMap<>();
    newPantry.put("Users", FieldValue.arrayUnion(uid));
    newPantry.put(String.format("PantryCarts.%s", uid), new HashMap<>());

    Tasks.await(this.firestore.collection("Pantry").document(pantryId).update(newPantry));
    return uid;
  }

  public boolean removeUserFromPantry(String pantryId, String uid)
      throws ExecutionException, InterruptedException {

    Map<String, Object> newPantry = new HashMap<>();
    newPantry.put("Users", FieldValue.arrayRemove(uid));
    newPantry.put(String.format("PantryCarts.%s", uid), FieldValue.delete());

    Tasks.await(this.firestore.collection("Pantry").document(pantryId).update(newPantry));
    return true;
  }

  public void deletePantry(String pantryId, WriteBatch writeBatch) {
    writeBatch.delete(this.firestore.collection("Pantry").document(pantryId));
  }

  public void deleteShopping(String shoppingId) throws ExecutionException, InterruptedException {
    Tasks.await(this.firestore.collection("Shopping").document(shoppingId).delete());
  }

  public void deleteItem(WriteBatch writeBatch, String itemId) {
    writeBatch.delete(this.firestore.collection("Item").document(itemId));
  }

  public void deletePantryItem(String pantryId, String itemId, WriteBatch writeBatch) {
    writeBatch.update(
        this.firestore.collection("Pantry").document(pantryId),
        String.format("PantryItems.%s", itemId),
        FieldValue.delete());
    writeBatch.update(
        this.firestore.collection("Pantry").document(pantryId),
        String.format("PantryCarts.%s.%s", user.getUid(), itemId),
        FieldValue.delete());
  }

  public List<Map.Entry<String, String>> getEmails(List<String> uids)
      throws ExecutionException, InterruptedException {

    QuerySnapshot userQS =
        Tasks.await(this.firestore.collection("User").whereIn(FieldPath.documentId(), uids).get());

    List<Map.Entry<String, String>> emails = new ArrayList<>();
    for (DocumentSnapshot userDS : userQS) {
      UserContract userContract = Objects.requireNonNull(userDS.toObject(UserContract.class));
      emails.add(new AbstractMap.SimpleEntry<>(userDS.getId(), userContract.Email));
    }
    return emails;
  }

  public Item getItemByBarcode(String barcode) throws ExecutionException, InterruptedException {
    QuerySnapshot crowdItemQS =
        Tasks.await(this.firestore.collection("CrowdItem").whereEqualTo("Barcode", barcode).get());

    Iterator<QueryDocumentSnapshot> crowdItemQSI = crowdItemQS.iterator();
    if (!crowdItemQSI.hasNext()) return null;
    return this.getItem(crowdItemQSI.next());
  }

  public Item getItemByCrowdItemId(String crowdItemId)
      throws ExecutionException, InterruptedException {
    DocumentSnapshot crowdItemDS =
        Tasks.await(this.firestore.collection("CrowdItem").document(crowdItemId).get());

    if (!crowdItemDS.exists()) return null;
    return this.getItem(crowdItemDS);
  }

  private Item getItem(DocumentSnapshot crowdItemDS) {
    CrowdItemContract crowdItemContract =
        Objects.requireNonNull(crowdItemDS.toObject(CrowdItemContract.class));

    Item item = new Item();
    item.setId(crowdItemDS.getId());
    item.setBarcode(crowdItemContract.Barcode);
    item.addAllPictureUris(crowdItemContract.Pictures);
    item.putAllPrices(crowdItemContract.Prices);
    return item;
  }

  public String getToken(String funcName, Map<String, String> data)
      throws ExecutionException, InterruptedException {
    return this.fireFunctions.getToken(funcName, data);
  }

  public Object getShared(String token) throws ExecutionException, InterruptedException {
    return this.fireFunctions.getShared(token);
  }

  public void checkIn(String crowdShoppingId, int numberOfItems)
      throws ExecutionException, InterruptedException {
    this.fireFunctions.checkIn(crowdShoppingId, numberOfItems);
  }

  public void checkOut(String crowdShoppingId, int numberOfItems)
      throws ExecutionException, InterruptedException {
    this.fireFunctions.checkOut(crowdShoppingId, numberOfItems);
  }

  public void updateSmartSort(
      String crowdShoppingId, String bCrowdItemId, List<String> aCrowdItemIds)
      throws ExecutionException, InterruptedException {
    this.fireFunctions.updateSmartSort(crowdShoppingId, bCrowdItemId, aCrowdItemIds);
  }

  public AuthResponse linkWithGoogle(String idToken) {
    String oldUid = this.user.getUid();
    AuthResponse authResponse = this.fireAuth.linkWithGoogle(this.user, idToken);
    if (authResponse.success()) {
      this.user = authResponse.getFirebaseUser();
      if (!this.user.getUid().equals(oldUid)) {
        this.commonViewModel.cleanDomain();
        this.dbFetch();
      }
    }
    return authResponse;
  }

  public AuthResponse linkWithEmailAndPassword(String email, String pwd) {
    AuthResponse authResponse = this.fireAuth.linkWithEmailAndPassword(this.user, email, pwd);
    if (authResponse.success()) this.user = authResponse.getFirebaseUser();
    return authResponse;
  }

  public AuthResponse signInWithEmailAndPassword(String email, String pwd) {
    AuthResponse authResponse = this.fireAuth.signInWithEmailAndPassword(email, pwd);
    if (authResponse.success()) {
      this.user = authResponse.getFirebaseUser();
      this.commonViewModel.cleanDomain();
      this.dbFetch();
    }
    return authResponse;
  }

  public FirebaseFirestore getDB() {
    return this.firestore;
  }

  public String getUid() {
    return this.user.getUid();
  }

  public String getAccount() {
    if (this.user.isAnonymous()) {
      return null;
    } else {
      return this.user.getEmail();
    }
  }

  public void logoutAccount() {
    this.fireAuth.signOut();
    this.user = this.fireAuth.signInAnonymously();
    this.commonViewModel.cleanDomain();
    this.dbFetch();
  }

  public void close() {
    this.firestore.terminate();
  }
}
