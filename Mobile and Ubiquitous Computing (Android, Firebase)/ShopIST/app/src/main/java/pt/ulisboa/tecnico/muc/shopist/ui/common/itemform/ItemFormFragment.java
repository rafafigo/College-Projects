package pt.ulisboa.tecnico.muc.shopist.ui.common.itemform;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.GlideApp;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Picture;
import pt.ulisboa.tecnico.muc.shopist.domain.ShoppingItem;
import pt.ulisboa.tecnico.muc.shopist.services.CameraService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

import static android.app.Activity.RESULT_OK;

public class ItemFormFragment extends Fragment {

  public static final String TEMP_ITEM_ID = "SCANNED_ITEM_ID";
  public static final String MODE_KEY = "MODE_KEY";
  public static final String PANTRY_ID_KEY = "PANTRY_ID_KEY";
  public static final String SHOPPING_ID_KEY = "SHOPPING_ID_KEY";
  public static final String ITEM_ID_KEY = "ITEM_ID_KEY";
  public static final int PANTRY_CREATE = 0;
  public static final int SHOPPING_CREATE = 1;
  public static final int PANTRY_EDIT = 2;
  public static final int SHOPPING_EDIT = 3;
  private Integer viewId;
  private Integer nPictures;
  private ShoppingItemsManager shoppingItemsManager;
  private CommonViewModel commonViewModel;
  private ItemFormViewModel itemFormViewModel;
  private EditText nameET;
  private EditText inpantryET;
  private EditText inneedET;
  private EditText barcodeET;
  private TextView picturesNrTV;
  private LinearLayout picturesLL;
  private Item myItem;
  private PantryItem myPantryItem;
  private List<Picture> pictures;
  private boolean isScanned = false;

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

