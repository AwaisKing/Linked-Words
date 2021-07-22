package awais.backworddictionary.interfaces;

import static awais.backworddictionary.helpers.Utils.firebaseCrashlytics;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;

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
                    actMain.viewPager2.setCurrentItem(index, true);
                }
                actMain.onSearch(word);
            } catch (final Exception e) {
                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "WordClickSearchListener", e);
                else firebaseCrashlytics.recordException(e);
            }
        }
        if (dialog != null) dialog.dismiss();
        return true;
    }
}