package awais.backworddictionary.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;

/**
 * thanks to dominicthomas
 * <a href="https://gist.github.com/dominicthomas/1a268f3a113b490f751d9fb30cdb5875">https://gist.github.com/dominicthomas/1a268f3a113b490f751d9fb30cdb5875</a>
 **/
public final class MaxHeightRecyclerView extends RecyclerView {
    private final int maxHeightDp;

    public MaxHeightRecyclerView(@NonNull final Context context) {
        this(context, null);
    }

    public MaxHeightRecyclerView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxHeightRecyclerView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView, defStyleAttr, 0);
        try {
            final int maxHeight = a.getInteger(R.styleable.MaxHeightRecyclerView_rv_maxHeight, 0);
            maxHeightDp = Math.round(maxHeight * context.getResources().getDisplayMetrics().density);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeightDp, MeasureSpec.AT_MOST));
    }
}