  public static Bundle newPantryCreateBundle(String pantryId, String itemId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, PANTRY_CREATE);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    bundle.putString(ITEM_ID_KEY, itemId);
    return bundle;
  }

  public static Bundle newShoppingCreateBundle(String pantryId, String shoppingId, String itemId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, SHOPPING_CREATE);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    bundle.putString(SHOPPING_ID_KEY, shoppingId);
    bundle.putString(ITEM_ID_KEY, itemId);
    return bundle;
  }

  public static Bundle newPantryEditBundle(String pantryId, String itemId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, PANTRY_EDIT);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    bundle.putString(ITEM_ID_KEY, itemId);
    return bundle;
  }

  public static Bundle newShoppingEditBundle(String pantryId, String shoppingId, String itemId) {
    Bundle bundle = new Bundle();
    bundle.putInt(MODE_KEY, SHOPPING_EDIT);
    bundle.putString(PANTRY_ID_KEY, pantryId);
    bundle.putString(SHOPPING_ID_KEY, shoppingId);
    bundle.putString(ITEM_ID_KEY, itemId);
    return bundle;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_itemform, container, false);
    this.nameET = view.findViewById(R.id.itemform_name);
    this.inpantryET = view.findViewById(R.id.itemform_inpantry);
    this.inneedET = view.findViewById(R.id.itemform_inneed);
    this.barcodeET = view.findViewById(R.id.itemform_barcode);
    this.picturesLL = view.findViewById(R.id.itemform_pictures_ll);
    this.picturesNrTV = view.findViewById(R.id.itemform_pictures_nr);
    ImageView galleryIconView = view.findViewById(R.id.itemform_gallery);
    ImageView cameraIconView = view.findViewById(R.id.itemform_camera);
    FloatingActionButton doneFab = view.findViewById(R.id.itemform_done);
    FloatingActionButton cancelFab = view.findViewById(R.id.itemform_cancel);

    this.viewId = 0;
    this.nPictures = 0;
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    this.itemFormViewModel = new ViewModelProvider(requireActivity()).get(ItemFormViewModel.class);
    this.parseArguments();
    this.setDefaultArgs();
    this.shoppingItemsManager = new ShoppingItemsManager(this, view, this.itemFormViewModel);

    galleryIconView.setOnClickListener((v) -> CameraService.onChoosePic(this));
    cameraIconView.setOnClickListener((v) -> CameraService.onTakePic(this, getContext()));
    doneFab.setOnClickListener(this::onDone);
    cancelFab.setOnClickListener(this::onCancel);

    return view;
  }

  private void parseArguments() {
    Bundle args = requireArguments();
    switch (args.getInt(MODE_KEY)) {
      case PANTRY_CREATE:
        this.setPantryCreate(args);
        break;
      case SHOPPING_CREATE:
        this.setShoppingCreate(args);
        break;
      case PANTRY_EDIT:
        this.isScanned = TEMP_ITEM_ID.equals(args.getString(ITEM_ID_KEY));
        this.setPantryEdit(args);
        break;
      case SHOPPING_EDIT:
        this.isScanned = TEMP_ITEM_ID.equals(args.getString(ITEM_ID_KEY));
        this.setShoppingEdit(args);
        break;
    }
    if (this.isScanned) this.commonViewModel.removeScannedItem();
  }

  private void setPantryCreate(Bundle args) {
    Pantry pantry = this.commonViewModel.getPantry(args.getString(PANTRY_ID_KEY));
    String itemId = args.getString(ITEM_ID_KEY);
    this.myItem = itemId != null ? this.commonViewModel.getItem(itemId) : new Item();
    this.myPantryItem = new PantryItem(pantry.getId(), pantry.getTimestamp());
    this.itemFormViewModel.setShoppingItems(
        this.commonViewModel.newShoppingItems(args.getString(ITEM_ID_KEY)));
    this.pictures = new ArrayList<>(this.myItem.getPictures());
  }

  private void setShoppingCreate(Bundle args) {
    Pantry pantry = this.commonViewModel.getPantry(args.getString(PANTRY_ID_KEY));
    String itemId = args.getString(ITEM_ID_KEY);
    this.myItem = itemId != null ? this.commonViewModel.getItem(itemId) : new Item();
    this.myPantryItem = new PantryItem(pantry.getId(), pantry.getTimestamp());
    List<ShoppingItem> shoppingItems = new ArrayList<>();
    ShoppingItem shoppingItem = this.myItem.getShoppingItem(args.getString(SHOPPING_ID_KEY));
    shoppingItems.add(
        shoppingItem != null
            ? shoppingItem
            : new ShoppingItem(commonViewModel.getShopping(args.getString(SHOPPING_ID_KEY))));
    this.itemFormViewModel.setShoppingItems(shoppingItems);
    this.pictures = new ArrayList<>(this.myItem.getPictures());
  }

  private void setPantryEdit(Bundle args) {
    this.myItem = this.commonViewModel.getItem(args.getString(ITEM_ID_KEY));
    this.myPantryItem = new PantryItem(this.myItem.getPantryItem(args.getString(PANTRY_ID_KEY)));
    List<ShoppingItem> shoppingItems =
        this.commonViewModel.newShoppingItems(args.getString(ITEM_ID_KEY));
    this.itemFormViewModel.setShoppingItems(shoppingItems);
    this.pictures = new ArrayList<>(this.myItem.getPictures());
  }

  private void setShoppingEdit(Bundle args) {
    this.myItem = this.commonViewModel.getItem(args.getString(ITEM_ID_KEY));
    this.myPantryItem = new PantryItem(this.myItem.getPantryItem(args.getString(PANTRY_ID_KEY)));
    Map<String, ShoppingItem> myShoppingItems = this.myItem.getShoppingItems();
    String crowdShoppingId =
        commonViewModel.getShopping(args.getString(SHOPPING_ID_KEY)).getCrowdShoppingId();
    List<ShoppingItem> shoppingItems = new ArrayList<>();
    shoppingItems.add(
        new ShoppingItem(Objects.requireNonNull(myShoppingItems.get(crowdShoppingId))));
    this.itemFormViewModel.setShoppingItems(shoppingItems);
    this.pictures = new ArrayList<>(this.myItem.getPictures());
  }

  private void setDefaultArgs() {
    if (this.myItem.getName() != null) {
      this.nameET.setText(this.myItem.getName());
    }
    if (this.myItem.getBarcode() != null) {
      this.barcodeET.setText(this.myItem.getBarcode());
    }
    if (this.myPantryItem.getInPantry() != null) {
      this.inpantryET.setText(String.valueOf(this.myPantryItem.getInPantry()));
    }
    if (this.myPantryItem.getInNeed() != null) {
      this.inneedET.setText(String.valueOf(this.myPantryItem.getInNeed()));
    }
    for (Picture picture : this.pictures) this.onAddPicture(picture);
  }

  public void onDone(View view) {
    if (this.nameET.getText().toString().length() <= 0) {
      Toast.makeText(getContext(), "Associate a Name to the Pantry Item!", Toast.LENGTH_SHORT)
          .show();
      return;
    }
    if (this.inpantryET.getText().toString().length() <= 0) {
      Toast.makeText(
              getContext(),
              "Associate an In Pantry Quantity to the Pantry Item!",
              Toast.LENGTH_SHORT)
          .show();
      return;
    }
    if (this.inneedET.getText().toString().length() <= 0) {
      Toast.makeText(
              getContext(), "Associate an In Need Quantity to the Pantry Item!", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    this.alertDialog(
        "Are you sure you want to save?",
        () -> {
          this.myPantryItem.setInPantry(Integer.parseInt(this.inpantryET.getText().toString()));
          this.myPantryItem.setInNeed(Integer.parseInt(this.inneedET.getText().toString()));

          ProgressDialog pDialog =
              ProgressDialog.show(getContext(), "Saving Item!", "Please Wait!", true, true);
          Thread onSaveThread = new Thread(() -> onSave(view, pDialog));
          onSaveThread.start();
          pDialog.setOnCancelListener(d -> onSaveThread.interrupt());
        });
  }

  private void onSave(View view, ProgressDialog pDialog) {

    String itemId =
        this.commonViewModel.onSaveItem(
            isScanned,
            this.myItem.getId(),
            this.nameET.getText().toString(),
            this.barcodeET.getText().length() > 0 ? this.barcodeET.getText().toString() : null,
            this.myPantryItem,
            new HashSet<>(this.shoppingItemsManager.fetchShoppingItems()),
            this.pictures);

    requireActivity()
        .runOnUiThread(
            () -> {
              pDialog.dismiss();
              if (itemId != null) {
                Navigation.findNavController(view).popBackStack();
              } else
                Toast.makeText(
                        getContext(), "Item Saving Failed! (Barcode is Unique)", Toast.LENGTH_LONG)
                    .show();
            });
  }

  private void onCancel(View view) {
    this.alertDialog(
        "Are you sure you want to cancel?",
        () -> Navigation.findNavController(view).popBackStack());
  }

  private void alertDialog(String msg, AlertDialogCallback callback) {
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            callback.onPositive();
          }
        };
    new AlertDialog.Builder(getContext())
        .setMessage(msg)
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener)
        .show();
  }

  public void onAddPicture(Picture picture) {

    RelativeLayout relativeLayout = new RelativeLayout(requireContext());
    LayoutParams lp = new LayoutParams(CameraService.PICTURE_WIDTH, CameraService.PICTURE_HEIGHT);
    lp.setMargins(0, 0, 12, 0);
    relativeLayout.setId(this.viewId++);
    relativeLayout.setLayoutParams(lp);

    ImageView imageView = new ImageView(requireContext());
    imageView.setId(this.viewId++);
    if (picture.getPictureBmp() == null) {
      GlideApp.with(this)
          .load(FirebaseStorage.getInstance().getReference().child(picture.getPictureUri()))
          .into(imageView);
    } else imageView.setImageBitmap(picture.getPictureBmp());

    FloatingActionButton deleteFab = new FloatingActionButton(requireContext());
    lp = new LayoutParams(CameraService.PICTURE_WIDTH / 4, CameraService.PICTURE_HEIGHT / 4);
    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    deleteFab.setId(this.viewId++);
    deleteFab.setLayoutParams(lp);
    deleteFab.setScaleType(ScaleType.CENTER);
    deleteFab.setImageResource(R.drawable.ic_clear);
    deleteFab.setBackgroundTintList(
        ColorStateList.valueOf(getResources().getColor(R.color.red_dark)));
    ImageViewCompat.setImageTintList(deleteFab, ColorStateList.valueOf(Color.WHITE));
    deleteFab.setOnClickListener((v) -> this.onDeletePicture(v, picture));

    relativeLayout.addView(imageView);
    relativeLayout.addView(deleteFab);
    this.picturesLL.addView(relativeLayout, 0);
    this.setPicturesNr(++this.nPictures);
  }

  public void onDeletePicture(View view, Picture picture) {
    this.alertDialog(
        "Are you sure you want to delete that picture?",
        () -> {
          this.picturesLL.removeView((View) view.getParent());
          this.pictures.remove(picture);
          this.setPicturesNr(--this.nPictures);
        });
  }

  @SuppressLint("DefaultLocale")
  private void setPicturesNr(Integer nPictures) {
    this.picturesNrTV.setText(String.format("%d Pictures", nPictures));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
      Bitmap bmp =
          CameraService.uriToBmp(requireContext().getContentResolver(), UCrop.getOutput(data));
      if (bmp != null) {
        Picture picture = new Picture(bmp);
        this.pictures.add(picture);
        this.onAddPicture(picture);
      }
    } else if (requestCode == CameraService.CHOOSE_PIC && resultCode == RESULT_OK && data != null) {
      if (data.getClipData() != null) {
        int count = data.getClipData().getItemCount();
        for (int i = 0; i < count; i++) {
          CameraService.openCropFromUri(
              this, getContext(), data.getClipData().getItemAt(i).getUri());
        }
      } else if (data.getData() != null) {
        CameraService.openCropFromUri(this, getContext(), data.getData());
      }
    } else if (requestCode == CameraService.TAKE_PIC && resultCode == RESULT_OK) {
      CameraService.openCropFromPath(this, getContext());
    }
  }

  private interface AlertDialogCallback {
    void onPositive();
  }
}
