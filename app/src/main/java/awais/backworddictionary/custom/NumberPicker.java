package awais.backworddictionary.custom;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class NumberPicker extends android.widget.NumberPicker {
    public NumberPicker(Context context) {
        super(context);
        getTextEdit();
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        getTextEdit();
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getTextEdit();
    }

    @SuppressWarnings("unused")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getTextEdit();
    }

    private void getTextEdit() {
        for (int i = 0; i < getChildCount(); i++) {
            if (!getChildAt(i).getClass().toString().contains("EditText")) continue;
            EditText editText = (EditText) getChildAt(i);
            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence c, int i, int j, int k) {}
                @Override public void onTextChanged(CharSequence c, int i, int j, int k) {}
                @Override public void afterTextChanged(Editable editable) {
                    if (editable == null || editable.length() < 1 || editable.toString().equals("")
                            || editable.toString().equals("1")) {
                        setValue(1);
                        editText.setSelection(editText.getText().length());
                    } else
                        try { setValue(Integer.parseInt(editable.toString())); }
                        catch (Exception e) {
                            setValue(1);
                            editText.setSelection(editText.getText().length());
                        }
                }
            });
        }
    }
}
