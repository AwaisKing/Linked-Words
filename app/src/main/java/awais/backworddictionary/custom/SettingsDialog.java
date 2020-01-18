package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.appcompat.widget.DialogTitle;
import androidx.appcompat.widget.LinearLayoutCompat;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

public class SettingsDialog extends Dialog {
    private final int maxWords = Main.sharedPreferences.getInt("maxWords", 80);
    private final boolean showAds = Main.sharedPreferences.getBoolean("showAds", true);
    private final boolean showDialog = Main.sharedPreferences.getBoolean("showDialog", false);
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
        ((DialogTitle) findViewById(R.id.alertTitle)).setText(R.string.settings);

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

        final View.OnClickListener onClickListener = v -> {
            if (v == showAdsLayout) cbShowAds.toggle();
            else if (v == showDialogLayout) cbShowDialog.toggle();
            else {
                final int id = v.getId();
                if (id == R.id.btnOK) {
                    Main.sharedPreferences.edit()
                            .putInt("maxWords", Math.max(numberPicker.getValue(), 1))
                            .putBoolean("showAds", cbShowAds.isChecked())
                            .putBoolean("showDialog", cbShowDialog.isChecked())
                            .apply();
                    if (activity instanceof Main) {
                        ((Main) activity).closeExpanded();
                        Utils.adsBox(activity);
                    }
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