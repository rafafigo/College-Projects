package pt.ulisboa.tecnico.muc.shopist.ui.cart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class CartFragment extends Fragment {

  private TextView totalCartItems;
  private TextView totalCartPrice;
  private FloatingActionButton cartButton;
  private CommonViewModel commonViewModel;
  private CartAdapter adapter;
  private List<Item> items;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    View view = inflater.inflate(R.layout.fragment_cart, container, false);

    RecyclerView recyclerView = view.findViewById(R.id.cart_items_recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

    this.items = this.getItems();
    this.adapter = new CartAdapter(this, items, this.commonViewModel, this::onCartChanged);
    recyclerView.setAdapter(adapter);

    PullRefreshLayout pullRefreshLayout = view.findViewById(R.id.cart_items_pulltorefresh);
    pullRefreshLayout.setRefreshing(false);
    pullRefreshLayout.setOnRefreshListener(
        () -> {
          items = this.getItems();
          adapter.updateItems(items);
          pullRefreshLayout.setRefreshing(false);
        });

    this.totalCartItems = view.findViewById(R.id.cart_total_items);
    this.totalCartPrice = view.findViewById(R.id.cart_total_price);

    this.onCartChanged();
    this.cartButton = view.findViewById(R.id.cart_add_all);
    this.cartButton.setOnClickListener((v) -> onCheckout(items));
    this.cartButton.setVisibility(items.size() == 0 ? View.GONE : View.VISIBLE);
    return view;
  }

  private List<Item> getItems() {
    List<Item> items = this.commonViewModel.getCartItems();
    // To Preserve Insertion Order
    Collections.sort(items);
    return items;
  }

  public void onCheckout(List<Item> items) {
    new Thread(() -> onCheckoutThread(this.commonViewModel.addAllToPantry(items))).start();
  }

  @SuppressLint("SetTextI18n")
  public void onCheckoutThread(int success) {
    this.requireActivity()
        .runOnUiThread(
            () -> {
              if (success == 0) {
                Toast.makeText(getContext(), "Error Adding To Pantry!", Toast.LENGTH_LONG).show();
              } else {
                this.adapter.clearItems();
                this.totalCartItems.setText("0");
                this.totalCartPrice.setText("0.00 €");
                this.cartButton.setVisibility(View.GONE);
              }
            });
  }

  @SuppressLint("DefaultLocale")
  public void onCartChanged() {
    this.totalCartItems.setText(
        String.format("Total In Cart: %d", this.commonViewModel.getTotalInCart()));
    this.totalCartPrice.setText(String.format("%.2f €", this.commonViewModel.getTotalPrice()));
  }
}
