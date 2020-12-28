package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import awais.backworddictionary.R;

/**
 * Taken from Google AppCompat library
 *
 * Used by dialogs to change the font size and number of lines to try to fit
 * the text to the available space.
 */
public final class AlertDialogTitle extends MaterialTextView {
    public AlertDialogTitle(@NonNull final Context context) {
        super(context);
    }

    public AlertDialogTitle(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public AlertDialogTitle(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (layout != null) {
            final int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                final int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    setSingleLine(false);
                    setMaxLines(2);

                    @SuppressLint("CustomViewStyleable") final TypedArray a = getContext().obtainStyledAttributes(null,
                            R.styleable.TextAppearance, android.R.attr.textAppearanceMedium,
                            android.R.style.TextAppearance_Medium);
                    final int textSize = a.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
                    if (textSize != 0) setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    a.recycle();

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }
}