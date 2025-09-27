package awais.backworddictionary.adapters.holders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.databinding.SearchItemBinding;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class ResultViewHolder extends RecyclerView.ViewHolder {
    public final SearchItemBinding searchItemBinding;

    public ResultViewHolder(@NonNull final SearchItemBinding searchItemBinding, final AdapterClickListener clickListener) {
        super(searchItemBinding.getRoot());

        if (clickListener != null) {
            itemView.setOnClickListener(clickListener);
            itemView.setOnLongClickListener(clickListener);
        }

        this.searchItemBinding = searchItemBinding;
    }
}