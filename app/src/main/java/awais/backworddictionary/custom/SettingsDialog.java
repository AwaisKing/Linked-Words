package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;

public class SettingsDialog extends Dialog {
    private final int maxWords = Main.sharedPreferences.getInt("maxWords", 80);

    public SettingsDialog(Activity act) {
        super(act, R.style.Dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.settings_dialog);
        ((DialogTitle)findViewById(R.id.alertTitle)).setText(R.string.settings);

        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);
        numberPicker.setValue(maxWords);

        (findViewById(R.id.btnOK)).setOnClickListener(view -> {
            Main.sharedPreferences.edit().putInt("maxWords", numberPicker.getValue()).apply();
            dismiss();
        });
        (findViewById(R.id.btnCancel)).setOnClickListener(view -> {
            Main.sharedPreferences.edit().putInt("maxWords", maxWords).apply();
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