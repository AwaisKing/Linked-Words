package awais.backworddictionary.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.helpers.other.Listener;
import awais.backworddictionary.interfaces.WordContextItemListener;

import static awais.backworddictionary.Main.tabBoolsArray;
import static java.lang.Character.MIN_LOW_SURROGATE;

public final class Utils {
    public static final int[] CUSTOM_TAB_COLORS = new int[]{0xFF4888F2, 0xFF333333, 0xFF3B496B};
    public static final String CHARSET = "UTF-8";
    public static final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    public static FirebaseCrashlytics firebaseCrashlytics;
    public static InputMethodManager inputMethodManager;
    public static Locale defaultLocale;
    public static int statusBarHeight = 0;
    private static ClipboardManager clipboard;

    public static void setSharedPrefs(final Context context) {
        SettingsHelper.setPreferences(context);
    }

    @Nullable
    public static String getResponse(final String url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final StringBuilder response = new StringBuilder();

                String currentLine;
                while ((currentLine = reader.readLine()) != null)
                    response.append(currentLine);

                return response.toString();
            }
            return null;
        } finally {
            connection.disconnect();
        }
    }

    public static int dpToPx(final float dp) {
        return Math.round(dp * displayMetrics.density);
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
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
            else firebaseCrashlytics.recordException(e);
        }
    }

    public static boolean isEmpty(final CharSequence cs) {
        if (cs instanceof String) return isEmpty((String) cs);
        return cs == null || cs.length() <= 0 || cs.equals("");
    }

    public static boolean isEmpty(String str) {
        if (str == null || str.length() <= 0 || "null".equals(str)) return true;
        str = str.trim();
        return str.isEmpty() || "null".equals(str);
    }

    public static void stopAsyncSilent(final AsyncTask<?, ?, ?> asyncTask) {
        try {
            if (asyncTask != null)
                asyncTask.cancel(true);
        } catch (final Exception e) {
            // ignore
        }
    }

    public static void removeHandlerCallbacksSilent(final Handler handler, final Runnable runnable) {
        try {
            if (handler != null)
                handler.removeCallbacks(runnable);
        } catch (final Exception e) {
            // ignore
        }
    }

    public static void adsBox(@NonNull final Activity activity) {
        final View adLayout = activity.findViewById(R.id.adLayout);
        if (adLayout == null) return;
        if (SettingsHelper.showAds()) {
            MobileAds.initialize(activity, initializationStatus -> { });
            final AdView adView = activity.findViewById(R.id.adView);
            adView.setAdListener(new Listener(adLayout));
            adView.loadAd(new AdRequest.Builder().build());
        } else adLayout.setVisibility(View.GONE);
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
        if (clipboard == null)
            clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

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
                            actMain.viewPager2.setCurrentItem(index, true);
                        }
                        actMain.onSearch(word);
                    } catch (final Exception e) {
                        if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
                        else firebaseCrashlytics.recordException(e);
                    }
                }
                if (dialog != null) dialog.dismiss();
                return true;
            });
            popup.show();
        }
    }

    // extracted from String class
    public static int indexOfChar(@NonNull final CharSequence sequence, final int ch, final int startIndex) {
        final int max = sequence.length();
        if (startIndex < max) {
            if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                for (int i = startIndex; i < max; i++) if (sequence.charAt(i) == ch) return i;
            } else if (Character.isValidCodePoint(ch)) {
                final char hi = (char) ((ch >>> 10) + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
                final char lo = (char) ((ch & 0x3ff) + MIN_LOW_SURROGATE);
                for (int i = startIndex; i < max; i++)
                    if (sequence.charAt(i) == hi && sequence.charAt(i + 1) == lo) return i;
            }
        }
        return -1;
    }
}