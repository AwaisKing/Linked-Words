package awais.backworddictionary;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class RecyclerViewFastScroller extends LinearLayout {
    private static final int SNAP_RANGE = 8;
    private View handle;
    private RecyclerView recyclerView;
    private int height;
    private boolean isInitialized = false;
    private ObjectAnimator currentAnimator = null;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            updateHandlePosition();
        }
    };

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RecyclerViewFastScroller(final Context context) {
        super(context);
        init();
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        if (isInitialized) return;
        isInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);
    }

    public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int handleResId) {
        LayoutInflater.from(getContext()).inflate(layoutResId, this, true);
        handle = findViewById(handleResId);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        updateHandlePosition();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX()-ViewCompat.getPaddingStart(handle)) return false;
                if (currentAnimator != null) currentAnimator.cancel();
                handle.setSelected(true);
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setHandlePosition(y);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                return true;
        }
        performClick();
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        if (this.recyclerView != recyclerView) {
            if (this.recyclerView!=null) this.recyclerView.removeOnScrollListener(onScrollListener);
            this.recyclerView = recyclerView;
            if (this.recyclerView == null) return;
            recyclerView.addOnScrollListener(onScrollListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(onScrollListener);
            recyclerView = null;
        }
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (handle.getY() == 0) proportion = 0f;
            else if (handle.getY() + handle.getHeight() >= height - SNAP_RANGE) proportion = 1f;
            else proportion = y / (float) height;
            final int targetPos = getValueInRange(itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .scrollToPositionWithOffset(targetPos, 0);
        }
    }

    private int getValueInRange(int max, int value) {
        return Math.min(Math.max(0, value), max);
    }

    private void updateHandlePosition() {
        float proportion = (float) recyclerView.computeVerticalScrollOffset() /
                ((float) recyclerView.computeVerticalScrollRange() - height);
        setHandlePosition(height * proportion);
    }

    private void setHandlePosition(float y) {
        final int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(height - handleHeight, (int) (y - handleHeight / 2)));
    }
}
