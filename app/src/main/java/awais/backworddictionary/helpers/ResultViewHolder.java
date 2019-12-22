package awais.backworddictionary.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.SearchAdapter;

public abstract class ResultViewHolder extends RecyclerView.ViewHolder {
    public final TextView text;
    public final ImageView icon;

    protected ResultViewHolder(View view, SearchAdapter.OnItemClickListener itemClickListener,
                               SearchAdapter.OnItemLongClickListener longClickListener) {
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
        int ofPos = getLayoutPosition();
        final int count = getItemsCount(), layoutPosition = getLayoutPosition(), adapterPosition = getAdapterPosition();

        if (layoutPosition <= count && adapterPosition > count)
            ofPos = layoutPosition;
        else if (layoutPosition > count && adapterPosition <= count)
            ofPos = adapterPosition;
        else if (layoutPosition > count && adapterPosition > count) {
            if (layoutPosition > adapterPosition)
                ofPos = adapterPosition;
            else if (layoutPosition < adapterPosition)
                ofPos = layoutPosition;
        }

        return ofPos > count ? count - 1 : ofPos;
    }
}