package pt.ulisboa.tecnico.muc.shopist.ui.cart;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class CartAdapter extends RecyclerSwipeAdapter<CartAdapter.CartViewHolder> {

  private final CartFragment fragment;
  private final SwipeItemRecyclerMangerImpl swipeManager;
  private final CommonViewModel commonViewModel;
  private final DeleteCallback dataSetChangedCallback;
  private List<Item> items;

  public CartAdapter(
      CartFragment fragment,
      List<Item> items,
      CommonViewModel commonViewModel,
      DeleteCallback dataSetChangedCallback) {
    this.fragment = fragment;
    this.swipeManager = new SwipeItemRecyclerMangerImpl(this);
    this.dataSetChangedCallback = dataSetChangedCallback;
    this.items = items;
    this.commonViewModel = commonViewModel;
  }

  public void updateItems(List<Item> items) {
    this.items = items;
    notifyDataSetChanged();
    this.dataSetChangedCallback.onDataSetChanged();
  }

  public void updateItem(int position) {
    notifyItemChanged(position);
  }

  public void removeItem(int position) {
    this.items.remove(position);
    notifyItemRemoved(position);
  }

  @NonNull
  @Override
  public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CartViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_cart_row, parent, false));
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
    Item cartItem = this.items.get(position);
    holder.itemName.setText(cartItem.getName());
    holder.itemPrice.setText(
        String.format("%.2f €", this.commonViewModel.getTotalItemPrice(cartItem.getId())));
    holder.quantityInCart.setText(String.format("In Cart: %d", cartItem.getTotalInCart()));
    holder.itemRemoveAll.setOnClickListener(v -> holder.removeAllFromCart(cartItem));
    holder.itemRemove.setOnClickListener(v -> holder.removeFromCart(cartItem));

    Picture picture = cartItem.getPicture();
    if (picture != null) {
      GlideApp.with(this.fragment)
          .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
          .into(holder.itemImage);
    }

    holder.swipeLayout.addDrag(
        SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.cart_row_swipe_left));
    swipeManager.bindView(holder.swipeLayout, position);
  }

  public void clearItems() {
    this.items = new ArrayList<>();
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.cart_row_swipe_layout;
  }

  protected interface DeleteCallback {
    void onDataSetChanged();
  }

  public class CartViewHolder extends RecyclerView.ViewHolder {

    private final SwipeLayout swipeLayout;

    private final ImageView itemImage;
    private final TextView itemName;
    private final TextView itemPrice;
    private final TextView quantityInCart;
    private final FloatingActionButton itemRemove;
    private final ImageView itemRemoveAll;

    public CartViewHolder(@NonNull View itemView) {
      super(itemView);

      this.swipeLayout = itemView.findViewById(R.id.cart_row_swipe_layout);

      this.itemImage = itemView.findViewById(R.id.cart_row_item_image);
      this.itemName = itemView.findViewById(R.id.cart_row_item_name);
      this.itemPrice = itemView.findViewById(R.id.cart_row_item_price);
      this.quantityInCart = itemView.findViewById(R.id.cart_row_item_quantity);
      this.itemRemove = itemView.findViewById(R.id.cart_row_item_remove);
      this.itemRemoveAll = itemView.findViewById(R.id.cart_row_remove_all);
    }

    private void removeAllFromCart(Item item) {
      new Thread(() -> removeAllFromCartThread(item)).start();
    }

    private void removeAllFromCartThread(Item item) {
      int success = commonViewModel.removeAllFromCart(item.getId());
      fragment.requireActivity().runOnUiThread(() -> updateLayoutOnRemoveAll(success));
    }

    private void updateLayoutOnRemoveAll(int success) {
      if (success == 0)
        Toast.makeText(fragment.getContext(), "Error while updating cart!", Toast.LENGTH_LONG)
            .show();
      else {
        removeItem(getLayoutPosition());
        dataSetChangedCallback.onDataSetChanged();
        this.swipeLayout.close();
      }
    }

    @SuppressLint("InflateParams")
    private void removeFromCart(Item item) {
      LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
      View dialogView = inflater.inflate(R.layout.fragment_cart_pantries, null);
      RecyclerView myRecyclerView = dialogView.findViewById(R.id.cart_pantries_recycler);
      myRecyclerView.setAdapter(
          new CartPantriesAdapter(commonViewModel.getPantryMap(), item.getInCartPantryItems()));
      myRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.getActivity()));
      Dialog dialog = new Dialog(fragment.getActivity());
      dialog.setContentView(dialogView);
      dialog.findViewById(R.id.cart_pantries_cancel).setOnClickListener((v) -> onCancel(dialog));
      dialog
          .findViewById(R.id.cart_pantries_save)
          .setOnClickListener((v) -> onSave(dialog, myRecyclerView, item));
      dialog.show();
      dialog
          .getWindow()
          .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void onSave(DialogInterface onSave, RecyclerView recyclerView, Item item) {
      Map<String, Integer> inCartPantries = new HashMap<>();
      for (int i = 0; i < recyclerView.getChildCount(); i++) {
        CartPantriesAdapter.CartPantriesViewHolder viewHolder =
            (CartPantriesAdapter.CartPantriesViewHolder)
                recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
        inCartPantries.put(
            viewHolder.myItemView.getId(), viewHolder.myNumberPickerView.getProgress());
      }
      new Thread(
              () -> {
                int success = commonViewModel.removeFromCart(item.getId(), inCartPantries);
                fragment
                    .requireActivity()
                    .runOnUiThread(() -> this.updateLayoutOnRemove(success, onSave, item));
              })
          .start();
    }

    public void updateLayoutOnRemove(int success, DialogInterface onSave, Item item) {
      if (success == 0)
        Toast.makeText(fragment.getContext(), "Error while updating cart!", Toast.LENGTH_LONG)
            .show();
      else {
        if (item.getTotalInCart() > 0) {
          updateItem(getLayoutPosition());
        } else {
          removeItem(getLayoutPosition());
        }
        dataSetChangedCallback.onDataSetChanged();
        onSave.dismiss();
      }
    }

    public void onCancel(DialogInterface onCancel) {
      onCancel.dismiss();
    }
  }
}
