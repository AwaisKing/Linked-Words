package awais.backworddictionary.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.helpers.ScrollingMovement;

public class TTSViewHolder extends RecyclerView.ViewHolder {
    public final TextView textView1, textView2;
    public final ImageView ivIcon, ivSelected;

    public TTSViewHolder(@NonNull final View itemView, final View.OnClickListener onClickListener) {
        super(itemView);

        this.ivIcon = itemView.findViewById(android.R.id.icon);
        this.textView1 = itemView.findViewById(android.R.id.text1);
        this.textView2 = itemView.findViewById(android.R.id.text2);
        this.ivSelected = itemView.findViewById(android.R.id.button1);

        this.textView2.setMovementMethod(ScrollingMovement.getInstance());
        this.textView2.setSelected(true);

        if (onClickListener != null) itemView.setOnClickListener(onClickListener);
    }
}