package pt.ulisboa.tecnico.muc.shopist.ui.pantry.items;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.taufiqrahman.reviewratings.BarLabels;
import com.taufiqrahman.reviewratings.RatingReviews;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class PantryItemAdapter
    extends RecyclerView.Adapter<PantryItemAdapter.PantryItemViewHolder> {

  private final PantryItemFragment fragment;
  private final Context context;
  private final String pantryId;
  private final List<Item> items;
  private final CommonViewModel commonViewModel;
  private View pantryItemView;

  public PantryItemAdapter(
      PantryItemFragment fragment,
      String pantryId,
      List<Item> items,
      CommonViewModel commonViewModel) {
    this.fragment = fragment;
    this.context = fragment.getContext();
    this.pantryId = pantryId;
    this.items = items;
    this.commonViewModel = commonViewModel;
  }

  @NonNull
  @Override
  public PantryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    this.pantryItemView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantry_item, parent, false);

    return new PantryItemViewHolder(this.pantryItemView);
  }

  @Override
  public void onBindViewHolder(@NonNull PantryItemViewHolder holder, int position) {
    Item item = this.items.get(position);
    PantryItem pantryItem = item.getPantryItem(this.pantryId);

    PantryItemLocationAdapter pantryItemLocationAdapter =
        new PantryItemLocationAdapter(
            commonViewModel.getItemShoppingItems(item.getId()),
            commonViewModel.getItemLocations(item.getId()));

    RecyclerView recyclerView = pantryItemView.findViewById(R.id.pantry_item_locations_list);
    recyclerView.setAdapter(pantryItemLocationAdapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(pantryItemView.getContext()));

    holder.itemId = item.getId();
    holder.inPantry = pantryItem.getInPantry();
    holder.inNeed = pantryItem.getInNeed();
    holder.itemName.setText(item.getName());
    holder.itemInPantry.setText(String.valueOf(pantryItem.getInPantry()));
    holder.itemInNeed.setText(String.valueOf(pantryItem.getInNeed()));
    holder.itemInCart.setText(String.valueOf(pantryItem.getInCart()));

    Picture picture = item.getPicture();
    if (picture != null) {
      GlideApp.with(this.fragment)
          .asBitmap()
          .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
          .into(this.getCustomTarget(holder));
    } else {
      holder.itemImage.setVisibility(View.GONE);
    }
    holder.itemImage.setOnClickListener(holder::gotoItemImages);

    bindNavButtons(holder, position);
    bindQuantityButtons(holder, pantryItem);
    bindRatingBar(holder, item.hasBarcode());
  }

  @NotNull
  private CustomTarget<Bitmap> getCustomTarget(@NonNull PantryItemViewHolder holder) {
    return new CustomTarget<Bitmap>() {
      @Override
      public void onResourceReady(
          @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        holder.itemImage.setBackground(new BitmapDrawable(context.getResources(), resource));
      }

      @Override
      public void onLoadCleared(@Nullable Drawable placeholder) {}
    };
  }

  private void bindNavButtons(@NonNull PantryItemViewHolder holder, int position) {

    holder.nextItemButton.setOnClickListener(v -> fragment.nextItem());
    holder.previousItemButton.setOnClickListener(v -> fragment.previousItem());

    if (position == 0) {
      holder.turnOffButton(holder.previousItemButton);
    } else {
      holder.turnOnButton(holder.previousItemButton);
    }

    if (position == getItemCount() - 1) {
      holder.turnOffButton(holder.nextItemButton);
    } else {
      holder.turnOnButton(holder.nextItemButton);
    }
  }

  private void bindQuantityButtons(@NonNull PantryItemViewHolder holder, PantryItem item) {

    holder.addInPantry.setOnClickListener(v -> holder.addItemInPantry());
    holder.removeInPantry.setOnClickListener(v -> holder.removeItemInPantry());
    holder.addInNeed.setOnClickListener(v -> holder.addItemInNeed());
    holder.removeInNeed.setOnClickListener(v -> holder.removeItemInNeed());

    if (item.getInPantry() == 0) {
      holder.turnOffButton(holder.removeInPantry);
    } else if (item.getInNeed() == 0) {
      holder.turnOffButton(holder.removeInNeed);
    }
  }

  @SuppressLint("DefaultLocale")
  private void bindRatingBar(@NonNull PantryItemViewHolder holder, boolean showRating) {
    if (showRating) {
      holder.ratingHeader.setOnClickListener(holder::gotoItemRatingsDialog);
      holder.itemRating.setRating(Math.round(holder.getAverageRating()));
      holder.ratingHeader.setVisibility(View.VISIBLE);
      holder.itemNumericRating.setText(String.format("%.1f", holder.getAverageRating()));
    } else {
      holder.ratingHeader.setVisibility(View.GONE);
    }
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  private interface OnSaveCallback {
    void onSave();
  }

  public class PantryItemViewHolder extends RecyclerView.ViewHolder {

    private final int onButtonColor;
    private final int offButtonColor;
    private final TextView itemName;
    private final TextView itemInPantry;
    private final TextView itemInNeed;
    private final TextView itemInCart;
    private final ImageView itemImage;
    private final RatingBar itemRating;
    private final TextView itemNumericRating;
    private final FloatingActionButton previousItemButton;
    private final FloatingActionButton nextItemButton;
    private final FloatingActionButton addInPantry;
    private final FloatingActionButton removeInPantry;
    private final FloatingActionButton addInNeed;
    private final FloatingActionButton removeInNeed;
    private final ConstraintLayout ratingHeader;
    private String itemId;
    private Integer inNeed;
    private Integer inPantry;

    public PantryItemViewHolder(@NonNull View itemView) {
      super(itemView);

      this.onButtonColor = itemView.getResources().getColor(R.color.blue_ist);
      this.offButtonColor = itemView.getResources().getColor(R.color.off_gray);

      this.itemName = itemView.findViewById(R.id.pantry_item_name);
      this.itemInPantry = itemView.findViewById(R.id.pantry_item_in_pantry_value);
      this.itemInNeed = itemView.findViewById(R.id.pantry_item_in_need_value);
      this.itemInCart = itemView.findViewById(R.id.pantry_item_in_cart_value);
      this.itemImage = itemView.findViewById(R.id.pantry_item_image);

      this.previousItemButton = itemView.findViewById(R.id.pantry_item_next_left);
      this.nextItemButton = itemView.findViewById(R.id.pantry_item_next_right);
      this.addInPantry = itemView.findViewById(R.id.pantry_item_in_pantry_add);
      this.removeInPantry = itemView.findViewById(R.id.pantry_item_in_pantry_subtract);
      this.addInNeed = itemView.findViewById(R.id.pantry_item_in_need_add);
      this.removeInNeed = itemView.findViewById(R.id.pantry_item_in_need_subtract);
      this.ratingHeader = itemView.findViewById(R.id.pantry_item_rating_header);
      this.itemRating = itemView.findViewById(R.id.pantry_item_average_rating);
      this.itemNumericRating = itemView.findViewById(R.id.pantry_item_average_rating_numeric);
    }

    public void addItemInPantry() {
      if (inPantry == 0) turnOnButton(this.removeInPantry);
      inPantry++;
      new Thread(() -> this.onSave(() -> this.itemInPantry.setText(String.valueOf(inPantry))))
          .start();
    }

    public void removeItemInPantry() {
      inPantry--;
      if (inPantry == 0) turnOffButton(this.removeInPantry);
      new Thread(() -> this.onSave(() -> this.itemInPantry.setText(String.valueOf(inPantry))))
          .start();
    }

    public void addItemInNeed() {
      if (inNeed == 0) turnOnButton(this.removeInNeed);
      inNeed++;
      new Thread(() -> this.onSave(() -> this.itemInNeed.setText(String.valueOf(inNeed)))).start();
    }

    public void removeItemInNeed() {
      inNeed--;
      if (inNeed == 0) turnOffButton(this.removeInNeed);
      new Thread(() -> this.onSave(() -> this.itemInNeed.setText(String.valueOf(inNeed)))).start();
    }

    public void turnOnButton(FloatingActionButton button) {
      button.setBackgroundTintList(ColorStateList.valueOf(onButtonColor));
      button.setEnabled(true);
    }

    public void turnOffButton(FloatingActionButton button) {
      button.setBackgroundTintList(ColorStateList.valueOf(offButtonColor));
      button.setEnabled(false);
    }

    public void gotoItemImages(View view) {
      Navigation.findNavController(view)
          .navigate(
              R.id.action_pantry_item_to_item_image,
              PantryItemImagesFragment.newBundle(this.itemId));
    }

    @SuppressLint("InflateParams")
    public void gotoItemRatingsDialog(View view) {
      View dialogRatingsView =
          LayoutInflater.from(fragment.getActivity())
              .inflate(R.layout.fragment_pantry_item_ratings, null);

      Dialog ratingsDialog = new Dialog(fragment.getActivity());
      ratingsDialog.setContentView(dialogRatingsView);

      ratingsDialog
          .findViewById(R.id.pantry_item_ratings_dialog_back)
          .setOnClickListener((v) -> onRatingDialogBack(ratingsDialog));

      this.makeRatingDialogHistogram(dialogRatingsView);

      ratingsDialog.show();
      ratingsDialog
          .getWindow()
          .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void onRatingDialogBack(DialogInterface onBack) {
      onBack.dismiss();
    }

    public void makeRatingDialogHistogram(View dialogRatingsView) {
      RatingReviews ratingReviews =
          dialogRatingsView.findViewById(R.id.pantry_item_ratings_dialog_histogram);
      RatingBar ratingBar =
          dialogRatingsView.findViewById(R.id.pantry_item_ratings_dialog_user_rating);

      int[] colors =
          new int[] {
            Color.parseColor("#0e9d58"),
            Color.parseColor("#bfd047"),
            Color.parseColor("#ffc105"),
            Color.parseColor("#ef7e14"),
            Color.parseColor("#d36259")
          };
      List<Integer> ratings = commonViewModel.getItemRatings(this.itemId);

      int max = 1;
      int[] raters = new int[5];
      for (int i = 0; i < 5; i++) {
        raters[i] = ratings.get(4 - i);
        if (max < raters[i]) {
          max = raters[i];
        }
      }

      ratingReviews.createRatingBars(max, BarLabels.STYPE1, colors, raters);

      new Thread(() -> ratingBar.setRating(commonViewModel.getItemUserRating(itemId))).start();
      ratingBar.setOnRatingBarChangeListener(
          (ratingBar1, rating, fromUser) ->
              new Thread(
                      () ->
                          onUpdateRatings(
                              commonViewModel.updateItemRatings(itemId, Math.round(rating))))
                  .start());
    }

    @SuppressLint("DefaultLocale")
    public void onUpdateRatings(boolean success) {
      fragment
          .requireActivity()
          .runOnUiThread(
              () -> {
                if (success) {
                  itemRating.setRating(Math.round(getAverageRating()));
                  itemNumericRating.setText(String.format("%.1f", getAverageRating()));
                } else {
                  Toast.makeText(
                          fragment.getContext(), "Error Updating Histogram!", Toast.LENGTH_LONG)
                      .show();
                }
              });
    }

    public float getAverageRating() {
      return commonViewModel.getItemRatingsAverage(this.itemId);
    }

    public void onSave(OnSaveCallback callback) {
      int success =
          commonViewModel.updatePantryItem(pantryId, this.itemId, this.inPantry, this.inNeed);

      fragment
          .requireActivity()
          .runOnUiThread(
              () -> {
                if (success == 0) {
                  Toast.makeText(fragment.getContext(), "Item Edition Failed!", Toast.LENGTH_LONG)
                      .show();
                } else callback.onSave();
              });
    }
  }
}
