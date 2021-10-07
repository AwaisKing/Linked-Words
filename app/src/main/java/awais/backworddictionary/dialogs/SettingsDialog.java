package awais.backworddictionary.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.TTSActivity;
import awais.backworddictionary.custom.MaterialCheckedTextView;
import awais.backworddictionary.databinding.SettingsDialogBinding;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;

public final class SettingsDialog extends Dialog {
    public final static int TTS_SETTINGS_REQUEST_CODE = 5320;
    private final int maxWords = SettingsHelper.getMaxWords();
    private final boolean showAds = SettingsHelper.showAds();
    private final boolean showPopup = SettingsHelper.showDefsPopup();
    private final boolean showDialog = SettingsHelper.showDialog();
    private final boolean showFloating = SettingsHelper.showFloating();
    private final boolean showFloatingDialog = SettingsHelper.showFloatingDialog();
    private final boolean wasTTSErrorShown;
    private final Activity activity;

    public SettingsDialog(final Activity act, final DialogInterface dialog) {
        super(act, R.style.DefinitionsDialogTheme);
        activity = act;
        wasTTSErrorShown = dialog != null;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        final Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        final SettingsDialogBinding dialogBinding = SettingsDialogBinding.inflate(getLayoutInflater());
        final LinearLayoutCompat rootView = dialogBinding.getRoot();

        dialogBinding.alertTitle.setText(R.string.settings);

        dialogBinding.numberPicker.setProgress(maxWords);
        dialogBinding.showFloating.setChecked(showFloating);
        dialogBinding.showAds.setChecked(showAds);
        dialogBinding.showWordDialog.setChecked(showDialog);
        dialogBinding.showDefsPopup.setChecked(showPopup);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            dialogBinding.showFloatingDialog.setEnabled(false);
            dialogBinding.showFloatingDialog.setVisibility(View.GONE);
        } else {
            dialogBinding.showFloatingDialog.setEnabled(true);
            dialogBinding.showFloatingDialog.setVisibility(View.VISIBLE);
            dialogBinding.showFloatingDialog.setChecked(showFloatingDialog);
        }

        final int darkMode = SettingsHelper.getNightMode();
        final int checkedTheme;
        if (darkMode == AppCompatDelegate.MODE_NIGHT_YES) checkedTheme = R.id.rbThemeDark;
        else if (darkMode == AppCompatDelegate.MODE_NIGHT_NO) checkedTheme = R.id.rbThemeLight;
        else checkedTheme = R.id.rbThemeAuto;
        dialogBinding.rgAppTheme.check(checkedTheme);

        ((View) dialogBinding.btnTTS.getParent()).setVisibility(wasTTSErrorShown ? View.GONE : View.VISIBLE);

        final View.OnClickListener onClickListener = v -> {
            if (v instanceof MaterialCheckedTextView) {
                final MaterialCheckedTextView tvCheck = (MaterialCheckedTextView) v;
                final boolean checked = tvCheck.isChecked();
                tvCheck.setChecked(!checked);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && v == dialogBinding.showFloating) {
                    dialogBinding.showFloatingDialog.setEnabled(!checked);
                    dialogBinding.showFloatingDialog.setAlpha(!checked ? 1f : 0.6f);
                }
                return;
            }

            if (v == dialogBinding.btnOK) {
                int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                final int selectedTheme = dialogBinding.rgAppTheme.getCheckedButtonId();
                if (selectedTheme == R.id.rbThemeDark) theme = AppCompatDelegate.MODE_NIGHT_YES;
                else if (selectedTheme == R.id.rbThemeLight) theme = AppCompatDelegate.MODE_NIGHT_NO;

                SettingsHelper.setValues(Math.max(Math.min(1000, dialogBinding.numberPicker.getProgress()), 1),
                        theme, dialogBinding.showAds.isChecked(), dialogBinding.showWordDialog.isChecked(),
                        dialogBinding.showFloating.isChecked(), dialogBinding.showFloatingDialog.isChecked(),
                        dialogBinding.showDefsPopup.isChecked());

                if (activity instanceof Main) {
                    ((Main) activity).closeExpanded();
                    Utils.adsBox(activity);
                }

                if (selectedTheme != checkedTheme) {
                    AppCompatDelegate.setDefaultNightMode(theme);
                    if (activity instanceof Main) ((Main) activity).loadFragments(false);
                    else if (activity != null) activity.recreate();
                }
            } else if (v == dialogBinding.btnTTS) {
                if (activity != null)
                    activity.startActivityForResult(new Intent(activity, TTSActivity.class),
                            TTS_SETTINGS_REQUEST_CODE);
                return;
            } else if (activity instanceof Main) Utils.adsBox(activity);

            dismiss();
        };

        dialogBinding.showAds.setOnClickListener(onClickListener);
        dialogBinding.showWordDialog.setOnClickListener(onClickListener);
        dialogBinding.showFloating.setOnClickListener(onClickListener);
        dialogBinding.showFloatingDialog.setOnClickListener(onClickListener);
        dialogBinding.showDefsPopup.setOnClickListener(onClickListener);

        dialogBinding.btnOK.setOnClickListener(onClickListener);
        dialogBinding.btnTTS.setOnClickListener(onClickListener);
        dialogBinding.btnCancel.setOnClickListener(onClickListener);

        setContentView(rootView);

        rootView.post(() -> {
            dialogBinding.dummy.requestFocusFromTouch();
            dialogBinding.dummy.requestFocus();

            dialogBinding.dummy.clearFocus();
        });
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}