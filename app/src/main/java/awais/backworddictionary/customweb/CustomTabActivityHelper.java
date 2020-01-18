package awais.backworddictionary.customweb;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.BuildConfig;

public final class CustomTabActivityHelper {
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    // todo androidx problems:
    //      androidx.browser.customtabs.action.CustomTabsService
    //      android.support.customtabs.action.CustomTabsService
    private static String sPackageNameToUse;

    public static void openCustomTab(final Context context, final CustomTabsIntent customTabsIntent, final Uri uri) {
        final String packageName = getPackageNameToUse(context);

        if (packageName == null) {
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(context, uri);
        }
    }

    private static String getPackageNameToUse(final Context context) {
        if (sPackageNameToUse != null) return sPackageNameToUse;

        final PackageManager pm = context.getPackageManager();

        final Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.wikipedia.com"));
        final ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null)
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;

        final List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        final List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (final ResolveInfo info : resolvedActivityList) {
            final Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null)
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
        }

        if (packagesSupportingCustomTabs.isEmpty()) sPackageNameToUse = null;
        else if (packagesSupportingCustomTabs.size() == 1)
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName))
            sPackageNameToUse = defaultViewHandlerPackageName;
        else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE))
            sPackageNameToUse = STABLE_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE))
            sPackageNameToUse = BETA_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE))
            sPackageNameToUse = DEV_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE))
            sPackageNameToUse = LOCAL_PACKAGE;
        return sPackageNameToUse;
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
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e("AWAISKING_CHROME_VIEW", "", e);
        }
        return false;
    }
}