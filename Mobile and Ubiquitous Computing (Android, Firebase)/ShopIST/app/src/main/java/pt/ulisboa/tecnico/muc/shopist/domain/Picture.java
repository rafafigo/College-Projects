package pt.ulisboa.tecnico.muc.shopist.domain;

import android.graphics.Bitmap;

public class Picture {

  private String pictureUri;
  private Bitmap pictureBmp;

  public Picture(String pictureUri) {
    this.pictureUri = pictureUri;
  }

  public Picture(Bitmap pictureBmp) {
    this.pictureBmp = pictureBmp;
  }

  public String getPictureUri() {
    return this.pictureUri;
  }

  public void setPictureUri(String pictureUri) {
    this.pictureUri = pictureUri;
  }

  public Bitmap getPictureBmp() {
    return this.pictureBmp;
  }
}
