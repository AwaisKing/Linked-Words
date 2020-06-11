package awais.backworddictionary.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;
import awais.backworddictionary.interfaces.SearchAdapterClickListener;

public final class ResultViewHolder extends RecyclerView.ViewHolder {
    public final TextView text;
    public final ImageView icon;

    public ResultViewHolder(final View view, final SearchAdapterClickListener listener) {
        super(view);

        this.icon = view.findViewById(R.id.searchItemIcon);
        this.text = view.findViewById(R.id.searchItemText);

        if (listener != null) {
            view.setOnClickListener(v -> listener.onItemClick(text.getText().toString()));
            view.setOnLongClickListener(v -> {
                listener.onItemLongClick(text.getText().toString());
                return true;
            });
        }
    }
}