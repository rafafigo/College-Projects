package pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Area;
import pt.ulisboa.tecnico.muc.shopist.domain.Cart;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.services.ShareService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglist.ShoppingListFragment;

import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.EDIT_SHOP;

/** {@link RecyclerView.Adapter} that can display a {@link Shopping}. */
public class MyShoppingListsItemAdapter
    extends RecyclerSwipeAdapter<MyShoppingListsItemAdapter.ViewHolder> {

  private final SwipeItemRecyclerMangerImpl myItemManger;
  private final ShoppingListsFragment fragment;
  private List<Shopping> shoppings;

  public MyShoppingListsItemAdapter(List<Shopping> shops, ShoppingListsFragment fragment) {
    this.shoppings = shops;
    this.fragment = fragment;
    this.myItemManger = new SwipeItemRecyclerMangerImpl(this);
  }

  public void updateShoppings(List<Shopping> shoppings) {
    this.shoppings = shoppings;
    notifyDataSetChanged();
  }

  public void updateItem(Shopping shopping, int position) {
    this.shoppings.set(position, shopping);
    Collections.sort(this.shoppings);
    notifyDataSetChanged();
  }

  public void addItem(Shopping shopping) {
    this.shoppings.add(0, shopping);
    notifyDataSetChanged();
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_shopping_lists_item, parent, false);
    return new ViewHolder(view);
  }

  public void canGoToShoppingList(View view, String shoppingId) {
    new Thread(
            () -> {
              if (fragment.getCommonViewModel().canGoToCart(shoppingId)) {
                fragment.requireActivity().runOnUiThread(() -> goToShoppingList(view, shoppingId));
              } else {
                fragment.requireActivity().runOnUiThread(() -> ongoingPurchaseDialog(view));
              }
            })
        .start();
  }

  private void ongoingPurchaseDialog(View view) {
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            Navigation.findNavController(view).navigate(R.id.action_shopping_lists_to_cart);
          }
        };
    new AlertDialog.Builder(view.getContext())
        .setMessage("You already have an ongoing purchase!")
        .setPositiveButton("Goto Cart", dialogClickListener)
        .setNegativeButton("Cancel", dialogClickListener)
        .show();
  }

  public void goToShoppingList(View view, String shoppingId) {
    Navigation.findNavController(view)
        .navigate(
            R.id.action_shopping_lists_to_shopping_list,
            ShoppingListFragment.newBundle(shoppingId));
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.myItem = this.shoppings.get(position);
    holder.myIdView.setText(String.format("%d", position + 1));
    holder.myNameView.setText(holder.myItem.getName());
    holder.myLocationView.setText(holder.myItem.getLocation().getProvider());
    holder.myAvailItemsView.setText(
        String.format(
            "%d Available Items",
            this.fragment
                .getCommonViewModel()
                .countShoppingItems(holder.myItem.getCrowdShoppingId())));

    Observer<Long> myObserver =
        duration -> holder.myDistanceView.setText(Area.formatTime(duration));
    holder.myItem.getDuration().removeObservers(this.fragment.getViewLifecycleOwner());
    holder.myItem.getDuration().observe(this.fragment.getViewLifecycleOwner(), myObserver);

    holder.myAwaitTimeView.setText(holder.myItem.getQueueTime());
    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Left,
        holder.mySwipeLayout.findViewById(R.id.shopping_lists_item_swipe_l));

    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Right,
        holder.mySwipeLayout.findViewById(R.id.shopping_lists_item_swipe_r));

    holder.myEditView.setOnClickListener(v -> onEdit(holder.myItem, holder.getLayoutPosition()));
    holder.myDeleteView.setOnClickListener(
        v -> this.onDelete(v, holder.myItem.getId(), holder.getLayoutPosition()));
    holder.myShareView.setOnClickListener(
        v -> {
          ProgressDialog pDialog =
              ProgressDialog.show(
                  fragment.getContext(), "Generating QR Code", "Please Wait!", true, false);
          new Thread(() -> onShare(holder.myItem, pDialog)).start();
        });
    myItemManger.bindView(holder.mySwipeLayout, position);
    holder
        .myView
        .findViewById(R.id.shopping_list_item_main)
        .setOnClickListener(v -> this.canGoToShoppingList(v, holder.myItem.getId()));
  }

  private void onEdit(Shopping shop, int position) {
    ListsFormDialogFragment listsFormDialog =
        ListsFormDialogFragment.newEditInstance(EDIT_SHOP, shop.getId(), position);
    listsFormDialog.setTargetFragment(fragment, ShoppingListsFragment.REQUEST_CODE);
    listsFormDialog.show(
        fragment.getParentFragmentManager(), ListsFormDialogFragment.class.getSimpleName());
  }

  private void onDelete(View view, String shoppingId, int position) {
    Cart cart = CommonViewModel.getInstance().getCart();
    if (cart.hasCart() && shoppingId.equals(cart.getShoppingId())) {
      Toast.makeText(
              this.fragment.getContext(), "The Shopping has Items in Cart!", Toast.LENGTH_LONG)
          .show();
      return;
    }
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            onPositiveDelete(this.fragment.getContext(), shoppingId, position);
          }
        };
    AlertDialog.Builder ab = new AlertDialog.Builder(view.getContext());
    ab.setMessage("Are you sure to delete?")
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener)
        .show();
  }

  private void onPositiveDelete(Context context, String shoppingId, int position) {
    ProgressDialog pDialog =
        ProgressDialog.show(context, "Deleting Shopping!", "Please Wait!", true, false);
    new Thread(
            () -> {
              int success = this.fragment.getCommonViewModel().removeShopping(shoppingId);
              fragment
                  .requireActivity()
                  .runOnUiThread(() -> verifyDeletion(context, position, pDialog, success));
            })
        .start();
  }

  private void verifyDeletion(Context context, int position, ProgressDialog pDialog, int success) {
    if (success == 0)
      Toast.makeText(context, "Shopping Deletion Failed!", Toast.LENGTH_SHORT).show();
    this.myItemManger.closeItem(position);
    this.shoppings.remove(position);
    notifyDataSetChanged();
    pDialog.dismiss();
  }

  private void onShare(Shopping shopping, Dialog pDialog) {
    String token = this.fragment.getCommonViewModel().getShoppingToken(shopping.getId());
    Activity activity = this.fragment.requireActivity();
    ShareService.onShareList(activity, pDialog, token);
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.shopping_lists_items;
  }

  @Override
  public int getItemCount() {
    return this.shoppings.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final View myView;
    private final TextView myIdView;
    private final TextView myNameView;
    private final TextView myLocationView;
    private final TextView myAvailItemsView;
    private final TextView myDistanceView;
    private final TextView myAwaitTimeView;
    private final ImageView myEditView;
    private final ImageView myDeleteView;
    private final ImageView myShareView;
    private final SwipeLayout mySwipeLayout;
    private Shopping myItem;

    public ViewHolder(View view) {
      super(view);
      this.myView = view;
      this.myIdView = view.findViewById(R.id.shopping_lists_item_id);
      this.myNameView = view.findViewById(R.id.shopping_lists_item_name);
      this.myLocationView = view.findViewById(R.id.shopping_lists_item_location);
      this.myAvailItemsView = view.findViewById(R.id.shopping_lists_item_nr);
      this.myDistanceView = view.findViewById(R.id.shopping_lists_item_distance);
      this.myAwaitTimeView = view.findViewById(R.id.shopping_lists_item_awaitTime);
      this.myEditView = view.findViewById(R.id.shopping_lists_item_edit);
      this.myDeleteView = view.findViewById(R.id.shopping_lists_item_delete);
      this.myShareView = view.findViewById(R.id.shopping_lists_item_share);
      this.mySwipeLayout = view.findViewById(R.id.shopping_lists_items);
    }
  }
}
