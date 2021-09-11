package pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;
import pt.ulisboa.tecnico.muc.shopist.services.CameraService;
import pt.ulisboa.tecnico.muc.shopist.services.DomainService;
import pt.ulisboa.tecnico.muc.shopist.services.GoogleApi;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.common.pickitem.PickItemFragment;

public class ShoppingListFragment extends Fragment {

  public static String SHOPPING_ID_KEY = "SHOPPING_ID_KEY";
  private CommonViewModel commonViewModel;
  private String shoppingId;
  private Pantry pantry;
  private Shopping shopping;

  public ShoppingListFragment() {}

  public static Bundle newBundle(String shoppingId) {
    Bundle bundle = new Bundle();
    bundle.putString(SHOPPING_ID_KEY, shoppingId);
    return bundle;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @SuppressLint("DefaultLocale")
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);

    RecyclerView recyclerView = view.findViewById(R.id.shopping_list_recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

    this.shoppingId = requireArguments().getString(SHOPPING_ID_KEY);
    this.shopping = this.commonViewModel.getShopping(this.shoppingId);

    MyShoppingListItemAdapter adapter =
        new MyShoppingListItemAdapter(this.shopping, this.getItems(), this.commonViewModel, this);
    recyclerView.setAdapter(adapter);

    PullRefreshLayout pullRefreshLayout = view.findViewById(R.id.shopping_list_pulltorefresh);
    pullRefreshLayout.setRefreshing(false);
    pullRefreshLayout.setOnRefreshListener(
        () -> {
          this.updateQueueTime(view);
          adapter.updateItems(this.getItems());
          pullRefreshLayout.setRefreshing(false);
        });

    this.updateQueueTime(view);
    ((TextView) view.findViewById(R.id.shopping_list_title))
        .setText(this.commonViewModel.getShopping(this.shoppingId).getName());
    view.findViewById(R.id.shopping_list_add_item).setOnClickListener(this::onAddItem);
    view.findViewById(R.id.shopping_list_directions)
        .setOnClickListener(v -> gotoLocation(commonViewModel.getShopping(this.shoppingId)));

    FloatingActionButton cartButton = view.findViewById(R.id.shopping_list_cart);
    cartButton.setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.action_shopping_list_to_cart));

    return view;
  }

  private void updateQueueTime(View view) {
    ((TextView) view.findViewById(R.id.shopping_list_await_time))
        .setText(
            String.format(
                "Queue Time: %s",
                this.commonViewModel.getShopping(this.shoppingId).getQueueTime()));
  }

  private List<Item> getItems() {
    List<Item> items = this.commonViewModel.getShoppingItems(this.shopping.getCrowdShoppingId());
    Collections.sort(items, this.shopping);
    return items;
  }

  private void onAddItem(View view) {
    List<Pantry> pantries = this.commonViewModel.getPantryList();
    if (pantries.size() == 1) {
      this.gotoCreateItemForm(view, pantries.get(0).getId());
    } else {
      Collections.sort(pantries);
      List<String> names = new ArrayList<>();
      for (Pantry pantry : pantries)
        names.add(String.format("%s (%s)", pantry.getName(), pantry.getLocation().getProvider()));
      DomainService.choosePantry(
          getContext(),
          names.toArray(new String[0]),
          (choice) -> this.gotoCreateItemForm(view, pantries.get(choice).getId()));
    }
  }

  private void gotoCreateItemForm(View view, String pantryId) {
    String[] options = new String[] {"Barcode", "Manual"};
    if (!this.commonViewModel.getNonPantryItems(pantryId).isEmpty()) {
      options = ArrayUtils.appendToArray(options, "Pick Existing");
    }
    AtomicReference<Integer> choice = new AtomicReference<>(0);
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle("Item Creation Type");
    builder.setSingleChoiceItems(options, choice.get(), (dialog, which) -> choice.set(which));
    builder.setPositiveButton(
        "Choose",
        (dialog, which) -> {
          switch (choice.get()) {
            case 0:
              this.pantry = this.commonViewModel.getPantryMap().get(pantryId);
              CameraService.onScanProductCode(this);
              break;
            case 1:
              Navigation.findNavController(view)
                  .navigate(
                      R.id.action_shopping_list_to_itemform,
                      ItemFormFragment.newShoppingCreateBundle(pantryId, this.shoppingId));
              break;
            case 2:
              Navigation.findNavController(view)
                  .navigate(
                      R.id.action_shopping_list_to_pickitem,
                      PickItemFragment.newShoppingCreateBundle(pantryId, this.shoppingId));
          }
        });
    builder.setNegativeButton("Cancel", null);
    builder.create().show();
  }

  @SuppressLint({"InflateParams", "DefaultLocale"})
  private void gotoLocation(Shopping shop) {
    ProgressDialog pDialog =
        ProgressDialog.show(getContext(), "Getting Location Preview!", "Please Wait!", true, false);
    Dialog dialog = new Dialog(getContext());
    View view = getLayoutInflater().inflate(R.layout.static_map_dialog, null);
    dialog.setContentView(view);
    new GoogleApi().onStaticMap(shop.getLocation(), dialog, pDialog);
    view.findViewById(R.id.static_map_direction)
        .setOnClickListener((v) -> getDirections(shop, dialog));
    view.findViewById(R.id.static_map_cancel).setOnClickListener((v) -> dialog.dismiss());
  }

  @SuppressLint("DefaultLocale")
  public void getDirections(Shopping shop, Dialog dialog) {
    Uri gmmIntentUri =
        Uri.parse(
            String.format(
                "google.navigation:q=%f,%f",
                shop.getLocation().getLatitude(), shop.getLocation().getLongitude()));
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
    mapIntent.setPackage("com.google.android.apps.maps");
    if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
      startActivity(mapIntent);
    }
    dialog.dismiss();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (intentResult != null) {
      if (intentResult.getContents() != null) {
        new Thread(
                () -> {
                  Item item = this.commonViewModel.addScannedItem(intentResult.getContents());
                  item.putPantryItem(
                      new PantryItem(this.pantry.getId(), this.pantry.getTimestamp()));
                  if (!item.getShoppingItems().containsKey(this.shopping.getCrowdShoppingId())) {
                    item.putShoppingItem(new ShoppingItem(this.shopping));
                  }
                  requireActivity()
                      .runOnUiThread(
                          () ->
                              Navigation.findNavController(requireView())
                                  .navigate(
                                      R.id.action_shopping_list_to_itemform,
                                      ItemFormFragment.newShoppingEditBundle(
                                          this.pantry.getId(),
                                          this.shoppingId,
                                          ItemFormFragment.TEMP_ITEM_ID)));
                })
            .start();
      } else {
        Toast.makeText(getContext(), "Scan Cancelled!", Toast.LENGTH_LONG).show();
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
