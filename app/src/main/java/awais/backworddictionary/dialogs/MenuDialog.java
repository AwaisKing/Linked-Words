package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import awais.backworddictionary.LinkedApp;
import awais.backworddictionary.databinding.DialogModalBinding;

public final class MenuDialog extends BottomSheetDialogFragment {
    private CharSequence title;
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

        final DialogModalBinding modalBinding = DialogModalBinding.inflate(LayoutInflater.from(getContext()));
        modalBinding.alertTitle.setText(title);

        if (message != null) {
            if (message instanceof Spanned) {
                modalBinding.aboutMessage.setVisibility(View.GONE);
                modalBinding.spannableMessage.setVisibility(View.VISIBLE);
                modalBinding.spannableMessage.setMovementMethod(LinkMovementMethod.getInstance());
                modalBinding.spannableMessage.setTypeface(LinkedApp.fontRegular);
                modalBinding.spannableMessage.setText((CharSequence) message, TextView.BufferType.SPANNABLE);
            } else {
                modalBinding.spannableMessage.setVisibility(View.GONE);
                modalBinding.aboutMessage.setBackgroundColor(Color.TRANSPARENT);
                modalBinding.aboutMessage.setVisibility(View.VISIBLE);
                modalBinding.aboutMessage.loadUrl("file:///android_asset/about.html");
            }

            dialog.setContentView(modalBinding.getRoot());
        }

        return dialog;
    }
}