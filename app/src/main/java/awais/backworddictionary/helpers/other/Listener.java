package awais.backworddictionary.helpers.other;

import android.view.View;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;

public final class Listener implements MaxAdViewAdListener {
    private final View v;
    public Listener(final View v) {this.v = v;}

    @Override public void onAdClicked(final MaxAd ad) {}

    @Override public void onAdLoaded(final MaxAd ad) {v.setVisibility(View.VISIBLE);}
    @Override public void onAdExpanded(final MaxAd ad) {v.setVisibility(View.VISIBLE);}
    @Override public void onAdDisplayed(final MaxAd ad) {v.setVisibility(View.VISIBLE);}

    @Override public void onAdHidden(final MaxAd ad) {v.setVisibility(View.GONE);}
    @Override public void onAdCollapsed(final MaxAd ad) {v.setVisibility(View.GONE);}
    @Override public void onAdLoadFailed(final String adUnitId, final MaxError error) {v.setVisibility(View.GONE);}
    @Override public void onAdDisplayFailed(final MaxAd ad, final MaxError error) {v.setVisibility(View.GONE);}
}