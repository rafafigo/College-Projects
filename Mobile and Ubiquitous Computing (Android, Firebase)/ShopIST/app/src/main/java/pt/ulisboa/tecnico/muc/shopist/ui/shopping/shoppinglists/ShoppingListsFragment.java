package pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment;

import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.ADD_SHOP;
import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.EDIT_POSITION;
import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.ID_KEY;
import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.RESULT_CODE_ADD;
import static pt.ulisboa.tecnico.muc.shopist.ui.common.listsform.ListsFormDialogFragment.RESULT_CODE_EDIT;

public class ShoppingListsFragment extends Fragment {

  public static int REQUEST_CODE = 0;
  private CommonViewModel commonViewModel;
  private MyShoppingListsItemAdapter adapter;
  private PullRefreshLayout pullRefreshLayout;

  public ShoppingListsFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);

    RecyclerView recyclerView = view.findViewById(R.id.shopping_lists_recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

    this.adapter = new MyShoppingListsItemAdapter(this.getShoppings(), this);
    recyclerView.setAdapter(this.adapter);

    this.pullRefreshLayout = view.findViewById(R.id.shopping_lists_pulltorefresh);
    this.pullRefreshLayout.setRefreshing(false);
    this.pullRefreshLayout.setOnRefreshListener(this::onRefreshListener);

    view.findViewById(R.id.shopping_lists_add).setOnClickListener(this::createShoppingList);

    FloatingActionButton cartButton = view.findViewById(R.id.shopping_lists_cart);
    cartButton.setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.action_shopping_lists_to_cart));
    cartButton.setVisibility(commonViewModel.hasCart() ? View.VISIBLE : View.GONE);
    return view;
  }

  private void onRefreshListener() {
    this.adapter.updateShoppings(this.getShoppings());
    this.pullRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onResume() {
    super.onResume();
    this.onRefreshListener();
  }

  private List<Shopping> getShoppings() {
    List<Shopping> shoppings = this.commonViewModel.getShoppingList();
    Collections.sort(shoppings);
    return shoppings;
  }

  public CommonViewModel getCommonViewModel() {
    return commonViewModel;
  }

  public void createShoppingList(View v) {
    ListsFormDialogFragment listsFormDialog = ListsFormDialogFragment.newAddInstance(ADD_SHOP);
    listsFormDialog.setTargetFragment(this, REQUEST_CODE);
    listsFormDialog.show(getParentFragmentManager(), ListsFormDialogFragment.class.getSimpleName());
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == REQUEST_CODE) {
      Shopping shopping =
          this.commonViewModel.getShopping(Objects.requireNonNull(data).getStringExtra(ID_KEY));
      if (resultCode == RESULT_CODE_ADD) {
        this.adapter.addItem(shopping);
      } else if (resultCode == RESULT_CODE_EDIT) {
        this.adapter.updateItem(shopping, data.getIntExtra(EDIT_POSITION, 0));
      }
    }
  }
}
