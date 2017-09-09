package awais.backworddictionary.interfaces;

import java.util.ArrayList;
import awais.backworddictionary.custom.WordItem;

public interface FragmentCallback {
    void done(ArrayList<WordItem> items, String word);
}
