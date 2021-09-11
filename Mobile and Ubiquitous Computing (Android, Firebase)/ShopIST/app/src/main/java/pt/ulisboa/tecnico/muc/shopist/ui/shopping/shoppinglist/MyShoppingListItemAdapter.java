package pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglist;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.services.DomainService;
import pt.ulisboa.tecnico.muc.shopist.services.ShareService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;

/** {@link RecyclerView.Adapter} that can display a {@link Item}. */
public class MyShoppingListItemAdapter
    extends RecyclerSwipeAdapter<MyShoppingListItemAdapter.ViewHolder> {

  private final Shopping shopping;
  private final SwipeItemRecyclerMangerImpl mItemManger;
  private final CommonViewModel commonViewModel;
  private final ShoppingListFragment fragment;
  private List<Item> items;

  public MyShoppingListItemAdapter(
      Shopping shopping,
      List<Item> items,
      CommonViewModel commonViewModel,
      ShoppingListFragment fragment) {
    this.shopping = shopping;
    this.items = items;
    this.commonViewModel = commonViewModel;
    this.fragment = fragment;
    this.mItemManger = new SwipeItemRecyclerMangerImpl(this);
  }

  public void updateItems(List<Item> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  public void updateItem(int position) {
    notifyItemChanged(position);
  }

  public void removeItem(int position) {
    this.items.remove(position);
    notifyItemRemoved(position);
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_shopping_list_item, parent, false);
    return new ViewHolder(view);
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.myItem = this.items.get(position);
    holder.myNameView.setText(holder.myItem.getName());
    holder.myInNeedView.setText(String.format("In Need: %d", holder.myItem.getTotalInNeed()));
    holder.myPriceView.setText(
        String.format("%.2f €", holder.myItem.getPrice(this.shopping.getCrowdShoppingId())));
    holder.myShareView.setOnClickListener(
        (v) -> new Thread(() -> ShareService.onShareItem(this.fragment, holder.myItem)).start());
    holder.myAddAllToCart.setOnClickListener(v -> holder.addAllToCart());
    holder.myActionBtnView.setOnClickListener(v -> holder.addToCart());

    Picture picture = holder.myItem.getPicture();
    if (picture != null) {
      GlideApp.with(fragment)
          .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
          .into(holder.myImgView);
    }

    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Left,
        holder.mySwipeLayout.findViewById(R.id.shopping_list_item_swipe_l));
    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Right,
        holder.mySwipeLayout.findViewById(R.id.shopping_list_item_swipe_r));
    mItemManger.bindView(holder.mySwipeLayout, position);
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.shopping_list_items;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView myNameView;
    private final TextView myInNeedView;
    private final TextView myPriceView;
    private final ImageView myImgView;
    private final SwipeLayout mySwipeLayout;
    private final ImageView myAddAllToCart;
    private final ImageView myShareView;
    private final FloatingActionButton myActionBtnView;
    private Item myItem;

    public ViewHolder(View view) {
      super(view);
      this.myNameView = view.findViewById(R.id.shopping_list_item_name);
      this.myInNeedView = view.findViewById(R.id.shopping_list_item_in_need);
      this.myPriceView = view.findViewById(R.id.shopping_list_item_price);
      this.myImgView = view.findViewById(R.id.shopping_list_item_img);
      this.myAddAllToCart = view.findViewById(R.id.shopping_list_item_all_cart);
      this.myShareView = view.findViewById(R.id.shopping_lists_item_share);
      this.myActionBtnView = view.findViewById((R.id.shopping_list_item_action));
      ImageView myEditView = view.findViewById(R.id.shopping_list_item_edit);
      this.mySwipeLayout = view.findViewById(R.id.shopping_list_items);
      myEditView.setOnClickListener(this::onEdit);
    }

    private void onEdit(View view) {
      Map<String, String> associatedPantries = getAssociatedPantries();
      String[] options = associatedPantries.keySet().toArray(new String[0]);
      if (options.length == 1) {
        this.gotoEditItemForm(view, associatedPantries.get(options[0]));
      } else {
        DomainService.choosePantry(
            view.getContext(),
            options,
            choice -> this.gotoEditItemForm(view, associatedPantries.get(options[choice])));
      }
    }

    @NotNull
    public Map<String, String> getAssociatedPantries() {
      Map<String, Pantry> pantries = CommonViewModel.getInstance().getPantryMap();
      Map<String, String> associatedPantries = new HashMap<>();
      for (PantryItem pantryItem : this.myItem.getPantryItems().values()) {
        Pantry pantry = Objects.requireNonNull(pantries.get(pantryItem.getId()));
        associatedPantries.put(
            String.format("%s (%s)", pantry.getName(), pantry.getLocation().getProvider()),
            pantryItem.getId());
      }
      return associatedPantries;
    }

    private void gotoEditItemForm(View view, String pantryId) {
      Navigation.findNavController(view)
          .navigate(
              R.id.action_shopping_list_to_itemform,
              ItemFormFragment.newShoppingEditBundle(
                  pantryId, shopping.getId(), this.myItem.getId()));
    }

    private void addAllToCart() {
      this.updateSmartSort();
      new Thread(this::addAllToCartThread).start();
    }

    private void addAllToCartThread() {
      int success = commonViewModel.addAllToCart(this.myItem.getId());
      fragment.requireActivity().runOnUiThread(() -> updateLayoutOnAddAll(success));
    }

    private void updateLayoutOnAddAll(int success) {
      if (success == 0)
        Toast.makeText(fragment.getContext(), "Error while updating inCart!", Toast.LENGTH_LONG)
            .show();
      else {
        this.mySwipeLayout.close();
        if (!this.myItem.hasPrompted()) {
          DomainService.onPrompt(
              fragment.getContext(), this.myItem, () -> onEdit(fragment.getView()));
        }
        removeItem(getLayoutPosition());
      }
    }

    @SuppressLint("InflateParams")
    private void addToCart() {
      LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
      View dialogView = inflater.inflate(R.layout.fragment_shopping_pantries, null);
      RecyclerView myRecyclerView = dialogView.findViewById(R.id.shopping_pantries_recycler);
      myRecyclerView.setAdapter(
          new MyShoppingPantriesAdapter(
              commonViewModel.getPantryMap(), this.myItem.getInNeedPantryItems()));
      myRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.getActivity()));
      Dialog dialog = new Dialog(fragment.getActivity());
      dialog.setContentView(dialogView);
      dialog
          .findViewById(R.id.shopping_pantries_cancel)
          .setOnClickListener((v) -> onCancel(dialog));
      dialog
          .findViewById(R.id.shopping_pantries_save)
          .setOnClickListener((v) -> onSave(dialog, myRecyclerView));
      dialog.show();
      dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void onSave(DialogInterface onSave, RecyclerView recyclerView) {
      Map<String, Integer> inCartPantries = new HashMap<>();
      for (int i = 0; i < recyclerView.getChildCount(); i++) {
        MyShoppingPantriesAdapter.ViewHolder viewHolder =
            (MyShoppingPantriesAdapter.ViewHolder)
                recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
        inCartPantries.put(
            viewHolder.myItemView.getId(), viewHolder.myNumberPickerView.getProgress());
      }
      new Thread(
              () -> {
                this.updateSmartSort();
                int success = commonViewModel.addToCart(this.myItem.getId(), inCartPantries);
                fragment
                    .requireActivity()
                    .runOnUiThread(
                        () -> {
                          this.updateLayoutOnAdd(success, onSave, this.myItem.getTotalInNeed());
                          if (!this.myItem.hasPrompted()) {
                            DomainService.onPrompt(
                                fragment.getContext(),
                                this.myItem,
                                () -> onEdit(fragment.getView()));
                          }
                        });
              })
          .start();
    }

    public void updateSmartSort() {
      if (this.myItem.getTotalInCart() > 0) return;
      new Thread(
              () ->
                  commonViewModel.updateSmartSort(
                      shopping.getCrowdShoppingId(), this.myItem, new ArrayList<>(items)))
          .start();
    }

    public void updateLayoutOnAdd(int success, DialogInterface onSave, int totalInNeed) {
      if (success == 0)
        Toast.makeText(fragment.getContext(), "Error while updating inCart!", Toast.LENGTH_LONG)
            .show();
      else {
        if (totalInNeed > 0) {
          updateItem(getLayoutPosition());
        } else {
          removeItem(getLayoutPosition());
        }
        onSave.dismiss();
      }
    }

    public void onCancel(DialogInterface onCancel) {
      onCancel.dismiss();
    }
  }
}
