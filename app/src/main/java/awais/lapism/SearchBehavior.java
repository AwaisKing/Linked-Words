package awais.lapism;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

import awais.backworddictionary.Main;

public class SearchBehavior extends CoordinatorLayout.Behavior<MaterialSearchView> {
    public SearchBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull MaterialSearchView child, @NonNull View dependency) {
        final boolean isAppBar = dependency instanceof AppBarLayout;
        if (isAppBar && Build.VERSION.SDK_INT >= 21) {
            child.setElevation(dependency.getElevation());
            child.setZ(dependency.getZ() + 10);
        }
        return isAppBar;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull MaterialSearchView child, @NonNull View dependency) {
        if (dependency instanceof AppBarLayout) {
            child.setTranslationY(dependency.getY() + Main.statusBarHeight);
            return true;
        }
        return false;
    }
}