package awais.backworddictionary.interfaces;

import java.util.ArrayList;

import awais.backworddictionary.adapters.holders.WordItem;

public interface MainCheck {
    void afterSearch(final ArrayList<WordItem> result);
}