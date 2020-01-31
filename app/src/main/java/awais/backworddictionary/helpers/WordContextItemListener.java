package awais.backworddictionary.helpers;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;

import java.net.URL;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

import static awais.backworddictionary.Main.tts;

final class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
    private final CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
    private final String currentWord;
    private final Context context;

    WordContextItemListener(final Context context, @NonNull final CharSequence word) {
        this.context = context;
        this.currentWord = word.toString();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_copy:
                Utils.copyText(context, currentWord);
                return true;

            case R.id.action_speak:
                tts.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null);
                return true;

            case R.id.action_google:
                final String wordRawGoogle = currentWord.replaceAll(" ", "+").replaceAll("\\s", "+");
                try {
                    final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, currentWord);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[0]);
                    CustomTabActivityHelper.openCustomTab(context,
                            customTabsIntent.build(), Uri.parse("https://google.com/search?q=define+" + wordRawGoogle));
                }
                return true;

            case R.id.action_wiki:
                String wordRawWiki = currentWord.replaceAll(" ", "_").replaceAll("\\s", "_");
                try { wordRawWiki = String.valueOf(new URL(wordRawWiki)); } catch (Exception ignored) {}

                final Uri wordWikiUri = Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki);

                final Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setPackage("org.wikipedia");
                intent.setData(wordWikiUri);

                final List<ResolveInfo> resInfo1 = context.getPackageManager().queryIntentActivities(intent, 0);
                if (resInfo1.size() > 0) context.startActivity(intent);
                else {
                    customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[1]);
                    CustomTabActivityHelper.openCustomTab(context,
                            customTabsIntent.build(), wordWikiUri);
                }
                return true;

            case R.id.action_urban:
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[2]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        Uri.parse("http://www.urbandictionary.com/define.php?term=" + currentWord));
                return true;
        }

        return false;
    }
}