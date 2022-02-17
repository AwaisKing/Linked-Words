package awais.backworddictionary.helpers.other;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.helpers.Utils;

public final class CustomTabActivityHelper {
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    private static final String ACTION_CUSTOM_TABS_CONNECTION_ANDROID_X = "androidx.browser.customtabs.action.CustomTabsService";
    private static String packageNameToUse;

    public static void openCustomTab(final Context context, final CustomTabsIntent customTabsIntent, final Uri uri) {
        final String packageName = getPackageNameToUse(context);

        if (packageName == null) context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(context, uri);
        }
    }

    private static String getPackageNameToUse(final Context context) {
        if (!Utils.isEmpty(packageNameToUse)) return packageNameToUse;

        final PackageManager pm = context.getPackageManager();

        final Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wikipedia.com"));
        final ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);

        String defaultBrowser = null;
        if (defaultViewHandlerInfo != null)
            defaultBrowser = defaultViewHandlerInfo.activityInfo.packageName;

        final ArrayList<String> customTabBrowsersList = new ArrayList<>(0);
        final Intent serviceIntent = new Intent(ACTION_CUSTOM_TABS_CONNECTION)
                .setAction(ACTION_CUSTOM_TABS_CONNECTION_ANDROID_X);

        final List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        for (final ResolveInfo info : resolvedActivityList) {
            final String packageName = info.activityInfo.packageName;
            serviceIntent.setPackage(packageName);
            ResolveInfo resolveInfo = pm.resolveService(serviceIntent, 0);
            if (resolveInfo == null) resolveInfo = pm.resolveService(serviceIntent, 0);
            if (resolveInfo != null) customTabBrowsersList.add(packageName);
        }

        if (customTabBrowsersList.isEmpty()) packageNameToUse = null;
        else if (customTabBrowsersList.size() == 1)
            packageNameToUse = customTabBrowsersList.get(0);
        else if (!Utils.isEmpty(defaultBrowser) && !hasSpecializedHandlerIntents(context, activityIntent) && customTabBrowsersList.contains(defaultBrowser))
            packageNameToUse = defaultBrowser;
        else {
            for (final String browser : customTabBrowsersList) {
                if (STABLE_PACKAGE.equals(browser)) {
                    packageNameToUse = STABLE_PACKAGE;
                    break;
                }
                if (BETA_PACKAGE.equals(browser)) {
                    packageNameToUse = BETA_PACKAGE;
                    break;
                }
                if (DEV_PACKAGE.equals(browser)) {
                    packageNameToUse = DEV_PACKAGE;
                    break;
                }
                if (LOCAL_PACKAGE.equals(browser)) {
                    packageNameToUse = LOCAL_PACKAGE;
                    break;
                }
            }
        }
        return packageNameToUse;
    }

    private static boolean hasSpecializedHandlerIntents(final Context context, final Intent intent) {
        try {
            final PackageManager pm = context.getPackageManager();
            final List<ResolveInfo> handlers = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
            if (handlers.size() == 0) return false;
            for (final ResolveInfo resolveInfo : handlers) {
                final IntentFilter filter = resolveInfo.filter;
                if (filter == null || filter.countDataAuthorities() == 0
                        || filter.countDataPaths() == 0 || resolveInfo.activityInfo == null)
                    continue;
                return true;
            }
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_CHROME_VIEW", "", e);
        }
        return false;
    }
}