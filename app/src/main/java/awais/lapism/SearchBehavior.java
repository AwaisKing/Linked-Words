package awais.lapism;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

import awais.backworddictionary.Main;

public class SearchBehavior extends CoordinatorLayout.Behavior<MaterialSearchView> {
    public SearchBehavior() {}

    public SearchBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull MaterialSearchView child, @NonNull View dependency) {
        boolean isAppBar = dependency instanceof AppBarLayout;
        if (isAppBar) {
            ViewCompat.setElevation(child, ViewCompat.getElevation(dependency));
            ViewCompat.setZ(child, ViewCompat.getZ(dependency) + 10);
        }
        return isAppBar;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull MaterialSearchView child, @NonNull View dependency) {
        boolean isAppBar = dependency instanceof AppBarLayout;
        if (isAppBar) child.setTranslationY(dependency.getY() + Main.statusBarHeight);
        return isAppBar;
    }
}