package awais.backworddictionary.helpers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

import static awais.backworddictionary.Main.tts;

public class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
    private final Context mContext;
    private final String currentWord;

    public WordContextItemListener(Context context, CharSequence word) {
        mContext = context;
        currentWord = word.toString();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

        switch (menuItem.getItemId()) {
            case R.id.action_copy:
                Object clipService = mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipService == null) return true;
                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) clipService;
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", currentWord));
                    Toast.makeText(mContext, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    try {
                        //noinspection deprecation
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) clipService;
                        clipboard.setText(currentWord);
                        Toast.makeText(mContext, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored) {}
                }
                return true;
            case R.id.action_speak:
                tts.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null);
                return true;
            case R.id.action_google:
                String wordRawGoogle = currentWord.replaceAll(" ", "+").replaceAll("\\s", "+");
                try {
                    Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent1.putExtra(SearchManager.QUERY, currentWord);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    mContext.startActivity(intent1);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#4888f2"));
                    CustomTabActivityHelper.openCustomTab(
                            (Activity) mContext, customTabsIntent.build(), Uri.parse("https://google.com/search?q=define+".concat(wordRawGoogle)));
                }

                return true;
            case R.id.action_wiki:
                String wordRawWiki = currentWord.replaceAll(" ", "_").replaceAll("\\s", "_");
                try {wordRawWiki = String.valueOf(new URL(wordRawWiki));} catch (Exception ignored) {}

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setPackage("org.wikipedia");
                intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                List<ResolveInfo> resInfo1 = mContext.getPackageManager().queryIntentActivities(intent1, 0);
                if (resInfo1 != null && resInfo1.size() > 0) mContext.startActivity(intent1);
                else {
                    customTabsIntent.setToolbarColor(Color.parseColor("#333333"));
                    CustomTabActivityHelper.openCustomTab(
                            (Activity) mContext, customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                }
                return true;
            case R.id.action_urban:
                customTabsIntent.setToolbarColor(Color.parseColor("#3b496b"));
                CustomTabActivityHelper.openCustomTab(
                        (Activity) mContext, customTabsIntent.build(), Uri.parse("http://www.urbandictionary.com/define.php?term=".concat(currentWord)));

                return true;
            default:
        }
        return false;
    }
}