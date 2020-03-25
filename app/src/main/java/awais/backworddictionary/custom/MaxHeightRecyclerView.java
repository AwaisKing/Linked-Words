package awais.backworddictionary.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

/**
 * thanks to dominicthomas
 * https://gist.github.com/dominicthomas/1a268f3a113b490f751d9fb30cdb5875
 **/
public class MaxHeightRecyclerView extends RecyclerView {
    private int maxHeightDp;

    public MaxHeightRecyclerView(@NonNull final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public MaxHeightRecyclerView(@NonNull final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(@NonNull final Context context, final AttributeSet attrs, final int defStyleAttr) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView, defStyleAttr, 0);
        try {
            maxHeightDp = a.getInteger(R.styleable.MaxHeightRecyclerView_maxHeight, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(maxHeightDp * Utils.displayMetrics.density),
                MeasureSpec.AT_MOST));
    }
}