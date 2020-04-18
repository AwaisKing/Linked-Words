package awais.backworddictionary.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.adapters.holders.ResultViewHolder;
import awais.backworddictionary.helpers.Utils;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public class SearchAdapter extends RecyclerView.Adapter<ResultViewHolder> implements Filterable {
    private final Filter filter;
    private final LayoutInflater layoutInflater;
    private final SearchHistoryTable historyDatabase;
    private final SearchAdapter.OnItemClickListener clickListener;
    private final SearchAdapter.OnItemLongClickListener longClickListener;
    private List<SearchItem> suggestionsList = new ArrayList<>();
    private List<SearchItem> resultList = new ArrayList<>();
    private String key = "";


    public SearchAdapter(final Context context, final SearchHistoryTable table, final OnItemClickListener clickListener,
                         final OnItemLongClickListener longClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.historyDatabase = table;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (!Utils.isEmpty(constraint)) {
                    key = constraint.toString().toLowerCase(Utils.defaultLocale);

                    final List<SearchItem> results = new ArrayList<>();
                    final List<SearchItem> history = new ArrayList<>();
                    final List<SearchItem> databaseAllItems = historyDatabase.getAllItems(null);

                    if (!databaseAllItems.isEmpty()) history.addAll(databaseAllItems);
                    history.addAll(suggestionsList);

                    for (final SearchItem item : history)
                        if (item.getText().toString().toLowerCase(Utils.defaultLocale).contains(key))
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
                    for (final Object object : (ArrayList<?>) results.values)
                        if (object instanceof SearchItem) dataSet.add((SearchItem) object);
                } else if (key.isEmpty()) {
                    final List<SearchItem> allItems = historyDatabase.getAllItems(null);
                    if (!allItems.isEmpty()) dataSet = allItems;
                }

                setData(dataSet);
            }
        };
        filter.filter("");
    }

    public void setSuggestionsList(final List<SearchItem> suggestionsList) {
        this.suggestionsList = suggestionsList;
        this.resultList = suggestionsList;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void setData(final List<SearchItem> data) {
        if (resultList.size() == 0) {
            resultList = data;
            notifyDataSetChanged();
        } else {
            final int previousSize = resultList.size();
            final int nextSize = data.size();
            resultList = data;
            if (previousSize == nextSize) notifyItemRangeChanged(0, previousSize);
            else if (previousSize > nextSize) {
                if (nextSize == 0) notifyItemRangeRemoved(0, previousSize);
                else {
                    notifyItemRangeChanged(0, nextSize);
                    notifyItemRangeRemoved(nextSize - 1, previousSize);
                }
            } else {
                notifyItemRangeChanged(0, previousSize);
                notifyItemRangeInserted(previousSize, nextSize - previousSize);
            }
        }
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ResultViewHolder(layoutInflater.inflate(R.layout.search_item, parent, false),
                clickListener, longClickListener) {
            @Override
            public int getItemsCount() {
                return getItemCount();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull final ResultViewHolder viewHolder, final int position) {
        final SearchItem item = resultList.get(position);

        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.icon.setColorFilter(MaterialSearchView.mIconColor, PorterDuff.Mode.SRC_IN);

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

    public interface OnItemClickListener {
        void onItemClick(final View view, final int position, final String text);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(final View view, int position, final String text);
    }
}