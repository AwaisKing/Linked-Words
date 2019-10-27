package awais.backworddictionary;

import android.graphics.Typeface;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDexApplication;

import awais.backworddictionary.helpers.Utils;

public class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium;

    @Override
    public void onCreate() {
        super.onCreate();

        Typeface fontBold;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                fontRegular = getResources().getFont(R.font.googlesans_regular);
            } catch (Exception e) {
                fontRegular = ResourcesCompat.getFont(this, R.font.googlesans_regular);
            }
            try {
                fontMedium = getResources().getFont(R.font.googlesans_medium);
            } catch (Exception e) {
                fontMedium = ResourcesCompat.getFont(this, R.font.googlesans_medium);
            }
            try {
                fontBold = getResources().getFont(R.font.googlesans_bold);
            } catch (Exception e) {
                fontBold = ResourcesCompat.getFont(this, R.font.googlesans_bold);
            }
        } else {
            fontRegular = ResourcesCompat.getFont(this, R.font.googlesans_regular);
            fontMedium = ResourcesCompat.getFont(this, R.font.googlesans_medium);
            fontBold = ResourcesCompat.getFont(this, R.font.googlesans_bold);
        }

        Utils.setDefaultFont("DEFAULT", fontMedium);
        Utils.setDefaultFont("SANS_SERIF", fontMedium);
        Utils.setDefaultFont("SERIF", fontMedium);
        Utils.setDefaultFont("MONOSPACE", fontMedium);
        Utils.setDefaultFont("DEFAULT_BOLD", fontBold);
    }
}