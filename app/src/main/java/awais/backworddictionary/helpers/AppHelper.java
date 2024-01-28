package awais.backworddictionary.helpers;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import awais.backworddictionary.LinkedApp;

public final class AppHelper {
    private static AppHelper _instance;

    public static AppHelper getInstance(Context context) {
        synchronized (AppHelper.class) {
            if (!(context instanceof Application)) context = context.getApplicationContext();
            if (_instance != null && (_instance.linkedApp == null || _instance.linkedApp != context)) _instance = null;
            if (_instance == null) _instance = new AppHelper((LinkedApp) context);
            return _instance;
        }
    }

    private final LinkedApp linkedApp;
    private ClipboardManager clipboardManager;
    private InputMethodManager inputMethodManager;
    private NotificationManager notificationManager;

    private AppHelper(final LinkedApp linkedApp) {
        this.linkedApp = (LinkedApp) linkedApp;
        this.inputMethodManager = getInputMethodManager();
        this.notificationManager = getNotificationManager();
    }

    public ClipboardManager getClipboardManager() {
        if (linkedApp == null) return clipboardManager;
        if (clipboardManager == null) clipboardManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                                         ? linkedApp.getSystemService(ClipboardManager.class)
                                                         : (ClipboardManager) linkedApp.getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboardManager;
    }

    public InputMethodManager getInputMethodManager() {
        if (linkedApp == null) return inputMethodManager;
        if (inputMethodManager == null) inputMethodManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                                             ? linkedApp.getSystemService(InputMethodManager.class)
                                                             : (InputMethodManager) linkedApp.getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager;
    }

    public NotificationManager getNotificationManager() {
        if (linkedApp == null) return notificationManager;
        if (notificationManager == null) notificationManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                                               ? linkedApp.getSystemService(NotificationManager.class)
                                                               : (NotificationManager) linkedApp.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }
}