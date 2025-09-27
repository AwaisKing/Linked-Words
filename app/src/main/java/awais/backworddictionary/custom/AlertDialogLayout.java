package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.GravityCompat;

import awais.backworddictionary.R;

/**
 * Taken from Google's AppCompat library
 * <p>
 * Special implementation of linear layout that's capable of laying out alert
 * dialog components.
 * <p>
 * A dialog consists of up to three panels. All panels are optional, and a
 * dialog may contain only a single panel. The panels are laid out according
 * to the following guidelines:
 * <ul>
 *     <li>topPanel: exactly wrap_content</li>
 *     <li>contentPanel OR customPanel: at most fill_parent, first priority for
 *         extra space</li>
 *     <li>buttonPanel: at least minHeight, at most wrap_content, second
 *         priority for extra space</li>
 * </ul>
 */
public final class AlertDialogLayout extends LinearLayoutCompat {
    public AlertDialogLayout(@NonNull final Context context) {
        super(context);
    }

    public AlertDialogLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (!tryOnMeasure(widthMeasureSpec, heightMeasureSpec))
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean tryOnMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        View topPanel = null;
        View buttonPanel = null;
        View middlePanel = null;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int id = child.getId();
                if (id == R.id.topPanel) topPanel = child;
                else if (id == R.id.buttonPanel) buttonPanel = child;
                else if (id == R.id.contentPanel || id == R.id.customPanel) {
                    if (middlePanel != null) return false;
                    middlePanel = child;
                } else return false;
            }
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);


        int childState = 0, buttonHeight = 0, middleHeight = 0,
                usedHeight = getPaddingTop() + getPaddingBottom();

        if (topPanel != null) {
            topPanel.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
            usedHeight += topPanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(childState, topPanel.getMeasuredState());
        }

        int buttonWantsHeight = 0;
        if (buttonPanel != null) {
            buttonPanel.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
            buttonHeight = resolveMinimumHeight(buttonPanel);
            buttonWantsHeight = buttonPanel.getMeasuredHeight() - buttonHeight;

            usedHeight += buttonHeight;
            childState = View.combineMeasuredStates(childState, buttonPanel.getMeasuredState());
        }

        if (middlePanel != null) {
            final int childHeightSpec = heightMode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED :
                                        MeasureSpec.makeMeasureSpec(Math.max(0, heightSize - usedHeight), heightMode);

            middlePanel.measure(widthMeasureSpec, childHeightSpec);
            middleHeight = middlePanel.getMeasuredHeight();

            usedHeight += middleHeight;
            childState = View.combineMeasuredStates(childState, middlePanel.getMeasuredState());
        }

        int remainingHeight = heightSize - usedHeight;

        // Time for the "real" button measure pass. If we have remaining space, make the button pane bigger up to its target height.
        // Otherwise, just re-measure the button at whatever height it needs.
        if (buttonPanel != null) {
            usedHeight -= buttonHeight;

            final int heightToGive = Math.min(remainingHeight, buttonWantsHeight);
            if (heightToGive > 0) {
                remainingHeight -= heightToGive;
                buttonHeight += heightToGive;
            }

            final int childHeightSpec = MeasureSpec.makeMeasureSpec(buttonHeight, MeasureSpec.EXACTLY);
            buttonPanel.measure(widthMeasureSpec, childHeightSpec);

            usedHeight += buttonPanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(childState, buttonPanel.getMeasuredState());
        }

        // If we still have remaining space, make the middle pane bigger up to the maximum height.
        if (middlePanel != null && remainingHeight > 0) {
            usedHeight -= middleHeight;
            middleHeight += remainingHeight;

            // Pass the same height mode as we're using for the dialog itself. If it's EXACTLY, then the middle pane MUST use the entire height.
            final int childHeightSpec = MeasureSpec.makeMeasureSpec(middleHeight, heightMode);
            middlePanel.measure(widthMeasureSpec, childHeightSpec);

            usedHeight += middlePanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(childState, middlePanel.getMeasuredState());
        }

        // Compute desired width as maximum child width.
        int maxWidth = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
        }

        maxWidth += getPaddingLeft() + getPaddingRight();

        final int widthSizeAndState = View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState);
        final int heightSizeAndState = View.resolveSizeAndState(usedHeight, heightMeasureSpec, 0);
        setMeasuredDimension(widthSizeAndState, heightSizeAndState);

        // If the children weren't already measured EXACTLY, we need to run another measure pass to for MATCH_PARENT widths.
        if (widthMode != MeasureSpec.EXACTLY) forceUniformWidth(count, heightMeasureSpec);

        return true;
    }

    /**
     * Remeasures child views to exactly match the layout's measured width.
     *
     * @param count             the number of child views
     * @param heightMeasureSpec the original height measure spec
     */
    private void forceUniformWidth(final int count, final int heightMeasureSpec) {
        // Pretend that the linear layout has an exact size.
        final int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child == null || child.getVisibility() == View.GONE ? null : child.getLayoutParams();
            if (lp == null || lp.width != LayoutParams.MATCH_PARENT) continue;

            // Temporarily force children to reuse their old measured height.
            final int oldHeight = lp.height;
            lp.height = child.getMeasuredHeight();
            // Remeasure with new dimensions.
            measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
            lp.height = oldHeight;
        }
    }

    /**
     * Attempts to resolve the minimum height of a view.
     * <p>
     * If the view doesn't have a minimum height set and only contains a single
     * child, attempts to resolve the minimum height of the child view.
     *
     * @param v the view whose minimum height to resolve
     * @return the minimum height
     */
    private static int resolveMinimumHeight(@NonNull final View v) {
        final int minHeight = v.getMinimumHeight();
        if (minHeight > 0) return minHeight;
        if (v instanceof final ViewGroup vg && vg.getChildCount() == 1) return resolveMinimumHeight(vg.getChildAt(0));
        return 0;
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        final int paddingLeft = getPaddingLeft();

        // Where right end of child should go
        final int width = right - left;
        final int childRight = width - getPaddingRight();

        // Space available for child
        final int childSpace = width - paddingLeft - getPaddingRight();

        final int totalLength = getMeasuredHeight();
        final int childCount = getChildCount();
        final int layoutDirection = getLayoutDirection();
        final int gravity = getGravity();
        final int majorGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int minorGravity = gravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;

        final int paddingTop = getPaddingTop();
        int childTop = switch (majorGravity) {
            // totalLength contains the padding already
            case Gravity.BOTTOM -> paddingTop + bottom - top - totalLength;

            // totalLength contains the padding already
            case Gravity.CENTER_VERTICAL -> paddingTop + (bottom - top - totalLength) / 2;

            default -> paddingTop;
        };

        final Drawable dividerDrawable = getDividerDrawable();
        final int dividerHeight = dividerDrawable == null ? 0 : dividerDrawable.getIntrinsicHeight();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) continue;

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            final MarginLayoutParams childLayoutParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

            int layoutGravity = childLayoutParams instanceof LayoutParams lp ? lp.gravity : -1;
            if (layoutGravity < 0) layoutGravity = minorGravity;

            final int absoluteGravity = GravityCompat.getAbsoluteGravity(layoutGravity, layoutDirection);
            final int childLeft = switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL -> paddingLeft + (childSpace - childWidth) / 2 + childLayoutParams.leftMargin - childLayoutParams.rightMargin;
                case Gravity.RIGHT -> childRight - childWidth - childLayoutParams.rightMargin;
                default -> paddingLeft + childLayoutParams.leftMargin;
            };

            if (hasDividerBeforeChildAt(i)) childTop += dividerHeight;

            childTop += childLayoutParams.topMargin;
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            childTop += childHeight + childLayoutParams.bottomMargin;
        }
    }
}