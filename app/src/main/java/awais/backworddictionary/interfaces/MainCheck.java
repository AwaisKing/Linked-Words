package awais.backworddictionary.interfaces;

import java.util.ArrayList;

import awais.backworddictionary.custom.WordItem;

public interface MainCheck {
    void beforeSearch();
    void afterSearch(ArrayList<WordItem> result);
}
