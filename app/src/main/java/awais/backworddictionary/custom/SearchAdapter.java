package awais.backworddictionary.custom;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultViewHolder> implements Filterable {
    private final SearchHistoryTable mHistoryDatabase;
    private List<SearchItem> mSuggestionsList = new ArrayList<>();
    private List<SearchItem> mResultList = new ArrayList<>();
    private SearchAdapter.OnItemClickListener mItemClickListener;
    private SearchAdapter.OnItemLongClickListener mItemLongClickListener;
    private final String mDatabaseKey = null;
    private String key = "";

    public SearchAdapter(SearchHistoryTable table) {
        mHistoryDatabase = table;
        getFilter().filter("");
    }

    public void setSuggestionsList(List<SearchItem> suggestionsList) {
        mSuggestionsList = suggestionsList;
        mResultList = suggestionsList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (!TextUtils.isEmpty(constraint)) {
                    key = constraint.toString().toLowerCase(Locale.getDefault());

                    List<SearchItem> results = new ArrayList<>();
                    List<SearchItem> history = new ArrayList<>();
                    List<SearchItem> databaseAllItems = mHistoryDatabase.getAllItems(mDatabaseKey);

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
                        List<SearchItem> allItems = mHistoryDatabase.getAllItems(mDatabaseKey);
                        if (!allItems.isEmpty()) dataSet = allItems;
                    }
                }

                setData(dataSet);
            }
        };
    }

    public void setData(List<SearchItem> data) {
        if (mResultList.size() == 0) {
            mResultList = data;
            notifyDataSetChanged();
        } else {
            int previousSize = mResultList.size();
            int nextSize = data.size();
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

    // ---------------------------------------------------------------------------------------------
    @NonNull
    @Override
    public SearchAdapter.ResultViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ResultViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(com.lapism.searchview.R.layout.search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ResultViewHolder viewHolder, int position) {
        SearchItem item = mResultList.get(position);

        viewHolder.icon_left.setImageResource(item.get_icon());
        viewHolder.icon_left.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN);
        viewHolder.text.setTypeface((Typeface.create(SearchView.getTextFont(), SearchView.getTextStyle())));
        viewHolder.text.setTextColor(SearchView.getTextColor());

        String itemText = item.get_text().toString();
        String itemTextLower = itemText.toLowerCase(Locale.getDefault());

        if (itemTextLower.contains(key) && !key.isEmpty()) {
            SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()),
                    itemTextLower.indexOf(key), itemTextLower.indexOf(key) + key.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        } else {
            viewHolder.text.setText(item.get_text());
        }
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void addOnItemClickListener(SearchAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void addOnItemLongClickListener(SearchAdapter.OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String text);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position, String text);
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ImageView icon_left;

        ResultViewHolder(View view) {
            super(view);
            icon_left = view.findViewById(com.lapism.searchview.R.id.imageView_item_icon_left);
            text = view.findViewById(com.lapism.searchview.R.id.textView_item_text);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(v, positionFix(), String.valueOf(text.getText()));
            });
            view.setOnLongClickListener(v -> {
                if (mItemLongClickListener != null)
                    return mItemLongClickListener.onItemLongClick(v, positionFix(), String.valueOf(text.getText()));
                return true;
            });
        }

        private int positionFix() {
            int ofPos = getLayoutPosition(), count = getItemCount();
            int layoutPosition = getLayoutPosition(), adapterPosition = getAdapterPosition();
            if (layoutPosition <= count && adapterPosition > count) ofPos = layoutPosition;
            else if (layoutPosition > count && adapterPosition <= count)
                ofPos = adapterPosition;
            else if (layoutPosition > count && adapterPosition > count) {
                if (layoutPosition > adapterPosition) ofPos = adapterPosition;
                else if (layoutPosition < adapterPosition) ofPos = layoutPosition;
            }
            if (ofPos > count) ofPos = count - 1;
            return ofPos;
        }
    }
}