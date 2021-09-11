package pt.ulisboa.tecnico.muc.shopist.ui.common.listsform;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Area;
import pt.ulisboa.tecnico.muc.shopist.services.GoogleApiClient;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

import static android.app.Activity.RESULT_OK;

public class ListsFormDialogFragment extends DialogFragment {

  public static final String ID_KEY = "ID_KEY";
  public static final String ACTION_KEY = "List Action";
  public static final String ADD_SHOP = "Add Shopping";
  public static final String EDIT_SHOP = "Edit Shopping";
  public static final String ADD_PANTRY = "Add Pantry";
  public static final String EDIT_PANTRY = "Edit Pantry";
  public static final String EDIT_POSITION = "Edit Position";
  public static final int RESULT_CODE_ADD = 1;
  public static final int RESULT_CODE_EDIT = 2;
  private static final int REQUEST_PLACE_PICKER = 1;
  private CommonViewModel commonViewModel;
  private EditText myNameView;
  private TextView myLocationView;
  private Location location;
  private String action;
  private Area area;
  private int position;

  public ListsFormDialogFragment() {}

  public static ListsFormDialogFragment newAddInstance(String addAction) {
    ListsFormDialogFragment listsFormDialog = new ListsFormDialogFragment();
    Bundle args = new Bundle();
    args.putString(ACTION_KEY, addAction);
    listsFormDialog.setArguments(args);
    return listsFormDialog;
  }

