package com.keiferstone.nonet;

import android.app.Activity;
import android.app.Service;
import android.content.Context;

/**
 * An Android library for monitoring network connectivity.
 */
public final class NoNet {
    private static NoNet instance = null;

    private Configuration configuration;
    private final MonitorManager monitorManager;

    private NoNet() {
        monitorManager = new MonitorManager();
    }

    /**
     * Set the global configuration.
     *
     * @return A {@link Configuration.Builder} that is automatically applied to the {@link NoNet} instance.
     */
    public static Configuration.Builder configure() {
        instantiate();
        Configuration.Builder builder = new Configuration.Builder();
        instance.configuration = builder.configuration;
        return builder;
    }

    /**
     * Start monitoring network connectivity.
     *
     * @param context Context for listening to connectivity events and displaying notifications.
     *                Must be an instance of {@link Activity} for
     *                {@link Monitor.Builder#snackbar()} or {@link BannerView}to work.
     *
     * @return A {@link Monitor.Builder}.
     */
    public static Monitor.Builder monitor(Context context) {
        instantiate();
        Monitor.Builder builder = new Monitor.Builder(context);
        if (instance.configuration != null) {
            builder.configure(instance.configuration);
        }
        if (context != null) {
            if (context instanceof Activity) {
                instance.monitorManager.attachToApplication(((Activity) context).getApplication());
            } else if (context instanceof Service) {
                instance.monitorManager.attachToApplication(((Service) context).getApplication());
            }
        }
        instance.monitorManager.registerMonitor(builder.monitor);
        return builder;
    }

    /**
     * Make a single check for network connectivity.
     *
     * @param context Check for displaying notifications. Must be an instance of
     *                {@link Activity} for {@link Check.Builder#snackbar()}
     *                to work.
     *
     * @return A {@link Check.Builder}.
     */
    static Check.Builder check(Context context) {
        instantiate();
        Check.Builder builder = new Check.Builder(context);
        if (instance.configuration != null) builder.configure(instance.configuration);
        return builder;
    }

    /**
     * Only call this method if you want to destroy all running {@link Monitor}s (i.e. Calling this
     * in one {@link Activity} will destroy {@link Monitor}s in all activities.)
     * <p>
     * Normal usage does not require calling this method.
     */
    public static void destroy() {
        if (instance != null) instance.monitorManager.destroy();
    }

    private static void instantiate() {
        if (instance == null) instance = new NoNet();
    }
}
