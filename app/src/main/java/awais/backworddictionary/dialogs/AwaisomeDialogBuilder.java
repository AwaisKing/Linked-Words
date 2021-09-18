package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import awais.backworddictionary.databinding.DialogLayoutBinding;

public final class AwaisomeDialogBuilder {
    private final Context context;
    @StyleRes
    private final int dialogStyle;
    private final CharSequence[] buttonTexts = new CharSequence[3];
    @Px
    private final int[] layoutPadding = {-1, -1, -1, -1};
    @Px
    private final int[] dialogInsets = {-1, -1, -1, -1};
    @LayoutRes
    private int layoutRes = 0;
    @LinearLayoutCompat.OrientationMode
    private int orientation = LinearLayoutCompat.HORIZONTAL;
    private int hideFlags = 0;
    private boolean cancelable = true;
    private View layoutView;
    private AwaisomeClickListener clickListener = null;
    private CharSequence title = null;
    private CharSequence text = null;
    private Integer backColor = null;

    public AwaisomeDialogBuilder(final Context context) {
        this(context, -1);
    }

    public AwaisomeDialogBuilder(final Context context, @StyleRes final int appThemeRes) {
        this.context = context;
        this.dialogStyle = appThemeRes;
    }

    public AwaisomeDialogBuilder setDialogCancelable(final boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public AwaisomeDialogBuilder setLayoutResID(@LayoutRes final int layoutRes) {
        this.layoutRes = layoutRes;
        return this;
    }

    public AwaisomeDialogBuilder setLayoutView(final View layoutView) {
        this.layoutView = layoutView;
        return this;
    }

    public AwaisomeDialogBuilder setButtonsOrientation(@LinearLayoutCompat.OrientationMode final int orientation) {
        this.orientation = orientation;
        return this;
    }

    public AwaisomeDialogBuilder setLayoutPadding(@Px final int left, @Px final int top, @Px final int right, @Px final int bottom) {
        this.layoutPadding[0] = left;
        this.layoutPadding[1] = top;
        this.layoutPadding[2] = right;
        this.layoutPadding[3] = bottom;
        return this;
    }

    public AwaisomeDialogBuilder setLayoutPadding(@Px final int horizontal, @Px final int vertical) {
        return setLayoutPadding(horizontal, vertical, horizontal, vertical);
    }

    public AwaisomeDialogBuilder setDialogInsets(@Px final int left, @Px final int top, @Px final int right, @Px final int bottom) {
        this.dialogInsets[0] = left;
        this.dialogInsets[1] = top;
        this.dialogInsets[2] = right;
        this.dialogInsets[3] = bottom;
        return this;
    }

    public AwaisomeDialogBuilder setDialogInsets(@Px final int horizontal, @Px final int vertical) {
        return setDialogInsets(horizontal, vertical, horizontal, vertical);
    }

    public AwaisomeDialogBuilder setTitle(final CharSequence title) {
        this.title = title;
        return this;
    }

    public AwaisomeDialogBuilder setTitle(@StringRes final int title) {
        return setTitle(context.getString(title));
    }

    public AwaisomeDialogBuilder setText(final CharSequence text) {
        this.text = text;
        return this;
    }

    public AwaisomeDialogBuilder setText(@StringRes final int text) {
        return setText(context.getString(text));
    }

    public AwaisomeDialogBuilder setButtonTexts(@Nullable final CharSequence textOK, @Nullable final CharSequence textCancel,
                                                @Nullable final CharSequence textNeutral, @Nullable final AwaisomeClickListener clickListener) {
        this.buttonTexts[0] = textOK;
        this.buttonTexts[1] = textCancel;
        this.buttonTexts[2] = textNeutral;
        this.clickListener = clickListener;
        return this;
    }

    public AwaisomeDialogBuilder setButtonTexts(@StringRes final Integer textOK, @StringRes final Integer textCancel,
                                                @StringRes final Integer textNeutral, @Nullable final AwaisomeClickListener clickListener) {
        return setButtonTexts(textOK == null || textOK == 0 || textOK == -1 ? null : context.getString(textOK),
                textCancel == null || textCancel == 0 || textCancel == -1 ? null : context.getString(textCancel),
                textNeutral == null || textNeutral == 0 || textNeutral == -1 ? null : context.getString(textNeutral),
                clickListener);
    }

    public AwaisomeDialogBuilder setViewHideFlags(final int flags) {
        this.hideFlags = flags;
        return this;
    }

    public AwaisomeDialogBuilder setBackgroundColor(final int backColor) {
        this.backColor = backColor;
        return this;
    }

    @NonNull
    public AwaisomeDialog build() {
        final AwaisomeDialog awaisomeDialog = new AwaisomeDialog(context, dialogStyle);

        awaisomeDialog.setCancelable(cancelable);
        awaisomeDialog.setCanceledOnTouchOutside(cancelable);
        awaisomeDialog.setTitle(title);

        if (layoutView != null) awaisomeDialog.setLayoutView(layoutView);
        else if (layoutRes != -1 && layoutRes != 0) awaisomeDialog.setLayoutID(layoutRes);
        else if (text != null && text.length() > 0) awaisomeDialog.setText(text);

        awaisomeDialog.dialogInsets(dialogInsets);
        awaisomeDialog.setLayoutPadding(layoutPadding);
        awaisomeDialog.setButtonsOrientation(orientation);

        awaisomeDialog.setButtonTexts(buttonTexts);
        awaisomeDialog.setClickListener(clickListener);

        awaisomeDialog.setViewHideFlags(hideFlags);
        if (backColor != null) awaisomeDialog.setBackgroundColor(ColorStateList.valueOf(backColor));

        return awaisomeDialog;
    }

    @NonNull
    public AwaisomeDialog show() {
        final AwaisomeDialog dialog = build();
        dialog.show();
        return dialog;
    }

    public final static class AwaisomeDialog extends Dialog {
        private final Context context;
        private CharSequence title;
        private CharSequence text;
        private AwaisomeClickListener clickListener;
        private CharSequence[] buttonTexts;
        private View layoutView;
        @Px
        private int[] layoutPadding = {-1, -1, -1, -1};
        @Px
        private int[] dialogInsets = {-1, -1, -1, -1};
        @LayoutRes
        private int layoutId = 0;
        private int hideFlags = 0;
        @LinearLayoutCompat.OrientationMode
        private int orientation = LinearLayoutCompat.HORIZONTAL;

        private ViewGroup contentParent;
        private ColorStateList backColor = null;

        public AwaisomeDialog(@NonNull Context context, @StyleRes final int style) {
            super(context = getStyledContext(context, style));
            this.context = context;
        }

        @Override
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public void setTitle(final int titleId) {
            setTitle(context.getString(titleId));
        }

        @Override
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public void setTitle(@Nullable final CharSequence title) {
            this.title = title;
        }

        private void setText(final CharSequence text) {
            this.text = text;
        }

        private void setLayoutID(final int layoutId) {
            this.layoutId = layoutId;
        }

        private void setLayoutView(final View layoutView) {
            this.layoutView = layoutView;
        }

        private void setLayoutPadding(final int[] layoutPadding) {
            this.layoutPadding = layoutPadding;
        }

        private void dialogInsets(final int[] dialogInsets) {
            this.dialogInsets = dialogInsets;
        }

        private void setButtonsOrientation(@LinearLayoutCompat.OrientationMode final int orientation) {
            this.orientation = orientation;
        }

        private void setButtonTexts(final CharSequence[] buttonTexts) {
            this.buttonTexts = buttonTexts;
        }

        private void setClickListener(final AwaisomeClickListener clickListener) {
            this.clickListener = clickListener;
        }

        private void setViewHideFlags(final int hideFlags) {
            this.hideFlags = hideFlags;
        }

        private void setBackgroundColor(final ColorStateList backColor) {
            this.backColor = backColor;
        }

        public ViewGroup getContentView() {
            return contentParent;
        }

        @Override
        public void show() {
            super.show();
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            final LayoutInflater layoutInflater = getLayoutInflater();
            final DialogLayoutBinding binding = DialogLayoutBinding.inflate(layoutInflater);

            final MaterialCardView cardView = binding.getRoot();

            {
                final int padLeft = dialogInsets[0] == -1 ? cardView.getContentPaddingLeft() : dialogInsets[0];
                final int padTop = dialogInsets[1] == -1 ? cardView.getContentPaddingTop() : dialogInsets[1];
                final int padRight = dialogInsets[2] == -1 ? cardView.getContentPaddingRight() : dialogInsets[2];
                final int padBottom = dialogInsets[3] == -1 ? cardView.getContentPaddingBottom() : dialogInsets[3];
                cardView.setContentPadding(padLeft, padTop, padRight, padBottom);
            }

            setContentView(cardView);

            if (backColor != null) cardView.setCardBackgroundColor(backColor);

            final boolean showButtonPanel = (hideFlags & HiddenFlags.BUTTON_PANEL) == 0;
            final boolean showTitle = (hideFlags & HiddenFlags.TITLE_PANEL) == 0 && title != null && title.length() >= 0;

            binding.title.setVisibility(showTitle ? View.VISIBLE : View.GONE);
            if (showTitle) binding.title.setText(title);

            contentParent = (ViewGroup) binding.content.getParent();

            {
                final int padLeft = layoutPadding[0] == -1 ? contentParent.getPaddingLeft() : layoutPadding[0];
                final int padTop = layoutPadding[1] == -1 ? contentParent.getPaddingTop() : layoutPadding[1];
                final int padRight = layoutPadding[2] == -1 ? contentParent.getPaddingRight() : layoutPadding[2];
                final int padBottom = layoutPadding[3] == -1 ? contentParent.getPaddingBottom() : layoutPadding[3];
                contentParent.setPadding(padLeft, padTop, padRight, padBottom);
            }

            try {
                contentParent.removeView(binding.content);
            } catch (final Exception e) {
                binding.content.setVisibility(View.GONE);
            }

            if (layoutView != null) contentParent.addView(layoutView);
            else if (layoutId != -1 && layoutId != 0) {
                contentParent.addView(layoutView = layoutInflater.inflate(layoutId, contentParent, false));
            } else if (text != null && text.length() > 0) {
                final MaterialTextView textView = new MaterialTextView(context);
                textView.setText(text);
                contentParent.addView(textView);
            }

            final LinearLayoutCompat buttonPanel = (LinearLayoutCompat) binding.button1.getParent();

            buttonPanel.setVisibility(showButtonPanel ? View.VISIBLE : View.GONE);
            if (showButtonPanel) {
                buttonPanel.setOrientation(orientation);

                if (buttonTexts[0] != null && buttonTexts[0].length() > 0) binding.button1.setText(buttonTexts[0]);
                if (buttonTexts[1] != null && buttonTexts[1].length() > 0) {
                    binding.button2.setText(buttonTexts[1]);
                    binding.button2.setVisibility(View.VISIBLE);
                } else binding.button2.setVisibility(orientation == LinearLayoutCompat.VERTICAL ? View.GONE : View.INVISIBLE);
                if (buttonTexts[2] != null && buttonTexts[2].length() > 0) {
                    binding.button3.setText(buttonTexts[2]);
                    binding.button3.setVisibility(View.VISIBLE);
                } else binding.button3.setVisibility(orientation == LinearLayoutCompat.VERTICAL ? View.GONE : View.INVISIBLE);

                if (orientation == LinearLayoutCompat.VERTICAL) {
                    int padding = 0;
                    for (int k = buttonPanel.getChildCount() - 1; k >= 0; k--) {
                        final MaterialButton button = (MaterialButton) buttonPanel.getChildAt(k);
                        buttonPanel.removeViewAt(k);
                        buttonPanel.addView(button);

                        if (padding == 0) padding = button.getInsetTop();

                        button.setInsetTop(0);
                        button.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    buttonPanel.setPadding(padding, 0, padding, 0);
                }

                final View.OnClickListener btnClickListener = v -> {
                    boolean dismiss = true;

                    if (clickListener != null) {
                        if (v == binding.button1)
                            dismiss = clickListener.onClick(this, binding.button1, DialogButton.BUTTON_OK);
                        else if (v == binding.button2)
                            dismiss = clickListener.onClick(this, binding.button2, DialogButton.BUTTON_CANCEL);
                        else if (v == binding.button3)
                            dismiss = clickListener.onClick(this, binding.button3, DialogButton.BUTTON_NEUTRAL);
                    }

                    if (dismiss) dismiss();
                };

                binding.button1.setOnClickListener(btnClickListener);
                binding.button2.setOnClickListener(btnClickListener);
                binding.button3.setOnClickListener(btnClickListener);
            }
        }

        @NonNull
        private static ContextThemeWrapper getStyledContext(final Context context, @StyleRes int style) {
            if (style == 0 || style == -1) {
                final Resources.Theme theme;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (theme = context.getTheme()) != null)
                    return new ContextThemeWrapper(context, theme);
                else {
                    final Context appContext = context.getApplicationContext();

                    final PackageManager packageManager = appContext.getPackageManager();
                    final String packageName = appContext.getPackageName();

                    int styleHack;
                    try {
                        final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                        styleHack = packageInfo.applicationInfo.theme;
                        Log.d("AWAISKING_APP", "styleHack1: " + styleHack);
                    } catch (final Exception e) {
                        Log.e("AWAISKING_APP", "err1", e);
                        styleHack = -1;
                    }

                    if (styleHack == 0 || styleHack == -1) {
                        try {
                            final Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                            //noinspection ConstantConditions
                            final ActivityInfo activityInfo = packageManager.getActivityInfo(intent.getComponent(), 0);
                            styleHack = activityInfo.getThemeResource();
                            Log.d("AWAISKING_APP", "styleHack2: " + styleHack);
                        } catch (final Exception e) {
                            Log.e("AWAISKING_APP", "err2", e);
                            styleHack = -1;
                        }
                    }

                    //                    try {
                    //                        final ActivityInfo activityInfo = packageManager.getActivityInfo(new ComponentName(appContext, Main.class), 0);
                    //                        styleHack = activityInfo.getThemeResource();
                    //                    } catch (final Throwable e) {
                    //                        styleHack = -1;
                    //                    }
                    //                    if (styleHack == 0 || styleHack == -1) {
                    //                        try {
                    //                            final ActivityInfo activityInfo = packageManager.getActivityInfo(new ComponentName(context, Main.class), 0);
                    //                            styleHack = activityInfo.getThemeResource();
                    //                        } catch (final Throwable e) {
                    //                            styleHack = -1;
                    //                        }
                    //                    }

                    if (styleHack != 0 && styleHack != -1) style = styleHack;

                }
            }
            return new ContextThemeWrapper(context, style);
        }
    }

    public interface AwaisomeClickListener {
        boolean onClick(final AwaisomeDialog dialog, final View button, final DialogButton which);
    }

    public enum DialogButton {
        BUTTON_OK, BUTTON_CANCEL, BUTTON_NEUTRAL
    }

    @IntDef({HiddenFlags.BUTTON_PANEL, HiddenFlags.TITLE_PANEL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HiddenFlags {
        int BUTTON_PANEL = 1, TITLE_PANEL = 2;
    }

}