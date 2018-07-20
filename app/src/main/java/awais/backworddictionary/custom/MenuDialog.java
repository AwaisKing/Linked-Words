package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import awais.backworddictionary.R;

public class MenuDialog extends BottomSheetDialogFragment {
    private CharSequence title = "";
    private Object message;

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss();
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
    };

    @Override @SuppressLint("RestrictedApi")
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.dialog_modal, null);
        ((TextView) contentView.findViewById(android.R.id.title)).setText(title);

        TextView spannableMessage = contentView.findViewById(android.R.id.message);
        WebView aboutMessage = contentView.findViewById(R.id.webViewAbout);
        spannableMessage.setMovementMethod(LinkMovementMethod.getInstance());
        if (message != null) {
            if (message.getClass() != String.class) {
                spannableMessage.setVisibility(View.VISIBLE);
                aboutMessage.setVisibility(View.GONE);
                spannableMessage.setText((SpannableStringBuilder) message);
            } else {
                spannableMessage.setVisibility(View.GONE);
                aboutMessage.setVisibility(View.VISIBLE);
                aboutMessage.loadUrl("file:///android_asset/about.html");
            }
        }
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior)
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
    }
}