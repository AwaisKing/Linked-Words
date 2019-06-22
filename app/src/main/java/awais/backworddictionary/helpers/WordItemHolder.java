package awais.backworddictionary.helpers;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import awais.backworddictionary.R;

public class WordItemHolder extends RecyclerView.ViewHolder {
    public final TextView word;
    public final TextView subtext;
    public final CardView cardView;
    public final ImageView overflow;


    public WordItemHolder(View view) {
        super(view);
        word = view.findViewById(R.id.word);
        subtext = view.findViewById(R.id.subText);
        overflow = view.findViewById(R.id.overflow);
        cardView = view.findViewById(R.id.card_view);
    }
}