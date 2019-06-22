package awais.backworddictionary.custom;

import android.view.View;
import com.google.android.gms.ads.AdListener;

public class Listener extends AdListener {
    private final View v;
    public Listener(View v) { this.v = v; }
    @Override public void onAdLoaded() { v.setVisibility(View.VISIBLE); }
    @Override public void onAdFailedToLoad(int errorCode) { v.setVisibility(View.GONE); }
    @Override public void onAdClosed() { v.setVisibility(View.GONE); }
}
