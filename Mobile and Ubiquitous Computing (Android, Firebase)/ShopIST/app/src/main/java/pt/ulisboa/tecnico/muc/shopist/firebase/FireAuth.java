package pt.ulisboa.tecnico.muc.shopist.firebase;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class FireAuth {

  private final FireManager fireManager;
  private final FirebaseAuth firebaseAuth;

  public FireAuth(FireManager fireManager, FirebaseAuth firebaseAuth) {
    this.fireManager = fireManager;
    this.firebaseAuth = firebaseAuth;
  }

  protected FirebaseUser signInAnonymously() {
    try {
      Tasks.await(this.firebaseAuth.signInAnonymously());
      return this.firebaseAuth.getCurrentUser();
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
  }

  protected AuthResponse linkWithGoogle(FirebaseUser user, String idToken) {
    AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
    try {
      AuthResult authResult = Tasks.await(user.linkWithCredential(authCredential));
      user = Objects.requireNonNull(authResult.getUser());
      this.fireManager.updateUser(user.getUid(), user.getEmail());
      return new AuthResponse(user);
    } catch (ExecutionException | InterruptedException e) {
      if (e.getCause() instanceof FirebaseAuthUserCollisionException) {
        return this.signInWithCredential(authCredential);
      }
      return new AuthResponse(e.getCause());
    }
  }

  protected AuthResponse linkWithEmailAndPassword(FirebaseUser user, String email, String pwd) {
    AuthCredential authCredential = EmailAuthProvider.getCredential(email, pwd);
    try {
      AuthResult authResult = Tasks.await(user.linkWithCredential(authCredential));
      user = Objects.requireNonNull(authResult.getUser());
      this.fireManager.updateUser(user.getUid(), user.getEmail());
      return new AuthResponse(user);
    } catch (ExecutionException | InterruptedException e) {
      return new AuthResponse(e.getCause());
    }
  }

  protected AuthResponse signInWithEmailAndPassword(String email, String pwd) {
    AuthCredential authCredential = EmailAuthProvider.getCredential(email, pwd);
    return this.signInWithCredential(authCredential);
  }

  private AuthResponse signInWithCredential(AuthCredential authCredential) {
    try {
      AuthResult authResult = Tasks.await(this.firebaseAuth.signInWithCredential(authCredential));
      FirebaseUser user = Objects.requireNonNull(authResult.getUser());
      this.fireManager.updateUser(user.getUid(), user.getEmail());
      return new AuthResponse(user);
    } catch (ExecutionException | InterruptedException e) {
      return new AuthResponse(e.getCause());
    }
  }

  protected void signOut() {
    this.firebaseAuth.signOut();
  }

  public static class AuthResponse {
    private final FirebaseUser firebaseUser;
    private final Throwable throwable;

    public AuthResponse(FirebaseUser firebaseUser) {
      this.firebaseUser = firebaseUser;
      this.throwable = null;
    }

    public AuthResponse(Throwable throwable) {
      this.firebaseUser = null;
      this.throwable = throwable;
    }

    public FirebaseUser getFirebaseUser() {
      return firebaseUser;
    }

    public String getErrorMsg() {
      return this.throwable != null ? this.throwable.getMessage() : null;
    }

    public boolean success() {
      return firebaseUser != null;
    }
  }
}
