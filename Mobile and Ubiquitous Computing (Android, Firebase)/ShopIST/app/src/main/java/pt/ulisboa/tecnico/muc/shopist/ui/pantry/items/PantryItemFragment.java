package pt.ulisboa.tecnico.muc.shopist.ui.pantry.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class PantryItemFragment extends Fragment {

  private static final String PANTRY_ID = "PANTRY_ID";
  private static final String POSITION = "POSITION";
  private ViewPager2 viewPager2;

  public static Bundle newBundle(String pantryId, int position) {
    Bundle bundle = new Bundle();
    bundle.putInt(POSITION, position);
    bundle.putString(PANTRY_ID, pantryId);
    return bundle;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);

    CommonViewModel commonViewModel =
        new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    View pantryItemPagerView =
        inflater.inflate(R.layout.fragment_pantry_item_view_pager, container, false);

    String pantryId = requireArguments().getString(PANTRY_ID);
    int position = requireArguments().getInt(POSITION);
    List<Item> items = commonViewModel.getPantryItems(pantryId);
    Collections.sort(items);

    PantryItemAdapter pantryItemAdapter =
        new PantryItemAdapter(this, pantryId, items, commonViewModel);
    this.viewPager2 = pantryItemPagerView.findViewById(R.id.pantry_item_view_pager);
    viewPager2.setAdapter(pantryItemAdapter);
    viewPager2.setCurrentItem(position, false);

    FloatingActionButton cartButton =
        pantryItemPagerView.findViewById(R.id.pantry_item_cart_access);
    cartButton.setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.action_pantry_item_to_cart));
    cartButton.setVisibility(commonViewModel.hasCart() ? View.VISIBLE : View.GONE);
    return pantryItemPagerView;
  }

  public void nextItem() {
    viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1, true);
  }

  public void previousItem() {
    viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1, true);
  }
}
