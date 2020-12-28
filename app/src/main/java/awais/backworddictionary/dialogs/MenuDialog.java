package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.R;

public final class MenuDialog extends BottomSheetDialogFragment {
    private CharSequence title = "";
    private Object message;

    public void setTitle(final CharSequence title) {
        this.title = title;
    }

    public void setMessage(final Object message) {
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        final View contentView = View.inflate(getContext(), R.layout.dialog_modal, null);
        ((TextView) contentView.findViewById(android.R.id.title)).setText(title);

        final WebView aboutMessage = contentView.findViewById(R.id.webViewAbout);

        final TextView spannableMessage = contentView.findViewById(android.R.id.message);

        if (message != null) {
            if (message instanceof Spanned) {
                aboutMessage.setVisibility(View.GONE);
                spannableMessage.setVisibility(View.VISIBLE);
                spannableMessage.setMovementMethod(LinkMovementMethod.getInstance());
                spannableMessage.setTypeface(LinkedApp.fontRegular);
                spannableMessage.setText((CharSequence) message, TextView.BufferType.SPANNABLE);
            } else {
                spannableMessage.setVisibility(View.GONE);
                aboutMessage.setBackgroundColor(Color.TRANSPARENT);
                aboutMessage.setVisibility(View.VISIBLE);
                aboutMessage.loadUrl("file:///android_asset/about.html");
            }

            dialog.setContentView(contentView);
        }

        return dialog;
    }
}