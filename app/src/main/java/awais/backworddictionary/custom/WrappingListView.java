package awais.backworddictionary.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * thanks to Raj008
 * https://stackoverflow.com/questions/18997729/listview-same-height-as-content
 **/
public class WrappingListView extends ListView {
    public WrappingListView(final Context context) {
        super(context);
    }

    public WrappingListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public WrappingListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
}