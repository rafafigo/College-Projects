package pt.ulisboa.tecnico.muc.shopist.ui.common.itemform;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;

public class ItemFormViewModel extends ViewModel {

  private final MutableLiveData<List<ShoppingItem>> filteredShoppingItems;
  private List<ShoppingItem> shoppingItems;

  public ItemFormViewModel() {
    this.filteredShoppingItems = new MutableLiveData<>();
    this.shoppingItems = new ArrayList<>();
  }

  public List<ShoppingItem> getShoppingItems() {
    return this.shoppingItems;
  }

  public void setShoppingItems(List<ShoppingItem> shoppingItems) {
    this.shoppingItems = shoppingItems;
    this.filteredShoppingItems.setValue(this.shoppingItems);
  }

  public void filterShoppingItems(CharSequence s) {
    List<ShoppingItem> filteredShoppingItems = new ArrayList<>();
    for (ShoppingItem shoppingItem : this.shoppingItems) {
      if (Objects.requireNonNull(shoppingItem.getShopping().getName()).contains(s)) {
        filteredShoppingItems.add(shoppingItem);
      }
    }
    this.filteredShoppingItems.setValue(filteredShoppingItems);
  }

  public LiveData<List<ShoppingItem>> getFilteredShopPrices() {
    return this.filteredShoppingItems;
  }
}
