package awais.backworddictionary.models;

import androidx.annotation.NonNull;

import awais.backworddictionary.R;

public enum Tab {
    TAB_REVERSE(R.string.reverse, true),
    TAB_SOUNDS_LIKE(R.string.sounds_like, true),
    TAB_SPELLED_LIKE(R.string.spelled_like, true),
    TAB_SYNONYMS(R.string.synonyms, true),
    TAB_ANTONYMS(R.string.antonyms, false),
    TAB_TRIGGERS(R.string.triggers, false),
    TAB_PART_OF(R.string.part_of, false),
    TAB_COMPRISES(R.string.comprises, false),
    TAB_RHYMES(R.string.rhymes, false),
    TAB_HOMOPHONES(R.string.homophones, false),
    ;

    private final int tabName;
    private boolean isEnabled;

    Tab(final int tabName, final boolean isEnabled) {
        this.tabName = tabName;
        this.isEnabled = isEnabled;
    }

    public int getTabName() {
        return tabName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Tab setEnabled(final boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Tab{" + tabName + ", " + isEnabled + '}';
    }
}