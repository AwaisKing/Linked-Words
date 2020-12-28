package awais.sephiroth.numberpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.res.ResourcesCompat;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;
import awais.sephiroth.uigestures.UIGestureRecognizer;
import awais.sephiroth.uigestures.UIGestureRecognizerDelegate;
import awais.sephiroth.uigestures.UILongPressGestureRecognizer;
import awais.sephiroth.uigestures.UITapGestureRecognizer;
import awais.sephiroth.xtooltip.Tooltip;
import awais.sephiroth.xtooltip.TooltipFunctions;

/**
 * Thanks to sephiroth74 for his NumberPicker library written in Kotlin
 * https://github.com/sephiroth74/NumberSlidingPicker
 */
public final class HorizontalNumberPicker extends LinearLayout {
    private static final long LONG_TAP_TIMEOUT = 300L;

    private static final int[] FOCUSED_STATE_ARRAY = {android.R.attr.state_focused};
    private static final int[] UNFOCUSED_STATE_ARRAY = {0, -android.R.attr.state_focused};

    private final ExponentialTracker tracker = new ExponentialTracker(this, Utils.dpToPx(200f),
            this::setProgress);
    private final UIGestureRecognizerDelegate delegate = new UIGestureRecognizerDelegate();
    private final AppCompatImageButton btnLeft, btnRight;
    private final EditText editText;
    private int value = 1;
    private Tooltip tooltip = null;

    public HorizontalNumberPicker(final Context context) {
        this(context, null);
    }

    public HorizontalNumberPicker(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.pickerStyle);
    }

    @SuppressLint("ClickableViewAccessibility")
    public HorizontalNumberPicker(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources resources = getResources();
        final Resources.Theme theme = context.getTheme();

        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER);
        setBackground(ResourcesCompat.getDrawable(resources, R.drawable.mtrl_background_outlined, theme));

        final TypedValue outValue = new TypedValue();
        theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true);
        final int resource = outValue.resourceId != 0 ? outValue.resourceId : outValue.data;

        btnRight = new AppCompatImageButton(context);
        btnRight.setImageResource(R.drawable.arrow_right_selector);
        btnRight.setBackgroundResource(resource);

        editText = new EditText(new ContextThemeWrapper(context, R.style.NumberPicker_EditTextStyle), null, 0);
        editText.setLines(1);
        editText.setEms(4);
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);
        editText.setClickable(true);
        editText.setLongClickable(false);

        btnLeft = new AppCompatImageButton(context);
        btnLeft.setImageResource(R.drawable.arrow_left_selector);
        btnLeft.setBackgroundResource(resource);

        final LayoutParams upButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LayoutParams editTextParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        final LayoutParams dwButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upButtonParams.weight = 0.1f;
        editTextParams.weight = 1f;
        dwButtonParams.weight = 0.1f;

        final View.OnTouchListener onTouchListener = (v, event) -> isEnabled() && delegate.onTouchEvent(event);
        final View.OnClickListener onClickListener = v -> {
            final boolean isUpButton = v == btnRight;
            if (isUpButton || v == btnLeft) {
                requestFocus();
                setProgress(isUpButton ? value + 1 : value - 1);
                editText.clearFocus();
                hideKeyboard();

                if (isUpButton) btnRight.requestFocus();
                else btnLeft.requestFocus();
            }
        };

        btnLeft.setOnClickListener(onClickListener);
        btnLeft.setOnTouchListener(onTouchListener);
        btnRight.setOnTouchListener(onTouchListener);
        btnRight.setOnClickListener(onClickListener);

        addView(btnLeft, dwButtonParams);
        addView(editText, editTextParams);
        addView(btnRight, upButtonParams);

        editText.setText(String.valueOf(value));

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            setBackgroundFocused(hasFocus);
            if (!hasFocus) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    setProgress(Integer.parseInt(editText.getText().toString()));
                } else {
                    editText.setText(String.valueOf(value));
                }
            }
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            editText.clearFocus();
            return true;
        });

        final int scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        final UILongPressGestureRecognizer longGesture = new UILongPressGestureRecognizer(scaledTouchSlop);
        longGesture.setLongPressTimeout(LONG_TAP_TIMEOUT);
        longGesture.setCancelsTouchesInView(false);
        longGesture.setActionListener(recognizer -> {
            final UIGestureRecognizer.State state = recognizer.getState();
            if (state == UIGestureRecognizer.State.BEGAN) {
                requestFocus();
                editText.setSelected(false);
                editText.clearFocus();

                tracker.begin(recognizer.getDownLocationX());
                startInteraction();
            } else if (state == UIGestureRecognizer.State.ENDED) {
                tracker.end();
                endInteraction();
            } else if (state == UIGestureRecognizer.State.CHANGED) {
                float diff = recognizer.getCurrentLocationX() - recognizer.getDownLocationX();
                if (diff > tracker.minDistance) diff = tracker.minDistance;
                else if (diff < -tracker.minDistance) diff = -tracker.minDistance;

                final float final2 = (float) Math.sin((diff / tracker.minDistance) * Math.PI / 2.0);
                if (tooltip != null)
                    tooltip.offsetTo(final2 / 2 * tracker.minDistance, tooltip.getOffsetY());

                tracker.addMovement(recognizer.getCurrentLocationX());
            }
        });

        final UITapGestureRecognizer tapGesture = new UITapGestureRecognizer(scaledTouchSlop);
        tapGesture.setCancelsTouchesInView(false);
        tapGesture.setActionListener(recognizer -> {
            requestFocus();
            if (!editText.isFocused()) editText.requestFocus();
        });

        delegate.addGestureRecognizer(longGesture);
        delegate.addGestureRecognizer(tapGesture);
        delegate.setEnabled(isEnabled());
        editText.setOnTouchListener((OnTouchListener) (view, event) -> delegate.onTouchEvent(event));
    }

    public int getProgress() {
        return value;
    }

    public void setProgress(int value) {
        value = Math.max(Math.min(value, 1000), 1);
        if (value != this.value) {
            this.value = value;
            final String valueOf = String.valueOf(this.value);
            if (tooltip != null) tooltip.update(valueOf);
            if (!editText.getText().toString().equals(valueOf))
                editText.setText(valueOf);
        }
    }

    private void setBackgroundFocused(final boolean hasFocus) {
        final Drawable background = getBackground();
        if (background != null)
            background.setState(hasFocus ? FOCUSED_STATE_ARRAY : UNFOCUSED_STATE_ARRAY);
    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void startInteraction() {
        animate().alpha(0.5f).start();

        tooltip = new Tooltip(getContext(), editText, "1000");

        final TooltipFunctions tooltipFunctions = new TooltipFunctions() {
            @Override
            public void doOnPrepare(@NonNull final Tooltip tooltip) {
                final View contentView = tooltip.getContentView();
                if (contentView != null) {
                    final TextView textView = contentView.findViewById(android.R.id.text1);
                    if (textView != null) {
                        textView.measure(0, 0);
                        textView.setMinWidth(textView.getMeasuredWidth());
                    }
                }
            }

            @Override
            public void doOnShown(@NonNull final Tooltip tooltip) {
                tooltip.update(String.valueOf(value));
            }
        };
        if (tooltip != null) {
            tooltip.setupTooltipFunction(tooltipFunctions);
            tooltip.show(this, Tooltip.Gravity.TOP, false);
        }
    }

    private void endInteraction() {
        animate().alpha(1.0f).start();
        if (tooltip != null) tooltip.dismiss();
        tooltip = null;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        delegate.setEnabled(enabled);
    }
}