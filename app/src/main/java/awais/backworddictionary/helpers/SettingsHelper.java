package awais.backworddictionary.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

public final class SettingsHelper {
    private static final int FALLBACK_TTS_DEFAULT_PITCH = 100;
    private static final int FALLBACK_TTS_DEFAULT_RATE = 100;
    private static int defaultPitch = FALLBACK_TTS_DEFAULT_PITCH, defaultRate = FALLBACK_TTS_DEFAULT_RATE;

    private static final String DEFAULT_TABS = "[true, true, true, true, false, false, false, false, false, false]";
    ////////////////////////////////////////////////
    private static final String KEY_TABS = "tabs";
    private static final String KEY_MAX_WORDS = "maxWords";
    private static final String KEY_SHOW_ADS = "showAds";
    private static final String KEY_SHOW_DIALOG = "showDialog";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_SHOW_FLOATING = "showFloating";
    private static final String KEY_SHOW_FLOATING_DIALOG = "showFloatingDialog";
    private static final String KEY_SHOW_DEFS_POPUP = "showDefsPopup";
    ////////////////////////////////////////////////
    private static final String KEY_TTS_ERROR_DLG_HIDDEN = "ttsErrorDialogHidden";
    private static final String KEY_TTS_DLG_SEEN = "ttsDialogShown";
    private static final String KEY_TTS_PITCH = "ttsPitch";
    private static final String KEY_TTS_RATE = "ttsRate";
    private static final String KEY_TTS_ENGINE = "ttsEngine";
    private static final String KEY_TTS_LANGUAGE = "ttsLanguage";
    private static final String KEY_TTS_VOICE = "ttsVoice";
    ////////////////////////////////////////////////
    private static final String KEY_FILTER_WORD = "filterWord";
    private static final String KEY_FILTER_DEFINITION = "filterDefinition";
    private static final String KEY_FILTER_CONTAINS = "filterContain";
    ////////////////////////////////////////////////
    private static SharedPreferences preferences;

