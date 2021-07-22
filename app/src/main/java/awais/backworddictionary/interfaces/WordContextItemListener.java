package awais.backworddictionary.interfaces;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;

import java.net.URLEncoder;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.CustomTabActivityHelper;

public final class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
    private final CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
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
            Utils.speakText(word);

        } else if (itemId == R.id.action_google) {
            final String wordRawGoogle = word.replaceAll(" ", "+").replaceAll("\\s", "+");
            try {
                context.startActivity(new Intent(Intent.ACTION_WEB_SEARCH)
                        .putExtra(SearchManager.QUERY, word)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            } catch (final Exception e) {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[0]);
                CustomTabActivityHelper.openCustomTab(context,
                        customTabsIntent.build(), Uri.parse("https://google.com/search?q=define+" + wordRawGoogle));
            }

        } else if (itemId == R.id.action_wiki) {
            String wordRawWiki = word.replaceAll(" ", "_").replaceAll("\\s", "_");
            try {
                wordRawWiki = URLEncoder.encode(wordRawWiki, Utils.CHARSET);
            } catch (final Exception e) {
                // ignore
            }

            final Uri wordWikiUri = Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki);

            final Intent intent = new Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setPackage("org.wikipedia")
                    .setData(wordWikiUri);

            final List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (resolveInfos.size() > 0) context.startActivity(intent);
            else {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[1]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        wordWikiUri);
            }

        } else if (itemId == R.id.action_urban) {
            customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[2]);
            CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                    Uri.parse("https://www.urbandictionary.com/define.php?term=" + word));

        } else
            return false;

        return true;
    }
}