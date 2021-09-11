package pt.ulisboa.tecnico.muc.shopist.firebase;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.CrowdItemContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.CrowdShoppingContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.ItemContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.PantryContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.ShoppingContract;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireContract.UserContract;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import timber.log.Timber;

public class FireFetch {

  private final FirebaseFirestore firestore;
  private final FirebaseUser user;
  private final CommonViewModel commonViewModel;
  private final List<ListenerRegistration> listenerRegistrations;
  private boolean isFetchCartDone = false;
  private boolean isFetchShoppingsDone = false;
  private boolean isFetchPantriesDone = false;

  protected FireFetch(
      FirebaseFirestore firestore, FirebaseUser user, CommonViewModel commonViewModel) {
    this.firestore = firestore;
    this.user = user;
    this.commonViewModel = commonViewModel;
    this.listenerRegistrations = new ArrayList<>();
  }

  protected void dbFetchCart() {
    this.listenerRegistrations.add(
        this.firestore
            .collection("User")
            .document(this.user.getUid())
            .addSnapshotListener(this::dbFetchCart));
  }

  protected void dbFetchCart(DocumentSnapshot value, FirebaseFirestoreException error) {

    if (error != null) {
      Timber.d(error);
      this.fetchCartDone();
      return;
    }

    if (!Objects.requireNonNull(value).exists()) {
      Map<String, Object> newUser = new HashMap<>();
      newUser.put("ShoppingId", null);
      newUser.put("Email", null);
      newUser.put("CrowdItemRatings", new HashMap<>());
      this.firestore.collection("User").document(this.user.getUid()).set(newUser);
      return;
    }
    UserContract userContract = Objects.requireNonNull(value.toObject(UserContract.class));
    this.commonViewModel.getCart().setShoppingId(userContract.ShoppingId);
    this.fetchCartDone();
  }

  protected void dbFetchShoppings() {
    this.listenerRegistrations.add(
        this.firestore
            .collection("Shopping")
            .whereArrayContains("Users", user.getUid())
            .addSnapshotListener(this::dbFetchShoppings));
  }

  protected void dbFetchShoppings(QuerySnapshot value, FirebaseFirestoreException error) {

    if (error != null) {
      Timber.d(error);
      this.fetchCartDone();
      return;
    }

    Map<String, Shopping> shoppingByCrowdShoppingId = new HashMap<>();

    for (DocumentChange dc : value.getDocumentChanges()) {
      switch (dc.getType()) {
        case ADDED:
        case MODIFIED:
          ShoppingContract shoppingContract =
              Objects.requireNonNull(dc.getDocument().toObject(ShoppingContract.class));

          Shopping shopping = this.commonViewModel.getShoppingMap().get(dc.getDocument().getId());
          if (shopping == null) {
            shopping =
                new Shopping(
                    dc.getDocument().getId(), shoppingContract.Name, shoppingContract.Timestamp);

            Shopping myShopping =
                shoppingByCrowdShoppingId.get(shoppingContract.CrowdShopping.getId());
            if (myShopping == null || myShopping.compareTo(shopping) > 0)
              shoppingByCrowdShoppingId.put(shoppingContract.CrowdShopping.getId(), shopping);
          }

          shopping.setName(shoppingContract.Name);
          shopping.setTimestamp(shoppingContract.Timestamp);
          break;
        case REMOVED:
          this.commonViewModel.deleteShopping(dc.getDocument().getId());
          break;
      }
    }

    if (shoppingByCrowdShoppingId.isEmpty()) {
      this.fetchShoppingsDone();
      return;
    }

    this.listenerRegistrations.add(
        this.firestore
            .collection("CrowdShopping")
            .whereIn(FieldPath.documentId(), new ArrayList<>(shoppingByCrowdShoppingId.keySet()))
            .addSnapshotListener(
                (v, e) -> this.dbFetchCrowdShoppings(v, e, shoppingByCrowdShoppingId)));
  }

