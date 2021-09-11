package pt.ulisboa.tecnico.muc.shopist.ui.pantry.items;

import android.annotation.SuppressLint;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;

public class PantryItemLocationAdapter
    extends RecyclerView.Adapter<PantryItemLocationAdapter.PantryItemLocationViewHolder> {

  private final List<ShoppingItem> items;
  private final List<Location> locations;

  public PantryItemLocationAdapter(List<ShoppingItem> items, List<Location> locations) {
    this.items = items;
    this.locations = locations;
  }

  @NonNull
  @Override
  public PantryItemLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new PantryItemLocationViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantry_item_location, parent, false));
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onBindViewHolder(@NonNull PantryItemLocationViewHolder holder, int position) {
    holder.locationName.setText(this.items.get(position).getShopping().getName());
    holder.locationAddr.setText(this.locations.get(position).getProvider());
    holder.locationPrice.setText(String.format("%.2f €", this.items.get(position).getPrice()));
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  public static class PantryItemLocationViewHolder extends RecyclerView.ViewHolder {

    private final TextView locationName;
    private final TextView locationAddr;
    private final TextView locationPrice;

    public PantryItemLocationViewHolder(@NonNull View itemView) {
      super(itemView);

      this.locationName = itemView.findViewById(R.id.pantry_item_location_name);
      this.locationAddr = itemView.findViewById(R.id.pantry_item_location_address);
      this.locationPrice = itemView.findViewById(R.id.pantry_item_location_price);
    }
  }
}
