package pt.ulisboa.tecnico.muc.shopist.services;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleApiClient {

  public static final String GOOGLE_KEY_API = "AIzaSyBrhvCS7QQoz8bHYrQq9pogvSIYEX3BQbg";
  public static final String FIREBASE_KEY_API = "AIzaSyAH3MYPxbFfT4dcHmAgowD42TcGshXD0rU";
  public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com/";
  public static final String FIREBASE_BASE_URL = "https://firebasedynamiclinks.googleapis.com/v1/";
  public static Retrofit googleRetrofit;
  public static Retrofit firebaseRetrofit;

  public static Retrofit newClient(boolean isGoogle) {

    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient okHttpClient =
        new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build();

    return new Retrofit.Builder()
        .baseUrl(isGoogle ? GOOGLE_BASE_URL : FIREBASE_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build();
  }

  public static Retrofit getGoogleInstance() {
    if (googleRetrofit == null) {
      googleRetrofit = newClient(true);
    }
    return googleRetrofit;
  }

  public static Retrofit getFirebaseInstance() {
    if (firebaseRetrofit == null) {
      firebaseRetrofit = newClient(false);
    }
    return firebaseRetrofit;
  }
}
