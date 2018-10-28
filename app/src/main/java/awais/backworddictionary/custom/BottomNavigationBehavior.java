package awais.backworddictionary.custom;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import awais.backworddictionary.R;

@SuppressWarnings( {"unused", "WeakerAccess"} )
public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {
    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout a, @NonNull View b,
                                       @NonNull View c, @NonNull View d, int axes, int e) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout cl, @NonNull View c,
                                  @NonNull View t, int dx, int dy, @NonNull int[] cd, int ty) {
        super.onNestedPreScroll(cl, c, t, dx, dy, cd, ty);
        c.setTranslationY(Math.max(0f, Math.min((float)c.getHeight(),
                c.getTranslationY() + dy)));
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout p, @NonNull View c, @NonNull View d) {
        if (d instanceof Snackbar.SnackbarLayout)
            updateSnackbar(c, (Snackbar.SnackbarLayout) d, c.getVisibility());
        return super.layoutDependsOn(p, c, d);
    }

    private void updateSnackbar(View child, Snackbar.SnackbarLayout snack, int visibility) {
        if (snack.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                    snack.getLayoutParams();
            params.setAnchorId(child.findViewById(R.id.adView).getId());
            params.anchorGravity = Gravity.TOP;
            params.gravity = Gravity.TOP;
            if (visibility == View.GONE) {
                params.setAnchorId(-1);
                params.anchorGravity = 0;
                params.gravity = Gravity.BOTTOM;
            } else {
                if (Build.VERSION.SDK_INT > 20) snack.setElevation(0f);
                else ViewCompat.setElevation(snack, 0f);
            }
            snack.setLayoutParams(params);
        }
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout p,
                                          @NonNull View c, @NonNull View d) {
        c.setTranslationY(-d.getTop());
        return super.onDependentViewChanged(p, c, d);
    }
}