package pt.ulisboa.tecnico.muc.shopist.ui.pantry.items;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;

public class PantryItemImagesAdapter
    extends RecyclerView.Adapter<PantryItemImagesAdapter.PantryItemImageViewHolder> {

  private final PantryItemImagesFragment fragment;
  private final List<Picture> pictures;

  public PantryItemImagesAdapter(PantryItemImagesFragment fragment, List<Picture> pictures) {
    this.fragment = fragment;
    this.pictures = pictures;
  }

  @NonNull
  @Override
  public PantryItemImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new PantryItemImagesAdapter.PantryItemImageViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantry_item_image, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull PantryItemImageViewHolder holder, int position) {
    GlideApp.with(this.fragment)
        .load(
            FirebaseStorage.getInstance()
                .getReference()
                .child(pictures.get(position).getPictureUri()))
        .into(holder.itemImage);
  }

  @Override
  public int getItemCount() {
    return pictures.size();
  }

  public static class PantryItemImageViewHolder extends RecyclerView.ViewHolder {

    private final ImageView itemImage;

    public PantryItemImageViewHolder(@NonNull View itemView) {
      super(itemView);
      this.itemImage = itemView.findViewById(R.id.pantry_item_image_fullscreen);
    }
  }
}
