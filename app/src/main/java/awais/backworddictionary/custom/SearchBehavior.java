package awais.backworddictionary.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.lapism.searchview.SearchView;

@SuppressWarnings( {"unused", "WeakerAccess"} )
public class SearchBehavior extends CoordinatorLayout.Behavior<SearchView> {
    public SearchBehavior() {}
    public SearchBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull SearchView child,
                                   @NonNull View dependency) {
        if (dependency instanceof AppBarLayout) {
            ViewCompat.setElevation(child, ViewCompat.getElevation(dependency));
            ViewCompat.setZ(child, ViewCompat.getZ(dependency) + 10);
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent,
                                          @NonNull SearchView child, @NonNull View dependency) {
        if (dependency instanceof AppBarLayout) {
            child.setTranslationY(dependency.getY());
            return true;
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }

}