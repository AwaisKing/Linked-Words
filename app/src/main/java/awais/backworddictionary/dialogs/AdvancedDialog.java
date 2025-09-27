package awais.backworddictionary.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.databinding.AdvancedDialogBinding;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.models.Tab;

public final class AdvancedDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {
    // private final boolean[] enabledChecks = {true, true, true, true, false, false, false, false, false, false};
    private final Tab[] tabs = Tab.values();
    private final List<CompoundButton> checkBoxes = new ArrayList<>(tabs.length);
    private final AdvancedDialogBinding dialogBinding;
    private final Main main;

    public AdvancedDialog(final Main act) {
        super(act, R.style.DefinitionsDialogTheme);
        this.dialogBinding = AdvancedDialogBinding.inflate(act.getLayoutInflater());
        this.main = act;
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

        final LayoutInflater layoutInflater = getLayoutInflater();
        final SettingsHelper settingsHelper = SettingsHelper.getInstance(getContext());

        final boolean[] tabBools = settingsHelper.getTabs();
        for (int i = 0; i < Math.min(tabBools.length, tabs.length); i++) {
            tabs[i] = tabs[i].setEnabled(tabBools[i]);

            CheckBox checkBox = (CheckBox) layoutInflater.inflate(R.layout.layout_checkbox, dialogBinding.checkboxContainer, false);

            checkBox.setChecked(tabs[i].isEnabled());
            checkBox.setText(tabs[i].getTabName());
            checkBox.setOnCheckedChangeListener(this);

            checkBoxes.add(checkBox);
            dialogBinding.checkboxContainer.addView(checkBox, i);
        }

        final View.OnClickListener clickListener = view -> {
            if (view == dialogBinding.btnOK) {
                if (getCheckedCount() <= 0) {
                    Toast.makeText(getContext(), R.string.select_a_option_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean[] enabledChecks = new boolean[tabs.length];
                for (int i = 0; i < tabs.length; i++) enabledChecks[i] = tabs[i].isEnabled();
                settingsHelper.setTabs(Arrays.toString(enabledChecks));
                main.loadFragments(false);
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
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCheckedChanged(@NonNull final CompoundButton compoundButton, final boolean isChecked) {
        tabs[checkBoxes.indexOf(compoundButton)].setEnabled(isChecked);
        if (dialogBinding != null) dialogBinding.btnOK.setEnabled(getCheckedCount() > 0);
    }

    private int getCheckedCount() {
        int count = 0;
        for (final var tab : tabs) count = count + (tab.isEnabled() ? 1 : 0);
        return count;
    }
}