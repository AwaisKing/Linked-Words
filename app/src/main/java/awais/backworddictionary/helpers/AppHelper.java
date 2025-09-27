package awais.backworddictionary.helpers;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import awais.backworddictionary.LinkedApp;

public final class AppHelper {
    private static AppHelper _instance;

    public static synchronized AppHelper getInstance(Context context) {
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
        this.linkedApp = linkedApp;
        this.inputMethodManager = getInputMethodManager();
        this.notificationManager = getNotificationManager();
    }

    public ClipboardManager getClipboardManager() {
        ClipboardManager clipboardManager = this.clipboardManager;
        if (linkedApp == null) return clipboardManager;
        if (clipboardManager == null) {
            try {
                clipboardManager = linkedApp.getSystemService(ClipboardManager.class);
            } catch (Exception e) {
                // ignore
            }
        }
        if (clipboardManager == null)
            clipboardManager = (ClipboardManager) linkedApp.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardManager = clipboardManager;
        return clipboardManager;
    }

    public InputMethodManager getInputMethodManager() {
        InputMethodManager inputMethodManager = this.inputMethodManager;
        if (linkedApp == null) return inputMethodManager;
        if (inputMethodManager == null) {
            try {
                inputMethodManager = linkedApp.getSystemService(InputMethodManager.class);
            } catch (Exception e) {
                // ignore
            }
        }
        if (inputMethodManager == null)
            inputMethodManager = (InputMethodManager) linkedApp.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.inputMethodManager = inputMethodManager;
        return inputMethodManager;
    }

    public NotificationManager getNotificationManager() {
        NotificationManager notificationManager = this.notificationManager;
        if (linkedApp == null) return notificationManager;
        if (notificationManager == null) {
            try {
                notificationManager = linkedApp.getSystemService(NotificationManager.class);
            } catch (Exception e) {
                // ignore
            }
        }
        if (notificationManager == null)
            notificationManager = (NotificationManager) linkedApp.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationManager = notificationManager;
        return notificationManager;
    }
}