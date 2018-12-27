package awais.backworddictionary.custom;

import android.content.Context;
import android.util.AttributeSet;

@SuppressWarnings("unused")
public class NumberPicker extends android.widget.NumberPicker {
    public NumberPicker(Context context) {
        super(context);
//        getTextEdit();
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
//        getTextEdit();
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        getTextEdit();
    }

    //    private void getTextEdit() {
//        setMinValue(1);
//        setMaxValue();
//        for (int i = 0; i < getChildCount(); i++) {
//            View v = getChildAt(i);
//            if (!v.getClass().toString().contains("EditText")) continue;
//            EditText editText = (EditText) v;
//            editText.addTextChangedListener(new TextWatcher() {
//                @Override public void beforeTextChanged(CharSequence c, int i, int j, int k) {}
//                @Override public void onTextChanged(CharSequence c, int i, int j, int k) {}
//                @Override public void afterTextChanged(Editable editable) {
//                    if (editable == null) return;
//                    if (editable.length() < 1 || editable.toString().equals("") || editable.toString().equals("1")) {
//                        setValue(1);
//                        editText.setSelection(editable.length());
//                    } else
//                        try { setValue(Integer.parseInt(editable.toString())); }
//                        catch (Exception e) {
//                            setValue(1);
//                            editText.setSelection(editText.getText().length());
//                        }
//                }
//            });
//            break;
//        }
//    }
}