  protected void dbFetchCrowdShoppings(
      QuerySnapshot value,
      FirebaseFirestoreException error,
      Map<String, Shopping> shoppingByCrowdShoppingId) {

    if (error != null) {
      Timber.d(error);
      this.fetchShoppingsDone();
      return;
    }

    for (DocumentChange dc : value.getDocumentChanges()) {
      switch (dc.getType()) {
        case ADDED:
        case MODIFIED:
          CrowdShoppingContract crowdShoppingContract =
              Objects.requireNonNull(dc.getDocument().toObject(CrowdShoppingContract.class));

          Shopping shopping =
              Objects.requireNonNull(shoppingByCrowdShoppingId.get(dc.getDocument().getId()));

          boolean isOldLocation =
              shopping.getLatitude() == crowdShoppingContract.Location.getLatitude()
                  && shopping.getLongitude() == crowdShoppingContract.Location.getLongitude();

          shopping.setCrowdShoppingId(dc.getDocument().getId());
          shopping.setLocation(
              crowdShoppingContract.LocationName,
              crowdShoppingContract.Location.getLatitude(),
              crowdShoppingContract.Location.getLongitude());
          shopping.setQueueTime(crowdShoppingContract.QueueTime);
          shopping.setSmartSort(crowdShoppingContract.SmartSort);
          this.commonViewModel.putShopping(shopping, isOldLocation);
          break;
        case REMOVED:
          break;
      }
    }
    this.associateShoppingItems();
    this.fetchShoppingsDone();
  }

  protected void dbFetchPantries() {
    this.listenerRegistrations.add(
        this.firestore
            .collection("Pantry")
            .whereArrayContains("Users", user.getUid())
            .addSnapshotListener(this::dbFetchPantries));
  }

  protected void dbFetchPantries(QuerySnapshot value, FirebaseFirestoreException error) {

    if (error != null) {
      Timber.d(error);
      this.fetchPantriesDone();
      return;
    }

    Map<String, Map<String, PantryItem>> pantryItemsByItemId = new HashMap<>();
    for (DocumentChange dc : value.getDocumentChanges()) {
      switch (dc.getType()) {
        case ADDED:
        case MODIFIED:
          PantryContract pantryContract =
              Objects.requireNonNull(dc.getDocument().toObject(PantryContract.class));

          Pantry pantry = this.commonViewModel.getPantryMap().get(dc.getDocument().getId());
          boolean isOldLocation =
              pantry != null
                  && pantryContract.Location.getLatitude() == pantry.getLatitude()
                  && pantryContract.Location.getLongitude() == pantry.getLongitude();
          if (pantry == null) {
            pantry =
                new Pantry(dc.getDocument().getId(), pantryContract.Name, pantryContract.Timestamp);
          }
          pantry.setUids(pantryContract.Users);
          pantry.setName(pantryContract.Name);
          pantry.setTimestamp(pantryContract.Timestamp);
          pantry.setLocation(
              pantryContract.LocationName,
              pantryContract.Location.getLatitude(),
              pantryContract.Location.getLongitude());

          Map<String, Integer> pantryCartContract = pantryContract.PantryCarts.get(user.getUid());

          for (Map.Entry<String, Map<String, Integer>> pantryItemContract :
              pantryContract.PantryItems.entrySet()) {

            Integer inCart =
                Objects.requireNonNull(pantryCartContract).get(pantryItemContract.getKey());
            if (inCart != null) {
              this.commonViewModel.putPantryItemCart(
                  dc.getDocument().getId(), pantryItemContract.getKey(), inCart);
            } else inCart = 0;

            PantryItem pantryItem =
                this.commonViewModel.putPantryItem(
                    pantryItemContract.getKey(),
                    dc.getDocument().getId(),
                    pantryItemContract.getValue().get("InPantry"),
                    pantryItemContract.getValue().get("InNeed"),
                    inCart,
                    pantry.getTimestamp());

            if (this.commonViewModel.getItemMap().containsKey(pantryItemContract.getKey())) {
              continue;
            }

            if (!pantryItemsByItemId.containsKey(pantryItemContract.getKey())) {
              pantryItemsByItemId.put(pantryItemContract.getKey(), new HashMap<>());
            }
            Objects.requireNonNull(pantryItemsByItemId.get(pantryItemContract.getKey()))
                .put(dc.getDocument().getId(), pantryItem);
          }
          this.commonViewModel.putPantry(pantry, isOldLocation);
          break;
        case REMOVED:
          this.commonViewModel.deletePantry(dc.getDocument().getId());
          break;
      }
    }
    if (pantryItemsByItemId.isEmpty()) {
      this.fetchPantriesDone();
      return;
    }

    this.listenerRegistrations.add(
        this.firestore
            .collection("Item")
            .whereIn(FieldPath.documentId(), new ArrayList<>(pantryItemsByItemId.keySet()))
            .addSnapshotListener((v, e) -> this.dbFetchItems(v, e, pantryItemsByItemId)));
  }

