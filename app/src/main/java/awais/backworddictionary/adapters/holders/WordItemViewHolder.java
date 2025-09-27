package awais.backworddictionary.adapters.holders;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.DefinitionsAdapter;
import awais.backworddictionary.databinding.WordItemBinding;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.AdapterClickListener;
import awais.backworddictionary.models.WordItem;

public final class WordItemViewHolder extends RecyclerView.ViewHolder {
    private final Context context;
    private final WordItemBinding wordItemBinding;
    private final AdapterClickListener wordItemClickListener;
    private final AdapterClickListener definitionItemClickListener;
    private final String[] noItemFound;

    public WordItemViewHolder(@NonNull final WordItemBinding wordItemBinding, final AdapterClickListener wordItemClickListener,
                              final AdapterClickListener definitionItemClickListener, final String[] noItemFound) {
        super(wordItemBinding.getRoot());
        this.context = itemView.getContext();
        this.wordItemBinding = wordItemBinding;
        this.wordItemClickListener = wordItemClickListener;
        this.definitionItemClickListener = definitionItemClickListener;
        this.noItemFound = noItemFound;
    }

    public void setupItem(@NonNull final WordItem wordItem, final int position) {
        wordItem.setPosition(position);

        final WordItemBinding wordItemBinding = this.wordItemBinding;
        final String wordItemWord = wordItem.getWord();
        final String[][] wordItemDefs = wordItem.getDefs();

        final ArrayList<String[]> defsList = new ArrayList<>();
        if (wordItemDefs != null && wordItemDefs.length > 0) Collections.addAll(defsList, wordItemDefs);
        else defsList.add(noItemFound);

        wordItemBinding.lvExpandedDefs.setVisibility(wordItem.isExpanded() ? View.VISIBLE : View.GONE);

        wordItemBinding.overflow.setTag(position);
        wordItemBinding.overflow.setTag(R.id.overflow, wordItem);

        wordItemBinding.cardView.setTag(R.id.overflow, wordItemBinding.overflow);

        wordItemBinding.ivExpandedSearch.setTag(wordItemWord);

        // wordItemBinding.cardView.setTag(R.id.expandableMenu, wordItemBinding.expandableMenu);
        wordItemBinding.cardView.setTag(R.id.word, wordItem);
        wordItemBinding.cardView.setTag(R.id.lvDefs, defsList);

        wordItemBinding.word.setText(wordItemWord);
        wordItemBinding.subText.setText(wordItem.getParsedTags());

        final DefinitionsAdapter<?> defsAdapter;
        if (wordItemBinding.lvExpandedDefs.getTag() instanceof final DefinitionsAdapter<?> adapter) defsAdapter = adapter;
        else {
            defsAdapter = new DefinitionsAdapter<>(context, wordItemWord, true, defsList, definitionItemClickListener);
            wordItemBinding.lvExpandedDefs.setTag(defsAdapter);
        }
        wordItemBinding.lvExpandedDefs.setAdapter(defsAdapter);

        wordItemBinding.cardView.setOnLongClickListener(wordItemClickListener);
        wordItemBinding.cardView.setOnClickListener(wordItemClickListener);
        wordItemBinding.overflow.setOnClickListener(wordItemClickListener);
        wordItemBinding.ivExpandedSearch.setOnClickListener(wordItemClickListener);

        Utils.setPopupMenuSlider(null, context, wordItemBinding.ivExpandedSearch, wordItemWord);
        Utils.setPopupMenuSlider(wordItemBinding.overflow, wordItem);
    }

    public void setCardViewBackground(final int cardBackColor) {
        if (wordItemBinding != null) wordItemBinding.cardView.setCardBackgroundColor(cardBackColor);
    }
}