  public static ListsFormDialogFragment newEditInstance(
      String editAction, String id, int position) {
    ListsFormDialogFragment listsFormDialog = new ListsFormDialogFragment();
    Bundle args = new Bundle();
    args.putString(ACTION_KEY, editAction);
    args.putString(ID_KEY, id);
    args.putInt(EDIT_POSITION, position);
    listsFormDialog.setArguments(args);
    return listsFormDialog;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_listsform, container);
  }

  @Override
  public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    TextView myTitle = view.findViewById(R.id.add_list_title);
    initializeArguments();
    this.myLocationView = view.findViewById(R.id.add_list_location_content);
    this.myNameView = view.findViewById(R.id.add_list_name_content);
    myTitle.setText(this.action);
    if (this.action.equals(EDIT_SHOP) || this.action.equals(EDIT_PANTRY)) {
      this.myNameView.setText(this.area.getName());
      this.myLocationView.setText(this.area.getLocation().getProvider());
    }
    this.myLocationView.setOnClickListener(this::startAutocompleteActivity);
    view.findViewById(R.id.shopping_pantries_cancel).setOnClickListener((v) -> dismiss());
    view.findViewById(R.id.shopping_pantries_save).setOnClickListener(this::onCreate);
  }

  @Override
  public void onResume() {
    super.onResume();
    Objects.requireNonNull(getDialog())
        .getWindow()
        .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  private void sendResult(String id, int position, int resultCode) {
    Intent intent = new Intent();
    intent.putExtra(ID_KEY, id);
    if (position != -1) intent.putExtra(EDIT_POSITION, position);
    Objects.requireNonNull(getTargetFragment())
        .onActivityResult(getTargetRequestCode(), resultCode, intent);
  }

  private void initializeArguments() {
    Bundle arguments = getArguments();
    assert arguments != null;
    this.action = arguments.getString(ACTION_KEY);
    String id = arguments.getString(ID_KEY);
    if (this.action.equals(EDIT_PANTRY)) {
      this.area = this.commonViewModel.getPantry(id);
      this.location = new Location(this.area.getLocation());
    } else if (this.action.equals(EDIT_SHOP)) {
      this.area = this.commonViewModel.getShopping(id);
      this.location = new Location(this.area.getLocation());
    }
    this.position = arguments.getInt(EDIT_POSITION);
  }

  public void startAutocompleteActivity(View view) {
    PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
    builder
        .setAndroidApiKey(GoogleApiClient.GOOGLE_KEY_API)
        .setMapsApiKey(GoogleApiClient.GOOGLE_KEY_API);
    if (location != null) {
      builder.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
    }
    try {
      startActivityForResult(builder.build(requireActivity()), REQUEST_PLACE_PICKER);
    } catch (Exception ex) {
      Toast.makeText(getContext(), "Google Location is Unavailable!", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == REQUEST_PLACE_PICKER && resultCode == RESULT_OK && data != null) {
      Place place = PingPlacePicker.getPlace(data);
      if (place != null) {
        LatLng latLng = Objects.requireNonNull(place.getLatLng());
        this.location = new Location(place.getName());
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        myLocationView.setText(this.location.getProvider());
      }
    }
  }

  public void onCreate(View view) {
    if ((this.action.equals(ADD_SHOP) || this.action.equals(ADD_PANTRY))
        && (this.myLocationView.length() == 0 || this.myNameView.length() == 0)) {
      Toast.makeText(getContext(), "Must Insert All Fields!", Toast.LENGTH_LONG).show();
      return;
    }
    if ((this.action.equals(EDIT_SHOP) || this.action.equals(EDIT_PANTRY))
        && (this.myLocationView.length() == 0 && this.myNameView.length() == 0)) {
      Toast.makeText(getContext(), "Must Change at Least One Field!", Toast.LENGTH_LONG).show();
      return;
    }
    String msg = String.format("Saving %s!", this.action.split(" ")[1]);
    ProgressDialog pDialog = ProgressDialog.show(getContext(), msg, "Please Wait!", true, true);
    Thread onSaveListThread = new Thread(() -> this.onCreateThread(pDialog));
    onSaveListThread.start();
    pDialog.setOnCancelListener(d -> onSaveListThread.interrupt());
  }

  private void onCreateThread(ProgressDialog pDialog) {
    boolean success = this.onSaveList();
    requireActivity()
        .runOnUiThread(
            () -> {
              pDialog.dismiss();
              if (success) dismiss();
            });
  }

  private boolean onSaveList() {
    String newName = this.myNameView.getText().toString();
    String id;
    int success;
    switch (this.action) {
      case ADD_SHOP:
        id =
            commonViewModel.addShopping(
                newName,
                this.location.getProvider(),
                this.location.getLatitude(),
                this.location.getLongitude());
        return this.onCreateList(id);
      case ADD_PANTRY:
        id =
            commonViewModel.addPantry(
                newName,
                this.location.getProvider(),
                this.location.getLatitude(),
                this.location.getLongitude());
        return this.onCreateList(id);
      case EDIT_SHOP:
        success =
            this.commonViewModel.updateShopping(
                this.area.getId(),
                newName,
                this.location.getProvider(),
                this.location.getLatitude(),
                this.location.getLongitude());
        return this.onEditList(success, this.area.getId());
      case EDIT_PANTRY:
        success =
            this.commonViewModel.updatePantry(
                this.area.getId(),
                newName,
                this.location.getProvider(),
                this.location.getLatitude(),
                this.location.getLongitude());
        return this.onEditList(success, this.area.getId());
    }
    return true;
  }

  private boolean onCreateList(String id) {
    requireActivity()
        .runOnUiThread(
            () -> {
              if (!invalidCreation(id)) this.onSendResult(id, RESULT_CODE_ADD);
            });
    return id != null;
  }

  private boolean onEditList(int success, String id) {
    requireActivity()
        .runOnUiThread(
            () -> {
              if (!invalidEdition(success)) this.onSendResult(id, RESULT_CODE_EDIT);
            });
    return success != 0;
  }

  private void onSendResult(String id, int resultCode) {
    this.sendResult(id, this.position, resultCode);
  }

  private boolean invalidEdition(int success) {
    if (success == 0) {
      Toast.makeText(
              getContext(),
              "Edit was not successful! (Location cannot be associated with other Lists)",
              Toast.LENGTH_LONG)
          .show();
      return true;
    }
    return false;
  }

  private boolean invalidCreation(String id) {
    if (id == null) {
      Toast.makeText(
              getContext(),
              "Creation was not successful! (Location cannot be associated with other Lists)",
              Toast.LENGTH_LONG)
          .show();
      return true;
    }
    return false;
  }
}
