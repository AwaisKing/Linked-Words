package awais.backworddictionary.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.view.GravityCompat;

@SuppressLint("RestrictedApi")
public final class MenuBuilderHelper extends MenuBuilder {
    public MenuBuilderHelper(final Context context) {
        super(context);
    }

    @Override
    public MenuItem add(final CharSequence title) {
        return super.add(title);
    }

    @Override
    public MenuItem add(final int titleRes) {
        return super.add(titleRes);
    }

    @Override
    public MenuItem add(final int group, final int id, final int categoryOrder, final CharSequence title) {
        return super.add(group, id, categoryOrder, title);
    }

    @Override
    public MenuItem add(final int group, final int id, final int categoryOrder, final int title) {
        return super.add(group, id, categoryOrder, title);
    }

    @Override
    public MenuBuilderHelper setDefaultShowAsAction(final int defaultShowAsAction) {
        final MenuBuilder menuBuilder = super.setDefaultShowAsAction(defaultShowAsAction);
        return menuBuilder instanceof MenuBuilderHelper ? (MenuBuilderHelper) menuBuilder : this;
    }

    @Override
    public MenuItem findItem(final int id) {
        return super.findItem(id);
    }

    public static final class PopupHelper extends MenuPopupHelper {
        public PopupHelper(@NonNull final Context context, @NonNull final MenuBuilder menu, @NonNull final View anchorView) {
            super(context, menu, anchorView);
        }

        @Override
        public boolean tryShow() {
            super.setGravity(GravityCompat.END);
            return super.tryShow();
        }

        @Override
        public void show() {
            super.setGravity(GravityCompat.END);
            super.show();
        }
    }
}