package awais.backworddictionary.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.List;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class DefinitionsAdapter<T> extends ArrayAdapter<T> {
    private final boolean isExpanded;
    private final List<T> items;
    private final LayoutInflater layoutInflater;
    private final View.OnClickListener onClickListener;
    private final String currentWord;
    private int topMargin = 0, subSize = 20;

    public DefinitionsAdapter(final Context context, final String currentWord, final boolean isExpanded,
                              final List<T> items, final AdapterClickListener adapterClickListener) {
        super(context, R.layout.word_dialog_item, items);
        this.currentWord = currentWord;
        this.onClickListener = adapterClickListener;
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
            viewHolder = new Holder(row.findViewById(android.R.id.text1), row.findViewById(android.R.id.text2));
            viewHolder.tvSub.measure(0, 0);
            topMargin = ((ViewGroup.MarginLayoutParams) viewHolder.tvSub.getLayoutParams()).topMargin;
            subSize = viewHolder.tvSub.getMeasuredHeight() / 2;
            viewHolder.tvWord.setTypeface(LinkedApp.fontRegular);
            viewHolder.tvSub.setTypeface(LinkedApp.fontMedium);
            row.setTag(viewHolder);
        } else viewHolder = (Holder) row.getTag();

        if (!(row.getTag(R.id.word) instanceof CharSequence))
            row.setTag(R.id.word, currentWord);

        final String[] wordItem = (String[]) items.get(position);

        final String definition = wordItem[1];
        final String tags = wordItem[0];

        row.setTag(R.id.word_key, definition);
        row.setOnClickListener(onClickListener);

        final boolean tvWordNotNull = viewHolder.tvWord != null;

        if (tvWordNotNull) viewHolder.tvWord.setText(definition);

        if (viewHolder.tvSub != null)
            if (!isExpanded && tags != null && !tags.isEmpty()) {
                final String itemSub = '[' + tags + ']';
                viewHolder.tvSub.setText(itemSub);
                viewHolder.tvSub.setVisibility(View.VISIBLE);
            } else {
                if (isExpanded && tvWordNotNull)
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