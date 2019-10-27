package awais.backworddictionary.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
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
import java.util.Locale;

import awais.backworddictionary.R;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.helpers.ResultViewHolder;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public class SearchAdapter extends RecyclerView.Adapter<ResultViewHolder> implements Filterable {
    private final Filter filter;
    private final LayoutInflater mLayoutInflater;
    private final SearchHistoryTable mHistoryDatabase;
    private final SearchAdapter.OnItemClickListener mItemClickListener;
    private final SearchAdapter.OnItemLongClickListener mItemLongClickListener;
    private List<SearchItem> mSuggestionsList = new ArrayList<>();
    private List<SearchItem> mResultList = new ArrayList<>();
    private String key = "";

    public SearchAdapter(Context context, SearchHistoryTable table, OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mHistoryDatabase = table;
        this.mItemClickListener = clickListener;
        this.mItemLongClickListener = longClickListener;
        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (!TextUtils.isEmpty(constraint)) {
                    key = constraint.toString().toLowerCase(Locale.getDefault());

                    final List<SearchItem> results = new ArrayList<>();
                    final List<SearchItem> history = new ArrayList<>();
                    final List<SearchItem> databaseAllItems = mHistoryDatabase.getAllItems(null);

                    if (!databaseAllItems.isEmpty()) history.addAll(databaseAllItems);
                    history.addAll(mSuggestionsList);

                    for (SearchItem item : history)
                        if (item.get_text().toString().toLowerCase(Locale.getDefault()).contains(key))
                            results.add(item);

                    if (results.size() > 0) {
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                } else key = "";

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<SearchItem> dataSet = new ArrayList<>();

                if (results.count > 0) {
                    for (Object object : (ArrayList) results.values)
                        if (object instanceof SearchItem) dataSet.add((SearchItem) object);
                } else {
                    if (key.isEmpty()) {
                        final List<SearchItem> allItems = mHistoryDatabase.getAllItems(null);
                        if (!allItems.isEmpty()) dataSet = allItems;
                    }
                }

                setData(dataSet);
            }
        };
        filter.filter("");
    }

    public void setSuggestionsList(List<SearchItem> suggestionsList) {
        this.mSuggestionsList = suggestionsList;
        this.mResultList = suggestionsList;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void setData(List<SearchItem> data) {
        if (mResultList.size() == 0) {
            mResultList = data;
            notifyDataSetChanged();
        } else {
            final int previousSize = mResultList.size();
            final int nextSize = data.size();
            mResultList = data;
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
    public ResultViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ResultViewHolder(mLayoutInflater.inflate(R.layout.search_item, parent, false),
                mItemClickListener, mItemLongClickListener) {
            @Override
            public int getItemCount() {
                return SearchAdapter.this.getItemCount();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder viewHolder, int position) {
        final SearchItem item = mResultList.get(position);

        viewHolder.icon_left.setImageResource(item.get_icon());
        viewHolder.icon_left.setColorFilter(MaterialSearchView.getIconColor(), PorterDuff.Mode.SRC_IN);
        viewHolder.text.setTypeface((Typeface.create(MaterialSearchView.getTextFont(), MaterialSearchView.getTextStyle())));
        viewHolder.text.setTextColor(MaterialSearchView.getTextColor());

        final String itemText = item.get_text().toString();
        final String itemTextLower = itemText.toLowerCase(Locale.getDefault());

        if (itemTextLower.contains(key) && !key.isEmpty()) {
            final SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(MaterialSearchView.getTextHighlightColor()),
                    itemTextLower.indexOf(key), itemTextLower.indexOf(key) + key.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        } else
            viewHolder.text.setText(item.get_text());
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String text);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position, String text);
    }
}