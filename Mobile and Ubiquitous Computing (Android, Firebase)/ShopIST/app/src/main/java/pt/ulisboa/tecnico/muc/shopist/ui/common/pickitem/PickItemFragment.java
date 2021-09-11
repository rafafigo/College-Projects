package pt.ulisboa.tecnico.muc.shopist.ui.common.pickitem;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class PickItemFragment extends Fragment {

  public static final String MODE_KEY = "MODE_KEY";
  public static final String PANTRY_ID_KEY = "PANTRY_ID_KEY";
  public static final String SHOPPING_ID_KEY = "SHOPPING_ID_KEY";
  public static final int PANTRY_CREATE = 0;
  public static final int SHOPPING_CREATE = 1;
  private List<Item> items;
  private MyPickItemAdapter adapter;

  public static Bundle newPantryCreateBundle(String pantryId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, PANTRY_CREATE);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    return bundle;
  }

  public static Bundle newShoppingCreateBundle(String pantryId, String shoppingId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, SHOPPING_CREATE);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    bundle.putString(SHOPPING_ID_KEY, shoppingId);
    return bundle;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_pickitem_list, container, false);

    CommonViewModel commonViewModel =
        new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    this.items = commonViewModel.getNonPantryItems(requireArguments().getString(PANTRY_ID_KEY));

    this.adapter =
        new MyPickItemAdapter(
            this,
            requireArguments().getString(PANTRY_ID_KEY),
            requireArguments().getString(SHOPPING_ID_KEY));
    ((RecyclerView) view.findViewById(R.id.pickitem_recycler)).setAdapter(adapter);
    this.adapter.setItems(this.items);

    ((EditText) view.findViewById(R.id.pickitem_searchbar))
        .addTextChangedListener(new SearchbarTextWatcher());
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (this.items.isEmpty()) Navigation.findNavController(requireView()).popBackStack();
  }

  private void filterItems(CharSequence s) {
    List<Item> filteredItems = new ArrayList<>();
    for (Item item : this.items) {
      if (item.getName().contains(s)) filteredItems.add(item);
    }
    this.adapter.setItems(filteredItems);
  }

  private class SearchbarTextWatcher implements TextWatcher {

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      filterItems(s);
    }
  }
}
