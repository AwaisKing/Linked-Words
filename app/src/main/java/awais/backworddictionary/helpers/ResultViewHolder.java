package awais.backworddictionary.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.SearchAdapter;

public abstract class ResultViewHolder extends RecyclerView.ViewHolder {
    public final TextView text;
    public final ImageView icon_left;

    public ResultViewHolder(View view, SearchAdapter.OnItemClickListener itemClickListener,
            SearchAdapter.OnItemLongClickListener longClickListener) {
        super(view);
        icon_left = view.findViewById(R.id.imageView_item_icon_left);
        text = view.findViewById(R.id.textView_item_text);
        if (itemClickListener != null)
            view.setOnClickListener(v -> itemClickListener.onItemClick(v, positionFix(), String.valueOf(text.getText())));
        if (longClickListener != null)
            view.setOnLongClickListener(v -> longClickListener.onItemLongClick(v, positionFix(), String.valueOf(text.getText())));
    }

    public abstract int getItemCount();

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