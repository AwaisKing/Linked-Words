package com.keiferstone.nonet;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class MonitorManager implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = MonitorManager.class.getSimpleName();

    private int attachedApplicationHashCode;
    private final List<Monitor> monitors;

    MonitorManager() {
        monitors = new ArrayList<>();
    }

    void attachToApplication(@NonNull Application application) {
        // Only attach if not already attached
        if (application.hashCode() != attachedApplicationHashCode) {
            attachedApplicationHashCode = application.hashCode();
            application.registerActivityLifecycleCallbacks(this);
            Log.d(TAG, "Registering activity lifecycle callbacks for application: " + application);
        }
    }

    void registerMonitor(Monitor monitor) {
        if (!monitors.contains(monitor)) {
            monitors.add(monitor);
            Log.d(TAG, "Registering monitor: " + monitor);
            Log.d(TAG, "Monitor count: " + monitors.size());
        }
    }

    void destroy() {
        monitors.clear();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        Monitor monitor = findMonitorByActivity(activity);
        if (monitor != null) {
            monitor.start();
            Log.d(TAG, "Starting monitor: " + monitor);
        }
    }

    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        Monitor monitor = findMonitorByActivity(activity);
        if (monitor != null) {
            monitor.stop();
            Log.d(TAG, "Stopping monitor: " + monitor);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        Monitor monitor = findMonitorByActivity(activity);
        if (monitor != null) {
            monitors.remove(monitor);
            Log.d(TAG, "Unregistering monitor: " + monitor);
            Log.d(TAG, "Monitor count: " + monitors.size());
        }
    }

    @Nullable
    private Monitor findMonitorByActivity(@NonNull Activity activity) {
        for (Monitor monitor : monitors)
            if (activity.equals(monitor.getContext())) return monitor;
        return null;
    }
}
