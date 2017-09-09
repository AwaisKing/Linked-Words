package awais.backworddictionary.customweb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CustomTabActivityHelper {
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    private static String sPackageNameToUse;

    public static void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri) {
        String packageName = getPackageNameToUse(activity);

        if (packageName == null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            activity.startActivity(intent);
        } else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(activity, uri);
        }
    }

    private static String getPackageNameToUse(Context context) {
        if (sPackageNameToUse != null) return sPackageNameToUse;

        PackageManager pm = context.getPackageManager();

        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.wikipedia.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null)
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;

        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
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

    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) return false;
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e("AWAISKING_CHROME_VIEW", "Runtime exception while getting specialized handlers");
        }
        return false;
    }

//    public static String[] getPackages() {
//        return new String[]{"", STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE, LOCAL_PACKAGE};
//    }
}