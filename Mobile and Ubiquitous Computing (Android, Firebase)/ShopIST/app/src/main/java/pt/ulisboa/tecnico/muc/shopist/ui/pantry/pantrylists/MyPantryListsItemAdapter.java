package pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrylists;

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
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.services.ShareService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrylist.PantryListFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrymanage.PantryManageFragment;

import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.EDIT_PANTRY;

/** {@link RecyclerView.Adapter} that can display a {@link Pantry}. */
public class MyPantryListsItemAdapter
    extends RecyclerSwipeAdapter<MyPantryListsItemAdapter.ViewHolder> {

  private final SwipeItemRecyclerMangerImpl myItemManger;
  private final PantryListsFragment fragment;
  private List<Pantry> pantries;

  public MyPantryListsItemAdapter(List<Pantry> pantries, PantryListsFragment fragment) {
    this.pantries = pantries;
    this.fragment = fragment;
    this.myItemManger = new SwipeItemRecyclerMangerImpl(this);
  }

  public void updatePantries(List<Pantry> pantries) {
    this.pantries = pantries;
    notifyDataSetChanged();
  }

  public void updateItem(Pantry pantry, int position) {
    this.pantries.set(position, pantry);
    Collections.sort(this.pantries);
    notifyDataSetChanged();
  }

  public void addItem(Pantry pantry) {
    this.pantries.add(0, pantry);
    notifyDataSetChanged();
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantry_lists_item, parent, false);
    return new ViewHolder(view);
  }

  public void goToPantryList(View v, Pantry pantry) {
    Navigation.findNavController(v)
        .navigate(
            R.id.action_pantry_lists_to_pantry_list, PantryListFragment.newBundle(pantry.getId()));
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.myItem = this.pantries.get(position);
    holder.myIdView.setText(String.format("%d", position + 1));
    holder.myNameView.setText(holder.myItem.getName());
    holder.myLocationView.setText(holder.myItem.getLocation().getProvider());
    holder.myAvailItemsView.setText(
        String.format(
            "%d Available Items",
            this.fragment.getCommonViewModel().countPantryItems(holder.myItem.getId())));

    Observer<Long> myObserver =
        duration -> holder.myDistanceView.setText(Area.formatTime(duration));
    holder.myItem.getDuration().removeObservers(this.fragment.getViewLifecycleOwner());
    holder.myItem.getDuration().observe(this.fragment.getViewLifecycleOwner(), myObserver);

    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Left,
        holder.mySwipeLayout.findViewById(R.id.pantry_lists_item_swipe_l));

    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Right,
        holder.mySwipeLayout.findViewById(R.id.pantry_lists_item_swipe_r));

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
    holder.myManageView.setOnClickListener(v -> this.onManage(v, holder));
    myItemManger.bindView(holder.mySwipeLayout, position);
    holder
        .myView
        .findViewById(R.id.pantry_lists_item_main)
        .setOnClickListener(v -> this.goToPantryList(v, holder.myItem));
  }

  private void onEdit(Pantry pantry, int position) {
    ListsFormDialogFragment listsFormDialog =
        ListsFormDialogFragment.newEditInstance(EDIT_PANTRY, pantry.getId(), position);
    listsFormDialog.setTargetFragment(fragment, PantryListsFragment.REQUEST_CODE);
    listsFormDialog.show(
        fragment.getParentFragmentManager(), ListsFormDialogFragment.class.getSimpleName());
  }

  private void onDelete(View view, String pantryId, int position) {
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            onPositiveDelete(this.fragment.getContext(), pantryId, position);
          }
        };
    AlertDialog.Builder ab = new AlertDialog.Builder(view.getContext());
    ab.setMessage("Are you sure to delete?")
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener)
        .show();
  }

  private void onPositiveDelete(Context context, String pantryId, int position) {
    ProgressDialog pDialog =
        ProgressDialog.show(context, "Deleting Pantry!", "Please Wait!", true, false);
    new Thread(
            () -> {
              int success = this.fragment.getCommonViewModel().removePantry(pantryId);
              fragment
                  .requireActivity()
                  .runOnUiThread(() -> verifyDeletion(context, position, pDialog, success));
            })
        .start();
  }

  private void verifyDeletion(Context context, int position, ProgressDialog pDialog, int success) {
    if (success == 0) Toast.makeText(context, "Pantry Deletion Failed!", Toast.LENGTH_SHORT).show();
    this.myItemManger.closeItem(position);
    this.pantries.remove(position);
    notifyDataSetChanged();
    pDialog.dismiss();
  }

  private void onShare(Pantry pantry, Dialog pDialog) {
    String token = this.fragment.getCommonViewModel().getPantryToken(pantry.getId());
    Activity activity = this.fragment.requireActivity();
    ShareService.onShareList(activity, pDialog, token);
  }

  private void onManage(View view, ViewHolder holder) {
    Navigation.findNavController(view)
        .navigate(
            R.id.action_pantry_lists_to_pantry_manage,
            PantryManageFragment.newBundle(holder.myItem.getId()));
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.pantry_lists_items;
  }

  @Override
  public int getItemCount() {
    return pantries.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final View myView;
    private final TextView myIdView;
    private final TextView myNameView;
    private final TextView myLocationView;
    private final TextView myAvailItemsView;
    private final TextView myDistanceView;
    private final ImageView myEditView;
    private final ImageView myDeleteView;
    private final ImageView myShareView;
    private final ImageView myManageView;
    private final SwipeLayout mySwipeLayout;
    private Pantry myItem;

    public ViewHolder(View view) {
      super(view);
      this.myView = view;
      this.myIdView = view.findViewById(R.id.pantry_lists_item_id);
      this.myNameView = view.findViewById(R.id.pantry_lists_item_name);
      this.myLocationView = view.findViewById(R.id.pantry_lists_item_location);
      this.myAvailItemsView = view.findViewById(R.id.pantry_lists_item_nr);
      this.myDistanceView = view.findViewById(R.id.pantry_lists_item_distance);
      this.myEditView = view.findViewById(R.id.pantry_lists_item_edit);
      this.myDeleteView = view.findViewById(R.id.pantry_lists_item_delete);
      this.myShareView = view.findViewById(R.id.pantry_lists_item_share);
      this.myManageView = view.findViewById(R.id.pantry_lists_item_manage);
      this.mySwipeLayout = view.findViewById(R.id.pantry_lists_items);
    }
  }
}
