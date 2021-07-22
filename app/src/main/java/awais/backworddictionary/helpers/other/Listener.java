package awais.backworddictionary.helpers.other;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;

public final class Listener extends AdListener {
    private final View v;
    public Listener(final View v) { this.v = v; }
    @Override public void onAdLoaded() { v.setVisibility(View.VISIBLE); }
    @Override public void onAdClosed() { v.setVisibility(View.GONE); }
    @Override public void onAdFailedToLoad(@NonNull final LoadAdError loadAdError) { v.setVisibility(View.GONE); }
}
