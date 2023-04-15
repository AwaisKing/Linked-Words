package awais.backworddictionary;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDexApplication;

import com.applovin.mediation.MaxMediatedNetworkInfo;
import com.applovin.sdk.AppLovinSdk;
import com.inmobi.sdk.InMobiSdk;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.List;

import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;

public final class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium;

    @Override
    public void onCreate() {
        super.onCreate();

        SettingsHelper.setPreferences(this);

        Typeface fontBold = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Resources resources = getResources();
            try {
                fontRegular = resources.getFont(R.font.googlesans_regular);
                fontMedium = resources.getFont(R.font.googlesans_medium);
                fontBold = resources.getFont(R.font.googlesans_bold);
            } catch (final Exception e) {
                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "LinkedApp", e);
            }
        }

        if (fontRegular == null) fontRegular = ResourcesCompat.getFont(this, R.font.googlesans_regular);
        if (fontMedium == null) fontMedium = ResourcesCompat.getFont(this, R.font.googlesans_medium);
        if (fontBold == null) fontBold = ResourcesCompat.getFont(this, R.font.googlesans_bold);

        Utils.setDefaultFont("DEFAULT", fontMedium);
        Utils.setDefaultFont("SANS_SERIF", fontMedium);
        Utils.setDefaultFont("SERIF", fontMedium);
        Utils.setDefaultFont("MONOSPACE", fontMedium);
        Utils.setDefaultFont("DEFAULT_BOLD", fontBold);

        {
            if (SettingsHelper.showAds()) {
                InMobiSdk.setLogLevel(BuildConfig.DEBUG ? InMobiSdk.LogLevel.DEBUG : InMobiSdk.LogLevel.NONE);
                if (!InMobiSdk.isSDKInitialized())
                    InMobiSdk.init(this, "56047691bf7041f0a0b29e2497650619", null, null);

                StartAppSDK.init(this, getString(R.string.startio_APP_ID), false);
                StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
                StartAppSDK.enableReturnAds(!BuildConfig.DEBUG);
                StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), false);
                StartAppAd.disableSplash();
                if (BuildConfig.DEBUG) StartAppAd.disableAutoInterstitial();
                else StartAppAd.enableAutoInterstitial();
            }

            final AppLovinSdk instance = AppLovinSdk.getInstance(this);
            instance.setMediationProvider("max");
            AppLovinSdk.initializeSdk(this, initStatus -> {
            });
        }
    }
}