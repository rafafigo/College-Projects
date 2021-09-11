package pt.ulisboa.tecnico.muc.shopist.services;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.domain.Area;
import pt.ulisboa.tecnico.muc.shopist.services.ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class GoogleApi {

  private final GoogleApiInterface googleApiService;
  private final GoogleApiInterface firebaseApiService;

  public GoogleApi() {
    this.googleApiService = GoogleApiClient.getGoogleInstance().create(GoogleApiInterface.class);
    this.firebaseApiService =
        GoogleApiClient.getFirebaseInstance().create(GoogleApiInterface.class);
  }

  @SuppressLint("DefaultLocale")
  public void onUpdateDistanceMatrix(Location location, List<Area> areas) {

    if (areas.isEmpty()) return;

    StringBuilder destinationsStr = new StringBuilder();
    for (Area area : areas) {
      destinationsStr.append(String.format("%f,%f|", area.getLatitude(), area.getLongitude()));
    }

    String originsValue = String.format("%f,%f", location.getLatitude(), location.getLongitude());
    String destinationsValue = destinationsStr.substring(0, destinationsStr.length() - 1);

    Call<ResultDistanceMatrix> call =
        googleApiService.getDistanceMatrix(
            new HashMap<String, String>() {
              {
                put("origins", originsValue);
                put("destinations", destinationsValue);
                put("key", GoogleApiClient.GOOGLE_KEY_API);
              }
            });

    call.enqueue(
        new Callback<ResultDistanceMatrix>() {
          @Override
          public void onResponse(
              @NotNull Call<ResultDistanceMatrix> call,
              @NotNull Response<ResultDistanceMatrix> response) {
            ResultDistanceMatrix resultDistanceMatrix = response.body();
            if ("OK".equalsIgnoreCase(resultDistanceMatrix.status)) {
              List<DistanceElement> infoDistanceMatrix = resultDistanceMatrix.rows.get(0).elements;
              for (int i = 0; i < infoDistanceMatrix.size(); i++) {
                DistanceElement distanceElement = infoDistanceMatrix.get(i);
                if ("OK".equalsIgnoreCase(distanceElement.status)) {
                  areas.get(i).onDurationChanged(distanceElement.duration.value);
                }
              }
            }
          }

          @Override
          public void onFailure(@NotNull Call<ResultDistanceMatrix> call, @NotNull Throwable e) {
            call.cancel();
            Timber.d(e, "Error: Duration Query Failed");
          }
        });
  }

  @SuppressLint("DefaultLocale")
  public void onStaticMap(Location location, Dialog dialog, ProgressDialog pDialog) {
    String centerValue = String.format("%f,%f", location.getLatitude(), location.getLongitude());
    String markerValue = "markers=size:mid|color:0xff0000|label:|" + centerValue;
    Call<ResponseBody> call =
        googleApiService.getStaticMap(
            new HashMap<String, String>() {
              {
                put("center", centerValue);
                put("zoom", "18");
                put("scale", "2");
                put("size", "470x350");
                put("markers", markerValue);
                put("key", GoogleApiClient.GOOGLE_KEY_API);
              }
            });
    call.enqueue(
        new Callback<ResponseBody>() {
          @Override
          public void onResponse(
              @NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
            if (response.body() != null) {
              Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
              if (bmp != null) {
                ((ImageView) dialog.findViewById(R.id.static_map)).setImageBitmap(bmp);
              }
            }
            pDialog.dismiss();
            dialog.show();
            dialog
                .getWindow()
                .setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          }

          @Override
          public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable e) {
            call.cancel();
            pDialog.dismiss();
            dialog.show();
            dialog
                .getWindow()
                .setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          }
        });
  }

  @SuppressLint("DefaultLocale")
  public void onShortLink(Fragment fragment, String title, String link) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    sendIntent.setType("text/plain");
    Call<ResultShortLink> call =
        firebaseApiService.getShortLink(
            GoogleApiClient.FIREBASE_KEY_API, Collections.singletonMap("longDynamicLink", link));
    call.enqueue(
        new Callback<ResultShortLink>() {
          @Override
          public void onResponse(
              @NotNull Call<ResultShortLink> call, @NotNull Response<ResultShortLink> response) {
            if (response.body() == null || response.body().shortLink == null) {
              fragment
                  .requireActivity()
                  .runOnUiThread(
                      () ->
                          Toast.makeText(
                                  fragment.getContext(),
                                  "Failed Generating Share Link!",
                                  Toast.LENGTH_LONG)
                              .show());
              return;
            }
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                String.format("%s to your ShopIST App!\n%s", title, response.body().shortLink));
            fragment.startActivity(Intent.createChooser(sendIntent, null));
          }

          @Override
          public void onFailure(@NotNull Call<ResultShortLink> call, @NotNull Throwable e) {
            fragment
                .requireActivity()
                .runOnUiThread(
                    () ->
                        Toast.makeText(
                                fragment.getContext(),
                                "Failed Generating Share Link!",
                                Toast.LENGTH_LONG)
                            .show());
          }
        });
  }
}
