package awais.backworddictionary;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDexApplication;

import com.applovin.mediation.MaxMediatedNetworkInfo;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;

import java.util.List;

import awais.backworddictionary.helpers.AppHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.Utils;

public final class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium;

    public LinkedApp() {
        super();
        TTSHelper.linkedAppContext = this;
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        TTSHelper.linkedAppContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TTSHelper.linkedAppContext = this;
        AppHelper.getInstance(this);

        final int nightMode = SettingsHelper.getInstance(this).getNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(nightMode);

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

        final String applovinSDK = getString(R.string.apl_SDK_KEY);
        final AppLovinSdk instance = AppLovinSdk.getInstance(this);
        instance.initialize(AppLovinSdkInitializationConfiguration.builder(applovinSDK).setMediationProvider("max").build(), initStatus -> {
            if (!BuildConfig.DEBUG) return;
            if (!true) instance.showMediationDebugger();
            Log.d("AWAISKING_APP", "--------------------------------------------");
            final List<MaxMediatedNetworkInfo> availableMediatedNetworks = instance.getAvailableMediatedNetworks();

            Log.d("AWAISKING_APP", "appLovinInit: " + initStatus
                                   + " -- " + initStatus.getCountryCode()
                                   + " -- " + initStatus.getConsentDialogState()
                                   + " -- " + initStatus.getConsentFlowUserGeography()
                                   + " -- " + availableMediatedNetworks
                 );

            if (availableMediatedNetworks != null) for (final MaxMediatedNetworkInfo availableMediatedNetwork : availableMediatedNetworks) {
                Log.d("AWAISKING_APP", "availableMediatedNetwork: " + availableMediatedNetwork
                                       + " -- " + availableMediatedNetwork.getName()
                                       + " -- " + availableMediatedNetwork.getSdkVersion()
                                       + " -- " + availableMediatedNetwork.getAdapterClassName()
                                       + " -- " + availableMediatedNetwork.getAdapterVersion()
                     );
            }

            Log.d("AWAISKING_APP", "--------------------------------------------");
        });
    }
}