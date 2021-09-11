package pt.ulisboa.tecnico.muc.shopist.ui.pantry.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class PantryItemImagesFragment extends Fragment {

  private static final String ITEM_ID = "ITEM_ID";

  public static Bundle newBundle(String itemId) {
    Bundle bundle = new Bundle();
    bundle.putString(ITEM_ID, itemId);
    return bundle;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    CommonViewModel commonViewModel =
        new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    View pantryItemImagesPagerView =
        inflater.inflate(R.layout.fragment_pantry_item_images_view_pager, container, false);

    String itemId = requireArguments().getString(ITEM_ID);

    PantryItemImagesAdapter pantryItemImagesAdapter =
        new PantryItemImagesAdapter(this, commonViewModel.getItem(itemId).getPictures());

    ViewPager2 viewPager =
        pantryItemImagesPagerView.findViewById(R.id.pantry_item_images_view_pager);
    viewPager.setAdapter(pantryItemImagesAdapter);

    return pantryItemImagesPagerView;
  }
}
