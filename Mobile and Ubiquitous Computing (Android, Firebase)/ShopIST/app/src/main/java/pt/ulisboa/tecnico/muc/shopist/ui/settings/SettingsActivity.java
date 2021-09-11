package pt.ulisboa.tecnico.muc.shopist.ui.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback;
import androidx.preference.SwitchPreferenceCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Objects;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.services.CameraService;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;
import pt.ulisboa.tecnico.muc.shopist.ui.login.LoginFragment;

public class SettingsActivity extends AppCompatActivity
    implements OnPreferenceStartFragmentCallback {

  public static final int HAS_LOGGED_OUT_RESULT_CODE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.preferences_layout, new SettingsFragment())
          .commit();
    }

    setSupportActionBar(findViewById(R.id.toolbar));
    ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
    actionBar.setTitle("Settings");
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    final Bundle args = pref.getExtras();

    final Fragment fragment =
        getSupportFragmentManager()
            .getFragmentFactory()
            .instantiate(getClassLoader(), pref.getFragment());

    fragment.setArguments(args);
    fragment.setTargetFragment(caller, 0);

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.preferences_layout, fragment)
        .addToBackStack(null)
        .commit();

    return true;
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {

    private @Nullable ViewGroup container;

    public static void setDefaultMode(boolean isDark) {
      AppCompatDelegate.setDefaultNightMode(
          isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
      this.container = container;
      String username = CommonViewModel.getInstance().getAccount();
      if (username != null) {
        Preference preference = Objects.requireNonNull(findPreference("Account"));
        preference.setTitle(username + " (logout)");
      }
      return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.main_preference, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
      if (preference.getKey().equals(getString(R.string.change_theme)))
        setDefaultMode(((SwitchPreferenceCompat) preference).isChecked());
      if (preference.getKey().equals(getString(R.string.qr_code_scan)))
        CameraService.onScanQRCode(this);
      if (preference.getKey().equals(getString(R.string.account))) manageAccount(preference);
      return true;
    }

    public void manageAccount(Preference preference) {
      if (preference.getTitle().equals((getString(R.string.login)))) {
        onLogin();
      } else {
        DialogInterface.OnClickListener dialogClickListener =
            (dialog, which) -> {
              if (which == DialogInterface.BUTTON_POSITIVE) {
                ProgressDialog pDialog =
                    ProgressDialog.show(getContext(), "Logging Out!", "Please Wait!", true, false);
                new Thread(
                        () -> {
                          CommonViewModel.getInstance().logoutAccount();
                          requireActivity()
                              .runOnUiThread(
                                  () -> {
                                    preference.setTitle(R.string.login);
                                    pDialog.dismiss();
                                    requireActivity().setResult(HAS_LOGGED_OUT_RESULT_CODE);
                                    requireActivity().finish();
                                  });
                        })
                    .start();
              }
            };
        new AlertDialog.Builder(getContext())
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show();
      }
    }

    public void onLogin() {
      requireActivity()
          .getSupportFragmentManager()
          .beginTransaction()
          .remove(this)
          .add(Objects.requireNonNull(this.container).getId(), new LoginFragment())
          .addToBackStack(null)
          .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      IntentResult intentResult =
          IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (intentResult != null) {
        if (intentResult.getContents() != null) {
          ProgressDialog pDialog =
              ProgressDialog.show(getContext(), "Syncing List!", "Please Wait!", true, false);
          new Thread(
                  () ->
                      this.fetchSharedList(
                          CommonViewModel.getInstance(), intentResult.getContents(), pDialog))
              .start();
        }
      } else {
        super.onActivityResult(requestCode, resultCode, data);
      }
    }

    public void fetchSharedList(
        CommonViewModel commonViewModel, String token, ProgressDialog pDialog) {
      int success = commonViewModel.getShared(token);
      requireActivity()
          .runOnUiThread(
              () -> {
                if (success == 0) {
                  Toast.makeText(requireContext(), "Invalid QR Code!", Toast.LENGTH_LONG).show();
                } else {
                  Toast.makeText(requireContext(), "List Added!", Toast.LENGTH_LONG).show();
                }
                pDialog.dismiss();
              });
    }
  }
}
