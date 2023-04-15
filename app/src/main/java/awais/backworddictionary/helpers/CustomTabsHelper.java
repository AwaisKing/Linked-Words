package awais.backworddictionary.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.BundleCompat;
import androidx.core.content.ContextCompat;

import awais.backworddictionary.BuildConfig;

public final class CustomTabsHelper {
    private static final String EXTRA_USER_OPT_OUT_FROM_CUSTOM_TABS = "android.support.customtabs.extra.user_opt_out";
    private static final String EXTRA_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    private static final String EXTRA_SECONDARY_TOOLBAR_COLOR = "android.support.customtabs.extra.SECONDARY_TOOLBAR_COLOR";
    private static final String EXTRA_ENABLE_INSTANT_APPS = "android.support.customtabs.extra.EXTRA_ENABLE_INSTANT_APPS";
    private static final String EXTRA_DEFAULT_SHARE_MENU_ITEM = "android.support.customtabs.extra.SHARE_MENU_ITEM";
    private static final String EXTRA_ENABLE_URLBAR_HIDING = "android.support.customtabs.extra.ENABLE_URLBAR_HIDING";
    private static final String EXTRA_TITLE_VISIBILITY_STATE = "android.support.customtabs.extra.TITLE_VISIBILITY";

    private static final int NO_TITLE = 0;
    private static final int SHOW_PAGE_TITLE = 1;

    public final Intent intent;

    public CustomTabsHelper() {
        final Bundle bundle = new Bundle();
        BundleCompat.putBinder(bundle, EXTRA_SESSION, null);

        boolean showTitle = !BuildConfig.DEBUG;

        this.intent = new Intent(Intent.ACTION_VIEW)
                .putExtra(EXTRA_ENABLE_INSTANT_APPS, true)
                .putExtra(EXTRA_TITLE_VISIBILITY_STATE, showTitle ? SHOW_PAGE_TITLE : NO_TITLE)
                .putExtra(EXTRA_DEFAULT_SHARE_MENU_ITEM, true)
                //.putExtra(EXTRA_ENABLE_URLBAR_HIDING, true)
                .putExtras(bundle);
    }

    public CustomTabsHelper setToolbarColor(@ColorInt int color) {
        intent.putExtra(EXTRA_TOOLBAR_COLOR, color);
        return this;
    }

    public CustomTabsHelper setSecondaryToolbarColor(@ColorInt int color) {
        intent.putExtra(EXTRA_SECONDARY_TOOLBAR_COLOR, color);
        return this;
    }

    public CustomTabsHelper setUseNewTask() {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return this;
    }

    public void launchUrl(final Context context, final Uri url) {
        intent.setData(url);
        ContextCompat.startActivity(context, intent, null);
    }

    @NonNull
    public static Intent setAlwaysUseBrowserUI(Intent intent) {
        if (intent == null) intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_USER_OPT_OUT_FROM_CUSTOM_TABS, true);
        return intent;
    }

    public static boolean shouldAlwaysUseBrowserUI(@NonNull Intent intent) {
        return intent.getBooleanExtra(EXTRA_USER_OPT_OUT_FROM_CUSTOM_TABS, false)
                && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0;
    }
}