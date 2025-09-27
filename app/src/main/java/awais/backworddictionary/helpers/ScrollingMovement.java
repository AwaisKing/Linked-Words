package awais.backworddictionary.helpers;

import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.method.BaseMovementMethod;
import android.text.method.MetaKeyKeyListener;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

public final class ScrollingMovement extends BaseMovementMethod implements MovementMethod {
    private static ScrollingMovement sInstance;

    public static ScrollingMovement getInstance() {
        if (sInstance == null) sInstance = new ScrollingMovement();
        return sInstance;
    }

    private static int getTopLine(@NonNull final TextView widget) {
        return widget.getLayout().getLineForVertical(widget.getScrollY());
    }

    private static int getBottomLine(@NonNull final TextView widget) {
        return widget.getLayout().getLineForVertical(widget.getScrollY() + getInnerHeight(widget));
    }

    private static int getInnerWidth(@NonNull final TextView widget) {
        return widget.getWidth() - widget.getTotalPaddingLeft() - widget.getTotalPaddingRight();
    }

    private static int getInnerHeight(@NonNull final TextView widget) {
        return widget.getHeight() - widget.getTotalPaddingTop() - widget.getTotalPaddingBottom();
    }

    private static int getCharacterWidth(@NonNull final TextView widget) {
        return (int) Math.ceil(widget.getPaint().getFontSpacing());
    }

    private static int getScrollBoundsLeft(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int topLine = getTopLine(widget);
        final int bottomLine = getBottomLine(widget);
        if (topLine > bottomLine) return 0;
        int left = Integer.MAX_VALUE;
        for (int line = topLine; line <= bottomLine; line++) {
            final int lineLeft = (int) Math.floor(layout.getLineLeft(line));
            if (lineLeft < left) left = lineLeft;
        }
        return left;
    }

    private static int getScrollBoundsRight(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int topLine = getTopLine(widget);
        final int bottomLine = getBottomLine(widget);
        if (topLine > bottomLine) {
            return 0;
        }
        int right = Integer.MIN_VALUE;
        for (int line = topLine; line <= bottomLine; line++) {
            final int lineRight = (int) Math.ceil(layout.getLineRight(line));
            if (lineRight > right) {
                right = lineRight;
            }
        }
        return right;
    }

