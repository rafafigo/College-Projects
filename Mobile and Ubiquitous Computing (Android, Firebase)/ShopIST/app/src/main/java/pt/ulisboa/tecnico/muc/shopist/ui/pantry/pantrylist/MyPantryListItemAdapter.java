package pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrylist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.services.DomainService;
import pt.ulisboa.tecnico.muc.shopist.services.ShareService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.pantry.items.PantryItemFragment;

/** {@link RecyclerSwipeAdapter} that can display a {@link Item}. */
public class MyPantryListItemAdapter
    extends RecyclerSwipeAdapter<MyPantryListItemAdapter.ViewHolder> {

  private final PantryListFragment fragment;
  private final CommonViewModel commonViewModel;
  private final String pantryId;
  private final SwipeItemRecyclerMangerImpl myItemManger;
  private final DeleteCallback deleteCallback;
  private List<Item> items;

  public MyPantryListItemAdapter(
      PantryListFragment fragment,
      String pantryId,
      CommonViewModel commonViewModel,
      List<Item> items,
      DeleteCallback deleteCallback) {
    this.fragment = fragment;
    this.pantryId = pantryId;
    this.commonViewModel = commonViewModel;
    this.items = items;
    this.deleteCallback = deleteCallback;
    this.myItemManger = new SwipeItemRecyclerMangerImpl(this);
  }

  public void updateItems(List<Item> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantry_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.onBindPantryListItem(this.items.get(position));
    holder.myEditFab.setOnClickListener((v) -> this.onEdit(v, holder));
    holder.myDeleteFab.setOnClickListener((v) -> this.onDelete(v, holder));
    holder.myDeductFab.setOnClickListener((v) -> this.onDeduct(v, holder));
    holder.myShareFab.setOnClickListener(
        (v) -> new Thread(() -> ShareService.onShareItem(this.fragment, holder.myItem)).start());
    holder
        .myView
        .findViewById(R.id.pantry_list_item_main)
        .setOnClickListener(v -> goToPantryItem(v, position));
    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Left,
        holder.mySwipeLayout.findViewById(R.id.pantry_list_item_swipe_l));

    holder.mySwipeLayout.addDrag(
        SwipeLayout.DragEdge.Right,
        holder.mySwipeLayout.findViewById(R.id.pantry_list_item_swipe_r));

    this.myItemManger.bindView(holder.mySwipeLayout, position);
  }

  public void goToPantryItem(View v, int position) {
    Navigation.findNavController(v)
        .navigate(
            R.id.action_pantry_list_to_pantry_item,
            PantryItemFragment.newBundle(this.pantryId, position));
  }

  public void onEdit(View view, final ViewHolder holder) {
    Navigation.findNavController(view)
        .navigate(
            R.id.action_pantry_list_to_itemform,
            ItemFormFragment.newPantryEditBundle(this.pantryId, holder.myItem.getId()));
  }

  public void onDelete(View view, final ViewHolder holder) {
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            int position = holder.getLayoutPosition();
            if (this.items.remove(position) != null) {
              this.deletePantryItem(holder.myItem.getId(), position);
            }
          }
        };
    new AlertDialog.Builder(view.getContext())
        .setMessage("Are you sure you want to delete?")
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener)
        .show();
  }

  @SuppressLint("DefaultLocale")
  public void onDeduct(View view, final ViewHolder holder) {
    PantryItem pantryItem = holder.myItem.getPantryItem(pantryId);

    int newInPantry = pantryItem.getInPantry() - 1;
    int newInNeed = pantryItem.getInNeed() + 1;

    new Thread(
            () -> {
              int success =
                  this.commonViewModel.updatePantryItem(
                      this.pantryId, holder.myItem.getId(), newInPantry, newInNeed);

              fragment
                  .requireActivity()
                  .runOnUiThread(
                      () -> {
                        if (success == 1) {
                          holder.onBindPantryListItemInPantryAndInNeed(newInPantry, newInNeed);
                        } else {
                          Toast.makeText(
                                  fragment.getContext(),
                                  "Item Deduction Failed!",
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                        if (!holder.myItem.hasPrompted()) {
                          DomainService.onPrompt(
                              view.getContext(), holder.myItem, () -> this.onEdit(view, holder));
                        }
                      });
            })
        .start();
  }

  private void deletePantryItem(String itemId, int position) {
    ProgressDialog pDialog =
        ProgressDialog.show(fragment.getContext(), "Fetching Users!", "Please Wait!", true, false);
    new Thread(
            () -> {
              boolean success = this.commonViewModel.deletePantryItem(this.pantryId, itemId);
              fragment
                  .requireActivity()
                  .runOnUiThread(
                      () -> {
                        if (success) {
                          notifyItemRemoved(position);
                          this.deleteCallback.onDelete(items);
                        } else {
                          Toast.makeText(
                                  fragment.getContext(),
                                  "Item Deduction Failed!",
                                  Toast.LENGTH_LONG)
                              .show();
                        }
                        pDialog.dismiss();
                      });
            })
        .start();
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.pantry_list_item;
  }

  protected interface DeleteCallback {
    void onDelete(List<Item> itemList);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    private final int onButtonColor;
    private final int offButtonColor;
    private final View myView;
    private final SwipeLayout mySwipeLayout;
    private final TextView myName;
    private final TextView myInPantry;
    private final TextView myInNeed;
    private final TextView myInCart;
    private final ImageView myPicture;
    private final ImageView myEditFab;
    private final ImageView myDeleteFab;
    private final ImageView myShareFab;
    private final FloatingActionButton myDeductFab;
    private Item myItem;

    public ViewHolder(View view) {
      super(view);
      this.myView = view;
      this.mySwipeLayout = view.findViewById(R.id.pantry_list_item);
      this.myName = view.findViewById(R.id.pantry_list_item_name);
      this.myInPantry = view.findViewById(R.id.pantry_list_item_inpantry);
      this.myInNeed = view.findViewById(R.id.pantry_list_item_inneed);
      this.myInCart = view.findViewById(R.id.pantry_list_item_incart);
      this.myPicture = view.findViewById(R.id.pantry_list_item_picture);
      this.myEditFab = view.findViewById(R.id.pantry_list_item_edit);
      this.myDeleteFab = view.findViewById(R.id.pantry_list_item_delete);
      this.myDeductFab = view.findViewById(R.id.pantry_list_item_deduct);
      this.myShareFab = view.findViewById(R.id.pantry_lists_item_share);
      this.onButtonColor = itemView.getResources().getColor(R.color.blue_ist);
      this.offButtonColor = itemView.getResources().getColor(R.color.off_gray);
    }

    @SuppressLint("DefaultLocale")
    private void onBindPantryListItem(Item item) {
      this.myItem = item;
      this.myName.setText(item.getName());

      Picture picture = item.getPicture();
      if (picture != null) {
        GlideApp.with(fragment)
            .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
            .into(this.myPicture);
      }

      PantryItem pantryItem = item.getPantryItem(pantryId);
      onBindPantryListItemInPantryAndInNeed(pantryItem.getInPantry(), pantryItem.getInNeed());
      this.myInCart.setText(String.format("In Cart: %d", pantryItem.getInCart()));
    }

    @SuppressLint("DefaultLocale")
    private void onBindPantryListItemInPantryAndInNeed(int inPantry, int inNeed) {
      if (inPantry == 0) {
        turnOffDeductButton();
      } else {
        turnOnDeductButton();
      }

      this.myInPantry.setText(String.format("In Pantry: %d", inPantry));
      this.myInNeed.setText(String.format("In Need: %d", inNeed));
    }

    public void turnOnDeductButton() {
      this.myDeductFab.setBackgroundTintList(ColorStateList.valueOf(onButtonColor));
      this.myDeductFab.setEnabled(true);
    }

    public void turnOffDeductButton() {
      this.myDeductFab.setBackgroundTintList(ColorStateList.valueOf(offButtonColor));
      this.myDeductFab.setEnabled(false);
    }
  }
}
