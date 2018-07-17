package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.view.ViewGroup;
import android.view.Window;

import awais.backworddictionary.R;

public class AdvancedDialog extends Dialog {
//    private final int maxWords = Main.sharedPreferences.getInt("maxWords", 80);

    public AdvancedDialog(Activity act) {
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
        setContentView(R.layout.advanced_dialog);

        ((DialogTitle)findViewById(R.id.alertTitle)).setText(R.string.advance);

        (findViewById(R.id.btnOK)).setOnClickListener(view -> {
//            Main.sharedPreferences.edit().putInt("maxWords", numberPicker.getValue()).apply();
            dismiss();
        });
        (findViewById(R.id.btnCancel)).setOnClickListener(view -> {
//            Main.sharedPreferences.edit().putInt("maxWords", maxWords).apply();
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