    private static boolean scrollLeft(final TextView widget) {
        final int minScrollX = getScrollBoundsLeft(widget);
        int scrollX = widget.getScrollX();
        if (scrollX > minScrollX) {
            scrollX = Math.max(scrollX - getCharacterWidth(widget), minScrollX);
            widget.scrollTo(scrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    private static boolean scrollRight(final TextView widget) {
        final int maxScrollX = getScrollBoundsRight(widget) - getInnerWidth(widget);
        int scrollX = widget.getScrollX();
        if (scrollX < maxScrollX) {
            scrollX = Math.min(scrollX + getCharacterWidth(widget), maxScrollX);
            widget.scrollTo(scrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    private static boolean scrollUp(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int top = widget.getScrollY();
        int topLine = layout.getLineForVertical(top);
        if (layout.getLineTop(topLine) == top) {
            // If the top line is partially visible, bring it all the way
            // into view; otherwise, bring the previous line into view.
            topLine -= 1;
        }
        if (topLine >= 0) {
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(topLine));
            return true;
        }
        return false;
    }

    private static boolean scrollDown(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int innerHeight = getInnerHeight(widget);
        final int bottom = widget.getScrollY() + innerHeight;
        int bottomLine = layout.getLineForVertical(bottom);
        if (layout.getLineTop(bottomLine + 1) < bottom + 1) {
            // Less than a pixel of this line is out of view,
            // so we must have tried to make it entirely in view
            // and now want the next line to be in view instead.
            bottomLine += 1;
        }
        final int limit = layout.getLineCount() - 1;
        if (bottomLine <= limit) {
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(bottomLine + 1) - innerHeight);
            return true;
        }
        return false;
    }

    private static boolean scrollPageUp(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int top = widget.getScrollY() - getInnerHeight(widget);
        final int topLine = layout.getLineForVertical(top);
        if (topLine >= 0) {
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(topLine));
            return true;
        }
        return false;
    }

    private static boolean scrollPageDown(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int innerHeight = getInnerHeight(widget);
        final int bottom = widget.getScrollY() + innerHeight + innerHeight;
        final int bottomLine = layout.getLineForVertical(bottom);
        if (bottomLine <= layout.getLineCount() - 1) {
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(bottomLine + 1) - innerHeight);
            return true;
        }
        return false;
    }

    private static boolean scrollTop(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        if (getTopLine(widget) >= 0) {
            Touch.scrollTo(widget, layout, widget.getScrollX(), layout.getLineTop(0));
            return true;
        }
        return false;
    }

    private static boolean scrollBottom(@NonNull final TextView widget) {
        final Layout layout = widget.getLayout();
        final int lineCount = layout.getLineCount();
        if (getBottomLine(widget) <= lineCount - 1) {
            Touch.scrollTo(widget, layout, widget.getScrollX(),
                    layout.getLineTop(lineCount) - getInnerHeight(widget));
            return true;
        }
        return false;
    }

    private static boolean scrollLineStart(final TextView widget) {
        final int minScrollX = getScrollBoundsLeft(widget);
        final int scrollX = widget.getScrollX();
        if (scrollX > minScrollX) {
            widget.scrollTo(minScrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    private static boolean scrollLineEnd(final TextView widget) {
        final int maxScrollX = getScrollBoundsRight(widget) - getInnerWidth(widget);
        final int scrollX = widget.getScrollX();
        if (scrollX < maxScrollX) {
            widget.scrollTo(maxScrollX, widget.getScrollY());
            return true;
        }
        return false;
    }

    @Override
    protected boolean left(final TextView widget, final Spannable buffer) {
        return scrollLeft(widget);
    }

    @Override
    protected boolean right(final TextView widget, final Spannable buffer) {
        return scrollRight(widget);
    }

    @Override
    protected boolean up(final TextView widget, final Spannable buffer) {
        return scrollUp(widget);
    }

    @Override
    protected boolean down(final TextView widget, final Spannable buffer) {
        return scrollDown(widget);
    }

    @Override
    protected boolean pageUp(final TextView widget, final Spannable buffer) {
        return scrollPageUp(widget);
    }

    @Override
    protected boolean pageDown(final TextView widget, final Spannable buffer) {
        return scrollPageDown(widget);
    }

    @Override
    protected boolean top(final TextView widget, final Spannable buffer) {
        return scrollTop(widget);
    }

    @Override
    protected boolean bottom(final TextView widget, final Spannable buffer) {
        return scrollBottom(widget);
    }

    @Override
    protected boolean lineStart(final TextView widget, final Spannable buffer) {
        return scrollLineStart(widget);
    }

    @Override
    protected boolean lineEnd(final TextView widget, final Spannable buffer) {
        return scrollLineEnd(widget);
    }

    @Override
    protected boolean home(final TextView widget, final Spannable buffer) {
        return top(widget, buffer);
    }

    @Override
    protected boolean end(final TextView widget, final Spannable buffer) {
        return bottom(widget, buffer);
    }

    @Override
    public void onTakeFocus(@NonNull final TextView widget, final Spannable text, final int dir) {
        final Layout layout = widget.getLayout();

        if (layout != null && (dir & View.FOCUS_FORWARD) != 0) {
            widget.scrollTo(widget.getScrollX(),
                    layout.getLineTop(0));
        }
        if (layout != null && (dir & View.FOCUS_BACKWARD) != 0) {
            final int padding = widget.getTotalPaddingTop() +
                    widget.getTotalPaddingBottom();
            final int line = layout.getLineCount() - 1;

            widget.scrollTo(widget.getScrollX(),
                    layout.getLineTop(line + 1) -
                            (widget.getHeight() - padding));
        }
    }

    @Override
    public boolean onTouchEvent(final TextView widget, @NonNull final Spannable buffer, @NonNull final MotionEvent event) {
        final DragState[] ds = buffer.getSpans(0, buffer.length(), DragState.class);

        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            for (final DragState dragState : ds) buffer.removeSpan(dragState);

            buffer.setSpan(new DragState(event.getX()),
                    0, 0, Spannable.SPAN_MARK_MARK);
            return true;
        } else if (actionMasked == MotionEvent.ACTION_UP) {
            for (final DragState d : ds) buffer.removeSpan(d);
            disallowIntercept(widget, false);
            final boolean isUsed = ds.length > 0 && ds[0].mUsed;
            if (!isUsed) passTouch(widget);
            return isUsed;
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            if (ds.length > 0) {
                if (!ds[0].mFarEnough) {
                    final int slop = ViewConfiguration.get(widget.getContext()).getScaledTouchSlop();
                    ds[0].mFarEnough = Math.abs(event.getX() - ds[0].mX) >= slop;
                }

                if (ds[0].mFarEnough) {
                    ds[0].mUsed = true;
                    final boolean cap = (event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0
                            || MetaKeyKeyListener.getMetaState(buffer, MetaKeyKeyListener.META_SHIFT_ON) == 1
                            || MetaKeyKeyListener.getMetaState(buffer, 0x800) != 0;

                    final float dx = cap ? event.getX() - ds[0].mX : ds[0].mX - event.getX();
                    ds[0].mX = event.getX();

                    final int newX = widget.getScrollX() + (int) dx;
                    final int oldX = widget.getScrollX();
                    final Layout layout = widget.getLayout();

                    Touch.scrollTo(widget, layout, newX, 0);

                    // If we actually scrolled, then cancel the up action.
                    if (oldX != widget.getScrollX()) {
                        disallowIntercept(widget, true);
                        widget.cancelLongPress();
                    }

                    return false;
                }
            }
        }

        return true; // was false
    }

    private static void disallowIntercept(@NonNull final TextView widget, final boolean disallow) {
        ViewParent parent = widget.getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    private static void passTouch(@NonNull final TextView widget) {
        ViewParent parent = widget.getParent();
        while (parent != null) {
            if (parent instanceof LinearLayoutCompat) {
                final Drawable background = ((LinearLayoutCompat) parent).getBackground();
                if (background != null) {
                    ((LinearLayoutCompat) parent).performClick();
                    background.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
                    widget.postDelayed(() -> background.setState(new int[]{}), 235);
                    break;
                }
            }
            parent = parent.getParent();
        }
    }

    private static class DragState implements NoCopySpan {
        public float mX;
        public boolean mFarEnough, mUsed;

        public DragState(final float x) {
            mX = x;
        }
    }
}