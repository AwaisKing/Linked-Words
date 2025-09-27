package awais.backworddictionary.helpers;

import android.app.Application;
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
    private static SettingsHelper _instance;
    private final SharedPreferences prefs;
    private final int defaultPitch, defaultRate;

    public static synchronized SettingsHelper getInstance(@NonNull final Context context) {
        if (_instance == null || _instance.prefs == null) synchronized (SettingsHelper.class) {
            _instance = new SettingsHelper(context instanceof Application ? context : context.getApplicationContext());
        }
        return _instance;
    }

    private SettingsHelper(@NonNull final Context context) {
        final Context appContext = context instanceof Application ? context : context.getApplicationContext();
        final ContentResolver contentResolver = appContext.getContentResolver();
        this.prefs = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.defaultPitch = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_PITCH, FALLBACK_TTS_DEFAULT_PITCH);
        this.defaultRate = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_RATE, FALLBACK_TTS_DEFAULT_RATE);
    }

    public int getNightMode() {
        return prefs != null ? prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    public int getMaxWords() {
        return prefs != null ? prefs.getInt(KEY_MAX_WORDS, 80) : 80;
    }

    public boolean showDefsPopup() {
        return prefs == null || prefs.getBoolean(KEY_SHOW_DEFS_POPUP, true);
    }

    public boolean showFloating() {
        return prefs == null || prefs.getBoolean(KEY_SHOW_FLOATING, true);
    }

    public boolean showFloatingDialog() {
        return prefs == null || prefs.getBoolean(KEY_SHOW_FLOATING_DIALOG, false);
    }

    public boolean showDialog() {
        return prefs != null && prefs.getBoolean(KEY_SHOW_DIALOG, false);
    }

    public boolean showAds() {
        return prefs == null || prefs.getBoolean(KEY_SHOW_ADS, true);
    }

    public boolean isFilterWords() {
        return prefs == null || prefs.getBoolean(KEY_FILTER_WORD, true);
    }

    public boolean isFilterDefinition() {
        return prefs != null && prefs.getBoolean(KEY_FILTER_DEFINITION, false);
    }

    public boolean isFilterContains() {
        return prefs != null && prefs.getBoolean(KEY_FILTER_CONTAINS, false);
    }

    public boolean isTTSHelpDialogSeen() {
        return prefs != null && prefs.getBoolean(KEY_TTS_DLG_SEEN, false);
    }

    public boolean isTTSErrorDialogHidden() {
        return prefs != null && prefs.getBoolean(KEY_TTS_ERROR_DLG_HIDDEN, false);
    }

    public int getTTSPitch() {
        return prefs != null ? prefs.getInt(KEY_TTS_PITCH, defaultPitch) : defaultPitch;
    }

    @Nullable
    public String getTTSEngine() {
        return prefs != null ? prefs.getString(KEY_TTS_ENGINE, null) : null;
    }

    @Nullable
    public String getTTSVoice() {
        return prefs != null ? prefs.getString(KEY_TTS_VOICE, null) : null;
    }

    @Nullable
    public String getTTSLanguage() {
        return prefs != null ? prefs.getString(KEY_TTS_LANGUAGE, null) : null;
    }

    public int getTTSSpeechRate() {
        return prefs != null ? prefs.getInt(KEY_TTS_RATE, defaultRate) : defaultRate;
    }

    @NonNull
    public boolean[] getTabs() {
        String bools = DEFAULT_TABS;
        if (prefs != null) {
            bools = prefs.getString("tabs", DEFAULT_TABS);
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

    public void setTabs(@NonNull final String tabs) {
        if (prefs != null)
            prefs.edit().putString(KEY_TABS, !Utils.isEmpty(tabs) ? tabs : DEFAULT_TABS).apply();
    }

    public void setFilter(@NonNull final String filterName, final boolean filtered) {
        if (prefs != null)
            prefs.edit().putBoolean(filterName, filtered).apply();
    }

    public void setTTSPitch(final Integer pitch) {
        if (prefs != null) {
            final SharedPreferences.Editor edit = prefs.edit();
            if (pitch == null) edit.remove(KEY_TTS_PITCH);
            else edit.putInt(KEY_TTS_PITCH, pitch);
            edit.apply();
        }
    }

    public void setTTSSpeechRate(final Integer speechRate) {
        if (prefs != null) {
            final SharedPreferences.Editor edit = prefs.edit();
            if (speechRate == null) edit.remove(KEY_TTS_RATE);
            else edit.putInt(KEY_TTS_RATE, speechRate);
            edit.apply();
        }
    }

    public void setTTSLanguage(final String ttsLanguage) {
        if (!Utils.isEmpty(ttsLanguage) && prefs != null)
            prefs.edit().putString(KEY_TTS_LANGUAGE, ttsLanguage).apply();
    }

    public void setTTSVoice(final String ttsVoice) {
        if (!Utils.isEmpty(ttsVoice) && prefs != null)
            prefs.edit().putString(KEY_TTS_VOICE, ttsVoice).apply();
    }

    public void setTTSEngine(final String ttsEngine) {
        if (!Utils.isEmpty(ttsEngine) && prefs != null)
            prefs.edit().putString(KEY_TTS_ENGINE, ttsEngine).apply();
    }

    public void setTTSHelpDialogSeen() {
        if (prefs != null) prefs.edit().putBoolean(KEY_TTS_DLG_SEEN, true).apply();
    }

    public void setTTSErrorDialogHidden() {
        if (prefs != null) prefs.edit().putBoolean(KEY_TTS_ERROR_DLG_HIDDEN, true).apply();
    }

    public void resetTTS() {
        if (prefs != null)
            prefs.edit().remove(KEY_TTS_DLG_SEEN)
                 .remove(KEY_TTS_PITCH)
                 .remove(KEY_TTS_RATE)
                 .remove(KEY_TTS_ENGINE)
                 .remove(KEY_TTS_LANGUAGE)
                 .remove(KEY_TTS_VOICE)
                 .apply();
    }

    public void setValues(final int maxWords, final int darkMode, final boolean showAds, final boolean showDialog,
                          final boolean isFloating, final boolean isFloatingDialog, final boolean showDefsPopup) {
        if (prefs != null) {
            prefs.edit()
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