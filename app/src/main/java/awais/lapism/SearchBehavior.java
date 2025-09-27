package awais.lapism;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

public final class SearchBehavior extends CoordinatorLayout.Behavior<MaterialSearchView> {
    public SearchBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull final CoordinatorLayout parent, @NonNull final MaterialSearchView child, @NonNull final View dependency) {
        final boolean isAppBar = dependency instanceof AppBarLayout;
        if (isAppBar) {
            child.setElevation(dependency.getElevation());
            child.setZ(dependency.getZ() + 10);
        }
        return isAppBar;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull final CoordinatorLayout parent, @NonNull final MaterialSearchView child, @NonNull final View dependency) {
        final boolean isAppBar = dependency instanceof AppBarLayout;
        if (isAppBar) child.setTranslationY(dependency.getY() + 0);
        return isAppBar;
    }
}