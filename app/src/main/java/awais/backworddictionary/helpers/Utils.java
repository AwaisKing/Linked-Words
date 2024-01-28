package awais.backworddictionary.helpers;

import static java.lang.Character.MIN_LOW_SURROGATE;
import static java.lang.Math.sqrt;
import static awais.backworddictionary.Main.tabBoolsArray;
import static awais.backworddictionary.Main.tts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import com.applovin.mediation.ads.MaxAdView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.executor.LocalAsyncTask;
import awais.backworddictionary.helpers.other.Listener;
import awais.backworddictionary.interfaces.WordClickSearchListener;
import awais.backworddictionary.interfaces.WordContextItemListener;

public final class Utils {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final Intent SEARCH_QUERY_INTENT = new Intent(Intent.ACTION_WEB_SEARCH);

    public static final int[] CUSTOM_TAB_COLORS = new int[]{0xFF4888F2, 0xFF333333, 0xFF3B496B};
    public static final String CHARSET = "UTF-8";
    public static Locale defaultLocale;
    public static int statusBarHeight = 0, navigationBarHeight = 0;

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

    /**
     * thanks to weston
     * <a href="https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281">https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281</a>
     */
    public static void setDefaultFont(final String typefaceName, final Typeface fontTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typefaceName);
            if (!staticField.isAccessible()) staticField.setAccessible(true);
            staticField.set(null, fontTypeface);
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Utils::setDefaultFont", e);
        }
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() <= 0 || cs.equals("") || isEmpty(cs.toString());
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isEmpty(String str) {
        if (str == null || str.length() <= 0 || str.isEmpty() || "null".equals(str)) return true;
        str = str.trim();
        return str.length() <= 0 || str.isEmpty() || "null".equals(str);
    }

    public static void stopAsyncSilent(final LocalAsyncTask<?, ?> localAsyncTask) {
        try {
            if (localAsyncTask != null)
                localAsyncTask.cancel(true);
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
        final MaxAdView adView = activity.findViewById(R.id.adView);
        if (adLayout == null || adView == null) return;
        if (SettingsHelper.showAds()) {
            adView.setListener(new Listener(adLayout));
            adView.loadAd();
        } else {
            adView.destroy();
            adLayout.setVisibility(View.GONE);
        }
    }

    @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
    public static int getStatusBarHeight(final Window window, final Resources resources) {
        if (Build.VERSION.SDK_INT == 19 && window != null && resources != null) {
            final Rect rectangle = new Rect();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

            final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);

            final int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
            final int statusBarHeight1 = contentView.getTop() - rectangle.top;
            final int statusBarHeight2 = resId > 0 ? resources.getDimensionPixelSize(resId) : 0;

            return statusBarHeight1 == 0 && statusBarHeight2 == 0 ? 50 :
                   statusBarHeight1 == 0 ? statusBarHeight2 : statusBarHeight1;
        }
        return 0;
    }

    @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
    public static int getNavigationBarHeight(final Window window, final Resources resources) {
        if (Build.VERSION.SDK_INT == 19 && window != null && resources != null) {
            final int appUsableSizeX, appUsableSizeY, realScreenSizeX, realScreenSizeY;

            final Display display = window.getWindowManager().getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);
            appUsableSizeX = size.x;
            appUsableSizeY = size.y;

            display.getRealSize(size);
            realScreenSizeX = size.x;
            realScreenSizeY = size.y;

            final int navBarHeight1;
            if (!(appUsableSizeX < realScreenSizeX) // !(navigation bar on the right)
                && appUsableSizeY < realScreenSizeY) // navigation bar at the bottom
                navBarHeight1 = realScreenSizeY - appUsableSizeY;
            else navBarHeight1 = 0;

            final int resId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            final int navBarHeight2 = resId > 0 ? resources.getDimensionPixelSize(resId) : 0;

            return navBarHeight1 == 0 && navBarHeight2 == 0 ? 0 :
                   navBarHeight1 == 0 ? navBarHeight2 : navBarHeight1;
        }
        return 0;
    }

    public static void copyText(final Context context, final String stringToCopy) {
        final ClipboardManager clipboardManager = AppHelper.getInstance(context).getClipboardManager();

        int toastMessage = R.string.error_copying_clipboard;
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("word", stringToCopy));
            toastMessage = R.string.copied_clipboard;
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    public static PopupMenu setPopupMenuSlider(@NonNull final View view, @NonNull final WordItem wordItem) {
        final Context context = view.getContext();

        Object tag = view.getTag(R.id.key_popup);
        final PopupMenu popup;
        if (tag instanceof PopupMenu) popup = (PopupMenu) tag;
        else {
            popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
            view.setTag(R.id.key_popup, popup);
        }

        tag = view.getTag(R.id.key_popup_item_listener);
        final WordContextItemListener itemListener;
        if (tag instanceof WordContextItemListener) itemListener = (WordContextItemListener) tag;
        else {
            itemListener = new WordContextItemListener(context);
            view.setTag(R.id.key_popup_item_listener, itemListener);
        }

        itemListener.setWord(wordItem.getWord());

        popup.setOnMenuItemClickListener(itemListener);
        view.setOnTouchListener(popup.getDragToOpenListener());

        return popup;
    }

    @NonNull
    public static PopupMenu setPopupMenuSlider(final Dialog dialog, final Context context, @NonNull final View view, final String word) {
        final PopupMenu popup = new PopupMenu(context, view);
        final Menu menu = popup.getMenu();
        popup.getMenuInflater().inflate(R.menu.menu_search, menu);

        for (int i = tabBoolsArray.length - 1; i >= 0; i--)
            menu.getItem(i).setVisible(tabBoolsArray[i]);

        popup.setOnMenuItemClickListener(new WordClickSearchListener(context, dialog, word));

        view.setOnTouchListener(popup.getDragToOpenListener());
        view.setTag(R.id.key_popup, popup);

        return popup;
    }

    public static void showPopupMenu(@NonNull final View view, @NonNull final WordItem wordItem) {
        setPopupMenuSlider(view, wordItem).show();
    }

    public static void showPopupMenu(final Dialog dialog, final Context context, final View view, final String word) {
        if (context != null && view != null && !isEmpty(word)) {
            final Object tag = view.getTag(R.id.key_popup);
            final PopupMenu popup = tag instanceof PopupMenu ? (PopupMenu) tag :
                                    setPopupMenuSlider(dialog, context, view, word);
            popup.show();
        }
    }

    public static void speakText(final CharSequence text) {
        if (tts == null || isEmpty(text)) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        else
            tts.speak(String.valueOf(text), TextToSpeech.QUEUE_FLUSH, null); // todo change deprecated
    }

    public static float distance(@NonNull final PointF pointF, @NonNull final PointF other) {
        return (float) sqrt((other.y - pointF.y) * (other.y - pointF.y) + (other.x - pointF.x) * (other.x - pointF.x));
    }

    @NonNull
    public static ContextThemeWrapper getStyledContext(final Context context, @StyleRes int style) {
        if (context instanceof ContextThemeWrapper) return (ContextThemeWrapper) context;
        if (style == 0 || style == -1) {
            final Resources.Theme theme;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (theme = context.getTheme()) != null)
                return new ContextThemeWrapper(context, theme);
            else {
                final Context appContext = context.getApplicationContext();

                final PackageManager packageManager = appContext.getPackageManager();
                final String packageName = appContext.getPackageName();

                int styleHack;
                try {
                    final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    styleHack = packageInfo.applicationInfo.theme;
                    Log.d("AWAISKING_APP", "styleHack1: " + styleHack);
                } catch (final Exception e) {
                    Log.e("AWAISKING_APP", "err1", e);
                    styleHack = -1;
                }

                if (styleHack == 0 || styleHack == -1) {
                    try {
                        final Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                        // noinspection ConstantConditions
                        final ActivityInfo activityInfo = packageManager.getActivityInfo(intent.getComponent(), 0);
                        styleHack = activityInfo.getThemeResource();
                        Log.d("AWAISKING_APP", "styleHack2: " + styleHack);
                    } catch (final Exception e) {
                        Log.e("AWAISKING_APP", "err2", e);
                        styleHack = -1;
                    }
                }

                // try {
                //    final ActivityInfo activityInfo = packageManager.getActivityInfo(new ComponentName(appContext, Main.class), 0);
                //    styleHack = activityInfo.getThemeResource();
                //} catch (final Throwable e) {
                //    styleHack = -1;
                //}
                // if (styleHack == 0 || styleHack == -1) {
                //    try {
                //        final ActivityInfo activityInfo = packageManager.getActivityInfo(new ComponentName(context, Main.class), 0);
                //        styleHack = activityInfo.getThemeResource();
                //    } catch (final Throwable e) {
                //        styleHack = -1;
                //    }
                //}

                if (styleHack != 0 && styleHack != -1) style = styleHack;
            }
        }
        return new ContextThemeWrapper(context, style);
    }

    public static boolean isAnySearchAppFound(@NonNull final Context context) {
        final String appID = context.getApplicationContext().getPackageName();
        final PackageManager packageManager = context.getPackageManager();

        final List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(SEARCH_QUERY_INTENT, 0);
        int pkgsSize = pkgAppsList.size();
        for (int i = pkgsSize - 1; i >= 0; i--) {
            final ResolveInfo resolveInfo = pkgAppsList.get(i);
            final String strResolveInfo = String.valueOf(resolveInfo);

            if (strResolveInfo.contains(appID) && strResolveInfo.contains("TextProcessHelper") ||
                resolveInfo != null && resolveInfo.activityInfo != null && appID.equals(resolveInfo.activityInfo.packageName))
                --pkgsSize;
        }

        return pkgsSize > 0;
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