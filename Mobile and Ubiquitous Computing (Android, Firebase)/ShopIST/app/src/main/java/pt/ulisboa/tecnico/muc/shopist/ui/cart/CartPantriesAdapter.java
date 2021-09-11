package pt.ulisboa.tecnico.muc.shopist.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.sephiroth.android.library.numberpicker.NumberPicker;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;

public class CartPantriesAdapter
    extends RecyclerView.Adapter<CartPantriesAdapter.CartPantriesViewHolder> {

  private final Map<String, Pantry> pantries;
  private final List<PantryItem> pantryItems;

  public CartPantriesAdapter(Map<String, Pantry> pantries, List<PantryItem> pantryItems) {
    this.pantries = pantries;
    this.pantryItems = pantryItems;
  }

  @Override
  public @NotNull CartPantriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new CartPantriesAdapter.CartPantriesViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_dialog_pantries_item, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull CartPantriesViewHolder holder, int position) {
    holder.myItemView = this.pantryItems.get(position);
    int maxValue = holder.myItemView.getInCart();
    holder.myPantryView.setText(
        formatMyPantryView(Objects.requireNonNull(this.pantries.get(holder.myItemView.getId()))));
    holder.myMaxView.setText(formatMyMaxView(maxValue));
    holder.myNumberPickerView.setMaxValue(maxValue);
    holder.myNumberPickerView.setProgress(maxValue);
    holder.myNumberPickerView.setMinValue(0);
  }

  private String formatMyMaxView(int max) {
    return String.format("Max: %s", max);
  }

  private String formatMyPantryView(Pantry pantry) {
    return String.format("%s", pantry.getName());
  }

  @Override
  public int getItemCount() {
    return this.pantryItems.size();
  }

  public static class CartPantriesViewHolder extends RecyclerView.ViewHolder {
    public final TextView myPantryView;
    public final TextView myMaxView;
    public final NumberPicker myNumberPickerView;
    public PantryItem myItemView;

    public CartPantriesViewHolder(View view) {
      super(view);
      this.myPantryView = view.findViewById(R.id.dialog_pantries_item_pantry);
      this.myNumberPickerView = view.findViewById(R.id.dialog_pantries_item_numberPicker);
      this.myMaxView = view.findViewById(R.id.dialog_pantries_item_max);
    }
  }
}
