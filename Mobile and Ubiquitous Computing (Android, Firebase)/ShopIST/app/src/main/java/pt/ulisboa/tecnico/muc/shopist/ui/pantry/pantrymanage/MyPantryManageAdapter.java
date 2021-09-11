package pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrymanage;

import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link User}. */
public class MyPantryManageAdapter extends RecyclerView.Adapter<MyPantryManageAdapter.ViewHolder> {

  private final PantryManageFragment fragment;
  private final CommonViewModel commonViewModel;
  private final String pantryId;
  private List<Map.Entry<String, String>> emails;

  protected MyPantryManageAdapter(
      PantryManageFragment fragment, CommonViewModel commonViewModel, String pantryId) {
    this.fragment = fragment;
    this.commonViewModel = commonViewModel;
    this.pantryId = pantryId;
    this.emails = new ArrayList<>();
  }

  protected void setEmails(List<Map.Entry<String, String>> emails) {
    this.emails = emails;
    notifyDataSetChanged();
  }

  protected void addEmail(Map.Entry<String, String> email) {
    this.emails.add(0, email);
    notifyItemInserted(0);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_pantrymanage, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.onBindUser(this.emails.get(position));
    holder.myRemoveButton.setOnClickListener(
        v -> {
          ProgressDialog pDialog =
              ProgressDialog.show(
                  fragment.getContext(), "Removing User!", "Please Wait!", true, false);
          new Thread(() -> this.onRemoveUser(holder, pDialog)).start();
        });
  }

  public void onRemoveUser(ViewHolder holder, ProgressDialog pDialog) {
    boolean success =
        this.commonViewModel.removeUserFromPantry(this.pantryId, holder.email.getKey());
    this.fragment
        .requireActivity()
        .runOnUiThread(
            () -> {
              if (success) {
                int position = holder.getLayoutPosition();
                this.emails.remove(position);
                notifyItemRemoved(position);
              } else {
                Toast.makeText(fragment.getContext(), "User Removal Failed!", Toast.LENGTH_LONG)
                    .show();
              }
              pDialog.dismiss();
            });
  }

  @Override
  public int getItemCount() {
    return this.emails.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView myEmailView;
    private final ImageView myRemoveButton;
    private Map.Entry<String, String> email;

    public ViewHolder(View view) {
      super(view);
      this.myEmailView = view.findViewById(R.id.pantrymanage_email);
      this.myRemoveButton = view.findViewById(R.id.pantrymanage_remove);
    }

    private void onBindUser(Map.Entry<String, String> email) {
      this.email = email;
      this.myEmailView.setText(email.getValue() != null ? email.getValue() : email.getKey());
    }
  }
}
