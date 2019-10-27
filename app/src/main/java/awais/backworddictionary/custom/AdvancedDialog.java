package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.widget.DialogTitle;

import java.util.Arrays;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.interfaces.FragmentLoader;

public class AdvancedDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {
    private final FragmentLoader fragmentLoader;
    private List<CompoundButton> checkBoxes;
    private final boolean[] enabledChecks = {true, true, true, true, false, false, false, false, false, false};
    private String bools;

    public AdvancedDialog(Activity act) {
        super(act, R.style.Dialog);
        this.fragmentLoader = (FragmentLoader) act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.advanced_dialog);

        ((DialogTitle) findViewById(R.id.alertTitle)).setText(R.string.advance);

        final ViewGroup container = findViewById(R.id.checkboxContainer);

        final CheckBox cbReverse     = (CheckBox) container.getChildAt(0);
        final CheckBox cbSoundsLike  = (CheckBox) container.getChildAt(1);
        final CheckBox cbSpelledLike = (CheckBox) container.getChildAt(2);
        final CheckBox cbSynonyms    = (CheckBox) container.getChildAt(3);
        final CheckBox cbAntonyms    = (CheckBox) container.getChildAt(4);
        final CheckBox cbTriggers    = (CheckBox) container.getChildAt(5);
        final CheckBox cbPartOf      = (CheckBox) container.getChildAt(6);
        final CheckBox cbComprises   = (CheckBox) container.getChildAt(7);
        final CheckBox cbRhymes      = (CheckBox) container.getChildAt(8);
        final CheckBox cbHomophones  = (CheckBox) container.getChildAt(9);

        bools = Main.sharedPreferences.getString("tabs", "[true, true, true, true, false, false, false, false, false, false]");
        checkBoxes = Arrays.asList(new CheckBox[] {cbReverse, cbSoundsLike, cbSpelledLike,
                cbSynonyms, cbAntonyms, cbTriggers, cbPartOf, cbComprises, cbRhymes, cbHomophones});

        loadChecks();

        cbReverse.setOnCheckedChangeListener(this);
        cbSoundsLike.setOnCheckedChangeListener(this);
        cbSpelledLike.setOnCheckedChangeListener(this);
        cbSynonyms.setOnCheckedChangeListener(this);
        cbAntonyms.setOnCheckedChangeListener(this);
        cbTriggers.setOnCheckedChangeListener(this);
        cbPartOf.setOnCheckedChangeListener(this);
        cbComprises.setOnCheckedChangeListener(this);
        cbRhymes.setOnCheckedChangeListener(this);
        cbHomophones.setOnCheckedChangeListener(this);

        (findViewById(R.id.btnOK)).setOnClickListener(view -> {
            if (getResult() == 0) {
                Toast.makeText(getContext(), R.string.select_a_option_first, Toast.LENGTH_SHORT).show();
                return;
            }
            Main.sharedPreferences.edit().putString("tabs", Arrays.toString(enabledChecks)).apply();
            fragmentLoader.loadFragments(false);
            dismiss();
        });
        (findViewById(R.id.btnCancel)).setOnClickListener(view -> dismiss());
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        enabledChecks[checkBoxes.indexOf(compoundButton)] = b;
        if (getResult() == 4) disableUnused();
        else enableAll();
    }

    private int getResult() {
        int count = 0;
        for (boolean var : enabledChecks) count += var ? 1 : 0;
        return count;
    }

    private void disableUnused() {
        for (int i = 0; i < enabledChecks.length; i++)
            checkBoxes.get(i).setEnabled(enabledChecks[i]);
    }

    private void enableAll() {
        for (int i = 0; i < enabledChecks.length; i++)
            checkBoxes.get(i).setEnabled(true);
    }

    private void loadChecks() {
        bools = bools.substring(1, bools.length() - 1);
        final String[] boolsArray = bools.split(", ");

        int count = 0;
        for (int i = 0; i < boolsArray.length; i++) {
            boolean parsed = Boolean.parseBoolean(boolsArray[i]);
            checkBoxes.get(i).setChecked(parsed);
            enabledChecks[i] = parsed;
            if (parsed) ++count;
        }
        if (count == 4) disableUnused();
        else enableAll();
    }
}