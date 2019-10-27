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

class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
    private final int[] colors = new int[] {0xFF4888F2, 0xFF333333, 0xFF3B496B};
    private final CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
    private final String currentWord;
    private final Context context;

    WordContextItemListener(Context context, @NonNull CharSequence word) {
        this.context = context;
        this.currentWord = word.toString();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

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
                    final Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent1.putExtra(SearchManager.QUERY, currentWord);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent1);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(colors[0]);
                    CustomTabActivityHelper.openCustomTab(context,
                            customTabsIntent.build(), Uri.parse("https://google.com/search?q=define+".concat(wordRawGoogle)));
                }
                return true;

            case R.id.action_wiki:
                String wordRawWiki = currentWord.replaceAll(" ", "_").replaceAll("\\s", "_");
                try { wordRawWiki = String.valueOf(new URL(wordRawWiki)); } catch (Exception ignored) {}

                final Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setPackage("org.wikipedia");
                intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));

                final List<ResolveInfo> resInfo1 = context.getPackageManager().queryIntentActivities(intent1, 0);
                if (resInfo1.size() > 0) context.startActivity(intent1);
                else {
                    customTabsIntent.setToolbarColor(colors[1]);
                    CustomTabActivityHelper.openCustomTab(context,
                            customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                }
                return true;

            case R.id.action_urban:
                customTabsIntent.setToolbarColor(colors[2]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        Uri.parse("http://www.urbandictionary.com/define.php?term=".concat(currentWord)));
                return true;
        }

        return false;
    }
}