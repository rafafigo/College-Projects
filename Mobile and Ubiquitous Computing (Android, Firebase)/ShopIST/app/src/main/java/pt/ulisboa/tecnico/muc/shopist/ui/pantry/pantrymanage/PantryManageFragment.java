package pt.ulisboa.tecnico.muc.shopist.ui.pantry.pantrymanage;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.muc.shopist.R;
import pt.ulisboa.tecnico.muc.shopist.ui.common.CommonViewModel;

public class PantryManageFragment extends Fragment {

  public static final String PANTRY_ID_KEY = "PANTRY_ID_KEY";
  private String pantryId;
  private CommonViewModel commonViewModel;
  private List<Map.Entry<String, String>> emails;
  private EditText emailET;
  private MyPantryManageAdapter adapter;

  public static Bundle newBundle(String pantryId) {
    Bundle bundle = new Bundle();
    bundle.putString(PANTRY_ID_KEY, pantryId);
    return bundle;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_pantrymanage_list, container, false);

    this.pantryId = requireArguments().getString(PANTRY_ID_KEY);
    this.commonViewModel = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);

    this.adapter = new MyPantryManageAdapter(this, this.commonViewModel, this.pantryId);
    ((RecyclerView) view.findViewById(R.id.pantrymanage_recycler)).setAdapter(adapter);

    ProgressDialog pDialog =
        ProgressDialog.show(getContext(), "Fetching Users!", "Please Wait!", true, false);
    new Thread(
            () -> {
              this.emails = this.commonViewModel.getEmails(this.pantryId);
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        if (this.emails != null) {
                          this.adapter.setEmails(this.emails);
                        } else {
                          Toast.makeText(getContext(), "Users Fetch Failed!", Toast.LENGTH_LONG)
                              .show();
                          requireActivity().getSupportFragmentManager().popBackStack();
                        }
                        pDialog.dismiss();
                      });
            })
        .start();

    ((EditText) view.findViewById(R.id.pantrymanage_searchbar))
        .addTextChangedListener(new SearchbarTextWatcher());

    this.emailET = view.findViewById(R.id.pantrymanage_box);
    view.findViewById(R.id.pantrymanage_add_fab).setOnClickListener(v -> this.onAddUser());
    return view;
  }

  private void onAddUser() {
    String newEmail = this.emailET.getText().toString().trim();
    if (newEmail.isEmpty()) {
      Toast.makeText(getContext(), "Insert an Email!", Toast.LENGTH_LONG).show();
      return;
    }
    for (Map.Entry<String, String> email : this.emails) {
      if (newEmail.equals(email.getValue())) {
        Toast.makeText(getContext(), "User Already Added!", Toast.LENGTH_LONG).show();
        return;
      }
    }

    ProgressDialog pDialog =
        ProgressDialog.show(getContext(), "Adding User!", "Please Wait!", true, false);
    new Thread(
            () -> {
              String uid = this.commonViewModel.addUserToPantry(this.pantryId, newEmail);
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        if (uid != null) {
                          this.adapter.addEmail(new AbstractMap.SimpleEntry<>(uid, newEmail));
                        } else {
                          Toast.makeText(getContext(), "Invalid Email!", Toast.LENGTH_LONG).show();
                        }
                        pDialog.dismiss();
                      });
            })
        .start();
  }

  private void filterEmails(CharSequence s) {
    if (s.length() == 0) {
      this.adapter.setEmails(this.emails);
      return;
    }
    List<Map.Entry<String, String>> filteredEmails = new ArrayList<>();
    for (Map.Entry<String, String> email : this.emails) {
      if (email.getValue() != null && email.getValue().contains(s)) filteredEmails.add(email);
    }
    this.adapter.setEmails(filteredEmails);
  }

  private class SearchbarTextWatcher implements TextWatcher {

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      filterEmails(s);
    }
  }
}