  protected void dbFetchItems(
      QuerySnapshot value,
      FirebaseFirestoreException error,
      Map<String, Map<String, PantryItem>> pantryItemsByItemId) {

    if (error != null) {
      Timber.d(error);
      this.fetchPantriesDone();
      return;
    }

    Map<String, Item> itemByCrowdId = new HashMap<>();

    for (DocumentChange dc : value.getDocumentChanges()) {
      switch (dc.getType()) {
        case ADDED:
        case MODIFIED:
          ItemContract itemContract =
              Objects.requireNonNull(dc.getDocument().toObject(ItemContract.class));
          Item item = this.commonViewModel.getItemMap().get(dc.getDocument().getId());

          if (item == null) {
            item = new Item(dc.getDocument().getId(), itemContract.Timestamp);
            itemByCrowdId.put(itemContract.CrowdItem.getId(), item);
          }

          item.setName(itemContract.Name);
          item.setTimestamp(itemContract.Timestamp);
          item.putAllPantryItems(
              Objects.requireNonNull(pantryItemsByItemId.get(dc.getDocument().getId())).values());
          break;
        case REMOVED:
          this.commonViewModel.deleteItem(dc.getDocument().getId());
          break;
      }
    }
    if (itemByCrowdId.isEmpty()) {
      this.fetchPantriesDone();
      return;
    }

    this.listenerRegistrations.add(
        this.firestore
            .collection("CrowdItem")
            .whereIn(FieldPath.documentId(), new ArrayList<>(itemByCrowdId.keySet()))
            .addSnapshotListener((v, e) -> this.dbFetchCrowdItems(v, e, itemByCrowdId)));
  }

  protected void dbFetchCrowdItems(
      QuerySnapshot value, FirebaseFirestoreException error, Map<String, Item> itemByCrowdId) {

    if (error != null) {
      Timber.d(error);
      this.fetchPantriesDone();
      return;
    }

    for (DocumentChange dc : value.getDocumentChanges()) {
      switch (dc.getType()) {
        case ADDED:
        case MODIFIED:
          Item item = itemByCrowdId.get(dc.getDocument().getId());
          if (item == null) continue;

          CrowdItemContract crowdItemContract =
              Objects.requireNonNull(dc.getDocument().toObject(CrowdItemContract.class));

          item.setCrowdItemId(dc.getDocument().getId());
          item.setBarcode(crowdItemContract.Barcode);
          item.putAllPictureUris(crowdItemContract.Pictures);
          item.putAllPrices(crowdItemContract.Prices);
          item.putAllRatings(crowdItemContract.Ratings);
          this.commonViewModel.putItem(item);
          break;
        case REMOVED:
          break;
      }
    }
    this.associateShoppingItems();
    this.fetchPantriesDone();
  }

  private void associateShoppingItems() {
    for (Shopping shopping : this.commonViewModel.getShoppingMap().values()) {
      for (Item item : this.commonViewModel.getItemMap().values()) {
        ShoppingItem shoppingItem = item.getShoppingItems().get(shopping.getCrowdShoppingId());
        if (shoppingItem != null) shoppingItem.setShopping(shopping);
      }
    }
  }

  protected synchronized void isFetchDone() {
    try {
      while (!this.isFetchCartDone || !this.isFetchShoppingsDone || !this.isFetchPantriesDone)
        wait();
    } catch (InterruptedException e) {
      Timber.d(e, "Error: Fetching Data From Firebase!");
    }
  }

  protected synchronized void remove() {
    for (ListenerRegistration listenerRegistration : this.listenerRegistrations) {
      listenerRegistration.remove();
    }
    this.listenerRegistrations.clear();
  }

  protected synchronized void fetchCartDone() {
    this.isFetchCartDone = true;
    notifyAll();
  }

  protected synchronized void fetchShoppingsDone() {
    this.isFetchShoppingsDone = true;
    notifyAll();
  }

  protected synchronized void fetchPantriesDone() {
    this.isFetchPantriesDone = true;
    notifyAll();
  }
}
