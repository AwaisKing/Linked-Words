package awais.backworddictionary.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * thanks to Raj008
 * https://stackoverflow.com/questions/18997729/listview-same-height-as-content
 **/
public class WrappingListView extends ListView {
    public WrappingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrappingListView(Context context) {
        super(context);
    }

    public WrappingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
}