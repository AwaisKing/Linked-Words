package awais.backworddictionary.helpers;

import static awais.backworddictionary.Main.tts;
import static awais.backworddictionary.helpers.BubbleHelper.LW_BUBBLES_CHANNEL_ID;
import static awais.backworddictionary.helpers.BubbleHelper.LW_BUBBLES_CHANNEL_NAME;
import static awais.backworddictionary.helpers.BubbleHelper.LW_BUBBLES_CONVO_ID;
import static awais.backworddictionary.helpers.Utils.notificationManager;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.custom.FloatingDialogView;
import awais.backworddictionary.dialogs.AwaisomeDialogBuilder;
import awais.backworddictionary.dialogs.AwaisomeDialogBuilder.AwaisomeDialog;
import awais.backworddictionary.dialogs.AwaisomeDialogBuilder.HiddenFlags;

public final class TextProcessHelper extends Activity {
    private static BubbleHelper bubbleHelper;
    private boolean isFound = false, dataHandled = false;
    private AwaisomeDialog awaisomeDialog = null;
    private NotificationChannel channel;
    private Intent intent;
    private Context context;

    public FloatingDialogView floatingDialogView = null;

    @Override
    public void onWindowAttributesChanged(final WindowManager.LayoutParams params) {
        super.onWindowAttributesChanged(params);
        findIntent();
    }

    @Override
    public Resources getResources() {
        final Resources resources = super.getResources();
        findIntent();
        return resources;
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(newBase);
        findIntent();
    }

