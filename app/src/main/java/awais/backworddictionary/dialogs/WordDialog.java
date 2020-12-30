package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.DefinitionsAdapter;
import awais.backworddictionary.custom.AlertDialogTitle;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.CustomTabActivityHelper;
import awais.backworddictionary.interfaces.SearchAdapterClickListener;

public final class WordDialog extends Dialog implements android.view.View.OnClickListener {
    private final String word;
    private final Context context;
    private final ArrayList<String[]> defs;
    private final CustomTabsIntent.Builder customTabsIntent;
    private final SearchAdapterClickListener itemClickListener;

    public WordDialog(final Context context, final String word, final ArrayList<String[]> defs, final SearchAdapterClickListener itemClickListener) {
        super(context, R.style.MaterialAlertDialogTheme);
        this.context = context;
        this.word = word;
        this.defs = defs;
        this.itemClickListener = itemClickListener;
        this.customTabsIntent = new CustomTabsIntent.Builder();
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.word_dialog);

        ((AlertDialogTitle) findViewById(R.id.alertTitle)).setText(word);

        final ListView lvDefs = findViewById(R.id.lvDefs);
        lvDefs.setAdapter(new DefinitionsAdapter(context, word,
                false, defs, itemClickListener));

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
    public void onClick(@NonNull final View v) {
        final int id = v.getId();

        if (id == R.id.btnCopy) {
            Utils.copyText(context, word);
            return;
        }

        if (id == R.id.btnSpeak) {
            Utils.speakText(word);
            return;
        }

        if (id == R.id.btnSearch) {
            Utils.showPopupMenu(this, context, v, word);
            return;
        }

        if (id == R.id.btnGoogle) {
            final String wordRawGoogle = word.replace(" ", "+").replace("\\s", "+");
            try {
                final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, word);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(intent);
            } catch (final Exception e) {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[0]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        Uri.parse("https://google.com/search?q=define+" + wordRawGoogle));
            }

        } else if (id == R.id.btnWiki) {
            String wordRawWiki = word.replace(" ", "_").replace("\\s", "_");
            try {
                wordRawWiki = String.valueOf(new URL(wordRawWiki));
            } catch (final Exception e) {
                // ignore
            }

            final Uri wordWikiUri = Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki);

            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("org.wikipedia");
            intent.setData(wordWikiUri);

            final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
            if (resInfo.size() > 0) context.startActivity(intent);
            else {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[1]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        wordWikiUri);
            }

        } else if (id == R.id.btnUrban) {
            customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[2]);
            CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                    Uri.parse("https://www.urban" + "dictionary.com/define.php?term=" + word));
        }
        dismiss();
    }
}