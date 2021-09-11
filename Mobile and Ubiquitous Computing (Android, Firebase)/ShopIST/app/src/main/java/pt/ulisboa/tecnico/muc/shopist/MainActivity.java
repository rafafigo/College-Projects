package pt.ulisboa.tecnico.muc.shopist;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.muc.shopist.domain.Area;
import pt.ulisboa.tecnico.muc.shopist.domain.Item;
import pt.ulisboa.tecnico.muc.shopist.domain.Pantry;
import pt.ulisboa.tecnico.muc.shopist.domain.PantryItem;
import pt.ulisboa.tecnico.muc.shopist.domain.Shopping;
import pt.ulisboa.tecnico.muc.shopist.services.DomainService;
import pt.ulisboa.tecnico.muc.shopist.services.GoogleApi;
import pt.ulisboa.tecnico.muc.shopist.services.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.common.itemform.ItemFormFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrylist.PantryListFragment;
import pt.ulisboa.tecnico.muc.shopist.ui.settings.SettingsActivity;
import pt.ulisboa.tecnico.muc.shopist.ui.shopping.shoppinglist.ShoppingListFragment;
import timber.log.Timber;

import static pt.ulisboa.tecnico.muc.shopist.ui.settings.SettingsActivity.SettingsFragment.setDefaultMode;

public class MainActivity extends AppCompatActivity {

