package awais.backworddictionary.helpers;

import static awais.backworddictionary.helpers.Utils.notificationManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.BubbleMetadata;
import androidx.core.app.Person;
import androidx.core.content.LocusIdCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.R;

@RequiresApi(api = 29)
public final class BubbleHelper {
    private final static HashSet<String> categorySet;

    static {
        categorySet = new HashSet<>();
        categorySet.add("android.shortcut.conversation");
        categorySet.add("awais.linkedwords.bubbles.SEARCH");
    }

    @RequiresApi(api = 14)
    public static final String INTENT_EXTRA_BUBBLING = "awais.intent.BUBBLING";

    public static final String LW_BUBBLES_CHANNEL_NAME = "Linked Words Bubbles";
    public static final String LW_BUBBLES_CHANNEL_ID = "LinkedWordsBubbles";
    public static final String LW_BUBBLES_CONVO_ID = "LinkedWordsFloatingDict";

    public static final int LW_REQUEST_BUBBLE = 100425;
    public static final int LW_NOTIFICATION_ID = 100473;

    public static final int LW_BUBBLE_ICON = R.mipmap.ic_launcher;              // icon for bubbles shortcut icon
    public static final int LW_SMALL_ICON = R.mipmap.ic_launcher_foreground;    // icon for notification in status bar
    public static final int LW_SHORTCUT_ICON = R.drawable.ic_shortcut_search;   // icon for quick actions (shortcuts)

    private final WeakReference<Context> context;
    private final ComponentName componentName;
    private final LocusIdCompat locusIdCompat;
    private final IconCompat bubbleIcon, defaultShortcutIcon;
    private final float screenHeight;

    private String text;
    private Intent intent;
    private PendingIntent pendingIntent;

    public BubbleHelper(final Context context, final String text) {
        this.context = new WeakReference<>(context);

        this.locusIdCompat = new LocusIdCompat(LW_BUBBLES_CONVO_ID);
        this.bubbleIcon = IconCompat.createWithResource(context, LW_BUBBLE_ICON);
        this.defaultShortcutIcon = IconCompat.createWithResource(context, LW_SHORTCUT_ICON);

        this.componentName = (this.intent = new Intent(context, Main.class)
                .setAction(Intent.ACTION_VIEW)).getComponent();
        setText(text);

        this.screenHeight = getScreenHeight(context);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void setText(final String text) {
        this.text = text;

        final Context context = this.context.get();

        if (intent == null)
            intent = new Intent(context, Main.class).setAction(Intent.ACTION_VIEW);

        final Bundle extras = intent.getExtras();
        if (extras != null) extras.clear();

        this.pendingIntent = PendingIntent.getActivity(context, LW_REQUEST_BUBBLE,
                intent.putExtra(INTENT_EXTRA_BUBBLING, true)
                        .putExtra(Intent.EXTRA_TEXT, text)
                        .setData(Uri.parse(text)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }

    public void showBubble() {
        final Context context = this.context.get();
        if (notificationManager != null && context != null) {
            /*
             cancels the notification, not used atm
                try {
                    notificationManager.cancel(LW_NOTIFICATION_ID);
                } catch (final Exception e) {
                    // ignore
                }
            */

            final Person bubblePerson = new Person.Builder().setName(LW_BUBBLES_CHANNEL_NAME).build();

            final ShortcutInfoCompat.Builder shortcutBuilder = new ShortcutInfoCompat.Builder(context, LW_BUBBLES_CONVO_ID)
                    .setIcon(bubbleIcon).setShortLabel(context.getString(R.string.search)).setIntent(intent).setLongLived(true)
                    .setLocusId(locusIdCompat).setCategories(categorySet).setActivity(componentName);
            ShortcutManagerCompat.setDynamicShortcuts(context, Collections.singletonList(shortcutBuilder.build()));

            final BubbleMetadata bubbleMetadata = new BubbleMetadata.Builder(pendingIntent, bubbleIcon)
                    .setDesiredHeight(Math.round(screenHeight)).setSuppressNotification(true).setAutoExpandBubble(true).build();

            final Notification notification = new NotificationCompat.Builder(context, LW_BUBBLES_CHANNEL_ID)
                    .setSmallIcon(LW_SMALL_ICON).setOnlyAlertOnce(true).setAutoCancel(true).setColorized(true)
                    .setVibrate(null).setDefaults(0)
                    // bubbles api stuff
                    .setBubbleMetadata(bubbleMetadata).setShortcutId(LW_BUBBLES_CONVO_ID).setLocusId(locusIdCompat)
                    // category and style stuff
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE).setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                    .setStyle(new NotificationCompat.MessagingStyle(bubblePerson)
                            .addMessage(this.text, System.currentTimeMillis(), bubblePerson)
                    ).build();

            notification.flags |= Notification.FLAG_BUBBLE | Notification.FLAG_LOCAL_ONLY | Notification.FLAG_AUTO_CANCEL
                    | Notification.FLAG_ONLY_ALERT_ONCE;

            if (notificationManager.canNotifyAsPackage(BuildConfig.APPLICATION_ID))
                notificationManager.notifyAsPackage(BuildConfig.APPLICATION_ID, LW_BUBBLES_CONVO_ID,
                        LW_NOTIFICATION_ID, notification);
            else
                notificationManager.notify(LW_NOTIFICATION_ID, notification);

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // this is for setting search icon back to shortcut
                    shortcutBuilder.setIcon(defaultShortcutIcon);
                    final ShortcutInfoCompat shortcutInfoCompat = shortcutBuilder.build();
                    ShortcutManagerCompat.setDynamicShortcuts(context,
                            Collections.singletonList(shortcutInfoCompat));
                    handler.removeCallbacks(this);
                }
            }, 400);
        }
    }

    private static float getScreenHeight(final Context context) {
        return (context != null ? context.getResources() : Resources.getSystem())
                .getDisplayMetrics().heightPixels;
    }
}