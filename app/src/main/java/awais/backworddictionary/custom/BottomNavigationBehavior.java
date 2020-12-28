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
    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout a, @NonNull View b, @NonNull View c, @NonNull View d, int axes, int e) {
        return d instanceof SwipeRefreshLayout && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout cl, @NonNull View c, @NonNull View t, int dx, int dy, @NonNull int[] cd, int ty) {
        super.onNestedPreScroll(cl, c, t, dx, dy, cd, ty);
        c.setTranslationY(Math.max(0f, Math.min(c.getHeight(), c.getTranslationY() + dy)));
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout p, @NonNull View c, @NonNull View d) {
        return super.layoutDependsOn(p, c, d);
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout p, @NonNull View c, @NonNull View d) {
        c.setTranslationY(-d.getTop());
        return super.onDependentViewChanged(p, c, d);
    }
}