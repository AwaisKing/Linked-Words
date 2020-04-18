package awais.backworddictionary.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.SearchAdapter;

public abstract class ResultViewHolder extends RecyclerView.ViewHolder {
    public final TextView text;
    public final ImageView icon;

    protected ResultViewHolder(final View view, final SearchAdapter.OnItemClickListener itemClickListener,
                               final SearchAdapter.OnItemLongClickListener longClickListener) {
        super(view);

        this.icon = view.findViewById(R.id.searchItemIcon);
        this.text = view.findViewById(R.id.searchItemText);

        if (itemClickListener != null)
            view.setOnClickListener(v -> itemClickListener.onItemClick(v, positionFix(), String.valueOf(text.getText())));

        if (longClickListener != null)
            view.setOnLongClickListener(v -> longClickListener.onItemLongClick(v, positionFix(), String.valueOf(text.getText())));
    }

    protected abstract int getItemsCount();

    private int positionFix() {
        final int count = getItemsCount(), layoutPosition = getLayoutPosition(), adapterPosition = getBindingAdapterPosition();

        final int ofPos;
        if (layoutPosition > count) {
            if (adapterPosition <= count) ofPos = adapterPosition;
            else ofPos = Math.min(layoutPosition, adapterPosition);
        } else if (adapterPosition <= count) ofPos = adapterPosition;
        else ofPos = layoutPosition;

        return ofPos > count ? count - 1 : ofPos;
    }
}