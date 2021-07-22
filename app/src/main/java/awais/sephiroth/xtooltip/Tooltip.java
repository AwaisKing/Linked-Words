package awais.sephiroth.xtooltip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.text.HtmlCompat;

import com.google.android.material.textview.MaterialTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import awais.backworddictionary.R;

public final class Tooltip {
    private final Context context;
    private final LayoutInflater inflater;

    private final int padding;
    private final float toleranceSize;
    private final boolean mShowArrow = true;
    private final WindowManager windowManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Point anchorPoint = new Point(0, 0);
    private final TooltipTextDrawable textDrawable;
    private final Runnable hideRunnable = this::hide, activateRunnable = () -> isActivated = true;
    private final Gravity[] gravities = {Gravity.LEFT, Gravity.RIGHT, Gravity.TOP, Gravity.BOTTOM};

    private boolean hasAnchorView = false;
    private boolean isActivated = false;
    private boolean isShowing = false;
    private boolean isVisible = false;
    private View mContentView;
    private TextView mTextView;
    private CharSequence text;
    private Positions currentPosition = null;
    private TooltipFunctions tooltipFunctions;
    private WeakReference<View> anchorView = null;
    private TooltipViewContainer popupView = null;

    public Tooltip(@NonNull final Context context, final View anchorView, final CharSequence text) {
        this.context = context;
        this.text = text;

        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.toleranceSize = displayMetrics.density * 10f;
        this.padding = Math.round(displayMetrics.density * 20f);

        this.inflater = LayoutInflater.from(context);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        this.textDrawable = new TooltipTextDrawable(context);

        if (anchorView != null) {
            this.anchorView = new WeakReference<>(anchorView);
            this.hasAnchorView = true;
        }
    }

    public View getContentView() {
        return mContentView;
    }

    public void setupTooltipFunction(final TooltipFunctions tooltipFunctions) {
        this.tooltipFunctions = tooltipFunctions;
    }

