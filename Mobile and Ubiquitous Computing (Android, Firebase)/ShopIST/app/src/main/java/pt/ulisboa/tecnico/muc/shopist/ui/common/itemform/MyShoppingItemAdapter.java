package pt.ulisboa.tecnico.muc.shopist.ui.common.itemform;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;

/** {@link RecyclerView.Adapter} that can display a {@link ShoppingItem}. */
public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.ViewHolder> {

  private List<ShoppingItem> shoppingItems;

  protected MyShoppingItemAdapter() {
    this.shoppingItems = new ArrayList<>();
  }

  protected void setShoppingItems(List<ShoppingItem> shoppingItems) {
    this.shoppingItems = shoppingItems;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_itemform_shopprice, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.onBindShopPrice(this.shoppingItems.get(position));
  }

  @Override
  public int getItemCount() {
    return this.shoppingItems.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView myShopView;
    private final EditText myPriceView;
    private ShoppingItem myShoppingItem;

    public ViewHolder(View view) {
      super(view);
      this.myShopView = view.findViewById(R.id.itemform_shop);
      this.myPriceView = view.findViewById(R.id.itemform_price);
    }

    private void onBindShopPrice(ShoppingItem shoppingItem) {
      this.myShoppingItem = shoppingItem;
      this.myShopView.setText(
          String.format("%s:", Objects.requireNonNull(shoppingItem.getShopping().getName())));
      if (shoppingItem.getPrice() != null) {
        this.myPriceView.setText(String.valueOf(shoppingItem.getPrice()));
      }
    }

    public ShoppingItem fetchShoppingItem() {
      this.myShoppingItem.setPrice(
          this.myPriceView.getText().length() > 0
              ? Float.parseFloat(this.myPriceView.getText().toString())
              : null);
      return this.myShoppingItem;
    }
  }
}
