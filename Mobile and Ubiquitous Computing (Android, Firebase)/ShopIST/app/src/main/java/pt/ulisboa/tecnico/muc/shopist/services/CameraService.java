package pt.ulisboa.tecnico.muc.shopist.services;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import timber.log.Timber;

public class CameraService {

  public static final int CHOOSE_PIC = 0;
  public static final int TAKE_PIC = 1;
  public static final String PICTURE_SUFFIX = ".jpg";
  public static final String FILEPROVIDER_AUTHORITY = "pt.ulisboa.tecnico.muc.shopist.fileprovider";
  public static final int PICTURE_WIDTH = 512;
  public static final int PICTURE_HEIGHT = 512;
  private static String picturePath;

  // Choose Picture From Gallery
  public static void onChoosePic(Fragment fragment) {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    intent.setAction(Intent.ACTION_GET_CONTENT);
    fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_PIC);
  }

  // Take Picture From Camera
  public static void onTakePic(Fragment fragment, Context context) {
    File pictureFile = getPictureFile(context);
    if (pictureFile == null) return;
    picturePath = String.format("file:%s", pictureFile.getAbsolutePath());
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    if (intent.resolveActivity(context.getPackageManager()) != null) {
      Uri pictureUri = FileProvider.getUriForFile(context, FILEPROVIDER_AUTHORITY, pictureFile);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
      fragment.startActivityForResult(intent, TAKE_PIC);
    }
  }

  public static void openCropFromUri(Fragment fragment, Context context, Uri srcUri) {
    File pictureFile = CameraService.getPictureFile(context);
    if (pictureFile != null) {
      openCropActivity(fragment, context, srcUri, Uri.fromFile(pictureFile));
    }
  }

  public static void openCropFromPath(Fragment fragment, Context context) {
    Uri pictureUri = Uri.parse(picturePath);
    openCropActivity(fragment, context, pictureUri, pictureUri);
  }

  private static void openCropActivity(Fragment fragment, Context context, Uri srcUri, Uri dstUri) {
    UCrop.of(srcUri, dstUri)
        .withAspectRatio(1, 1)
        .withMaxResultSize(PICTURE_WIDTH, PICTURE_HEIGHT)
        .start(context, fragment);
  }

  public static Bitmap uriToBmp(ContentResolver contentResolver, Uri uri) {
    try {
      return BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
    } catch (FileNotFoundException e) {
      Timber.d("Picture Not Found!");
      return null;
    }
  }

  public static File getPictureFile(Context context) {
    try {
      return File.createTempFile(
          UUID.randomUUID().toString(),
          PICTURE_SUFFIX,
          context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
    } catch (IOException e) {
      Timber.d("Could Not Create Picture File!");
      return null;
    }
  }

  // Barcode Scanner
  public static void onScanQRCode(Fragment fragment) {
    IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(fragment);
    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
    intentIntegrator.setPrompt("Scan a QRCode");
    intentIntegrator.setCameraId(0);
    intentIntegrator.setBeepEnabled(false);
    intentIntegrator.setOrientationLocked(false);
    intentIntegrator.initiateScan();
  }

  public static void onScanProductCode(Fragment fragment) {
    IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(fragment);
    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
    intentIntegrator.setPrompt("Scan a Product");
    intentIntegrator.setCameraId(0);
    intentIntegrator.setBeepEnabled(false);
    intentIntegrator.setOrientationLocked(false);
    intentIntegrator.initiateScan();
  }
}
