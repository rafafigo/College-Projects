package pt.ulisboa.tecnico.muc.shopist.ui.common.pickitem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;

/** {@link RecyclerView.Adapter} that can display a {@link Item}. */
public class MyPickItemAdapter extends RecyclerView.Adapter<MyPickItemAdapter.ViewHolder> {

  private final String pantryId;
  private final String shoppingId;
  private final PickItemFragment fragment;
  private List<Item> items;

  protected MyPickItemAdapter(PickItemFragment fragment, String pantryId, String shoppingId) {
    this.fragment = fragment;
    this.pantryId = pantryId;
    this.shoppingId = shoppingId;
    this.items = new ArrayList<>();
  }

  protected void setItems(List<Item> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_pickitem, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.onBindItem(this.items.get(position));
    holder.view.setOnClickListener(v -> this.onAddItem(v, holder));
  }

  public void onAddItem(View view, ViewHolder holder) {
    Navigation.findNavController(view)
        .navigate(
            R.id.action_pickitem_to_itemform,
            this.shoppingId != null
                ? ItemFormFragment.newShoppingCreateBundle(
                    this.pantryId, this.shoppingId, holder.myItem.getId())
                : ItemFormFragment.newPantryCreateBundle(this.pantryId, holder.myItem.getId()));
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private final ImageView myPictureView;
    private final TextView myNameView;
    private final TextView myBarcodeView;
    private final View view;
    private Item myItem;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      this.myPictureView = view.findViewById(R.id.pickitem_picture);
      this.myNameView = view.findViewById(R.id.pickitem_name);
      this.myBarcodeView = view.findViewById(R.id.pickitem_barcode);
    }

    private void onBindItem(Item item) {
      this.myItem = item;

      Picture picture = item.getPicture();
      if (picture != null) {
        GlideApp.with(fragment)
            .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
            .into(this.myPictureView);
      } else {
        this.myPictureView.setVisibility(View.GONE);
      }

      this.myNameView.setText(item.getName());
      if (item.getBarcode() != null) {
        this.myBarcodeView.setText(String.valueOf(item.getBarcode()));
      }
    }
  }
}