    @Override
    public Context getBaseContext() {
        final Context baseContext = super.getBaseContext();
        findIntent();
        return baseContext;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(this.intent = intent);
        findIntent();
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.intent == null) this.intent = getIntent();
        if (this.context == null) this.context = this;
        findIntent();
    }

    private synchronized void findIntent() {
        if (isFound) return;

        if (intent == null) intent = getIntent();
        if (context == null) {
            try {
                context = getApplicationContext();
            } catch (final Exception e) {
                try {
                    context = getBaseContext();
                } catch (final Exception e1) {
                    context = this;
                }
            }
        }

        isFound = intent != null && context != null;
        if (isFound) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null)
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            SettingsHelper.setPreferences(context);

            if (!dataHandled) handleData();
        }
    }

    private synchronized void handleData() {
        if (intent == null) intent = getIntent();
        if (intent != null) {
            dataHandled = true;

            final String action = intent.getAction();
            final String type = intent.getType();
            final Bundle bundle = intent.getExtras();

            ClipData clipData;
            ClipData.Item clipDataItem;
            final String[] str = {null};

            //// these are to check if bubbles can be shown
            boolean isBubbles;
            final boolean bubblesApiEnabled;
            final boolean isProcessText = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Intent.ACTION_PROCESS_TEXT.equals(action);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                bubblesApiEnabled = false;
                isBubbles = isProcessText;
            } else {
                createBubbleChannel();

                if (channel != null) {
                    channel.setAllowBubbles(true);
                    if (Utils.isEmpty(channel.getDescription()))
                        channel.setDescription(context.getString(R.string.linked_words_notif_channel_descr));
                }

                bubblesApiEnabled = notificationManager != null && notificationManager.areBubblesAllowed()
                        || channel != null && channel.canBubble();
                isBubbles = bubblesApiEnabled && Intent.ACTION_SEND.equals(action) || isProcessText;
            }


            //// this is if the app is started with MAIN action, that means it just starts normally
            if (Intent.ACTION_MAIN.equals(action)) {
                Context context = super.getBaseContext();
                if (context == null) context = this.context;
                if (context == null) context = getApplicationContext();
                if (context == null) context = this;
                finishTask();
                context.startActivity(new Intent(context, Main.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        .addCategory(Intent.CATEGORY_LAUNCHER).setAction(Intent.ACTION_MAIN));
                return;
            }


            //// depth scan to find some text
            if (bundle != null && ("text/plain".equals(type) || Intent.ACTION_WEB_SEARCH.equals(action)
                    || Intent.ACTION_SEARCH.equals(action))) {
                final Object object = bundle.get(action);
                if (object instanceof CharSequence) str[0] = object.toString();
                else for (final String key : bundle.keySet()) {
                    if ((isBubbles |= Intent.EXTRA_PROCESS_TEXT.equalsIgnoreCase(key))
                            || Intent.EXTRA_TEXT.equalsIgnoreCase(key)) {
                        final Object o = bundle.get(key);
                        if (o instanceof CharSequence) str[0] = o.toString();
                        else if (o instanceof ClipData && (clipData = (ClipData) o).getItemCount() > 0
                                && (clipDataItem = clipData.getItemAt(0)) != null
                                && !Utils.isEmpty(clipDataItem.getText())) str[0] = clipDataItem.getText().toString();
                    } else {
                        final String lcKey = key.toLowerCase();
                        if ("query".equals(lcKey) || "text".equals(lcKey)) {
                            final Object o = bundle.get(key);
                            if (o != null) str[0] = o.toString();
                        }
                    }

                    if (!Utils.isEmpty(str[0])) break;
                }
            }

            if (Utils.isEmpty(str[0])) {
                final String intentData = intent.getDataString();
                if (!Utils.isEmpty(intentData)) str[0] = Uri.decode(intentData);
            }

            if (Utils.isEmpty(str[0]) && (clipData = intent.getClipData()) != null && clipData.getItemCount() > 0
                    && (clipDataItem = clipData.getItemAt(0)) != null) {
                str[0] = clipDataItem.getHtmlText();
                if (Utils.isEmpty(str[0])) str[0] = clipDataItem.getText().toString();
                if (Utils.isEmpty(str[0])) str[0] = null;
            }


            //// bruh moment when every scan is empty
            if (Utils.isEmpty(str[0])) {
                startMain(str[0]);
                return;
            }


            //// sets up bubbles helper, only one instance to show one bubble only (or probably notification show one bubble, idk)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && bubbleHelper == null)
                bubbleHelper = new BubbleHelper(context, str[0]);


            final boolean showFloatingDialog = SettingsHelper.showFloatingDialog();
            final boolean showFloating = SettingsHelper.showFloating();

            //Log.d("AWAISKING_APP", "isBubbles:" + isBubbles
            //        + " -- showFloating:" + showFloating
            //        + " -- bubblesApiEnabled:" + bubblesApiEnabled
            //        + " -- canBubble: " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && channel != null && channel.canBubble())
            //        + " -- areBubblesAllowed: " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && notificationManager != null && notificationManager.areBubblesAllowed())
            //);

            final boolean handleFallback;
            if (showFloating && showFloatingDialog || !showFloating || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) handleFallback = true;
            else {
                if (bubbleHelper != null) {
                    bubbleHelper.setText(str[0]);
                    bubbleHelper.showBubble();
                }

                final boolean bubblesApiEnabledIntrl = bubblesApiEnabled || notificationManager != null
                        && notificationManager.areBubblesAllowed() || channel != null && channel.canBubble();
                isBubbles |= bubblesApiEnabledIntrl && Intent.ACTION_SEND.equals(action) || isProcessText;
                handleFallback = bubbleHelper == null || channel == null || !channel.canBubble() || !isBubbles && !bubblesApiEnabledIntrl;

                //Log.d("AWAISKING_APP", "isBubbles: " + isBubbles
                //        + " -- bubblesApiEnabledIntrl: " + bubblesApiEnabledIntrl
                //        + " -- fallback: " + handleFallback);
            }

            if (handleFallback) {
                final Handler handler = new Handler(Looper.getMainLooper());

                if (showFloating || showFloatingDialog) {
                    if (tts == null)
                        tts = new TextToSpeech(getApplicationContext(), Main::onTTSInit);

                    final int nightMode = SettingsHelper.getNightMode();
                    if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                        final Configuration configOverlay = getResources().getConfiguration();

                        final int newNightMode;
                        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) newNightMode = Configuration.UI_MODE_NIGHT_YES;
                        else if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) newNightMode = Configuration.UI_MODE_NIGHT_NO;
                        else newNightMode = configOverlay.uiMode & Configuration.UI_MODE_NIGHT_MASK;

                        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                        final Configuration overrideConf = new Configuration();
                        overrideConf.fontScale = 0;
                        if (configOverlay != null) overrideConf.setTo(configOverlay);
                        overrideConf.uiMode = newNightMode | (overrideConf.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);

                        getResources().updateConfiguration(overrideConf, displayMetrics);
                    }

                    final ContextThemeWrapper styledContext = Utils.getStyledContext(this, R.style.MaterialAlertDialogTheme);
                    awaisomeDialog = new AwaisomeDialogBuilder(styledContext)
                            .setLayoutView(floatingDialogView = new FloatingDialogView(styledContext).setWord(str[0]))
                            .setDialogInsets(0, 0)
                            .setLayoutPadding(0, 0)
                            .setViewHideFlags(HiddenFlags.BUTTON_PANEL | HiddenFlags.TITLE_PANEL)
                            .build();
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ((showFloating || showFloatingDialog) && awaisomeDialog != null) {
                            final DialogInterface.OnDismissListener onDismissListener = dialogInterface -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
                                else finish();
                            };
                            awaisomeDialog.setOnCancelListener(onDismissListener::onDismiss);
                            awaisomeDialog.setOnDismissListener(onDismissListener);
                            awaisomeDialog.show();
                        } else {
                            startMain(str[0]);
                        }

                        handler.removeCallbacks(this);
                    }
                }, showFloating || showFloatingDialog ? 50 : 100);
            }
        }

        dataHandled = false;
        finishTask();
    }

    private synchronized void createBubbleChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        if (channel != null) {
            channel.setAllowBubbles(true);
            return;
        }

        channel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                notificationManager.getNotificationChannel(LW_BUBBLES_CHANNEL_ID, LW_BUBBLES_CONVO_ID) : null;
        if (channel == null)
            channel = notificationManager.getNotificationChannel(LW_BUBBLES_CHANNEL_ID);

        final List<NotificationChannel> notificationChannels;
        if (channel == null && (notificationChannels = notificationManager.getNotificationChannels()) != null && notificationChannels.size() >= 1) {
            for (final NotificationChannel notificationChannel : notificationChannels) {
                if (notificationChannel == null) continue;
                if (LW_BUBBLES_CHANNEL_NAME.contentEquals(notificationChannel.getName()) &&
                        LW_BUBBLES_CHANNEL_ID.equals(notificationChannel.getId())) {

                    boolean setChannel = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        final String conversationId = notificationChannel.getConversationId();
                        if (LW_BUBBLES_CHANNEL_ID.equals(conversationId) || LW_BUBBLES_CONVO_ID.equals(conversationId))
                            setChannel = true;
                    }
                    setChannel = setChannel | notificationChannel.canBubble();

                    if (setChannel) {
                        channel = notificationChannel;
                        break;
                    }
                }
            }
        }

        if (channel == null)
            notificationManager.createNotificationChannel(new NotificationChannel(LW_BUBBLES_CHANNEL_ID,
                    LW_BUBBLES_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));

        if (channel == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            channel = notificationManager.getNotificationChannel(LW_BUBBLES_CHANNEL_ID, LW_BUBBLES_CONVO_ID);
        if (channel == null)
            channel = notificationManager.getNotificationChannel(LW_BUBBLES_CHANNEL_ID);
    }

    private synchronized void startMain(final String data) {
        Context context = super.getBaseContext();
        if (context == null) context = getApplicationContext();
        if (context == null) context = this.context;
        if (context == null) context = this;

        final boolean dataNotNull = !Utils.isEmpty(data);
        final Uri dataUri = dataNotNull ? Uri.parse(data) : null;
        final Intent intent = new Intent(Intent.ACTION_VIEW, dataUri, context, Main.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        if (dataNotNull) intent.putExtra(Intent.EXTRA_TEXT, data).setData(dataUri);

        if (!isFinishing()) finishTask();

        context.startActivity(intent);
    }

    private void finishTask() {
        if (awaisomeDialog != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
        else finish();
    }
}