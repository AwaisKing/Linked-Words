package awais.backworddictionary;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;

public final class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium;

    @Override
    public void onCreate() {
        super.onCreate();

        SettingsHelper.setPreferences(this);
        Utils.firebaseCrashlytics = FirebaseCrashlytics.getInstance();

        Typeface fontBold = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Resources resources = getResources();
            try {
                fontRegular = resources.getFont(R.font.googlesans_regular);
                fontMedium = resources.getFont(R.font.googlesans_medium);
                fontBold = resources.getFont(R.font.googlesans_bold);
            } catch (final Exception e) {
                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "LinkedApp", e);
                else Utils.firebaseCrashlytics.recordException(e);
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
    }
}