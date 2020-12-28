package awais.backworddictionary.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;

public final class ResultViewHolder extends RecyclerView.ViewHolder {
    public final TextView text;
    public final ImageView icon;

    public ResultViewHolder(final View view, final View.OnClickListener onClickListener,
                            final View.OnLongClickListener onLongClickListener) {
        super(view);

        this.icon = view.findViewById(R.id.searchItemIcon);
        this.text = view.findViewById(R.id.searchItemText);

        if (onClickListener != null)
            view.setOnClickListener(onClickListener);
        if (onLongClickListener != null)
            view.setOnLongClickListener(onLongClickListener);
    }
}