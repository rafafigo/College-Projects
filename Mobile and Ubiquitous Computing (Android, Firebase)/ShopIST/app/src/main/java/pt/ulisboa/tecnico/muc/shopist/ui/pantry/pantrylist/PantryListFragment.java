package pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrylist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.services.CameraService;
import pt.ulisboa.tecnico.muc.shopist.services.GoogleApi;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.common.pickitem.PickItemFragment;

public class PantryListFragment extends Fragment {

  public static final String PANTRY_ID_KEY = "PANTRY_ID_KEY";
  private CommonViewModel commonViewModel;
  private String pantryId;
  private Pantry pantry;
  private TextView itemsNrTV;

  public static Bundle newBundle(String pantryId) {
    Bundle bundle = new Bundle();
    bundle.putString(PANTRY_ID_KEY, pantryId);
    return bundle;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);
    this.itemsNrTV = view.findViewById(R.id.pantry_list_items_nr);
    Context context = view.getContext();

    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);

    this.pantryId = requireArguments().getString(PANTRY_ID_KEY);
    this.pantry = this.commonViewModel.getPantryMap().get(this.pantryId);
    RecyclerView recyclerView = view.findViewById(R.id.pantry_list_recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));

    MyPantryListItemAdapter adapter =
        new MyPantryListItemAdapter(
            this, this.pantryId, this.commonViewModel, this.getItems(), this::setItemsNr);
    recyclerView.setAdapter(adapter);

    PullRefreshLayout pullRefreshLayout = view.findViewById(R.id.pantry_list_pulltorefresh);
    pullRefreshLayout.setRefreshing(false);
    pullRefreshLayout.setOnRefreshListener(
        () -> {
          adapter.updateItems(this.getItems());
          pullRefreshLayout.setRefreshing(false);
        });

    ((TextView) view.findViewById(R.id.pantry_list_title))
        .setText(this.commonViewModel.getPantry(this.pantryId).getName());

    FloatingActionButton directionsFab = view.findViewById(R.id.pantry_list_directions);
    Pantry pantry = this.commonViewModel.getPantry(this.pantryId);
    directionsFab.setOnClickListener((v) -> this.gotoLocation(pantry));

    FloatingActionButton addFab = view.findViewById(R.id.pantry_list_add);
    addFab.setOnClickListener(this::onAdd);

    FloatingActionButton cartButton = view.findViewById(R.id.pantry_list_cart);
    cartButton.setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.action_pantry_list_to_cart));
    cartButton.setVisibility(commonViewModel.hasCart() ? View.VISIBLE : View.GONE);

    return view;
  }

  private List<Item> getItems() {
    List<Item> items = this.commonViewModel.getPantryItems(this.pantryId);
    this.setItemsNr(items);
    Collections.sort(items);
    return items;
  }

  @SuppressLint("DefaultLocale")
  private void setItemsNr(List<Item> itemList) {
    Integer itemsNr = 0;
    for (Item item : itemList) {
      itemsNr += item.getPantryItem(this.pantryId).getInPantry();
    }
    itemsNrTV.setText(String.format("Available Items: %d", itemsNr));
  }

  public void onAdd(View view) {
    String[] options = new String[] {"Barcode", "Manual"};
    if (!this.commonViewModel.getNonPantryItems(this.pantryId).isEmpty()) {
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
              CameraService.onScanProductCode(this);
              break;
            case 1:
              Navigation.findNavController(view)
                  .navigate(
                      R.id.action_pantry_list_to_itemform,
                      ItemFormFragment.newPantryCreateBundle(this.pantryId));
              break;
            case 2:
              Navigation.findNavController(view)
                  .navigate(
                      R.id.action_pantry_list_to_pickitem,
                      PickItemFragment.newPantryCreateBundle(this.pantryId));
          }
        });
    builder.setNegativeButton("Cancel", null);
    builder.create().show();
  }

  @SuppressLint({"InflateParams", "DefaultLocale"})
  private void gotoLocation(Pantry pantry) {
    ProgressDialog pDialog =
        ProgressDialog.show(getContext(), "Getting Location Preview!", "Please Wait!", true, false);
    Dialog dialog = new Dialog(getContext());
    View view = getLayoutInflater().inflate(R.layout.static_map_dialog, null);
    dialog.setContentView(view);
    new GoogleApi().onStaticMap(pantry.getLocation(), dialog, pDialog);
    view.findViewById(R.id.static_map_direction)
        .setOnClickListener((v) -> getDirections(pantry, dialog));
    view.findViewById(R.id.static_map_cancel).setOnClickListener((v) -> dialog.dismiss());
  }

  @SuppressLint("DefaultLocale")
  public void getDirections(Pantry pantry, Dialog dialog) {
    Uri gmmIntentUri =
        Uri.parse(
            String.format(
                "google.navigation:q=%f,%f",
                pantry.getLocation().getLatitude(), pantry.getLocation().getLongitude()));
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
                  item.putPantryItem(new PantryItem(this.pantryId, this.pantry.getTimestamp()));
                  requireActivity()
                      .runOnUiThread(
                          () ->
                              Navigation.findNavController(requireView())
                                  .navigate(
                                      R.id.action_pantry_list_to_itemform,
                                      ItemFormFragment.newPantryEditBundle(
                                          this.pantryId, ItemFormFragment.TEMP_ITEM_ID)));
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
