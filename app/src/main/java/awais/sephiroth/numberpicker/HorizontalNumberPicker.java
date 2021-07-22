package awais.sephiroth.numberpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.NumberPickerProgressListener;
import awais.sephiroth.uigestures.UIGestureRecognizer.State;
import awais.sephiroth.uigestures.UIGestureRecognizerDelegate;
import awais.sephiroth.uigestures.UILongPressGestureRecognizer;
import awais.sephiroth.uigestures.UITapGestureRecognizer;
import awais.sephiroth.xtooltip.Tooltip;
import awais.sephiroth.xtooltip.TooltipFunctions;

/**
 * Thanks to sephiroth74 for his NumberPicker library written in Kotlin
 * https://github.com/sephiroth74/NumberSlidingPicker
 */
public final class HorizontalNumberPicker extends LinearLayoutCompat implements TextWatcher {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int DEFAULT_MAX_VALUE = 1000;
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final long LONG_TAP_TIMEOUT = 300L;

    private static final int[] FOCUSED_STATE_ARRAY = {android.R.attr.state_focused};
    private static final int[] UNFOCUSED_STATE_ARRAY = {0, -android.R.attr.state_focused};

    private final ExponentialTracker tracker;
    private final UIGestureRecognizerDelegate delegate = new UIGestureRecognizerDelegate();
    private final AppCompatImageButton btnLeft, btnRight;
    private final EditText editText;
    private int minValue = DEFAULT_MIN_VALUE, maxValue = DEFAULT_MAX_VALUE, value = 1;
    private boolean requestedDisallowIntercept = false;
    private NumberPickerProgressListener progressListener;
    private Tooltip tooltip = null;

    public HorizontalNumberPicker(final Context context) {
        this(context, null);
    }

