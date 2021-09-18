package awais.backworddictionary.adapters.holders;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.DefinitionsAdapter;
import awais.backworddictionary.databinding.WordItemBinding;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class WordItemViewHolder extends RecyclerView.ViewHolder {
    private final Context context;
    private final WordItemBinding wordItemBinding;
    private final AdapterClickListener wordItemClickListener;
    private final AdapterClickListener definitionItemClickListener;
    private final String[] noItemFound;
    private final boolean showExpandedSearchIcon;

    public final CardView cardView;

    public WordItemViewHolder(@NonNull final WordItemBinding wordItemBinding, final AdapterClickListener wordItemClickListener,
                              final AdapterClickListener definitionItemClickListener, final String[] noItemFound,
                              final boolean showExpandedSearchIcon) {
        super(wordItemBinding.getRoot());
        this.cardView = wordItemBinding.cardView;
        this.wordItemBinding = wordItemBinding;
        this.context = wordItemBinding.getRoot().getContext();
        this.wordItemClickListener = wordItemClickListener;
        this.definitionItemClickListener = definitionItemClickListener;
        this.noItemFound = noItemFound;
        this.showExpandedSearchIcon = showExpandedSearchIcon;
    }

    public void setupItem(@NonNull final WordItem wordItem, final int position) {
        wordItem.setPosition(position);

        final String wordItemWord = wordItem.getWord();
        final String[][] wordItemDefs = wordItem.getDefs();

        wordItemBinding.ivExpandedSearch.setVisibility(showExpandedSearchIcon ? View.VISIBLE : View.GONE);
        wordItemBinding.lvExpandedDefs.setVisibility(wordItem.isExpanded() ? View.VISIBLE : View.GONE);

        wordItemBinding.overflow.setTag(position);
        wordItemBinding.overflow.setTag(R.id.overflow, wordItem);
        wordItemBinding.cardView.setTag(R.id.overflow, wordItemBinding.overflow);
        if (showExpandedSearchIcon)
            wordItemBinding.ivExpandedSearch.setTag(wordItemWord);

        wordItemBinding.word.setText(wordItemWord);
        wordItemBinding.subText.setText(wordItem.getParsedTags());

        final ArrayList<String[]> defsList = new ArrayList<>(1);
        if (wordItemDefs != null && wordItemDefs.length > 0) defsList.addAll(Arrays.asList(wordItemDefs));
        else defsList.add(noItemFound);

        wordItemBinding.lvExpandedDefs.setAdapter(new DefinitionsAdapter<>(context, wordItemWord,
                true, defsList, definitionItemClickListener));

        //wordItemBinding.cardView.setTag(R.id.expandableMenu, wordItemBinding.expandableMenu);
        wordItemBinding.cardView.setTag(R.id.word, wordItem);
        wordItemBinding.cardView.setTag(R.id.lvDefs, defsList);

        wordItemBinding.cardView.setOnLongClickListener(wordItemClickListener);
        wordItemBinding.cardView.setOnClickListener(wordItemClickListener);

        if (showExpandedSearchIcon) {
            wordItemBinding.ivExpandedSearch.setOnClickListener(wordItemClickListener);
            Utils.setPopupMenuSlider(null, context, wordItemBinding.ivExpandedSearch, wordItemWord);
        }

        Utils.setPopupMenuSlider(wordItemBinding.overflow, wordItem);

        wordItemBinding.overflow.setOnClickListener(wordItemClickListener);
    }
}