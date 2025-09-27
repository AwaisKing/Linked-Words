package awais.backworddictionary.interfaces;

import java.util.ArrayList;

import awais.backworddictionary.models.WordItem;

public interface FragmentCallback {
    void done(final ArrayList<WordItem> items, final String word);
    void wordStarted();
}