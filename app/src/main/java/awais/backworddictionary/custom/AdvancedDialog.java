package awais.backworddictionary.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.widget.DialogTitle;

import java.util.Arrays;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.interfaces.FragmentLoader;

public class AdvancedDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {
    private final boolean[] enabledChecks = {true, true, true, true, false, false, false, false, false, false};
    private final FragmentLoader fragmentLoader;
    private List<CompoundButton> checkBoxes;

    public AdvancedDialog(final Activity act) {
        super(act, R.style.Dialog);
        this.fragmentLoader = (FragmentLoader) act;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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

        final CheckBox cbReverse = (CheckBox) container.getChildAt(0);
        final CheckBox cbSoundsLike = (CheckBox) container.getChildAt(1);
        final CheckBox cbSpelledLike = (CheckBox) container.getChildAt(2);
        final CheckBox cbSynonyms = (CheckBox) container.getChildAt(3);
        final CheckBox cbAntonyms = (CheckBox) container.getChildAt(4);
        final CheckBox cbTriggers = (CheckBox) container.getChildAt(5);
        final CheckBox cbPartOf = (CheckBox) container.getChildAt(6);
        final CheckBox cbComprises = (CheckBox) container.getChildAt(7);
        final CheckBox cbRhymes = (CheckBox) container.getChildAt(8);
        final CheckBox cbHomophones = (CheckBox) container.getChildAt(9);

        checkBoxes = Arrays.asList(new CheckBox[]{cbReverse, cbSoundsLike, cbSpelledLike,
                cbSynonyms, cbAntonyms, cbTriggers, cbPartOf, cbComprises, cbRhymes, cbHomophones});

        final boolean[] tabBools = SettingsHelper.getTabs();
        for (int i = 0; i < tabBools.length; i++) {
            final boolean bool = tabBools[i];
            checkBoxes.get(i).setChecked(bool);
            enabledChecks[i] = bool;
        }

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

        final View.OnClickListener clickListener = view -> {
            if (view.getId() == R.id.btnOK) {
                if (getResult() != 0) {
                    SettingsHelper.setTabs(Arrays.toString(enabledChecks));
                    fragmentLoader.loadFragments(false);
                } else {
                    Toast.makeText(getContext(), R.string.select_a_option_first, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            dismiss();
        };
        findViewById(R.id.btnOK).setOnClickListener(clickListener);
        findViewById(R.id.btnCancel).setOnClickListener(clickListener);
    }

    @Override
    public void show() {
        super.show();
        final Window window = getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
        enabledChecks[checkBoxes.indexOf(compoundButton)] = b;
    }

    private int getResult() {
        int count = 0;
        for (final boolean enabled : enabledChecks)
            count = count + (enabled ? 1 : 0);
        return count;
    }
}