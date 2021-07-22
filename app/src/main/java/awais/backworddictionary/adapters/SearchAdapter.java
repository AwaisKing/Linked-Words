package awais.backworddictionary.adapters;

import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import awais.backworddictionary.interfaces.AdapterClickListener;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public final class SearchAdapter extends RecyclerView.Adapter<ResultViewHolder> implements Filterable {
    private final Filter filter;
    private final SearchHistoryTable historyDatabase;
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;
    private List<SearchItem> suggestionsList = new ArrayList<>(0);
    private List<SearchItem> resultList = new ArrayList<>(0);
    private LayoutInflater layoutInflater;
    private View viewDivider;
    private String key = "";

    public SearchAdapter(@Nullable final SearchHistoryTable table, @NonNull final AdapterClickListener clickListener) {
        this.historyDatabase = table;
        this.onClickListener = clickListener;
        this.onLongClickListener = clickListener;
        this.filter = new Filter() {
            private final FilterResults filterResults = new FilterResults();

            @NonNull
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                filterResults.values = null;
                filterResults.count = 0;

                if (Utils.isEmpty(constraint)) key = "";
                else {
                    key = constraint.toString().toLowerCase(Utils.defaultLocale);

                    final List<SearchItem> history = new ArrayList<>(0);
                    if (historyDatabase != null) {
                        final List<SearchItem> databaseAllItems = historyDatabase.getAllItems(null);
                        if (databaseAllItems != null && !databaseAllItems.isEmpty())
                            history.addAll(databaseAllItems);
                    }
                    history.addAll(suggestionsList);

                    final ArrayList<SearchItem> results = new ArrayList<>(0);
                    for (final SearchItem item : history) {
                        boolean containsSearch = false;
                        for (int i = 0; i < results.size(); i++) {
                            final SearchItem searchItem = results.get(i);
                            if (searchItem != null && (searchItem == item || searchItem.equals(item) ||
                                    TextUtils.equals(searchItem.getText(), item.getText()))) {
                                containsSearch = true;
                                break;
                            }
                        }

                        if (!containsSearch && item.getText().toLowerCase().contains(key))
                            results.add(item);
                    }

                    filterResults.values = results;
                    filterResults.count = results.size();
                }

                if (viewDivider != null) viewDivider.setVisibility(filterResults.values == null ||
                        filterResults.count < 1 ? View.GONE : View.VISIBLE);

                return filterResults;
            }

            @Override
            protected void publishResults(final CharSequence constraint, FilterResults results) {
                if (results == null) results = filterResults;

                //noinspection unchecked
                final List<SearchItem> dataSet = results.count > 0 ? (List<SearchItem>) results.values
                        : key.isEmpty() && historyDatabase != null ? historyDatabase.getAllItems(null)
                        : null;
                setData(dataSet);

                filterResults.values = null;
                filterResults.count = 0;
            }
        };
        this.filter.filter("");
    }

    public void setSuggestionsList(final List<SearchItem> suggestionsList) {
        this.suggestionsList = suggestionsList;
        getFilter().filter(key);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private void setData(final List<SearchItem> data) {
        final int previousSize = getItemCount();
        final int nextSize = data == null ? 0 : data.size();
        resultList = data;
        if (previousSize == 0 && nextSize == 0) return;
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

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        return new ResultViewHolder(layoutInflater.inflate(R.layout.search_item, parent, false),
                onClickListener, onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ResultViewHolder viewHolder, final int position) {
        final SearchItem item = resultList.get(position);

        viewHolder.itemView.setTag(item);

        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.icon.setColorFilter(MaterialSearchView.iconColor, PorterDuff.Mode.SRC_IN);

        final String itemText = item.getText();
        final String itemTextLower = itemText.toLowerCase(Utils.defaultLocale);

        if (!itemTextLower.contains(key) || key.isEmpty()) viewHolder.text.setText(itemText);
        else {
            final SpannableString highlightedText = new SpannableString(itemText);
            final int keyIndex = itemTextLower.indexOf(key);
            highlightedText.setSpan(new ForegroundColorSpan(0xA72196F3),
                    keyIndex, keyIndex + key.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(highlightedText, TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(recyclerView.getContext());
        final ViewParent parent = recyclerView.getParent();
        if (parent instanceof View) viewDivider = ((View) parent).findViewById(R.id.view_divider);
    }
}