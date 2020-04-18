package awais.backworddictionary.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;

public class SettingsDialog extends Dialog {
    private final int maxWords = SettingsHelper.getMaxWords();
    private final boolean showAds = SettingsHelper.showAds();
    private final boolean showDialog = SettingsHelper.showDialog();
    private final Activity activity;

    public SettingsDialog(final Activity act) {
        super(act, R.style.Dialog);
        activity = act;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.settings_dialog);
        ((TextView) findViewById(R.id.alertTitle)).setText(R.string.settings);

        final NumberPicker numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);
        numberPicker.setValue(maxWords);

        final LinearLayoutCompat showAdsLayout = findViewById(R.id.showAds);
        final CheckBox cbShowAds = (CheckBox) showAdsLayout.getChildAt(1);
        cbShowAds.setChecked(showAds);

        final LinearLayoutCompat showDialogLayout = findViewById(R.id.showWordDialog);
        final CheckBox cbShowDialog = (CheckBox) showDialogLayout.getChildAt(1);
        cbShowDialog.setChecked(showDialog);

        final RadioGroup rgAppTheme = findViewById(R.id.rgAppTheme);
        final int checkedId;
        final int darkMode = SettingsHelper.getNightMode();
        if (darkMode == AppCompatDelegate.MODE_NIGHT_YES) checkedId = R.id.rbThemeDark;
        else if (darkMode == AppCompatDelegate.MODE_NIGHT_NO) checkedId = R.id.rbThemeLight;
        else checkedId = R.id.rbThemeAuto;
        rgAppTheme.check(checkedId);

        final View.OnClickListener onClickListener = v -> {
            if (v == showAdsLayout) cbShowAds.toggle();
            else if (v == showDialogLayout) cbShowDialog.toggle();
            else {
                final int id = v.getId();
                if (id == R.id.btnOK) {
                    int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    final int buttonId = rgAppTheme.getCheckedRadioButtonId();
                    if (buttonId == R.id.rbThemeDark) theme = AppCompatDelegate.MODE_NIGHT_YES;
                    else if (buttonId == R.id.rbThemeLight) theme = AppCompatDelegate.MODE_NIGHT_NO;

                    SettingsHelper.setValues(Math.max(numberPicker.getValue(), 1),
                            theme, cbShowAds.isChecked(), cbShowDialog.isChecked());

                    if (activity instanceof Main) {
                        ((Main) activity).closeExpanded();
                        Utils.adsBox(activity);
                    }

                    if (buttonId != checkedId)
                        AppCompatDelegate.setDefaultNightMode(theme);
                } else if (activity instanceof Main) Utils.adsBox(activity);
                dismiss();
            }
        };

        showAdsLayout.setOnClickListener(onClickListener);
        showDialogLayout.setOnClickListener(onClickListener);
        findViewById(R.id.btnOK).setOnClickListener(onClickListener);
        findViewById(R.id.btnCancel).setOnClickListener(onClickListener);
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}