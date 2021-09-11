package pt.ulisboa.tecnico.muc.shopist.ui.common.itemform;

import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;

public class ShoppingItemsManager {

  // Multiple Shopping Items
  private final ScrollView scrollView;
  private final ItemFormViewModel itemFormViewModel;
  private final RecyclerView shoppingItemsRV;
  private final TextView showShoppingItemsTV;
  private final EditText shoppingsSearchbarET;
  // Single Shopping Item
  private final EditText shoppingPriceET;
  private final ShoppingItem myShoppingItem;

  private final boolean isMultiple;
  private boolean areShoppingItemsVisible = false;
  private boolean didRender = false;

  public ShoppingItemsManager(
      ItemFormFragment fragment, View view, ItemFormViewModel itemFormViewModel) {
    this.scrollView = (ScrollView) view;
    this.itemFormViewModel = itemFormViewModel;

    // Multiple Shop Prices
    this.showShoppingItemsTV = view.findViewById(R.id.itemform_show_shopprices);
    this.shoppingsSearchbarET = view.findViewById(R.id.itemform_shopprices_searchbar);
    this.shoppingItemsRV = view.findViewById(R.id.itemform_shopprices);
    // Single Shop Price
    TextView shoppingNameTV = view.findViewById(R.id.itemform_shopprice_label);
    this.shoppingPriceET = view.findViewById(R.id.itemform_shopprice);

    List<ShoppingItem> shoppingItems = this.itemFormViewModel.getShoppingItems();
    this.isMultiple = shoppingItems.size() != 1;

    if (this.isMultiple) {
      this.showShoppingItemsTV.setCompoundDrawablesWithIntrinsicBounds(
          0, 0, R.drawable.ic_keyboard_arrow_down, 0);

      this.shoppingItemsRV.setLayoutManager(new LinearLayoutManager(fragment.getContext()));

      MyShoppingItemAdapter myShoppingItemAdapter = new MyShoppingItemAdapter();
      this.shoppingItemsRV.setAdapter(myShoppingItemAdapter);

      this.itemFormViewModel
          .getFilteredShopPrices()
          .observe(fragment.getViewLifecycleOwner(), myShoppingItemAdapter::setShoppingItems);

      this.shoppingsSearchbarET.addTextChangedListener(new SearchbarTextWatcher());
      this.showShoppingItemsTV.setOnClickListener(this::onShowToggle);

      this.myShoppingItem = null;
      shoppingNameTV.setVisibility(View.GONE);
      this.shoppingPriceET.setVisibility(View.GONE);
    } else {
      this.myShoppingItem = shoppingItems.get(0);
      shoppingNameTV.setText(
          String.format("%s:", Objects.requireNonNull(myShoppingItem.getShopping().getName())));
      if (this.myShoppingItem.getPrice() != null) {
        this.shoppingPriceET.setText(String.valueOf(this.myShoppingItem.getPrice()));
      }

      this.showShoppingItemsTV.setVisibility(View.GONE);
    }
    this.shoppingsSearchbarET.setVisibility(View.GONE);
    this.shoppingItemsRV.setVisibility(View.GONE);
  }

  public void onShowToggle(View view) {
    this.areShoppingItemsVisible = !this.areShoppingItemsVisible;

    this.showShoppingItemsTV.setCompoundDrawablesWithIntrinsicBounds(
        0,
        0,
        this.areShoppingItemsVisible
            ? R.drawable.ic_keyboard_arrow_up
            : R.drawable.ic_keyboard_arrow_down,
        0);

    this.shoppingsSearchbarET.setVisibility(
        this.areShoppingItemsVisible ? View.VISIBLE : View.GONE);
    this.shoppingItemsRV.setVisibility(this.areShoppingItemsVisible ? View.VISIBLE : View.GONE);

    if (this.areShoppingItemsVisible) {
      this.didRender = true;
      this.scrollView.postDelayed(
          () -> {
            Point childOffset = new Point();
            this.getDeepChildOffset(
                this.scrollView,
                this.showShoppingItemsTV.getParent(),
                this.showShoppingItemsTV,
                childOffset);
            this.scrollView.smoothScrollTo(0, childOffset.y);
          },
          100);
    }
  }

  public List<ShoppingItem> fetchShoppingItems() {
    List<ShoppingItem> shoppingItems = new ArrayList<>();
    if (this.isMultiple && this.didRender) {
      for (int i = 0; i < this.shoppingItemsRV.getChildCount(); i++) {
        ShoppingItem shoppingItem =
            ((MyShoppingItemAdapter.ViewHolder)
                    this.shoppingItemsRV.getChildViewHolder(this.shoppingItemsRV.getChildAt(i)))
                .fetchShoppingItem();
        shoppingItems.add(shoppingItem);
      }
    } else if (!this.isMultiple) {
      this.myShoppingItem.setPrice(
          this.shoppingPriceET.getText().length() > 0
              ? Float.parseFloat(this.shoppingPriceET.getText().toString())
              : null);
      shoppingItems.add(this.myShoppingItem);
    }
    return shoppingItems;
  }

  private void getDeepChildOffset(
      final ViewGroup mainParent,
      final ViewParent parent,
      final View child,
      final Point accumulatedOffset) {
    ViewGroup parentGroup = (ViewGroup) parent;
    accumulatedOffset.x += child.getLeft();
    accumulatedOffset.y += child.getTop();
    if (parentGroup.equals(mainParent)) {
      return;
    }
    this.getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
  }

  private class SearchbarTextWatcher implements TextWatcher {

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      itemFormViewModel.filterShoppingItems(s);
    }
  }
}
