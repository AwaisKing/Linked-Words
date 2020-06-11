package awais.backworddictionary.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.ResultViewHolder;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.SearchAdapterClickListener;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public final class SearchAdapter extends RecyclerView.Adapter<ResultViewHolder> implements Filterable {
    private final Filter filter;
    private final LayoutInflater layoutInflater;
    private final SearchHistoryTable historyDatabase;
    private final SearchAdapterClickListener clickListener;
    private List<SearchItem> suggestionsList = new ArrayList<>();
    private List<SearchItem> resultList = new ArrayList<>();
    private String key = "";

    public SearchAdapter(final Context context, @Nullable final SearchHistoryTable table, final SearchAdapterClickListener listener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.historyDatabase = table;
        this.clickListener = listener;
        this.filter = new Filter() {
            @NonNull
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (!Utils.isEmpty(constraint)) {
                    key = constraint.toString().toLowerCase();

                    final List<SearchItem> results = new ArrayList<>();
                    final List<SearchItem> history = new ArrayList<>();
                    final List<SearchItem> databaseAllItems = historyDatabase != null ? historyDatabase.getAllItems(null) : null;

                    if (databaseAllItems != null && !databaseAllItems.isEmpty()) history.addAll(databaseAllItems);
                    history.addAll(suggestionsList);

                    for (final SearchItem item : history)
                        if (item.getText().toString().toLowerCase().contains(key))
                            results.add(item);

                    if (results.size() > 0) {
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                } else key = "";

                return filterResults;
            }

            @Override
            protected void publishResults(final CharSequence constraint, final FilterResults results) {
                List<SearchItem> dataSet = new ArrayList<>();

                if (results.count > 0) {
                    //noinspection unchecked
                    dataSet = (List<SearchItem>) results.values;
                } else if (key.isEmpty() && historyDatabase != null) {
                    final List<SearchItem> allItems = historyDatabase.getAllItems(null);
                    if (!allItems.isEmpty()) dataSet = allItems;
                }

                setData(dataSet);
            }
        };
        this.filter.filter("");
    }

    public void setSuggestionsList(final List<SearchItem> suggestionsList) {
        this.suggestionsList = suggestionsList;
        setData(suggestionsList);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private void setData(final List<SearchItem> data) {
        final int previousSize = resultList.size();
        if (previousSize == 0) {
            resultList = data;
            notifyDataSetChanged();
        } else {
            final int nextSize = data.size();
            resultList = data;
            if (previousSize == nextSize) notifyItemRangeChanged(0, previousSize);
            else if (previousSize <= nextSize) {
                notifyItemRangeChanged(0, previousSize);
                notifyItemRangeInserted(previousSize, nextSize - previousSize);
            } else if (nextSize != 0) {
                notifyItemRangeChanged(0, nextSize);
                notifyItemRangeRemoved(nextSize - 1, previousSize);
            } else
                notifyItemRangeRemoved(0, previousSize);
        }
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ResultViewHolder(layoutInflater.inflate(R.layout.search_item, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ResultViewHolder viewHolder, final int position) {
        final SearchItem item = resultList.get(position);

        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.icon.setColorFilter(MaterialSearchView.iconColor, PorterDuff.Mode.SRC_IN);

        final String itemText = item.getText().toString();
        final String itemTextLower = itemText.toLowerCase(Utils.defaultLocale);

        if (itemTextLower.contains(key) && !key.isEmpty()) {
            final SpannableString s = new SpannableString(itemText);
            final int keyIndex = itemTextLower.indexOf(key);
            s.setSpan(new ForegroundColorSpan(0xA72196F3), keyIndex, keyIndex + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        } else
            viewHolder.text.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }
}