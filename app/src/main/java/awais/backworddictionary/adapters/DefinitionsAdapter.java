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
import awais.backworddictionary.interfaces.SearchAdapterClickListener;

public final class DefinitionsAdapter extends ArrayAdapter<String[]> {
    private final boolean isExpanded;
    private final ArrayList<String[]> items;
    private final LayoutInflater layoutInflater;
    private final View.OnClickListener onClickListener;
    private int topMargin = 0, subSize = 20;

    public DefinitionsAdapter(final Context context, final boolean isExpanded, final ArrayList<String[]> items,
                              final SearchAdapterClickListener adapterClickListener) {
        super(context, R.layout.word_dialog_item, items);
        this.onClickListener = v -> {
            final Object tag = v.getTag(R.id.word_key);
            if (tag instanceof String)
                adapterClickListener.onItemClick((String) tag);
        };
        this.layoutInflater = LayoutInflater.from(context);
        this.items = items;
        this.isExpanded = isExpanded;
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View row = convertView;
        final Holder viewHolder;

        if (row == null) {
            row = layoutInflater.inflate(R.layout.word_dialog_item, parent, false);
            viewHolder = new Holder(row.findViewById(R.id.item_text), row.findViewById(R.id.sub_text));
            viewHolder.tvSub.measure(0, 0);
            topMargin = ((ViewGroup.MarginLayoutParams) viewHolder.tvSub.getLayoutParams()).topMargin;
            subSize = viewHolder.tvSub.getMeasuredHeight() / 2;
            viewHolder.tvWord.setTypeface(LinkedApp.fontRegular);
            viewHolder.tvSub.setTypeface(LinkedApp.fontMedium);
            row.setTag(viewHolder);
        } else viewHolder = (Holder) row.getTag();

        final String[] wordItem = items.get(position);

        final String word = wordItem[1];
        final String subWord = wordItem[0];

        row.setTag(R.id.word_key, word);
        row.setOnClickListener(onClickListener);

        if (viewHolder.tvWord != null)
            viewHolder.tvWord.setText(word);

        if (viewHolder.tvSub != null)
            if (!isExpanded && subWord != null && !subWord.isEmpty()) {
                final String itemSub = '[' + subWord + ']';
                viewHolder.tvSub.setText(itemSub);
                viewHolder.tvSub.setVisibility(View.VISIBLE);
            } else {
                if (isExpanded && viewHolder.tvWord != null)
                    viewHolder.tvWord.setPadding(ViewCompat.getPaddingStart(viewHolder.tvWord), subSize,
                            ViewCompat.getPaddingEnd(viewHolder.tvWord), subSize + topMargin);
                viewHolder.tvSub.setVisibility(View.GONE);
            }

        return row;
    }

    private static class Holder {
        private final TextView tvWord, tvSub;

        public Holder(final TextView tvWord, final TextView tvSub) {
            this.tvWord = tvWord;
            this.tvSub = tvSub;
        }
    }
}