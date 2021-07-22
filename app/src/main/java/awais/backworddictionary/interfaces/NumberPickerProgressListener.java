package awais.backworddictionary.interfaces;

import awais.sephiroth.numberpicker.HorizontalNumberPicker;

public interface NumberPickerProgressListener {
    void onProgressChanged(final HorizontalNumberPicker numberPicker, final int progressValue,
                           final boolean isChangeCompleted);
}