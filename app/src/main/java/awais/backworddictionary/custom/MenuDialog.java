package awais.backworddictionary.custom;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;

public class MenuDialog extends BottomSheetDialogFragment {
    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss();
        }

        @Override
        public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {}
    };
    private CharSequence title = "";
    private Object message;

    void setTitle(final CharSequence title) {
        this.title = title;
    }

    void setMessage(final Object message) {
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        final View contentView = View.inflate(getContext(), R.layout.dialog_modal, null);
        ((AppCompatTextView) contentView.findViewById(android.R.id.title)).setText(title);

        final WebView aboutMessage = contentView.findViewById(R.id.webViewAbout);
        aboutMessage.setBackgroundColor(Color.TRANSPARENT);

        final AppCompatTextView spannableMessage = contentView.findViewById(android.R.id.message);
        spannableMessage.setMovementMethod(LinkMovementMethod.getInstance());

        if (message == null) return dialog;

        if (message instanceof SpannableStringBuilder) {
            spannableMessage.setVisibility(View.VISIBLE);
            aboutMessage.setVisibility(View.GONE);
            spannableMessage.setTypeface(LinkedApp.fontRegular);
            spannableMessage.setText((SpannableStringBuilder) message);
        } else {
            spannableMessage.setVisibility(View.GONE);
            aboutMessage.setVisibility(View.VISIBLE);
            aboutMessage.loadUrl("file:///android_asset/about.html");
        }
        dialog.setContentView(contentView);

        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        final CoordinatorLayout.Behavior<?> behavior = layoutParams.getBehavior();
        if (behavior instanceof BottomSheetBehavior)
            ((BottomSheetBehavior<?>) behavior).addBottomSheetCallback(mBottomSheetBehaviorCallback);

        return dialog;
    }
}