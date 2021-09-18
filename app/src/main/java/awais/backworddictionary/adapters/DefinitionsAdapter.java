package awais.backworddictionary.adapters;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.List;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;
import awais.backworddictionary.databinding.WordDialogItemBinding;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class DefinitionsAdapter<T> implements ListAdapter {
    private final boolean isExpanded;
    private final List<T> items;
    private final LayoutInflater layoutInflater;
    private final View.OnClickListener onClickListener;
    private final String currentWord;
    private int topMargin = 0, subSize = 20;

    public DefinitionsAdapter(final Context context, final String currentWord, final boolean isExpanded,
                              final List<T> items, final AdapterClickListener adapterClickListener) {
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
        final WordDialogItemBinding dialogItemBinding;

        if (row == null) {
            dialogItemBinding = WordDialogItemBinding.inflate(layoutInflater);
            row = dialogItemBinding.getRoot();

            dialogItemBinding.tvSub.measure(0, 0);
            topMargin = ((ViewGroup.MarginLayoutParams) dialogItemBinding.tvSub.getLayoutParams()).topMargin;
            subSize = dialogItemBinding.tvSub.getMeasuredHeight() / 2;

            dialogItemBinding.tvWord.setTypeface(LinkedApp.fontRegular);
            dialogItemBinding.tvSub.setTypeface(LinkedApp.fontMedium);

            row.setTag(dialogItemBinding);
        } else dialogItemBinding = (WordDialogItemBinding) row.getTag();

        if (!(row.getTag(R.id.word) instanceof CharSequence))
            row.setTag(R.id.word, currentWord);

        final String[] wordItem = (String[]) items.get(position);

        final String definition = wordItem[1];
        final String tags = wordItem[0];

        row.setTag(R.id.word_key, definition);
        row.setOnClickListener(onClickListener);

        if (dialogItemBinding != null) {
            dialogItemBinding.tvWord.setText(definition);

            if (!isExpanded && tags != null && !tags.isEmpty()) {
                final String itemSub = '[' + tags + ']';
                dialogItemBinding.tvSub.setText(itemSub);
                dialogItemBinding.tvSub.setVisibility(View.VISIBLE);
            } else {
                if (isExpanded)
                    dialogItemBinding.tvWord.setPadding(ViewCompat.getPaddingStart(dialogItemBinding.tvWord), subSize,
                            ViewCompat.getPaddingEnd(dialogItemBinding.tvWord), subSize + topMargin);
                dialogItemBinding.tvSub.setVisibility(View.GONE);
            }
        }

        return row;
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        dataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        dataSetObservable.unregisterObserver(observer);
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() <= 0;
    }
    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(final int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEnabled(final int position) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    private final DataSetObservable dataSetObservable = new DataSetObservable();
}