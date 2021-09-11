package pt.ulisboa.tecnico.muc.shopist.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class DomainService {

  public static Map<String, String> getParsedPantries() {
    Map<String, String> associatedPantries = new HashMap<>();
    for (Pantry pantry : CommonViewModel.getInstance().getPantryList()) {
      associatedPantries.put(
          String.format("%s (%s)", pantry.getName(), pantry.getLocation().getProvider()),
          pantry.getId());
    }
    return associatedPantries;
  }

  public static void choosePantry(Context context, String[] options, AlertDialogCallback callback) {
    AtomicReference<Integer> choice = new AtomicReference<>(0);
    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            callback.onChosen(choice.get());
          }
        };
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Choose a Pantry");
    builder.setSingleChoiceItems(options, choice.get(), (dialog, which) -> choice.set(which));
    builder.setPositiveButton("Choose", dialogClickListener);
    builder.setNegativeButton("Cancel", dialogClickListener);
    builder.create().show();
  }

  public static void onPrompt(Context context, Item item, PromptDialogCallback callback) {
    boolean hasBarcode = item.getBarcode() != null;
    boolean hasPrice = !item.getShoppingItems().isEmpty();
    boolean hasPicture = item.getPicture() != null;
    if (hasBarcode && hasPrice && hasPicture) return;

    DialogInterface.OnClickListener dialogClickListener =
        (dialog, which) -> {
          if (which == DialogInterface.BUTTON_POSITIVE) {
            callback.onPrompt();
          }
        };
    new AlertDialog.Builder(context)
        .setMessage(getPromptMessage(hasBarcode, hasPrice, hasPicture))
        .setPositiveButton("Yes", dialogClickListener)
        .setNegativeButton("No", dialogClickListener)
        .show();
  }

  private static String getPromptMessage(boolean hasBarcode, boolean hasPrice, boolean hasPicture) {
    if (!hasBarcode && !hasPrice && !hasPicture) {
      return "Do you want to add a Barcode, a Price or a Picture to your Item?";
    } else if (!hasBarcode && !hasPrice) {
      return "Do you want to add a Barcode or a Price to your Item?";
    } else if (!hasBarcode && !hasPicture) {
      return "Do you want to add a Barcode or a Picture to your Item?";
    } else if (!hasPrice && !hasPicture) {
      return "Do you want to add a Price or a Picture to your Item?";
    } else if (!hasBarcode) {
      return "Do you want to add a Barcode to your Item?";
    } else if (!hasPrice) {
      return "Do you want to add a Price to your Item?";
    } else {
      return "Do you want to add a Picture to your Item?";
    }
  }

  public interface AlertDialogCallback {
    void onChosen(Integer choice);
  }

  public interface PromptDialogCallback {
    void onPrompt();
  }
}
