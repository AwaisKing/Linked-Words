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
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.databinding.LayoutFloatingDialogBinding;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;

public final class FloatingDialogView extends FrameLayout implements View.OnClickListener, TextWatcher, FragmentCallback,
        TextView.OnEditorActionListener, PopupMenu.OnMenuItemClickListener {
    private final ArrayList<WordItem> wordList = new ArrayList<>(0);
    private final LayoutFloatingDialogBinding dialogBinding;
    private final DictionaryWordsAdapter wordsAdapter;
    private final PopupMenu popup;

    private String word, method;

    public FloatingDialogView(final Context context) {
        this(context, null);
    }

    public FloatingDialogView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.style.MaterialAlertDialogTheme);
    }

    public FloatingDialogView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        dialogBinding = LayoutFloatingDialogBinding.inflate(layoutInflater, this, true);

        // setup search popup
        {
            final boolean[] tabBoolsArray = SettingsHelper.getTabs();

            popup = new PopupMenu(getContext(), dialogBinding.btnSearch);
            final Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.menu_search, menu);
            for (int i = tabBoolsArray.length - 1; i >= 0; i--)
                menu.getItem(i).setVisible(tabBoolsArray[i]);

            popup.setOnMenuItemClickListener(this);

            dialogBinding.btnSearch.setTag(R.id.key_popup, popup);
        }

        // setup recycler view and adapter
        {
            wordList.clear();
            wordsAdapter = new DictionaryWordsAdapter(context, wordList);
            wordsAdapter.setShowExpandedSearchIcon(false);
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
        dialogBinding.progressBar.setVisibility(GONE);
        dialogBinding.etSearchView.setEnabled(true);
        dialogBinding.btnClear.setVisibility(VISIBLE);
        dialogBinding.btnSearch.setVisibility(VISIBLE);

        dialogBinding.etSearchView.setSelection(word.length());

        this.word = word;

        wordList.clear();
        if (items != null) wordList.addAll(items);
        wordsAdapter.updateList(wordList);

        dialogBinding.rvItems.setAdapter(wordsAdapter);
    }

    @Override
    public void onClick(final View v) {
        if (v == dialogBinding.btnClear) dialogBinding.etSearchView.setText(null);
        else if (v == dialogBinding.btnSearch) popup.show();
        else if (v == dialogBinding.btnCopy) Utils.copyText(getContext(), word);
        else if (v == dialogBinding.btnSpeak) Utils.speakText(word);
    }

    public boolean searchWord(final String word) {
        dialogBinding.etSearchView.setText(word);

        if (!Utils.isEmpty(word)) {
            new WordsAsync(this, this.word = word, this.method, getContext()).execute();
            return true;
        }

        return false;
    }

    public boolean searchWord() {
        return searchWord(String.valueOf(dialogBinding.etSearchView.getText()));
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        final String defMethod = popup.getMenu().getItem(0).getTitle().toString();
        if (item == null && Utils.isEmpty(method)) method = defMethod;
        if (Utils.isEmpty(method)) method = defMethod;
        return searchWord();
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

        //floatingDialogBinding.btnSpeak.setVisibility(hasText ? VISIBLE : GONE);
        dialogBinding.btnSpeak.setEnabled(hasText);
        dialogBinding.btnSpeak.setAlpha(hasText ? 1f : 0.7f);
        //floatingDialogBinding.btnCopy.setVisibility(hasText ? VISIBLE : GONE);
        dialogBinding.btnCopy.setEnabled(hasText);
        dialogBinding.btnCopy.setAlpha(hasText ? 1f : 0.7f);

        dialogBinding.btnSearch.setOnTouchListener(hasText ? popup.getDragToOpenListener() : null);
    }

    @Override
    public void afterTextChanged(final Editable editable) { }

    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) { }
}