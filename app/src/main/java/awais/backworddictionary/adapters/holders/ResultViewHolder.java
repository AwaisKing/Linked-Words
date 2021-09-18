package awais.backworddictionary.adapters.holders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.databinding.SearchItemBinding;

public final class ResultViewHolder extends RecyclerView.ViewHolder {
    public final SearchItemBinding searchItemBinding;

    public ResultViewHolder(@NonNull final SearchItemBinding searchItemBinding, final View.OnClickListener onClickListener,
                            final View.OnLongClickListener onLongClickListener) {
        super(searchItemBinding.getRoot());

        this.searchItemBinding = searchItemBinding;

        if (onClickListener != null) itemView.setOnClickListener(onClickListener);
        if (onLongClickListener != null) itemView.setOnLongClickListener(onLongClickListener);
    }
}