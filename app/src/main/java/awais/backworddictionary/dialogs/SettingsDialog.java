package awais.backworddictionary.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.TTSActivity;
import awais.backworddictionary.custom.MaterialCheckedTextView;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.sephiroth.numberpicker.HorizontalNumberPicker;

public final class SettingsDialog extends Dialog {
    public final static int TTS_SETTINGS_REQUEST_CODE = 5320;
    private final int maxWords = SettingsHelper.getMaxWords();
    private final boolean showAds = SettingsHelper.showAds();
    private final boolean showPopup = SettingsHelper.showDefsPopup();
    private final boolean showDialog = SettingsHelper.showDialog();
    private final boolean showFloating = SettingsHelper.showFloating();
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

        setContentView(R.layout.settings_dialog);

        ((TextView) findViewById(R.id.alertTitle)).setText(R.string.settings);

        final HorizontalNumberPicker numberPicker = findViewById(R.id.horizontalNumberPicker);
        numberPicker.setProgress(maxWords);

        final MaterialCheckedTextView cbShowFloating = findViewById(R.id.showFloating);
        cbShowFloating.setChecked(showFloating);

        final MaterialCheckedTextView cbShowAds = findViewById(R.id.showAds);
        cbShowAds.setChecked(showAds);

        final MaterialCheckedTextView cbShowDialog = findViewById(R.id.showWordDialog);
        cbShowDialog.setChecked(showDialog);

        final MaterialCheckedTextView cbShowDefsPopup = findViewById(R.id.showDefsPopup);
        cbShowDefsPopup.setChecked(showPopup);

        final RadioGroup rgAppTheme = findViewById(R.id.rgAppTheme);
        final int checkedTheme;
        final int darkMode = SettingsHelper.getNightMode();
        if (darkMode == AppCompatDelegate.MODE_NIGHT_YES) checkedTheme = R.id.rbThemeDark;
        else if (darkMode == AppCompatDelegate.MODE_NIGHT_NO) checkedTheme = R.id.rbThemeLight;
        else checkedTheme = R.id.rbThemeAuto;
        rgAppTheme.check(checkedTheme);

        final View btnTTS = findViewById(R.id.btnTTS);
        ((View) btnTTS.getParent()).setVisibility(wasTTSErrorShown ? View.GONE : View.VISIBLE);

        final View.OnClickListener onClickListener = v -> {
            if (v instanceof MaterialCheckedTextView) {
                final MaterialCheckedTextView tvCheck = (MaterialCheckedTextView) v;
                tvCheck.setChecked(!tvCheck.isChecked());
                return;
            }

            final int id = v.getId();
            if (id == R.id.btnOK) {
                int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                final int selectedTheme = rgAppTheme.getCheckedRadioButtonId();
                if (selectedTheme == R.id.rbThemeDark) theme = AppCompatDelegate.MODE_NIGHT_YES;
                else if (selectedTheme == R.id.rbThemeLight) theme = AppCompatDelegate.MODE_NIGHT_NO;

                SettingsHelper.setValues(Math.max(Math.min(1000, numberPicker.getProgress()), 1),
                        theme, cbShowAds.isChecked(), cbShowDialog.isChecked(),
                        cbShowFloating.isChecked(), cbShowDefsPopup.isChecked());

                if (activity instanceof Main) {
                    ((Main) activity).closeExpanded();
                    Utils.adsBox(activity);
                }

                if (selectedTheme != checkedTheme) {
                    AppCompatDelegate.setDefaultNightMode(theme);
                    if (activity instanceof Main) ((Main) activity).loadFragments(false);
                    else if (activity != null) activity.recreate();
                }
            } else if (id == R.id.btnTTS) {
                if (activity != null)
                    activity.startActivityForResult(new Intent(activity, TTSActivity.class),
                            TTS_SETTINGS_REQUEST_CODE);
                return;
            } else if (activity instanceof Main) Utils.adsBox(activity);
            dismiss();
        };

        cbShowAds.setOnClickListener(onClickListener);
        cbShowDialog.setOnClickListener(onClickListener);
        cbShowFloating.setOnClickListener(onClickListener);
        cbShowDefsPopup.setOnClickListener(onClickListener);

        findViewById(R.id.btnOK).setOnClickListener(onClickListener);
        findViewById(R.id.btnCancel).setOnClickListener(onClickListener);
        btnTTS.setOnClickListener(onClickListener);
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}