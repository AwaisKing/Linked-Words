package awais.backworddictionary.custom;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.DialogTitle;
import androidx.browser.customtabs.CustomTabsIntent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.adapters.WordAdapter;
import awais.backworddictionary.customweb.CustomTabActivityHelper;
import awais.backworddictionary.helpers.Utils;

import static awais.backworddictionary.Main.tts;

public class WordDialog extends Dialog implements android.view.View.OnClickListener {
    private final Context context;
    private final String word;
    private final ArrayList<String[]> defs;
    private final SearchAdapter.OnItemClickListener itemClickListener;
    private final CustomTabsIntent.Builder customTabsIntent;
    private final int[] colors;

    public WordDialog(Context context, String word, ArrayList<String[]> defs, SearchAdapter.OnItemClickListener itemClickListener) {
        super(context, R.style.Dialog);
        this.context = context;
        this.word = word;
        this.defs = defs;
        this.itemClickListener = itemClickListener;
        this.customTabsIntent = new CustomTabsIntent.Builder();
        this.colors = new int[] {Color.parseColor("#4888f2"), Color.parseColor("#333333"),
                Color.parseColor("#3b496b")};
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.word_dialog);

        ((DialogTitle) findViewById(R.id.alertTitle)).setText(word);

        final ListView lvDefs = findViewById(R.id.lvDefs);
        lvDefs.setAdapter(new WordAdapter(context, false, defs, itemClickListener));

        final Button copy = findViewById(R.id.btnCopy);
        final Button speak = findViewById(R.id.btnSpeak);
        final Button google = findViewById(R.id.btnGoogle);
        final Button wiki = findViewById(R.id.btnWiki);
        final Button urban = findViewById(R.id.btnUrban);
        final Button search = findViewById(R.id.btnSearch);
        final Button close = findViewById(R.id.btnClose);

        copy.setOnClickListener(this);
        speak.setOnClickListener(this);
        google.setOnClickListener(this);
        wiki.setOnClickListener(this);
        urban.setOnClickListener(this);
        search.setOnClickListener(this);
        close.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnCopy:
                Utils.copyText(context, word);
                return;

            case R.id.btnSpeak:
                if (tts != null) tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                return;

            case R.id.btnGoogle:
                final String wordRawGoogle = word.replace(" ", "+").replace("\\s", "+");
                try {
                    final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, word);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(colors[0]);
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://google.com/search?q=define+".concat(wordRawGoogle)));
                }
                break;

            case R.id.btnWiki:
                String wordRawWiki = word.replace(" ", "_").replace("\\s", "_");
                try { wordRawWiki = String.valueOf(new URL(wordRawWiki)); } catch (Exception ignored) {}

                final Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setPackage("org.wikipedia");
                intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));

                final List<ResolveInfo> resInfo1 = context.getPackageManager().queryIntentActivities(intent1, 0);
                if (resInfo1.size() > 0) context.startActivity(intent1);
                else {
                    customTabsIntent.setToolbarColor(colors[1]);
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                }
                break;

            case R.id.btnUrban:
                customTabsIntent.setToolbarColor(colors[2]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        Uri.parse("http://www.urbandictionary.com/define.php?term=".concat(word)));
                break;

            case R.id.btnSearch:
                Utils.showPopupMenu(this, context, v, word);
                return;
            default: break;
        }
        dismiss();
    }
}