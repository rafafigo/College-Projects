package pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglist;

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

public class MyShoppingPantriesAdapter
    extends RecyclerView.Adapter<MyShoppingPantriesAdapter.ViewHolder> {

  private final Map<String, Pantry> pantries;
  private final List<PantryItem> pantryItems;

  public MyShoppingPantriesAdapter(Map<String, Pantry> pantries, List<PantryItem> pantryItems) {
    this.pantries = pantries;
    this.pantryItems = pantryItems;
  }

  @Override
  public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_dialog_pantries_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.myItemView = this.pantryItems.get(position);
    int maxValue = holder.myItemView.getInNeed();
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

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final TextView myPantryView;
    public final TextView myMaxView;
    public final NumberPicker myNumberPickerView;
    public PantryItem myItemView;

    public ViewHolder(View view) {
      super(view);
      this.myPantryView = view.findViewById(R.id.dialog_pantries_item_pantry);
      this.myNumberPickerView = view.findViewById(R.id.dialog_pantries_item_numberPicker);
      this.myMaxView = view.findViewById(R.id.dialog_pantries_item_max);
    }
  }
}
