package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

import static awais.backworddictionary.Main.boolsArray;
import static awais.backworddictionary.Main.tts;

public class WordDialog extends Dialog implements android.view.View.OnClickListener {
    private final WordItem wordItem;
    private final Activity activity;

    public WordDialog(Activity act, WordItem wordItem) {
        super(act, R.style.Dialog);
        this.activity = act;
        this.wordItem = wordItem;
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.word_dialog);

        DialogTitle alertTitle = findViewById(R.id.alertTitle);
        alertTitle.setText(wordItem.getWord());

        ListView lvDefs = findViewById(R.id.lvDefs);
        ArrayList<String[]> list = new ArrayList<>();
        if (wordItem.getDefs() != null) {
            for (String item : wordItem.getDefs())
                list.add(item.split("\t"));
        } else
            list.add(new String[] {"", activity.getString(R.string.no_definition_found)});

        lvDefs.setAdapter(new WordAdapter(activity, list, (view, position, text) -> {
            if (!String.valueOf(text).equals(activity.getString(R.string.no_definition_found))
                    && !String.valueOf(text).isEmpty())
                copyText(String.valueOf(text).replaceAll("^(.*)\\t", ""));
        }));

        Button copy = findViewById(R.id.btnCopy);
        Button speak = findViewById(R.id.btnSpeak);
        Button google = findViewById(R.id.btnGoogle);
        Button wiki = findViewById(R.id.btnWiki);
        Button urban = findViewById(R.id.btnUrban);
        Button search = findViewById(R.id.btnSearch);

        copy.setOnClickListener(this);
        speak.setOnClickListener(this);
        google.setOnClickListener(this);
        wiki.setOnClickListener(this);
        urban.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

        switch (v.getId()) {
            case R.id.btnCopy:
                copyText(wordItem.getWord());
                return;

            case R.id.btnSpeak:
                if (tts != null) tts.speak(wordItem.getWord(), TextToSpeech.QUEUE_FLUSH, null);
                return;

            case R.id.btnGoogle:
                String wordRawGoogle = wordItem.getWord().replace(" ", "+").replace("\\s", "+");
                try {
                    Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent1.putExtra(SearchManager.QUERY, wordItem.getWord());
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    activity.startActivity(intent1);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#4888f2"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(), Uri.parse("https://google.com/search?q=".concat(wordRawGoogle)));
                }
                break;

            case R.id.btnWiki:
                String wordRawWiki = wordItem.getWord().replace(" ", "_").replace("\\s", "_");
                try {wordRawWiki = String.valueOf(new URL(wordRawWiki));} catch (Exception ignored) {}

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setPackage("org.wikipedia");
                intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                List<ResolveInfo> resInfo1 = activity.getPackageManager().queryIntentActivities(intent1, 0);
                if (resInfo1 != null && resInfo1.size() > 0) activity.startActivity(intent1);
                else {
                    customTabsIntent.setToolbarColor(Color.parseColor("#333333"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                }
                break;

            case R.id.btnUrban:
                customTabsIntent.setToolbarColor(Color.parseColor("#3b496b"));
                CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                        Uri.parse("http://www.urbandictionary.com/define.php?term=".concat(wordItem.getWord())));
                break;
            case R.id.btnSearch: showPopupMenu(v); return;
            default: break;
        }
        dismiss();
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_search, popup.getMenu());

        for (int i = boolsArray.length - 1; i >= 0; i--)
            popup.getMenu().getItem(i).setVisible(Boolean.parseBoolean(boolsArray[i]));

        popup.setOnMenuItemClickListener(menuItem -> {
            if (activity.getClass() == Main.class) {
                try {
                    int index = ((Main) activity).getItemPosition((String) menuItem.getTitle());
                    ((Main)activity).fragmentsAdapter.getItem(index).title = wordItem.getWord();
                    ((Main)activity).viewPager.setCurrentItem(index, true);
                    ((Main)activity).onSearch(wordItem.getWord());
                } catch (Exception e) {
                    Log.e("AWAISKING_APP", "", e);
                }
            }
            dismiss();
            return true;
        });
        popup.show();
    }

    private void copyText(String stringToCopy) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null)
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", stringToCopy));
            Toast.makeText(activity, R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            try {
                //noinspection deprecation
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) clipboard.setText(stringToCopy);
                Toast.makeText(activity, R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                Toast.makeText(activity, R.string.error_copying_clipboard, Toast.LENGTH_SHORT).show();
            }
        }
    }
}