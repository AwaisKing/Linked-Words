package awais.backworddictionary.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.PopupMenu;

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
import awais.backworddictionary.custom.WordItem;
import io.fabric.sdk.android.Fabric;

import static awais.backworddictionary.Main.boolsArray;

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

    public static boolean isConnectedToInternet(@NonNull Context context) {
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

    public static int getStatusBarHeight(Window window, Resources resources) {
        if (Build.VERSION.SDK_INT != 19 || window == null || resources == null) return 0;

        final Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        final int statusBarHeight1 = window.findViewById(Window.ID_ANDROID_CONTENT).getTop() - rectangle.top;
        int statusBarHeight2 = 0;

        final int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) statusBarHeight2 = resources.getDimensionPixelSize(resId);

        return statusBarHeight1 == 0 && statusBarHeight2 == 0 ? 50 :
                statusBarHeight1 == 0 ? statusBarHeight2 : statusBarHeight1;
    }

    public static void copyText(Context context, String stringToCopy) {
        try {
            final android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", stringToCopy));
                Toast.makeText(context, R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            try {
                //noinspection deprecation
                final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    //noinspection deprecation
                    clipboard.setText(stringToCopy);
                    Toast.makeText(context, R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(context, R.string.error_copying_clipboard, Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                Toast.makeText(context, R.string.error_copying_clipboard, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void showPopupMenu(@NonNull View view, @NonNull WordItem wordItem) {
        final Context context = view.getContext();

        final PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
        popup.setOnMenuItemClickListener(new WordContextItemListener(context, wordItem.getWord()));
        popup.show();
    }

    public static void showPopupMenu(Dialog dialog, Context context, View view, String word) {
        if (word == null || context == null || view == null) return;

        final PopupMenu popup = new PopupMenu(context, view);
        final Menu menu = popup.getMenu();
        popup.getMenuInflater().inflate(R.menu.menu_search, menu);

        for (int i = boolsArray.length - 1; i >= 0; i--)
            menu.getItem(i).setVisible(Boolean.parseBoolean(boolsArray[i]));

        popup.setOnMenuItemClickListener(menuItem -> {
            if (context instanceof Main) {
                try {
                    final Main actMain = (Main) context;
                    int index = actMain.getItemPosition((String) menuItem.getTitle());
                    actMain.fragmentsAdapter.getItem(index).title = word;
                    actMain.viewPager.setCurrentItem(index, true);
                    actMain.onSearch(word);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
                }
            }
            if (dialog != null) dialog.dismiss();
            return true;
        });
        popup.show();
    }
}