    public void update(final CharSequence text) {
        this.text = text;
        if (isShowing && null != popupView) mTextView.setText(text instanceof Spannable ? text :
                HtmlCompat.fromHtml(String.valueOf(text), HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    @Nullable
    private Positions findPosition(final View parent, @Nullable final View anchor, final Point offset, final ArrayList<Gravity> gravities,
                                   final WindowManager.LayoutParams params, final boolean fitToScreen) {
        if (popupView == null || gravities.isEmpty()) return null;

        final Gravity gravity = gravities.remove(0);

        final Rect displayFrame = new Rect();
        final int[] anchorPosition = {0, 0};

        parent.getWindowVisibleDisplayFrame(displayFrame);

        if (anchor != null) {
            anchor.getLocationOnScreen(anchorPosition);
            final int width = anchor.getWidth();
            final int height = anchor.getHeight();

            if (gravity == Gravity.LEFT) {
                anchorPosition[1] += height / 2;
            } else if (gravity == Gravity.RIGHT) {
                anchorPosition[0] += width;
                anchorPosition[1] += height / 2;
            } else if (gravity == Gravity.TOP) {
                anchorPosition[0] += width / 2;
            } else if (gravity == Gravity.BOTTOM) {
                anchorPosition[0] += width / 2;
                anchorPosition[1] += height;
            } else if (gravity == Gravity.CENTER) {
                anchorPosition[0] += width / 2;
                anchorPosition[1] += height / 2;
            }
        }

        anchorPosition[0] += offset.x;
        anchorPosition[1] += offset.y;

        final int w = mContentView.getMeasuredWidth();
        final int h = mContentView.getMeasuredHeight();

        final Point contentPosition = new Point(), arrowPosition = new Point();

        if (gravity == Gravity.LEFT) {
            contentPosition.x = anchorPosition[0] - w;
            contentPosition.y = anchorPosition[1] - h / 2;
            arrowPosition.y = h / 2 - padding / 2;
        } else if (gravity == Gravity.TOP) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1] - h;
            arrowPosition.x = w / 2 - padding / 2;
        } else if (gravity == Gravity.RIGHT) {
            contentPosition.x = anchorPosition[0];
            contentPosition.y = anchorPosition[1] - h / 2;
            arrowPosition.y = h / 2 - padding / 2;
        } else if (gravity == Gravity.BOTTOM) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1];
            arrowPosition.x = w / 2 - padding / 2;
        } else if (gravity == Gravity.CENTER) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1] - h / 2;
        }

        if (fitToScreen) {
            final Rect finalRect = new Rect(contentPosition.x, contentPosition.y,
                    contentPosition.x + w, contentPosition.y + h);
            if (!displayFrame.contains(Math.round(finalRect.left + toleranceSize), Math.round(finalRect.top + toleranceSize),
                    Math.round(finalRect.right - toleranceSize), Math.round(finalRect.bottom - toleranceSize))) {
                return findPosition(parent, anchor, offset, gravities, params, true);
            }
        }

        return new Positions(new PointF(arrowPosition), new PointF(contentPosition),
                gravity, params);
    }

    public float getOffsetY() {
        return currentPosition == null ? 0f : currentPosition.mOffsetY;
    }

    private void invokePopup(final Positions positions) {
        if (positions != null) {
            isShowing = true;
            currentPosition = positions;

            if (hasAnchorView && anchorView.get() != null) setupListeners(anchorView.get());

            textDrawable.setAnchor(positions.gravity, !mShowArrow ? 0 : padding / 2,
                    !mShowArrow ? null : new PointF(positions.getArrowPointX(), positions.getArrowPointY()));

            offsetBy();

            positions.params.packageName = context.getPackageName();
            popupView.setFitsSystemWindows(true);
            windowManager.addView(popupView, positions.params);
            fadeIn();
        } else
            tooltipFunctions.doOnFailure(this);
    }

    void offsetBy() {
        if (isShowing && popupView != null && currentPosition != null) {
            mContentView.setTranslationX(currentPosition.getContentPointX());
            mContentView.setTranslationY(currentPosition.getContentPointY());
        }
    }

    public void offsetTo(final float xoff, final float yoff) {
        if (isShowing && popupView != null && currentPosition != null) {
            currentPosition.offsetTo(xoff, yoff);
            mContentView.setTranslationX(currentPosition.getContentPointX());
            mContentView.setTranslationY(currentPosition.getContentPointY());
        }
    }

    private void setupListeners(@NonNull final View anchorView) {
        anchorView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewDetachedFromWindow(final View v) {
                v.removeOnAttachStateChangeListener(this);
                dismiss();
            }

            @Override
            public void onViewAttachedToWindow(final View v) { }
        });
    }

    public void show(final View parent, final Gravity gravity, final boolean fitToScreen) {
        if (isShowing || (hasAnchorView && anchorView.get() == null)) return;

        isVisible = false;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = android.view.Gravity.START | android.view.Gravity.TOP;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | 0x00010000; // == FLAG_LAYOUT_INSET_DECOR
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.token = parent.getWindowToken();
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        params.setTitle("ToolTip:" + Integer.toHexString(hashCode()));

        popupView = new TooltipViewContainer(context);
        mContentView = inflater.inflate(R.layout.tooltip_textview, popupView, false);

        mTextView = new MaterialTextView(new ContextThemeWrapper(context, R.style.ToolTipTextStyle));
        ((ViewGroup) mContentView).addView(mTextView);

        if (textDrawable != null) mTextView.setBackground(textDrawable);
        if (mShowArrow) mTextView.setPadding(padding, padding, padding, padding);
        else mTextView.setPadding(padding / 2, padding / 2, padding / 2, padding / 2);
        mTextView.setText(text instanceof Spannable ? text :
                HtmlCompat.fromHtml(String.valueOf(text), HtmlCompat.FROM_HTML_MODE_COMPACT));

        popupView.addView(mContentView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupView.setMeasureAllChildren(true);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        mTextView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(final View v) {
                handler.removeCallbacks(activateRunnable);
                handler.postDelayed(activateRunnable, 0);
            }

            @Override
            public void onViewDetachedFromWindow(final View v) {
                if (v != null) v.removeOnAttachStateChangeListener(this);
                removeCallbacks();
            }
        });

        final ArrayList<Gravity> gravities = new ArrayList<>(0);
        Collections.addAll(gravities, this.gravities);
        gravities.remove(gravity);
        gravities.add(0, gravity);

        if (tooltipFunctions != null) tooltipFunctions.doOnPrepare(this);

        invokePopup(findPosition(parent, anchorView.get(),
                anchorPoint, gravities, params, fitToScreen));
    }

    void hide() {
        if (isShowing) fadeOut();
    }

    public void dismiss() {
        if (isShowing && popupView != null) {
            removeCallbacks();
            windowManager.removeView(popupView);
            popupView = null;
            isShowing = false;
            isVisible = false;

            if (tooltipFunctions != null) tooltipFunctions.doOnHidden(this);
        }
    }

    private void removeCallbacks() {
        handler.removeCallbacks(hideRunnable);
        handler.removeCallbacks(activateRunnable);
    }

    private void fadeIn() {
        if (!isShowing || isVisible) return;

        mTextView.clearAnimation();
        mTextView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_in_bottom));

        isVisible = true;
        if (tooltipFunctions != null) tooltipFunctions.doOnShown(this);
    }

    private void fadeOut() {
        if (!isShowing || !isVisible) return;

        final Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_out);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                isVisible = false;
                removeCallbacks();
                dismiss();
            }

            @Override
            public void onAnimationStart(final Animation animation) { }

            @Override
            public void onAnimationRepeat(final Animation animation) { }
        });
        animation.start();

        mTextView.clearAnimation();
        mTextView.startAnimation(animation);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public enum Gravity {LEFT, RIGHT, TOP, BOTTOM, CENTER,}

    private final class TooltipViewContainer extends FrameLayout {
        private TooltipViewContainer(final Context context) {
            super(context);
            setClipChildren(false);
            setClipToPadding(false);
        }

        @Override
        protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
            super.onLayout(changed, left, top, right, bottom);

            if (changed) {
                final int[] out = {-1, -1};
                getLocationOnScreen(out);
                offsetTopAndBottom(-out[1]);
            }
        }

        @Override
        public boolean dispatchKeyEvent(final KeyEvent event) {
            if (isShowing && isVisible && isActivated && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                final KeyEvent.DispatcherState keyDispatcherState = getKeyDispatcherState();

                if (keyDispatcherState == null) return super.dispatchKeyEvent(event);

                final int action = event.getAction();
                if (action == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    keyDispatcherState.startTracking(event, this);
                    return true;
                }
                if (action == KeyEvent.ACTION_UP) {
                    if (keyDispatcherState.isTracking(event) && !event.isCanceled()) {
                        hide();
                        return true;
                    }
                }
            }
            return super.dispatchKeyEvent(event);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(final MotionEvent event) {
            if (!isShowing || !isVisible || !isActivated) return false;
            hide();
            return false;
        }
    }

    private static final class Positions {
        private final Gravity gravity;
        private final PointF arrowPoint, contentPoint;
        private final WindowManager.LayoutParams params;
        private float mOffsetX = 0f, mOffsetY = 0f;

        private Positions(final PointF arrowPoint, final PointF contentPoint, final Gravity gravity,
                          final WindowManager.LayoutParams params) {
            this.arrowPoint = arrowPoint;
            this.contentPoint = contentPoint;
            this.gravity = gravity;
            this.params = params;
        }

        private void offsetTo(final float x, final float y) {
            mOffsetX = x;
            mOffsetY = y;
        }

        public float getArrowPointX() {
            return arrowPoint.x + mOffsetX;
        }

        public float getArrowPointY() {
            return arrowPoint.y + mOffsetY; // - displayFrame.top
        }

        public float getContentPointX() {
            return contentPoint.x + mOffsetX;
        }

        public float getContentPointY() {
            return contentPoint.y + mOffsetY; // - displayFrame.top
        }
    }
}