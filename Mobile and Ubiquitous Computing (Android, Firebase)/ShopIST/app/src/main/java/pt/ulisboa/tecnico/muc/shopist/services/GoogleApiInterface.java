package pt.ulisboa.tecnico.muc.shopist.services;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface GoogleApiInterface {

  @GET("maps/api/distancematrix/json")
  Call<ResultDistanceMatrix> getDistanceMatrix(@QueryMap Map<String, String> parameters);

  @GET("maps/api/staticmap")
  Call<ResponseBody> getStaticMap(@QueryMap Map<String, String> parameters);

  @Headers({"Content-Type: application/json"})
  @POST("shortLinks")
  Call<ResultShortLink> getShortLink(@Query("key") String key, @Body Map<String, String> link);
}
