package pt.ulisboa.tecnico.muc.shopist.firebase;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FireFunctions {

  private final FirebaseFunctions firebaseFunctions;

  protected FireFunctions() {
    this.firebaseFunctions = FirebaseFunctions.getInstance();
  }

  protected String getToken(String funcName, Map<String, String> data)
      throws ExecutionException, InterruptedException {
    return Tasks.await(
        this.firebaseFunctions
            .getHttpsCallable(funcName)
            .call(data)
            .continueWith(
                task -> {
                  Object token = task.getResult().getData();
                  return token != null ? (String) token : null;
                }));
  }

  protected Object getShared(String token) throws ExecutionException, InterruptedException {
    Map<String, Object> data = Collections.singletonMap("token", token);
    return Tasks.await(this.firebaseFunctions.getHttpsCallable("getListId").call(data)).getData();
  }

  protected void checkIn(String crowdShoppingId, int numberOfItems)
      throws ExecutionException, InterruptedException {
    Map<String, Object> data = new HashMap<>();
    data.put("crowdShoppingId", crowdShoppingId);
    data.put("numberOfItems", numberOfItems);
    Tasks.await(this.firebaseFunctions.getHttpsCallable("checkIn").call(data));
  }

  protected void checkOut(String crowdShoppingId, int numberOfItems)
      throws ExecutionException, InterruptedException {
    Map<String, Object> data = new HashMap<>();
    data.put("crowdShoppingId", crowdShoppingId);
    data.put("numberOfItems", numberOfItems);
    Tasks.await(this.firebaseFunctions.getHttpsCallable("checkOut").call(data));
  }

  protected void updateSmartSort(
      String crowdShoppingId, String bCrowdItemId, List<String> aCrowdItemIds)
      throws ExecutionException, InterruptedException {
    Map<String, Object> data = new HashMap<>();
    data.put("crowdShoppingId", crowdShoppingId);
    data.put("bCrowdItemId", bCrowdItemId);
    data.put("aCrowdItemIds", aCrowdItemIds);
    Tasks.await(this.firebaseFunctions.getHttpsCallable("updateSmartSort").call(data));
  }
}
