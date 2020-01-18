package awais.backworddictionary.interfaces;

import android.view.View;

import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;

public final class DictionaryWordsItemListener implements View.OnLongClickListener, View.OnClickListener {
    private final View overflow;
    private final WordItem wordItem;

    public DictionaryWordsItemListener(final View overflow, final WordItem wordItem) {
        this.overflow = overflow;
        this.wordItem = wordItem;
    }

    @Override
    public void onClick(final View v) {
        Utils.showPopupMenu(overflow, wordItem);
    }

    @Override
    public boolean onLongClick(final View v) {
        Utils.showPopupMenu(overflow, wordItem);
        return true;
    }
}