package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.databinding.AdvancedDialogBinding;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.interfaces.FragmentLoader;

public final class AdvancedDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {
    private final boolean[] enabledChecks = {true, true, true, true, false, false, false, false, false, false};
    private final AdvancedDialogBinding dialogBinding;
    private final FragmentLoader fragmentLoader;
    private List<CompoundButton> checkBoxes;

    public AdvancedDialog(final Main act) {
        super(act, R.style.DefinitionsDialogTheme);
        this.dialogBinding = AdvancedDialogBinding.inflate(act.getLayoutInflater());
        this.fragmentLoader = act;
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

        setContentView(dialogBinding.getRoot());

        dialogBinding.alertTitle.setText(R.string.advance);

        final CheckBox cbReverse = (CheckBox) dialogBinding.checkboxContainer.getChildAt(0);
        final CheckBox cbSoundsLike = (CheckBox) dialogBinding.checkboxContainer.getChildAt(1);
        final CheckBox cbSpelledLike = (CheckBox) dialogBinding.checkboxContainer.getChildAt(2);
        final CheckBox cbSynonyms = (CheckBox) dialogBinding.checkboxContainer.getChildAt(3);
        final CheckBox cbAntonyms = (CheckBox) dialogBinding.checkboxContainer.getChildAt(4);
        final CheckBox cbTriggers = (CheckBox) dialogBinding.checkboxContainer.getChildAt(5);
        final CheckBox cbPartOf = (CheckBox) dialogBinding.checkboxContainer.getChildAt(6);
        final CheckBox cbComprises = (CheckBox) dialogBinding.checkboxContainer.getChildAt(7);
        final CheckBox cbRhymes = (CheckBox) dialogBinding.checkboxContainer.getChildAt(8);
        final CheckBox cbHomophones = (CheckBox) dialogBinding.checkboxContainer.getChildAt(9);

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
            if (view == dialogBinding.btnOK) {
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
        dialogBinding.btnOK.setOnClickListener(clickListener);
        dialogBinding.btnCancel.setOnClickListener(clickListener);
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