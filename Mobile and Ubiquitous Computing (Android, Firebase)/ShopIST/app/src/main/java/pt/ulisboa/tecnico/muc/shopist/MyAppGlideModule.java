package pt.ulisboa.tecnico.muc.shopist;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

  private static final int CACHE_SIZE_BYTES = 10485760; // 10 MB

  @Override
  public void registerComponents(
      @NotNull Context context, @NotNull Glide glide, Registry registry) {
    registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
  }

  @Override
  public void applyOptions(@NotNull Context context, GlideBuilder builder) {
    // LRU By Default
    builder.setDiskCache(new InternalCacheDiskCacheFactory(context, CACHE_SIZE_BYTES));
  }
}
