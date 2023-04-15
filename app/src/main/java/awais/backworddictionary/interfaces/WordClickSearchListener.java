package awais.backworddictionary.interfaces;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.custom.FloatingDialogView;
import awais.backworddictionary.helpers.TextProcessHelper;

public final class WordClickSearchListener implements PopupMenu.OnMenuItemClickListener {
    private final Context context;
    private final Dialog dialog;
    private final String word;

    public WordClickSearchListener(final Context context, final Dialog dialog, @NonNull final CharSequence word) {
        this.context = context;
        this.dialog = dialog;
        this.word = word.toString();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        if (context instanceof Main) {
            try {
                final Main actMain = (Main) context;
                final int index = actMain.fragmentsAdapter.fragmentIndex(item.getTitle());
                if (index != -1) {
                    actMain.fragmentsAdapter.getItem(index).title = word;
                    actMain.mainBinding.viewPager.setCurrentItem(index, true);
                }
                actMain.onSearch(word);
            } catch (final Exception e) {
                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "WordClickSearchListener", e);
            }
        } else {
            Context baseContext = null;
            final FloatingDialogView floatingDialogView;

            if (context instanceof ContextThemeWrapper)
                baseContext = ((ContextThemeWrapper) context).getBaseContext();
            else if (context instanceof android.view.ContextThemeWrapper)
                baseContext = ((android.view.ContextThemeWrapper) context).getBaseContext();

            if (baseContext instanceof TextProcessHelper
                    && (floatingDialogView = ((TextProcessHelper) baseContext).floatingDialogView) != null)
                floatingDialogView.searchWord(word);
        }

        if (dialog != null) dialog.dismiss();
        return true;
    }
}