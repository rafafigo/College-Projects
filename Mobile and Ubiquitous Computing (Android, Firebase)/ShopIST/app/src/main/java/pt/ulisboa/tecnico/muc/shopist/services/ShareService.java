package pt.ulisboa.tecnico.muc.shopist.services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.DynamicLink.AndroidParameters;
import com.google.firebase.dynamiclinks.DynamicLink.SocialMetaTagParameters;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import timber.log.Timber;

public class ShareService {

  public static void onShareItem(Fragment fragment, Item item) {
    String crowdItemId = item.getCrowdItemId();
    String name = item.getName();
    String barcode = item.getBarcode();
    Uri pictureUri = CommonViewModel.getInstance().getPictureUri(item);
    if (crowdItemId == null) {
      fragment
          .requireActivity()
          .runOnUiThread(
              () ->
                  Toast.makeText(fragment.getContext(), "Share Unavailable", Toast.LENGTH_LONG)
                      .show());
      return;
    }
    String title = String.format("Add item '%s' ", name);
    String url = String.format("https://shopist/item?name=%s&crowd=%s", name, crowdItemId);
    if (barcode != null) title += String.format("with barcode '%s' ", barcode);
    SocialMetaTagParameters.Builder previewBuilder =
        new DynamicLink.SocialMetaTagParameters.Builder();
    previewBuilder.setTitle(title.split("Add item ")[1]);

    if (pictureUri != null) previewBuilder.setImageUrl(pictureUri);

    DynamicLink dynamicLink =
        FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(url))
            .setDomainUriPrefix("https://shopist.page.link")
            .setAndroidParameters(new AndroidParameters.Builder().build())
            .setSocialMetaTagParameters(previewBuilder.build())
            .buildDynamicLink();
    new GoogleApi().onShortLink(fragment, title, dynamicLink.getUri().toString());
  }

  public static void onShareList(Activity activity, Dialog pDialog, String token) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if (token == null) {
      activity.runOnUiThread(() -> Timber.d("Error Generating QR Code"));
    }
    Bitmap bmp;
    try {
      bmp = new BarcodeEncoder().encodeBitmap(token, BarcodeFormat.QR_CODE, 512, 512);
    } catch (WriterException e) {
      activity.runOnUiThread(
          () -> {
            Timber.d("Error: Generating QRCode");
            pDialog.dismiss();
          });
      return;
    }
    ImageView imageView = new ImageView(activity);
    activity.runOnUiThread(
        () -> {
          pDialog.dismiss();
          imageView.setImageBitmap(bmp);
          imageView.setAdjustViewBounds(true);
          imageView.setMinimumWidth(512);
          imageView.setMinimumHeight(512);
          builder.setView(imageView);
          builder.create().show();
        });
  }
}