    public HorizontalNumberPicker(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.pickerStyle);
    }

    @SuppressLint("ClickableViewAccessibility")
    public HorizontalNumberPicker(Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context = new ContextThemeWrapper(context, R.style.AppTheme),
                attrs, defStyleAttr);

        final Resources.Theme theme = context.getTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setNestedScrollingEnabled(false);
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        if (isInEditMode()) setBackgroundResource(R.drawable.mtrl_background_outlined);
        else setBackground(Utils.getNumberPickerBackground(context));

        final TypedValue outValue = new TypedValue();
        theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true);
        final int resource = outValue.resourceId != 0 ? outValue.resourceId : outValue.data;

        btnRight = new AppCompatImageButton(context);
        btnRight.setImageResource(R.drawable.arrow_right_selector);
        btnRight.setBackgroundResource(resource);

        editText = new EditText(new ContextThemeWrapper(context, isEnabled() ? R.style.NumberPicker_EditTextStyle
                : R.style.NumberPicker_EditTextStyle_Disabled), null, 0);
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

        final OnClickListener onClickListener = v -> {
            final boolean isUpButton = v == btnRight;
            if (isUpButton || v == btnLeft) {
                requestFocus();
                setProgress(isUpButton ? value + 1 : value - 1);
                // editText.clearFocus();
                if (isUpButton) btnRight.requestFocus();
                else btnLeft.requestFocus();
            }
        };
        btnLeft.setOnClickListener(onClickListener);
        btnRight.setOnClickListener(onClickListener);

        addView(btnLeft, dwButtonParams);
        addView(editText, editTextParams);
        addView(btnRight, upButtonParams);

        editText.setText(String.valueOf(value));
        editText.removeTextChangedListener(this);
        editText.addTextChangedListener(this);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            setBackgroundFocused(hasFocus);
            if (!hasFocus) refreshTextProgress();
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            if (progressListener != null)
                progressListener.onProgressChanged(this, this.value, true);
            editText.clearFocus();
            hideKeyboard();
            return true;
        });

        final int trackerMaxDistance = (int) context.getResources().getDimension(R.dimen.gestures_tracker_max_dist);
        tracker = new ExponentialTracker(this, trackerMaxDistance, value -> {
            setProgress(value);
            if (editText.isFocused()) editText.selectAll();
        });

        final int scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final UITapGestureRecognizer tapGesture = new UITapGestureRecognizer(scaledTouchSlop);
        final UILongPressGestureRecognizer longGesture = new UILongPressGestureRecognizer(scaledTouchSlop);

        tapGesture.setCancelsTouchesInView(true);
        longGesture.setCancelsTouchesInView(true);
        longGesture.setLongPressTimeout(LONG_TAP_TIMEOUT);

        tapGesture.setActionListener(recognizer -> {
            requestFocus();
            if (!editText.isFocused()) editText.requestFocus();
        });
        longGesture.setActionListener(recognizer -> {
            final State state = recognizer.getState();

            if (state == State.BEGAN) {
                disallowIntercept(true);

                // todo enable? i think it works better without these
                // requestFocus();
                // editText.setSelected(false);
                // editText.clearFocus();

                tracker.begin(recognizer.getDownLocationX());

                startInteraction();
            } else if (state == State.CHANGED) {
                disallowIntercept(true);

                float diff = recognizer.getCurrentLocationX() - recognizer.getDownLocationX();
                if (diff > tracker.minDistance) diff = tracker.minDistance;
                else if (diff < -tracker.minDistance) diff = -tracker.minDistance;

                final float final2 = (float) Math.sin((diff / tracker.minDistance) * Math.PI / 2.0);
                if (tooltip != null)
                    tooltip.offsetTo(final2 / 2 * tracker.minDistance, tooltip.getOffsetY());

                tracker.addMovement(recognizer.getCurrentLocationX());
            } else if (state == State.ENDED) {
                disallowIntercept(false);

                tracker.end();
                endInteraction();
            }
        });

        delegate.addGestureRecognizer(tapGesture);
        delegate.addGestureRecognizer(longGesture);
        setEnabled(isEnabled());

        editText.setOnTouchListener((view, event) -> delegate.onTouchEvent(event, true));
    }

    private void disallowIntercept(final boolean disallow) {
        requestedDisallowIntercept = disallow;

        requestDisallowInterceptTouchEvent(disallow);
        super.requestDisallowInterceptTouchEvent(disallow);

        ViewParent parent = getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent instanceof View)
                ((View) parent).setNestedScrollingEnabled(!disallow);
            else if (parent instanceof NestedScrollView)
                ((NestedScrollView) parent).setNestedScrollingEnabled(!disallow);
            else if (parent instanceof RecyclerView)
                ((RecyclerView) parent).setNestedScrollingEnabled(!disallow);

            parent = parent.getParent();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return requestedDisallowIntercept || super.onInterceptTouchEvent(ev);
    }

    public void setProgressListener(final NumberPickerProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    private void refreshTextProgress() {
        try {
            final CharSequence text = editText.getText();
            if (!Utils.isEmpty(text)) {
                setProgress(Integer.parseInt(text.toString()));
                if (progressListener != null)
                    progressListener.onProgressChanged(this, value, true);
                return;
            }
        } catch (final Exception e) {
            // ignore
        }
        editText.setText(String.valueOf(value));
        if (progressListener != null)
            progressListener.onProgressChanged(this, value, true);
    }

    public void setMaxValue(final int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(final int minValue) {
        this.minValue = minValue;
    }

    public int getProgress() {
        return value;
    }

    public void setProgress(int value) {
        value = Math.max(Math.min(value, maxValue), minValue);
        if (value != this.value) {
            this.value = value;
            final String valueOf = String.valueOf(this.value);
            if (tooltip != null) tooltip.update(valueOf);
            if (!editText.getText().toString().equals(valueOf))
                editText.setText(valueOf);
            if (progressListener != null)
                progressListener.onProgressChanged(this, this.value, false);
        }
    }

    private void setBackgroundFocused(final boolean hasFocus) {
        final Drawable background = getBackground();
        if (background != null)
            background.setState(hasFocus ? FOCUSED_STATE_ARRAY : UNFOCUSED_STATE_ARRAY);
    }

    private void hideKeyboard() {
        if (Utils.inputMethodManager != null)
            Utils.inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
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
        if (progressListener != null)
            progressListener.onProgressChanged(this, this.value, true);
        animate().alpha(1.0f).start();
        if (tooltip != null) tooltip.dismiss();
        tooltip = null;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        delegate.setEnabled(enabled);
        editText.setEnabled(enabled);
        btnLeft.setEnabled(enabled);
        btnRight.setEnabled(enabled);
    }

    @Override
    public void onTextChanged(final CharSequence text, final int start, final int before, final int count) {
        if (text != null && text.length() > 0) refreshTextProgress();
    }

    @Override
    public void afterTextChanged(final Editable s) { }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) { }
}