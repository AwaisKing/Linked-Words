package awais.backworddictionary.helpers;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
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
    private final Context context;
    private final String word;

    WordContextItemListener(final Context context, @NonNull final CharSequence word) {
        this.context = context;
        this.word = word.toString();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_copy:
                Utils.copyText(context, word);
                return true;

            case R.id.action_speak:
                if (tts != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                    else
                        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null); // todo change deprecated
                }
                return true;

            case R.id.action_google:
                final String wordRawGoogle = word.replaceAll(" ", "+").replaceAll("\\s", "+");
                try {
                    final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, word);
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
                String wordRawWiki = word.replaceAll(" ", "_").replaceAll("\\s", "_");
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
                        Uri.parse("http://www.urbandictionary.com/define.php?term=" + word));
                return true;
        }

        return false;
    }
}