  private static final int PERMISSIONS_REQUEST_CODE = 0;
  private static final int SETTINGS_REQUEST_CODE = 1;
  private final GoogleApi googleApi = new GoogleApi();
  private final ServiceConnection connection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {}

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
      };
  private AppBarConfiguration mAppBarConfiguration;
  private Location lastLocation;
  private CommonViewModel commonViewModel;
  private NavController navController;
  private SimWifiP2pBroadcastReceiver receiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setAppTheme();
    super.onCreate(savedInstanceState);
    if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    setContentView(R.layout.activity_main);
    requestPermissions();

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    this.commonViewModel = new ViewModelProvider(this).get(CommonViewModel.class);
    this.commonViewModel.setMainActivity(this);
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);

    mAppBarConfiguration =
        new AppBarConfiguration.Builder(navController.getGraph())
            .setOpenableLayout(findViewById(R.id.drawer_layout))
            .build();

    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(
        (NavigationView) findViewById(R.id.nav_view), navController);

    this.doDataFetch();
    IntentFilter filter = new IntentFilter();
    filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
    receiver = new SimWifiP2pBroadcastReceiver();
    registerReceiver(receiver, filter);
    bindService();
  }

  private void doDataFetch() {
    ProgressDialog pDialog =
        ProgressDialog.show(this, "Fetching Data!", "Please Wait!", true, false);
    new Thread(
            () -> {
              this.commonViewModel.newDB();
              runOnUiThread(
                  () -> {
                    this.runLocation();
                    boolean doStartDestination = true;
                    try {
                      doStartDestination = !this.checkIntentItem();
                    } catch (UnsupportedEncodingException ignored) {
                    }
                    if (doStartDestination) this.setAppStartDestination();
                    pDialog.dismiss();
                    this.commonViewModel.preloadPictures();
                  });
            })
        .start();
  }

  private boolean checkIntentItem() throws UnsupportedEncodingException {
    Uri data = getIntent().getData();
    if (data != null && data.getQuery() != null && data.getPath().equals("/item")) {
      Map<String, String> queryPairs = new HashMap<>();
      for (String pair : data.getQuery().split("&")) {
        int idx = pair.indexOf("=");
        queryPairs.put(
            URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()),
            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
      }
      this.handleIntentItem(queryPairs);
      return true;
    }
    return false;
  }

  public void setAppStartDestination() {
    if (PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.default_page_location), false)
        && hasLocationPermissions()) {
      LocationServices.getFusedLocationProviderClient(this)
          .getLastLocation()
          .addOnCompleteListener(this::onStartUpLocation);
    }
  }

  private void onStartUpLocation(Task<Location> task) {
    Location location = task.getResult();

    if (location != null && commonViewModel.isUniqueAreaLocation(location)) {
      Area startAppArea = commonViewModel.getStartAppArea();

      if (startAppArea instanceof Pantry) {
        this.navController.navigate(
            R.id.action_home_to_pantry_list, PantryListFragment.newBundle(startAppArea.getId()));
      } else if (startAppArea instanceof Shopping) {
        this.navController.navigate(
            R.id.action_home_to_shopping_list,
            ShoppingListFragment.newBundle(startAppArea.getId()));
      }
    }
  }

  private void setAppTheme() {
    boolean isDark =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.change_theme), true);
    setDefaultMode(isDark);
  }

  private void requestPermissions() {
    if (!this.hasPermissions()) {
      String[] permissions =
          new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE
          };
      ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
    }
  }

  public boolean hasPermissions() {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        && hasLocationPermissions();
  }

  public boolean hasLocationPermissions() {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
  }

  public void runLocation() {
    if (hasLocationPermissions()) {
      LocationServices.getFusedLocationProviderClient(MainActivity.this)
          .getLastLocation()
          .addOnCompleteListener((task) -> locationListener(task.getResult()));
      updateLastLocation();
    }
  }

  public void locationListener(Location location) {
    if (location != null && (lastLocation == null || location.distanceTo(lastLocation) >= 500)) {
      lastLocation = location;
      onLocationUpdate();
    }
  }

  public void updateLastLocation() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(20 * 1000);
    LocationCallback locationCallback =
        new LocationCallback() {
          @Override
          public void onLocationResult(LocationResult locationResult) {
            locationListener(locationResult.getLastLocation());
          }
        };
    LocationServices.getFusedLocationProviderClient(MainActivity.this)
        .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
  }

  public void onLocationUpdate() {
    if (this.lastLocation != null) {
      googleApi.onUpdateDistanceMatrix(
          this.lastLocation,
          new ArrayList<Area>() {
            {
              addAll(commonViewModel.getPantryList());
              addAll(commonViewModel.getShoppingList());
            }
          });
    }
  }

  private void handleIntentItem(Map<String, String> queryPairs) {
    if (!queryPairs.containsKey("name") || !queryPairs.containsKey("crowd")) {
      Toast.makeText(this, "Bad Share Link!", Toast.LENGTH_LONG).show();
      return;
    }
    if (this.commonViewModel.getPantryMap().size() == 0) {
      Toast.makeText(this, "Please Create a Pantry!", Toast.LENGTH_LONG).show();
      return;
    }
    String itemName = queryPairs.get("name");
    String crowdItemId = queryPairs.get("crowd");
    Map<String, String> associatedPantries = DomainService.getParsedPantries();
    String[] options = associatedPantries.keySet().toArray(new String[0]);
    ProgressDialog pDialog =
        ProgressDialog.show(this, "Fetching Item!", "Please Wait!", true, false);
    if (options.length == 1) {
      new Thread(
              () ->
                  this.createPantryItem(
                      associatedPantries.get(options[0]), itemName, crowdItemId, pDialog))
          .start();
    } else {
      DomainService.choosePantry(
          this,
          options,
          choice ->
              new Thread(
                      () ->
                          this.createPantryItem(
                              associatedPantries.get(options[choice]),
                              itemName,
                              crowdItemId,
                              pDialog))
                  .start());
    }
  }

  private void createPantryItem(
      String pantryId, String itemName, String crowdItemId, ProgressDialog pDialog) {
    Item item = this.commonViewModel.addLinkedItem(crowdItemId, itemName);
    if (item != null) {
      item.putPantryItem(
          new PantryItem(pantryId, this.commonViewModel.getPantry(pantryId).getTimestamp()));
    }
    runOnUiThread(
        () -> {
          if (item != null) {
            this.navController.navigate(
                R.id.action_home_to_item_form,
                ItemFormFragment.newPantryEditBundle(pantryId, ItemFormFragment.TEMP_ITEM_ID));
          } else {
            Toast.makeText(this, "The Item is no Longer Available!", Toast.LENGTH_LONG).show();
          }
          pDialog.dismiss();
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      getSupportFragmentManager().popBackStack();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }

  private void bindService() {
    Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST_CODE) this.runLocation();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SETTINGS_REQUEST_CODE
        && resultCode == SettingsActivity.HAS_LOGGED_OUT_RESULT_CODE) {
      this.navController.popBackStack(R.id.nav_home, false);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(receiver);
    unbindService(connection);
  }
}
