package pt.ulisboa.tecnico.muc.shopist.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.firebase.FireAuth.AuthResponse;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class LoginFragment extends Fragment {

  private static final int GOOGLE_REQUEST_CODE = 0;
  private EditText usernameEditText;
  private EditText passwordEditText;
  private GoogleSignInClient googleSignInClient;
  private Button linkWithEmailAndPassword;
  private Button signInWithEmailAndPassword;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_login, container, false);

    this.usernameEditText = view.findViewById(R.id.username);
    this.passwordEditText = view.findViewById(R.id.password);
    SignInButton linkWithGoogle = view.findViewById(R.id.login_with_google);
    this.linkWithEmailAndPassword = view.findViewById(R.id.register);
    this.signInWithEmailAndPassword = view.findViewById(R.id.login);

    linkWithGoogle.setOnClickListener(this::linkWithGoogle);
    linkWithEmailAndPassword.setOnClickListener(this::linkWithEmailAndPassword);
    signInWithEmailAndPassword.setOnClickListener(this::signInWithEmailAndPassword);

    this.usernameEditText.addTextChangedListener(new FieldsWatcher());
    this.passwordEditText.addTextChangedListener(new FieldsWatcher());

    GoogleSignInOptions googleSignInOptions =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
    this.googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);
    return view;
  }

  public void linkWithGoogle(View view) {
    startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_REQUEST_CODE);
  }

  public void linkWithEmailAndPassword(View view) {
    this.doLogin(
        () ->
            CommonViewModel.getInstance()
                .linkWithEmailAndPassword(
                    this.usernameEditText.getText().toString(),
                    this.passwordEditText.getText().toString()));
  }

  public void signInWithEmailAndPassword(View view) {
    this.doLogin(
        () ->
            CommonViewModel.getInstance()
                .signInWithEmailAndPassword(
                    this.usernameEditText.getText().toString(),
                    this.passwordEditText.getText().toString()));
  }

  private void doLogin(LoginCallback loginCallback) {
    ProgressDialog pDialog =
        ProgressDialog.show(getContext(), "Logging In!", "Please Wait!", true, false);
    new Thread(
            () -> {
              AuthResponse authResponse = loginCallback.doLogin();
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        pDialog.dismiss();
                        if (authResponse.success()) {
                          Toast.makeText(
                                  getContext(), "User Successfully Logged In!", Toast.LENGTH_LONG)
                              .show();
                          this.onLoggedIn();
                        } else {
                          Toast.makeText(
                                  getContext(), authResponse.getErrorMsg(), Toast.LENGTH_LONG)
                              .show();
                        }
                      });
            })
        .start();
  }

  public void onLoggedIn() {
    requireActivity().getSupportFragmentManager().popBackStack();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == GOOGLE_REQUEST_CODE) {
      Task<GoogleSignInAccount> googleSignInAccountTask =
          GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount googleSignInAccount =
            googleSignInAccountTask.getResult(ApiException.class);
        this.doLogin(
            () -> CommonViewModel.getInstance().linkWithGoogle(googleSignInAccount.getIdToken()));
      } catch (ApiException e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      }
    }
  }

  private interface LoginCallback {
    AuthResponse doLogin();
  }

  private class FieldsWatcher implements TextWatcher {

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      boolean isEnabled =
          usernameEditText.getText().length() > 0 && passwordEditText.getText().length() > 0;
      if (linkWithEmailAndPassword.isEnabled() != isEnabled)
        linkWithEmailAndPassword.setEnabled(isEnabled);
      if (signInWithEmailAndPassword.isEnabled() != isEnabled)
        signInWithEmailAndPassword.setEnabled(isEnabled);
    }
  }
}
