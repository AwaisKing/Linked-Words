package awais.backworddictionary.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.custom.Listener;
import awais.backworddictionary.custom.WordItem;
import io.fabric.sdk.android.Fabric;

import static awais.backworddictionary.Main.tabBoolsArray;

public final class Utils {
    public static final int[] CUSTOM_TAB_COLORS = new int[]{0xFF4888F2, 0xFF333333, 0xFF3B496B};
    public static final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    public static InputMethodManager inputMethodManager;
    public static Locale defaultLocale;
    public static int statusBarHeight = 0;
    private static AppCompatDrawableManager drawableManager;
    private static ClipboardManager clipboard;

    public static void setSharedPrefs(final Context context) {
        SettingsHelper.setPreferences(context);
    }

    @Nullable
    public static String getResponse(final String url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        final int responseCode = connection.getResponseCode();
        if (responseCode == 200 || responseCode == 201 || responseCode == 202) {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                final StringBuilder response = new StringBuilder();

                String currentLine;
                while ((currentLine = reader.readLine()) != null)
                    response.append(currentLine);

                return response.toString();
            }
        }

        return null;
    }

    /**
     * thanks to weston
     * https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281
     */
    public static void setDefaultFont(final String typefaceName, final Typeface fontTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typefaceName);
            if (!staticField.isAccessible()) staticField.setAccessible(true);
            staticField.set(null, fontTypeface);
        } catch (Exception ignored) {}
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() <= 0 || cs == "" || cs.equals("");
    }

    public static boolean isEmpty(final String str) {
        return str == null || str.length() <= 0 || "null".equals(str) || str.trim().isEmpty() || "".equals(str.trim()) || "null".equals(str.trim());
    }

    public static void initCrashlytics(final Activity activity) {
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(activity).addOnCompleteListener(task -> {
            if (!BuildConfig.DEBUG && task.isSuccessful())
                Fabric.with(activity, new Crashlytics());
        });
    }

    public static void adsBox(@NonNull final Activity activity) {
        final View adLayout = activity.findViewById(R.id.adLayout);
        if (SettingsHelper.showAds()) {
            MobileAds.initialize(activity, activity.getResources().getString(R.string.appid));
            final AdView adView = activity.findViewById(R.id.adView);
            adView.setAdListener(new Listener(adLayout));
            adView.loadAd(new AdRequest.Builder().build());
        } else adLayout.setVisibility(View.GONE);
    }

    @SuppressLint("RestrictedApi")
    public static Drawable getDrawable(final Context context, final int resId) {
        Drawable drawable;
        try {
            if (drawableManager == null) drawableManager = AppCompatDrawableManager.get();
            drawable = drawableManager.getDrawable(context, resId);
        } catch (Exception e) {
            drawable = ContextCompat.getDrawable(context, resId);
        }
        return drawable;
    }

    public static int getStatusBarHeight(final Window window, final Resources resources) {
        if (Build.VERSION.SDK_INT == 19 && window != null && resources != null) {
            final Rect rectangle = new Rect();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

            final int statusBarHeight1 = window.findViewById(Window.ID_ANDROID_CONTENT).getTop() - rectangle.top;
            int statusBarHeight2 = 0;

            final int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) statusBarHeight2 = resources.getDimensionPixelSize(resId);

            return statusBarHeight1 == 0 && statusBarHeight2 == 0 ? 50 :
                    statusBarHeight1 == 0 ? statusBarHeight2 : statusBarHeight1;
        }
        return 0;
    }

    public static void copyText(final Context context, final String stringToCopy) {
        if (clipboard == null) clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("word", stringToCopy));
            Toast.makeText(context, R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.error_copying_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showPopupMenu(@NonNull final View view, @NonNull final WordItem wordItem) {
        final Context context = view.getContext();
        final PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
        popup.setOnMenuItemClickListener(new WordContextItemListener(context, wordItem.getWord()));
        popup.show();
    }

    public static void showPopupMenu(final Dialog dialog, final Context context, final View view, final String word) {
        if (word != null && context != null && view != null) {
            final PopupMenu popup = new PopupMenu(context, view);
            final Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.menu_search, menu);

            for (int i = tabBoolsArray.length - 1; i >= 0; i--)
                menu.getItem(i).setVisible(tabBoolsArray[i]);

            popup.setOnMenuItemClickListener(menuItem -> {
                if (context instanceof Main) {
                    try {
                        final Main actMain = (Main) context;
                        final int index = actMain.fragmentsAdapter.fragmentIndex(menuItem.getTitle());
                        if (index != -1) {
                            actMain.fragmentsAdapter.getItem(index).title = word;
                            actMain.viewPager.setCurrentItem(index, true);
                        }
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
}