package awais.backworddictionary.interfaces;

import android.view.View;

import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;

public class DictionaryWordsItemListener implements View.OnLongClickListener, View.OnClickListener {
    private final View overflow;
    private final WordItem wordItem;

    public DictionaryWordsItemListener(View overflow, WordItem wordItem) {
        this.overflow = overflow;
        this.wordItem = wordItem;
    }

    @Override
    public void onClick(View v) {
        Utils.showPopupMenu(overflow, wordItem);
    }

    @Override
    public boolean onLongClick(View v) {
        Utils.showPopupMenu(overflow, wordItem);
        return true;
    }
}