package pt.ulisboa.tecnico.muc.shopist.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import pt.ulisboa.tecnico.muc.shopist.R;

public class HomeFragment extends Fragment {

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    CardView pCard = view.findViewById(R.id.home_pantry_card);
    CardView sCard = view.findViewById(R.id.home_shopping_card);
    pCard.setOnClickListener(this::goToPantryLists);
    sCard.setOnClickListener(this::goToShoppingList);
    return view;
  }

  public void goToPantryLists(View view) {
    Navigation.findNavController(view).navigate(R.id.action_home_to_pantry_lists);
  }

  public void goToShoppingList(View view) {
    Navigation.findNavController(view).navigate(R.id.action_home_to_shopping_lists);
  }
}
