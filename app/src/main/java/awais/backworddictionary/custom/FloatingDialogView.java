package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.TooltipCompat;

import java.util.ArrayList;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.DictionaryWordsAdapter;
import awais.backworddictionary.databinding.LayoutFloatingDialogBinding;
import awais.backworddictionary.executors.WordsAsync;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;
import awais.backworddictionary.models.Tab;
import awais.backworddictionary.models.WordItem;

public final class FloatingDialogView extends FrameLayout implements View.OnClickListener, TextWatcher, FragmentCallback,
                                                                             TextView.OnEditorActionListener, PopupMenu.OnMenuItemClickListener {
    private final ArrayList<WordItem> wordList = new ArrayList<>();
    private final LayoutFloatingDialogBinding dialogBinding;
    private final DictionaryWordsAdapter wordsAdapter;
    private final PopupMenu popup;

    private final Tab[] tabs = Tab.values();

    private String word;
    private int method;

    public FloatingDialogView(final Context context) {
        this(context, null);
    }

    public FloatingDialogView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.style.MaterialAlertDialogTheme);
    }

    public FloatingDialogView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        final LayoutFloatingDialogBinding dialogBinding = LayoutFloatingDialogBinding.inflate(layoutInflater, this, true);
        this.dialogBinding = dialogBinding;

        /// setup search popup
        {
            popup = new PopupMenu(getContext(), dialogBinding.btnSearch);
            final Menu menu = popup.getMenu();

            final boolean[] tabBools = SettingsHelper.getInstance(context).getTabs();
            for (int i = 0; i < Math.min(tabBools.length, tabs.length); i++) {
                tabs[i] = tabs[i].setEnabled(tabBools[i]);
                menu.add(0, i, i, tabs[i].getTabName()).setVisible(tabs[i].isEnabled());
            }

            popup.setOnMenuItemClickListener(this);

            dialogBinding.btnSearch.setTag(R.id.key_popup, popup);
        }

        /// setup recycler view and adapter
        {
            wordList.clear();
            wordsAdapter = new DictionaryWordsAdapter(context, wordList);
            dialogBinding.rvItems.setAdapter(wordsAdapter);
        }

        TooltipCompat.setTooltipText(dialogBinding.btnCopy, context.getString(R.string.copy));
        TooltipCompat.setTooltipText(dialogBinding.btnSpeak, context.getString(R.string.speak));

        dialogBinding.etSearchView.removeTextChangedListener(this);
        dialogBinding.etSearchView.addTextChangedListener(this);

        dialogBinding.etSearchView.setOnEditorActionListener(this);

        dialogBinding.btnClear.setOnClickListener(this);
        dialogBinding.btnSearch.setOnClickListener(this);

        dialogBinding.btnCopy.setOnClickListener(this);
        dialogBinding.btnSpeak.setOnClickListener(this);

        dialogBinding.etSearchView.post(new Runnable() {
            @Override
            public void run() {
                dialogBinding.etSearchView.removeCallbacks(this);

                dialogBinding.etSearchView.setText(word);
                dialogBinding.etSearchView.setSelection(word.length());

                if (!Utils.isEmpty(word))
                    onMenuItemClick(popup.getMenu().getItem(0));

                onTextChanged(word, 0, 0, 0);
            }
        });
    }

    public FloatingDialogView setWord(final String word) {
        this.word = word;
        return this;
    }

    @Override
    public void wordStarted() {
        final LayoutFloatingDialogBinding dialogBinding = this.dialogBinding;
        dialogBinding.etSearchView.post(new Runnable() {
            @Override
            public void run() {
                dialogBinding.etSearchView.setEnabled(false);
                dialogBinding.btnClear.setVisibility(GONE);
                dialogBinding.btnSearch.setVisibility(GONE);
                dialogBinding.progressBar.setVisibility(VISIBLE);
                dialogBinding.etSearchView.removeCallbacks(this);
            }
        });
    }

    @Override
    public void done(final ArrayList<WordItem> items, @NonNull final String word) {
        final LayoutFloatingDialogBinding dialogBinding = this.dialogBinding;

        dialogBinding.progressBar.setVisibility(GONE);
        dialogBinding.etSearchView.setEnabled(true);
        dialogBinding.btnClear.setVisibility(VISIBLE);
        dialogBinding.btnSearch.setVisibility(VISIBLE);

        try {
            dialogBinding.etSearchView.setSelection(word.length());
        } catch (final Exception e) {
            final CharSequence str = dialogBinding.etSearchView.getText();
            if (!Utils.isEmpty(str)) dialogBinding.etSearchView.setSelection(str.length());
        }

        this.word = word;

        wordList.clear();
        if (items != null) wordList.addAll(items);
        wordsAdapter.updateList(wordList);

        dialogBinding.rvItems.setAdapter(wordsAdapter);
    }

    @Override
    public void onClick(final View v) {
        final LayoutFloatingDialogBinding dialogBinding = this.dialogBinding;
        if (v == dialogBinding.btnClear) dialogBinding.etSearchView.setText(null);
        else if (v == dialogBinding.btnSearch) popup.show();
        else if (v == dialogBinding.btnCopy) Utils.copyText(getContext(), word);
        else if (v == dialogBinding.btnSpeak) TTSHelper.speakText(word);
    }

    public boolean searchWord(final int method, final String word) {
        dialogBinding.etSearchView.setText(word);
        final boolean hasWord = !Utils.isEmpty(word);
        if (hasWord) new WordsAsync(this, this.word = word, this.method = method, getContext()).execute();
        return hasWord;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        if (item == null && (method == 0 || method == -1)) method = tabs[0].getTabName();
        if (method == 0 || method == -1) {
            final int itemIdx = item == null || item.getItemId() < 0 || item.getItemId() >= tabs.length ? 0 : item.getItemId();
            try {
                method = tabs[itemIdx].getTabName();
            } catch (Exception e) {
                method = tabs[0].getTabName();
            }
        }
        return searchWord(method, String.valueOf(dialogBinding.etSearchView.getText()));
    }

    @Override
    public boolean onEditorAction(final TextView textView, final int i, final KeyEvent keyEvent) {
        return onMenuItemClick(null);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
        final boolean hasText = !Utils.isEmpty(charSequence);
        dialogBinding.btnSearch.setVisibility(hasText ? VISIBLE : INVISIBLE);
        dialogBinding.btnClear.setVisibility(hasText ? VISIBLE : INVISIBLE);

        // floatingDialogBinding.btnSpeak.setVisibility(hasText ? VISIBLE : GONE);
        dialogBinding.btnSpeak.setEnabled(hasText);
        dialogBinding.btnSpeak.setAlpha(hasText ? 1f : 0.7f);
        // floatingDialogBinding.btnCopy.setVisibility(hasText ? VISIBLE : GONE);
        dialogBinding.btnCopy.setEnabled(hasText);
        dialogBinding.btnCopy.setAlpha(hasText ? 1f : 0.7f);

        dialogBinding.btnSearch.setOnTouchListener(hasText ? popup.getDragToOpenListener() : null);
    }

    @Override
    public void afterTextChanged(final Editable editable) {}

    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {}
}