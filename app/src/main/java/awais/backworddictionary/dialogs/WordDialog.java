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

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.DefinitionsAdapter;
import awais.backworddictionary.databinding.WordDialogBinding;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.CustomTabActivityHelper;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class WordDialog extends Dialog implements android.view.View.OnClickListener {
    private static final int ALERT_DIALOG_THEME = R.style.DefinitionsDialogTheme;
    private static final Exception EMPTY_EXCEPTION = new Exception();

    private final String word;
    private final Context context;
    private final List<?> defs;
    private final CustomTabsIntent.Builder customTabsIntent;
    private final AdapterClickListener itemClickListener;
    private final boolean anySearchAppFound;

    private WordDialogBinding wordDialogBinding;

    public WordDialog(Context context, final String word, final List<?> defs, final AdapterClickListener itemClickListener) {
        super(new ContextThemeWrapper(context, ALERT_DIALOG_THEME), ALERT_DIALOG_THEME);
        this.anySearchAppFound = Utils.isAnySearchAppFound(context);
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

        wordDialogBinding = WordDialogBinding.inflate(getLayoutInflater());
        setContentView(wordDialogBinding.getRoot());

        wordDialogBinding.alertTitle.setText(word);

        wordDialogBinding.lvDefs.setAdapter(new DefinitionsAdapter<>(context, word,
                false, defs, itemClickListener));

        wordDialogBinding.btnCopy.setOnClickListener(this);
        wordDialogBinding.btnSpeak.setOnClickListener(this);
        wordDialogBinding.btnGoogle.setOnClickListener(this);
        wordDialogBinding.btnWiki.setOnClickListener(this);
        wordDialogBinding.btnUrban.setOnClickListener(this);
        wordDialogBinding.btnSearch.setOnClickListener(this);
        wordDialogBinding.btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull final View v) {
        if (v == wordDialogBinding.btnCopy) {
            Utils.copyText(context, word);
            return;
        }

        if (v == wordDialogBinding.btnSpeak) {
            Utils.speakText(word);
            return;
        }

        if (v == wordDialogBinding.btnSearch) {
            Utils.showPopupMenu(this, context, v, word);
            return;
        }

        if (v == wordDialogBinding.btnGoogle) {
            try {
                if (!anySearchAppFound) throw EMPTY_EXCEPTION;
                context.startActivity(new Intent(Intent.ACTION_WEB_SEARCH)
                        .putExtra(SearchManager.QUERY, word)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            } catch (final Exception e) {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[0]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        Uri.parse("https://google.com/search?q=define+".concat(URLEncoder.encode(word))));
            }

        } else if (v == wordDialogBinding.btnWiki) {
            final Uri wordWikiUri = Uri.parse("https://en.wikipedia.org/wiki/".concat(URLEncoder.encode(word)));

            final Intent intent = new Intent().setAction(Intent.ACTION_VIEW)
                    .setPackage("org.wikipedia").setData(wordWikiUri);

            final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
            if (resInfo.size() > 0) context.startActivity(intent);
            else {
                customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[1]);
                CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                        wordWikiUri);
            }

        } else if (v == wordDialogBinding.btnUrban) {
            customTabsIntent.setToolbarColor(Utils.CUSTOM_TAB_COLORS[2]);
            CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                    Uri.parse("https://www.urban".concat("dictionary.com/define.php?term=").concat(word)));
        }
        dismiss();
    }
}