    public static void setPreferences(@NonNull final Context context) {
        if (preferences == null) preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        final ContentResolver contentResolver = context.getContentResolver();
        defaultPitch = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_PITCH, FALLBACK_TTS_DEFAULT_PITCH);
        defaultRate = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_RATE, FALLBACK_TTS_DEFAULT_RATE);
    }

    public static int getNightMode() {
        return preferences != null ? preferences.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    public static int getMaxWords() {
        return preferences != null ? preferences.getInt(KEY_MAX_WORDS, 80) : 80;
    }

    public static boolean showDefsPopup() {
        return preferences == null || preferences.getBoolean(KEY_SHOW_DEFS_POPUP, true);
    }

    public static boolean showFloating() {
        return preferences == null || preferences.getBoolean(KEY_SHOW_FLOATING, true);
    }

    public static boolean showFloatingDialog() {
        return preferences == null || preferences.getBoolean(KEY_SHOW_FLOATING_DIALOG, false);
    }

    public static boolean showDialog() {
        return preferences != null && preferences.getBoolean(KEY_SHOW_DIALOG, false);
    }

    public static boolean showAds() {
        return preferences == null || preferences.getBoolean(KEY_SHOW_ADS, true);
    }

    public static boolean isFilterWords() {
        return preferences == null || preferences.getBoolean(KEY_FILTER_WORD, true);
    }

    public static boolean isFilterDefinition() {
        return preferences != null && preferences.getBoolean(KEY_FILTER_DEFINITION, false);
    }

    public static boolean isFilterContains() {
        return preferences != null && preferences.getBoolean(KEY_FILTER_CONTAINS, false);
    }

    public static boolean isTTSHelpDialogSeen() {
        return preferences != null && preferences.getBoolean(KEY_TTS_DLG_SEEN, false);
    }

    public static boolean isTTSErrorDialogHidden() {
        return preferences != null && preferences.getBoolean(KEY_TTS_ERROR_DLG_HIDDEN, false);
    }

    public static int getTTSPitch() {
        return preferences != null ? preferences.getInt(KEY_TTS_PITCH, defaultPitch) : defaultPitch;
    }

    @Nullable
    public static String getTTSEngine() {
        return preferences != null ? preferences.getString(KEY_TTS_ENGINE, null) : null;
    }

    @Nullable
    public static String getTTSVoice() {
        return preferences != null ? preferences.getString(KEY_TTS_VOICE, null) : null;
    }

    @Nullable
    public static String getTTSLanguage() {
        return preferences != null ? preferences.getString(KEY_TTS_LANGUAGE, null) : null;
    }

    public static int getTTSSpeechRate() {
        return preferences != null ? preferences.getInt(KEY_TTS_RATE, defaultRate) : defaultRate;
    }

    @NonNull
    public static boolean[] getTabs() {
        String bools = DEFAULT_TABS;
        if (preferences != null) {
            bools = preferences.getString("tabs", DEFAULT_TABS);
            if (Utils.isEmpty(bools)) bools = DEFAULT_TABS;
        }
        bools = bools.substring(1, bools.length() - 1);

        final boolean[] tabBoolsArray = {true, true, true, true, false, false, false, false, false, false};
        int idx = 0;
        for (int i = 0, len = bools.length(); i < len; ) {
            final char c = bools.charAt(i);
            if (c != ' ' && c != ',') {
                if (c == 't') {
                    tabBoolsArray[idx++] = true;
                    i = i + 4;
                    continue;
                }
                if (c == 'f') {
                    tabBoolsArray[idx++] = false;
                    i = i + 5;
                    continue;
                }
            }
            i = i + 1;
        }

        return tabBoolsArray;
    }

    public static void setTabs(@NonNull final String tabs) {
        if (preferences != null)
            preferences.edit().putString(KEY_TABS, !Utils.isEmpty(tabs) ? tabs : DEFAULT_TABS).apply();
    }

    public static void setFilter(@NonNull final String filterName, final boolean filtered) {
        if (preferences != null)
            preferences.edit().putBoolean(filterName, filtered).apply();
    }

    public static void setTTSPitch(final Integer pitch) {
        if (preferences != null) {
            final SharedPreferences.Editor edit = preferences.edit();
            if (pitch == null) edit.remove(KEY_TTS_PITCH);
            else edit.putInt(KEY_TTS_PITCH, pitch);
            edit.apply();
        }
    }

    public static void setTTSSpeechRate(final Integer speechRate) {
        if (preferences != null) {
            final SharedPreferences.Editor edit = preferences.edit();
            if (speechRate == null) edit.remove(KEY_TTS_RATE);
            else edit.putInt(KEY_TTS_RATE, speechRate);
            edit.apply();
        }
    }

    public static void setTTSLanguage(final String ttsLanguage) {
        if (!Utils.isEmpty(ttsLanguage) && preferences != null)
            preferences.edit().putString(KEY_TTS_LANGUAGE, ttsLanguage).apply();
    }

    public static void setTTSVoice(final String ttsVoice) {
        if (!Utils.isEmpty(ttsVoice) && preferences != null)
            preferences.edit().putString(KEY_TTS_VOICE, ttsVoice).apply();
    }

    public static void setTTSEngine(final String ttsEngine) {
        if (!Utils.isEmpty(ttsEngine) && preferences != null)
            preferences.edit().putString(KEY_TTS_ENGINE, ttsEngine).apply();
    }

    public static void setTTSHelpDialogSeen() {
        if (preferences != null) preferences.edit().putBoolean(KEY_TTS_DLG_SEEN, true).apply();
    }

    public static void setTTSErrorDialogHidden() {
        if (preferences != null) preferences.edit().putBoolean(KEY_TTS_ERROR_DLG_HIDDEN, true).apply();
    }

    public static void resetTTS() {
        if (preferences != null)
            preferences.edit().remove(KEY_TTS_DLG_SEEN)
                    .remove(KEY_TTS_PITCH)
                    .remove(KEY_TTS_RATE)
                    .remove(KEY_TTS_ENGINE)
                    .remove(KEY_TTS_LANGUAGE)
                    .remove(KEY_TTS_VOICE)
                    .apply();
    }

    public static void setValues(final int maxWords, final int darkMode, final boolean showAds, final boolean showDialog,
                                 final boolean isFloating, final boolean isFloatingDialog, final boolean showDefsPopup) {
        if (preferences != null) {
            preferences.edit()
                    .putInt(KEY_MAX_WORDS, maxWords)
                    .putInt(KEY_DARK_MODE, darkMode)
                    .putBoolean(KEY_SHOW_ADS, showAds)
                    .putBoolean(KEY_SHOW_DIALOG, showDialog)
                    .putBoolean(KEY_SHOW_FLOATING, isFloating)
                    .putBoolean(KEY_SHOW_FLOATING_DIALOG, isFloatingDialog)
                    .putBoolean(KEY_SHOW_DEFS_POPUP, showDefsPopup)
                    .apply();
        }
    }
}
