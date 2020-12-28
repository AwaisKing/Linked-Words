package awais.backworddictionary.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class SettingsHelper {
    private static final String DEFAULT_TABS = "[true, true, true, true, false, false, false, false, false, false]";
    ////////////////////////////////////////////////
    private static final String KEY_TABS = "tabs";
    private static final String KEY_MAX_WORDS = "maxWords";
    private static final String KEY_SHOW_ADS = "showAds";
    private static final String KEY_SHOW_DIALOG = "showDialog";
    private static final String KEY_DARK_MODE = "darkMode";
    ////////////////////////////////////////////////
    private static final String KEY_FILTER_WORD = "filterWord";
    private static final String KEY_FILTER_DEFINITION = "filterDefinition";
    private static final String KEY_FILTER_CONTAINS = "filterContain";
    ////////////////////////////////////////////////
    private static SharedPreferences preferences;

    static void setPreferences(@NonNull final Context context) {
        if (preferences == null)
            preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static int getNightMode() {
        return preferences != null ? preferences.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    public static int getMaxWords() {
        return preferences != null ? preferences.getInt(KEY_MAX_WORDS, 80) : 80;
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

    public static void setFilter(@NonNull final String filterName, final boolean filtered) {
        if (preferences != null)
            preferences.edit().putBoolean(filterName, filtered).apply();
    }

    public static void setTabs(@NonNull final String tabs) {
        if (preferences != null)
            preferences.edit().putString(KEY_TABS, !Utils.isEmpty(tabs) ? tabs : DEFAULT_TABS).apply();
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

    public static void setValues(final int maxWords, final int darkMode, final boolean showAds, final boolean showDialog) {
        if (preferences != null) {
            preferences.edit()
                    .putInt(KEY_MAX_WORDS, maxWords)
                    .putInt(KEY_DARK_MODE, darkMode)
                    .putBoolean(KEY_SHOW_ADS, showAds)
                    .putBoolean(KEY_SHOW_DIALOG, showDialog)
                    .apply();
        }
    }
}
