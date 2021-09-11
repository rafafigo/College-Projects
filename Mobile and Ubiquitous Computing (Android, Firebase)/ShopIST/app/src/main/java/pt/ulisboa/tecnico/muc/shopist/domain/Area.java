package pt.ulisboa.tecnico.muc.shopist.domain;

import android.annotation.SuppressLint;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;

public abstract class Area extends DomainComparator {

  private final Location location;
  private final MutableLiveData<Long> duration;
  private String name;

  protected Area(String id, String name, Timestamp timestamp) {
    super(id, timestamp);
    this.name = name;
    this.location = new Location("");
    this.duration = new MutableLiveData<>();
  }

  @SuppressLint("DefaultLocale")
  public static String formatTime(Long duration) {
    if (duration > 86400) return "Days";
    if (duration >= 3600) return String.format("%d Hr", Math.round(duration / 3600.0));
    if (duration >= 60) return String.format("%d Min", Math.round(duration / 60.0));
    return String.format("%d Sec", Math.round(duration));
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLocation(String locationName, Double latitude, Double longitude) {
    this.location.setProvider(locationName);
    this.location.setLatitude(latitude);
    this.location.setLongitude(longitude);
  }

  public Location getLocation() {
    return this.location;
  }

  public double getLatitude() {
    return this.location.getLatitude();
  }

  public double getLongitude() {
    return this.location.getLongitude();
  }

  public LiveData<Long> getDuration() {
    return this.duration;
  }

  public void onDurationChanged(Long duration) {
    this.duration.setValue(duration);
  }
}
