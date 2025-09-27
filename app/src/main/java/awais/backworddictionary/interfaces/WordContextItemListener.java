package awais.backworddictionary.interfaces;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.CustomTabsHelper;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.CustomTabActivityHelper;

public final class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
    private final CustomTabsHelper customTabsHelper = new CustomTabsHelper();
    private final Context context;
    private String word;

    public WordContextItemListener(final Context context) {
        this.context = context;
    }

    public void setWord(@NonNull final CharSequence word) {
        this.word = word.toString();
    }

    @Override
    public boolean onMenuItemClick(@NonNull final MenuItem menuItem) {
        final int itemId = menuItem.getItemId();

        if (itemId == R.id.action_copy) {
            Utils.copyText(context, word);

        } else if (itemId == R.id.action_speak) {
            TTSHelper.speakText(word);

        } else if (itemId == R.id.action_google) {
            try {
                context.startActivity(new Intent(Intent.ACTION_WEB_SEARCH)
                        .putExtra(SearchManager.QUERY, word)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            } catch (final Exception e) {
                CustomTabActivityHelper.openCustomTab(context, customTabsHelper.setToolbarColor(Utils.CUSTOM_TAB_COLORS[0]),
                        Uri.parse("https://google.com/search?q=define+" .concat(URLEncoder.encode(word))));
            }

        } else if (itemId == R.id.action_wiki) {
            final Uri wordWikiUri = Uri.parse("https://en.wikipedia.org/wiki/".concat(URLEncoder.encode(word)));

            final Intent intent = new Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setPackage("org.wikipedia")
                    .setData(wordWikiUri);

            final List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (!resolveInfos.isEmpty()) context.startActivity(intent);
            else CustomTabActivityHelper.openCustomTab(context, customTabsHelper.setToolbarColor(Utils.CUSTOM_TAB_COLORS[1]), wordWikiUri);

        } else if (itemId == R.id.action_urban) {
            CustomTabActivityHelper.openCustomTab(context, customTabsHelper.setToolbarColor(Utils.CUSTOM_TAB_COLORS[2]),
                    Uri.parse("https://www.urbandictionary.com/define.php?term=".concat(word)));

        } else
            return false;

        return true;
    }
}