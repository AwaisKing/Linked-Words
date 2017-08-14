package awais.backworddictionary.customweb;

import android.app.Activity;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;

public class CustomTabActivityHelper implements ServiceConnection.ServiceConnectionCallback {
    private CustomTabsClient mClient;
    private final ConnectionCallback mConnectionCallback;

    public CustomTabActivityHelper(ConnectionCallback mConnectionCallback) {
        this.mConnectionCallback = mConnectionCallback;
    }

    public static void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri, CustomTabFallback fallback) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        if (packageName == null) {
            if (fallback != null) fallback.openUri(activity, uri);
        } else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(activity, uri);
        }
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        mClient = client;
        mClient.warmup(0L);
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsConnected();
    }

    @Override
    public void onServiceDisconnected() {
        mClient = null;
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsDisconnected();
    }

    public interface ConnectionCallback {
        void onCustomTabsConnected();
        void onCustomTabsDisconnected();
    }

    public interface CustomTabFallback {
        void openUri(Activity activity, Uri uri);
    }

}