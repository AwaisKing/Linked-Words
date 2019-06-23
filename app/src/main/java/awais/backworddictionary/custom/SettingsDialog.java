package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

public class SettingsDialog extends Dialog {
    private final int maxWords = Main.sharedPreferences.getInt("maxWords", 80);
    private final boolean showAds = Main.sharedPreferences.getBoolean("showAds", true);
    private final Activity activity;

    public SettingsDialog(Activity act) {
        super(act, R.style.Dialog);
        activity = act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.settings_dialog);
        ((DialogTitle)findViewById(R.id.alertTitle)).setText(R.string.settings);

        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);
        numberPicker.setValue(maxWords);

        LinearLayoutCompat showAdsLayout = findViewById(R.id.showAds);
        CheckBox cbShowAds = (CheckBox) showAdsLayout.getChildAt(1);
        cbShowAds.setChecked(showAds);
        showAdsLayout.setOnClickListener(v -> cbShowAds.toggle());

        findViewById(R.id.btnOK).setOnClickListener(view -> {
            SharedPreferences.Editor sharedEditor = Main.sharedPreferences.edit();
            sharedEditor.putInt("maxWords", numberPicker.getValue());
            sharedEditor.putBoolean("showAds", cbShowAds.isChecked());
            sharedEditor.apply();
            if (activity instanceof Main) Utils.adsBox(activity);
            dismiss();
        });
        findViewById(R.id.btnCancel).setOnClickListener(view -> {
            if (activity instanceof Main) Utils.adsBox(activity);
            dismiss();
        });
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}