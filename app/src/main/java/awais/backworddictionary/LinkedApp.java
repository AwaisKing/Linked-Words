package awais.backworddictionary;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDexApplication;

import com.applovin.mediation.MaxMediatedNetworkInfo;
import com.applovin.sdk.AppLovinSdk;

import java.util.List;

import awais.backworddictionary.helpers.AppHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;

public final class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium;

    @Override
    public void onCreate() {
        super.onCreate();

        SettingsHelper.setPreferences(this);
        AppHelper.getInstance(this);

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

        final AppLovinSdk instance = AppLovinSdk.getInstance(this);
        instance.setMediationProvider("max");
        AppLovinSdk.initializeSdk(this, initStatus -> {
            if (BuildConfig.DEBUG) instance.showMediationDebugger();
            if (BuildConfig.DEBUG) {
                Log.d("AWAISKING_APP", "--------------------------------------------");
                final List<MaxMediatedNetworkInfo> availableMediatedNetworks = instance.getAvailableMediatedNetworks();

                Log.d("AWAISKING_APP", "appLovinInit: " + initStatus
                                       + " -- " + initStatus.getCountryCode()
                                       + " -- " + initStatus.getConsentDialogState()
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
            }
        });
    }
}