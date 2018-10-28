package awais.backworddictionary.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;

class WordAdapter extends ArrayAdapter<String[]> {
    final private ArrayList<String[]> items;
    final private SearchAdapter.OnItemClickListener onItemClickListener;

    WordAdapter(Context context, ArrayList<String[]> items, SearchAdapter.OnItemClickListener onItemClickListener) {
        super(context, R.layout.word_dialog_item, items);
        this.items = items;
        this.onItemClickListener  = onItemClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Holder viewHolder;

        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.word_dialog_item, parent, false);
            viewHolder = new Holder();
            viewHolder.tvWord = row.findViewById(R.id.item_text);
            viewHolder.tvSub = row.findViewById(R.id.sub_text);
            viewHolder.tvWord.setTypeface(LinkedApp.fontRegular);
            viewHolder.tvSub.setTypeface(LinkedApp.fontMedium);
            row.setTag(viewHolder);
        } else viewHolder = (Holder) row.getTag();

        String[] wordItem = items.get(position);
        if (viewHolder.tvSub != null) {
            if (wordItem[0] != null && !wordItem[0].isEmpty()) {
                viewHolder.tvSub.setText(String.format("[%s]", wordItem[0]));
                viewHolder.tvSub.setVisibility(View.VISIBLE);
            } else viewHolder.tvSub.setVisibility(View.GONE);
        }
        if (viewHolder.tvWord != null) viewHolder.tvWord.setText(wordItem[1]);

        View finalRow = row;
        row.setOnClickListener(view -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(finalRow, getPosition(wordItem), wordItem[1]);
        });

        return row;
    }

    private class Holder {
        TextView tvWord;
        TextView tvSub;
    }
}