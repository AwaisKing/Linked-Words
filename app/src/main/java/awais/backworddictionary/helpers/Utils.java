package awais.backworddictionary.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.reflect.Field;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.custom.Listener;
import io.fabric.sdk.android.Fabric;

public class Utils {
    private static AppCompatDrawableManager drawableManager;

    // thanks to weston
    // source: https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281
    public static void setDefaultFont(String typefaceName, Typeface fontTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typefaceName);
            staticField.setAccessible(true);
            staticField.set(null, fontTypeface);
        } catch (Exception ignored) {}
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() <= 0 || str.trim().isEmpty() || str.trim().equals("");
    }

    public static boolean isEmpty(CharSequence sequence) {
        return sequence == null || sequence.length() <= 0 || sequence == "" || sequence.equals("");
    }

    public static void initCrashlytics(Activity activity) {
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity)
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful() || BuildConfig.DEBUG) return;
                Fabric.with(activity, new Crashlytics());
            });
    }

    public static void adsBox(Activity activity) {
        if (Main.sharedPreferences.getBoolean("showAds", true)) {
            MobileAds.initialize(activity, activity.getResources().getString(R.string.appid));
            AdView adView = activity.findViewById(R.id.adView);
            adView.setAdListener(new Listener(activity.findViewById(R.id.adLayout)));
            adView.loadAd(new AdRequest.Builder().build());
        } else activity.findViewById(R.id.adLayout).setVisibility(View.GONE);
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @SuppressLint( "RestrictedApi" )
    public static Drawable getDrawable(Context context, int resId) {
        if (drawableManager == null) drawableManager = AppCompatDrawableManager.get();
        return drawableManager.getDrawable(context, resId);
    }
}