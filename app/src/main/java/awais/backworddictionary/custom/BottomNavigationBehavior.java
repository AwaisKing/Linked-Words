package awais.backworddictionary.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

@SuppressWarnings({"RedundantSuppression", "unused"})
public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {
    public BottomNavigationBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull final CoordinatorLayout a, @NonNull final View b, @NonNull final View c, @NonNull final View d, final int axes, final int e) {
        return d instanceof SwipeRefreshLayout && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(@NonNull final CoordinatorLayout cl, @NonNull final View c, @NonNull final View t, final int dx, final int dy, @NonNull final int[] cd, final int ty) {
        super.onNestedPreScroll(cl, c, t, dx, dy, cd, ty);
        c.setTranslationY(Math.max(0f, Math.min(c.getHeight(), c.getTranslationY() + dy)));
    }

    @Override
    public boolean layoutDependsOn(@NonNull final CoordinatorLayout p, @NonNull final View c, @NonNull final View d) {
        return super.layoutDependsOn(p, c, d);
    }

    @Override
    public boolean onDependentViewChanged(@NonNull final CoordinatorLayout p, @NonNull final View c, @NonNull final View d) {
        c.setTranslationY(-d.getTop());
        return super.onDependentViewChanged(p, c, d);
    }
}