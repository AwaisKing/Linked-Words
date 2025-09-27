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
import awais.backworddictionary.models.Tab;

public final class WordClickSearchListener implements PopupMenu.OnMenuItemClickListener {
    private final Tab[] tabs = Tab.values();
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
        final int itemIndex = item == null || item.getItemId() < 0 || item.getItemId() >= tabs.length ? 0 : item.getItemId();

        if (context instanceof final Main actMain) {
            try {
                final int tabIndex = actMain.fragmentsAdapter.fragmentIndex(tabs[itemIndex].getTabName());
                if (tabIndex != -1) {
                    actMain.fragmentsAdapter.getItem(tabIndex).title = word;
                    actMain.mainBinding.viewPager.setCurrentItem(tabIndex, true);
                }
                actMain.onSearch(word);
            } catch (final Exception e) {
                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "WordClickSearchListener", e);
            }
        } else {
            Context baseContext = null;

            if (context instanceof final ContextThemeWrapper ctx) baseContext = ctx.getBaseContext();
            else if (context instanceof final android.view.ContextThemeWrapper ctx) baseContext = ctx.getBaseContext();

            final FloatingDialogView floatingDialogView = baseContext instanceof final TextProcessHelper helper ? helper.floatingDialogView : null;
            if (floatingDialogView != null) floatingDialogView.searchWord(tabs[itemIndex].getTabName(), word);
        }

        if (dialog != null) dialog.dismiss();
        return true;
    }
}