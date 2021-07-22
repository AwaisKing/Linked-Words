package awais.backworddictionary.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;

public final class WordItemViewHolder extends RecyclerView.ViewHolder {
    public final TextView word, subtext;
    public final CardView cardView;
    public final ListView lvExpandedDefs;
    public final ImageView ivExpandedSearch, overflow;

    public WordItemViewHolder(final View view) {
        super(view);
        word = view.findViewById(R.id.word);
        subtext = view.findViewById(R.id.subText);
        overflow = view.findViewById(R.id.overflow);
        cardView = view.findViewById(R.id.card_view);
        lvExpandedDefs = view.findViewById(R.id.lvExpandedDefs);
        ivExpandedSearch = view.findViewById(R.id.ivExpandedSearch);
    }
}