package awais.backworddictionary.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;

public class WordAdapter extends ArrayAdapter<String[]> {
    private final ArrayList<String[]> items;
    private final SearchAdapter.OnItemClickListener onItemClickListener;
    private final boolean isExpanded;
    private int topMargin = 0, subSize = 20;

    public WordAdapter(Context context, boolean isExpanded, ArrayList<String[]> items,
            SearchAdapter.OnItemClickListener onItemClickListener) {
        super(context, R.layout.word_dialog_item, items);
        this.isExpanded = isExpanded;
        this.items = items;
        this.onItemClickListener  = onItemClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final Holder viewHolder;

        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.word_dialog_item, parent, false);
            viewHolder = new Holder();
            viewHolder.tvWord = row.findViewById(R.id.item_text);
            viewHolder.tvSub = row.findViewById(R.id.sub_text);
            viewHolder.tvSub.measure(0, 0);
            topMargin = ((ViewGroup.MarginLayoutParams) viewHolder.tvSub.getLayoutParams()).topMargin;
            subSize = viewHolder.tvSub.getMeasuredHeight() / 2;
            viewHolder.tvWord.setTypeface(LinkedApp.fontRegular);
            viewHolder.tvSub.setTypeface(LinkedApp.fontMedium);
            row.setTag(viewHolder);
        } else viewHolder = (Holder) row.getTag();

        final String[] wordItem = items.get(position);

        if (viewHolder.tvWord != null) viewHolder.tvWord.setText(wordItem[1]);

        if (viewHolder.tvSub != null)
            if (!isExpanded && wordItem[0] != null && !wordItem[0].isEmpty()) {
                final String itemSub = '[' + wordItem[0] + ']';
                viewHolder.tvSub.setText(itemSub);
                viewHolder.tvSub.setVisibility(View.VISIBLE);
            } else {
                if (isExpanded && viewHolder.tvWord != null)
                    viewHolder.tvWord.setPadding(ViewCompat.getPaddingStart(viewHolder.tvWord), subSize,
                            ViewCompat.getPaddingEnd(viewHolder.tvWord), subSize + topMargin);
                viewHolder.tvSub.setVisibility(View.GONE);
            }

        final View finalRow = row;
        if (onItemClickListener != null) row.setOnClickListener(v ->
                onItemClickListener.onItemClick(finalRow, getPosition(wordItem), wordItem[1]));

        return finalRow;
    }

    private static class Holder {
        TextView tvWord;
        TextView tvSub;
    }
}