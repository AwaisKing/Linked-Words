package awais.backworddictionary.helpers;

import static java.lang.Character.MIN_LOW_SURROGATE;
import static java.lang.Math.sqrt;
import static awais.backworddictionary.Main.tabBoolsArray;
import static awais.backworddictionary.Main.tts;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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

    private static final int[][] API_20_BELOW_STATE_DRAWABLES = {
            new int[]{-android.R.attr.state_focused},
            new int[]{android.R.attr.state_focused},
            new int[]{-android.R.attr.state_enabled},
    };

    public static final int[] CUSTOM_TAB_COLORS = new int[]{0xFF4888F2, 0xFF333333, 0xFF3B496B};
    public static final String CHARSET = "UTF-8";
    public static FirebaseCrashlytics firebaseCrashlytics;
    public static InputMethodManager inputMethodManager;
    public static NotificationManager notificationManager;
    public static Locale defaultLocale;
    public static int statusBarHeight = 0, navigationBarHeight = 0;
    private static ClipboardManager clipboard;

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
     * https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281
     */
    public static void setDefaultFont(final String typefaceName, final Typeface fontTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typefaceName);
            if (!staticField.isAccessible()) staticField.setAccessible(true);
            staticField.set(null, fontTypeface);
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Utils::setDefaultFont", e);
            else firebaseCrashlytics.recordException(e);
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

            final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);

            final int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
            final int statusBarHeight1 = contentView.getTop() - rectangle.top;
            final int statusBarHeight2 = resId > 0 ? resources.getDimensionPixelSize(resId) : 0;

            return statusBarHeight1 == 0 && statusBarHeight2 == 0 ? 50 :
                    statusBarHeight1 == 0 ? statusBarHeight2 : statusBarHeight1;
        }
        return 0;
    }

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
        if (clipboard == null)
            clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        int toastMessage = R.string.error_copying_clipboard;
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("word", stringToCopy));
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

    public static void showPopupMenu(@NonNull final View view, @NonNull final WordItem wordItem) {
        setPopupMenuSlider(view, wordItem).show();
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

    public static void showPopupMenu(final Dialog dialog, final Context context, final View view, final String word) {
        if (context != null && view != null && !isEmpty(word)) {
            final Object tag = view.getTag(R.id.key_popup);
            final PopupMenu popup = tag instanceof PopupMenu ? (PopupMenu) tag :
                    setPopupMenuSlider(dialog, context, view, word);
            popup.show();
        }
    }

    public static void speakText(final CharSequence text) {
        if (tts != null && !isEmpty(text)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            else
                tts.speak(String.valueOf(text), TextToSpeech.QUEUE_FLUSH, null); // todo change deprecated
        }
    }

    public static float distance(@NonNull final PointF pointF, @NonNull final PointF other) {
        return (float) sqrt((other.y - pointF.y) * (other.y - pointF.y) + (other.x - pointF.x) * (other.x - pointF.x));
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

    public static Drawable getNumberPickerBackground(Context context) {
        if (context == null) return null;

        if (!(context instanceof ContextThemeWrapper))
            context = new ContextThemeWrapper(context, R.style.DefinitionsDialogTheme);

        final Resources.Theme theme = context.getTheme();
        final Resources resources = context.getResources();
        final TypedValue outValue = new TypedValue();

        Drawable drawable;
        try {
            drawable = ResourcesCompat.getDrawable(resources, R.drawable.mtrl_background_outlined, theme);
        } catch (final Exception ex) {
            try {
                drawable = ContextCompat.getDrawable(context, R.drawable.mtrl_background_outlined);
            } catch (final Exception e) {
                drawable = null;
            }
        }

        if (drawable == null) {
            theme.resolveAttribute(R.attr.colorControlNormal, outValue, true);
            final int colorControlNormal = outValue.resourceId != 0 ?
                    ResourcesCompat.getColor(resources, outValue.resourceId, theme) : outValue.data;

            theme.resolveAttribute(R.attr.colorControlActivated, outValue, true);
            final int colorControlActivated = outValue.resourceId != 0 ?
                    ResourcesCompat.getColor(resources, outValue.resourceId, theme) : outValue.data;

            theme.resolveAttribute(R.attr.colorControlHighlight, outValue, true);
            final int colorControlHighlight = outValue.resourceId != 0 ?
                    ResourcesCompat.getColor(resources, outValue.resourceId, theme) : outValue.data;

            final float density = context.getResources().getDisplayMetrics().density;
            final float cornerRadius = density * 4f;
            final int strokeSize = Math.round(density * 0.99f);

            final StateListDrawable stateListDrawable = new StateListDrawable();
            GradientDrawable gradientDrawable;

            // state_focused="false"
            {
                gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(Color.TRANSPARENT);
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setCornerRadius(cornerRadius);
                gradientDrawable.setStroke(strokeSize, colorControlNormal);
                stateListDrawable.addState(API_20_BELOW_STATE_DRAWABLES[0], gradientDrawable);
            }

            // state_focused="true"
            {
                gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(Color.TRANSPARENT);
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setCornerRadius(cornerRadius);
                gradientDrawable.setStroke(strokeSize, colorControlActivated);
                stateListDrawable.addState(API_20_BELOW_STATE_DRAWABLES[1], gradientDrawable);
            }

            // state_enabled="false"
            {
                gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(Color.TRANSPARENT);
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setCornerRadius(cornerRadius);
                gradientDrawable.setStroke(strokeSize, colorControlHighlight);
                stateListDrawable.addState(API_20_BELOW_STATE_DRAWABLES[2], gradientDrawable);
            }

            drawable = stateListDrawable;
        }

        return drawable;
    }
}