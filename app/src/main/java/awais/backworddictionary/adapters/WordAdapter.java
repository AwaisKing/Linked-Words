package awais.backworddictionary.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;

public class WordAdapter extends ArrayAdapter<String[]> {
    private final ArrayList<String[]> items;
    private final SearchAdapter.OnItemClickListener onItemClickListener;
    private final boolean isDialog;
    private int topMargin = 0, subSize = 20;

    public WordAdapter(Context context, boolean isDialog, ArrayList<String[]> items,
            SearchAdapter.OnItemClickListener onItemClickListener) {
        super(context, R.layout.word_dialog_item, items);
        this.isDialog = isDialog;
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
            viewHolder.tvSub.measure(0, 0);
            topMargin = ((ViewGroup.MarginLayoutParams) viewHolder.tvSub.getLayoutParams()).topMargin;
            subSize = viewHolder.tvSub.getMeasuredHeight() / 2;
            viewHolder.tvWord.setTypeface(LinkedApp.fontRegular);
            viewHolder.tvSub.setTypeface(LinkedApp.fontMedium);
            row.setTag(viewHolder);
        } else viewHolder = (Holder) row.getTag();

        String[] wordItem = items.get(position);

        if (viewHolder.tvWord != null) viewHolder.tvWord.setText(wordItem[1]);

        if (viewHolder.tvSub != null)
            if (!isDialog && wordItem[0] != null && !wordItem[0].isEmpty()) {
                String itemSub = '[' + wordItem[0] + ']';
                viewHolder.tvSub.setText(itemSub);
                viewHolder.tvSub.setVisibility(View.VISIBLE);
            } else {
                if (isDialog && viewHolder.tvWord != null)
                    viewHolder.tvWord.setPadding(ViewCompat.getPaddingStart(viewHolder.tvWord), subSize,
                            ViewCompat.getPaddingEnd(viewHolder.tvWord), subSize + topMargin);
                viewHolder.tvSub.setVisibility(View.GONE);
            }

        View finalRow = row;
        if (onItemClickListener != null)
            row.setOnClickListener(view -> onItemClickListener.onItemClick(
                    finalRow, getPosition(wordItem), wordItem[1]));

        return row;
    }

    private class Holder {
        TextView tvWord;
        TextView tvSub;
    }
}