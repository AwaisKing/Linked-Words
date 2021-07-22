package awais.backworddictionary.helpers;

import android.content.res.Resources;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// same as from LinearSmoothScroller
public final class SmoothScroller extends RecyclerView.SmoothScroller {
    public static final int SNAP_TO_START = -1, SNAP_TO_ANY = 0, SNAP_TO_END = 1;
    private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2f;
    private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
    private final LinearInterpolator linearInterpolator = new LinearInterpolator();
    private final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private PointF targetVector;
    private boolean hasCalculated = false;
    private float scrollPerInch = 25f, interimTargetDx = 0, interimTargetDy = 0, millisPerPixel;

    private DisplayMetrics displayMetrics;

    @Override
    protected void onChildAttachedToWindow(final View child) {
        super.onChildAttachedToWindow(child);
        if (child != null && displayMetrics == null)
            displayMetrics = child.getResources().getDisplayMetrics();
    }

    @Override
    protected void onStart() { }

    @Override
    protected void onTargetFound(@NonNull final View targetView, @NonNull final RecyclerView.State state, @NonNull final Action action) {
        int result = 0;
        final RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null) {
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) targetView.getLayoutParams();
            result = calculateDtToFit(layoutManager.getDecoratedTop(targetView) - params.topMargin,
                    layoutManager.getDecoratedBottom(targetView) + params.bottomMargin, layoutManager.getPaddingTop(),
                    layoutManager.getHeight() - layoutManager.getPaddingBottom(),
                    targetVector == null || targetVector.y == 0 ? SNAP_TO_ANY : targetVector.y > 0 ? SNAP_TO_END : SNAP_TO_START);
        }
        displayMetrics = targetView.getResources().getDisplayMetrics();
        final double time = Math.ceil(calculateTimeForScrolling(Math.sqrt(result * result)) / .3356);
        if (time > 0) action.update(0, -result, (int) time, decelerateInterpolator);
    }

    @Override
    protected void onSeekTargetStep(final int dx, final int dy, @NonNull final RecyclerView.State state, @NonNull final Action action) {
        if (getChildCount() == 0) stop();
        else {
            interimTargetDx = clampApplyScroll(interimTargetDx, dx);
            interimTargetDy = clampApplyScroll(interimTargetDy, dy);

            if (interimTargetDx == 0 && interimTargetDy == 0) {
                final int targetPosition = getTargetPosition();

                final PointF scrollVector = computeScrollVectorForPosition(targetPosition);
                if (scrollVector != null && (scrollVector.x != 0 || scrollVector.y != 0)) {
                    normalize(scrollVector);
                    targetVector = scrollVector;

                    interimTargetDx = TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.x;
                    interimTargetDy = TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y;

                    action.update((int) (interimTargetDx * TARGET_SEEK_EXTRA_SCROLL_RATIO),
                            (int) (interimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO),
                            (int) (calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX) * TARGET_SEEK_EXTRA_SCROLL_RATIO),
                            linearInterpolator);
                } else {
                    action.jumpTo(targetPosition);
                    stop();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        interimTargetDx = interimTargetDy = 0;
        targetVector = null;
    }

    public void setScrollPerInch(@NonNull final DisplayMetrics displayMetrics, final float scrollPerInch) {
        this.scrollPerInch = scrollPerInch;
        this.millisPerPixel = scrollPerInch / displayMetrics.densityDpi;
    }

    private double calculateTimeForScrolling(final double dx) {
        DisplayMetrics displayMetrics = this.displayMetrics;
        if (displayMetrics == null) displayMetrics = Resources.getSystem().getDisplayMetrics();
        if (!hasCalculated) {
            millisPerPixel = scrollPerInch / displayMetrics.densityDpi;
            hasCalculated = true;
        }
        return Math.ceil(Math.abs(dx) * millisPerPixel);
    }

    private float clampApplyScroll(float tmpDt, final float dt) {
        final float before = tmpDt;
        tmpDt = tmpDt - dt;
        return before * tmpDt <= 0 ? 0 : tmpDt;
    }

    private int calculateDtToFit(final int viewStart, final int viewEnd, final int boxStart, final int boxEnd, final int snapPreference) {
        if (snapPreference == SNAP_TO_START)
            return boxStart - viewStart;
        else if (snapPreference == SNAP_TO_END)
            return boxEnd - viewEnd;
        else if (snapPreference == SNAP_TO_ANY) {
            final int dtStart = boxStart - viewStart;
            if (dtStart > 0) return dtStart;
            final int dtEnd = boxEnd - viewEnd;
            if (dtEnd < 0) return dtEnd;
        } else
            throw new IllegalArgumentException("snap preference should be one of the constants defined in SmoothScroller, starting with SNAP_");
        return 0;
    }
}