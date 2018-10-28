package awais.backworddictionary;

import android.graphics.Typeface;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.res.ResourcesCompat;

import awais.backworddictionary.custom.Utils;

@SuppressWarnings( "WeakerAccess" )
public class LinkedApp extends MultiDexApplication {
    public static Typeface fontRegular, fontMedium, fontBold;

    @Override
    public void onCreate() {
        super.onCreate();

        // fontRegular = Typeface.createFromAsset(getAssets(), "fonts/googlesans_regular.ttf");
        // fontMedium = Typeface.createFromAsset(getAssets(), "fonts/googlesans_medium.ttf");
        // fontBold = Typeface.createFromAsset(getAssets(), "fonts/googlesans_bold.ttf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fontRegular = getResources().getFont(R.font.googlesans_regular);
            fontMedium = getResources().getFont(R.font.googlesans_medium);
            fontBold = getResources().getFont(R.font.googlesans_bold);
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