package awais.sephiroth.xtooltip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.text.Spannable;
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
import awais.backworddictionary.helpers.Utils;

public final class Tooltip {
    private final Context context;

    private final int mPadding = Utils.dpToPx(20f);
    private final float mSizeTolerance;
    private final boolean mShowArrow = true;
    private final WindowManager windowManager;
    private final Handler mHandler = new Handler();
    private final Point mAnchorPoint = new Point(0, 0);
    private final TooltipTextDrawable mDrawable;
    private final Runnable hideRunnable = this::hide, activateRunnable = () -> mActivated = true;
    private final Gravity[] mGravities = {Gravity.LEFT, Gravity.RIGHT, Gravity.TOP, Gravity.BOTTOM};

    private boolean mActivated = false;
    private boolean mHasAnchorView = false;
    private boolean isShowing = false;
    private boolean isVisible = false;
    private View mContentView;
    private TextView mTextView;
    private CharSequence mText;
    private Positions mCurrentPosition = null;
    private TooltipFunctions tooltipFunctions;
    private WeakReference<View> mAnchorView = null;
    private TooltipViewContainer mPopupView = null;

    public Tooltip(@NonNull final Context context, final View anchorView, final CharSequence text) {
        this.context = context;

        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mSizeTolerance = context.getResources().getDisplayMetrics().density * 10f;

        this.mText = text;
        this.mDrawable = new TooltipTextDrawable(context);

        if (anchorView != null) {
            this.mAnchorView = new WeakReference<>(anchorView);
            this.mHasAnchorView = true;
        }
    }

    public View getContentView() {
        return mContentView;
    }

    public void setupTooltipFunction(final TooltipFunctions tooltipFunctions) {
        this.tooltipFunctions = tooltipFunctions;
    }

    public void update(final CharSequence text) {
        mText = text;
        if (isShowing && null != mPopupView) mTextView.setText(text instanceof Spannable ? text :
                HtmlCompat.fromHtml(String.valueOf(text), HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    @NonNull
    private WindowManager.LayoutParams createPopupLayoutParams(final IBinder token) {
        final WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.gravity = android.view.Gravity.START | android.view.Gravity.TOP;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.MATCH_PARENT;
        p.format = PixelFormat.TRANSLUCENT;
        p.flags = p.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.token = token;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        p.setTitle("ToolTip:" + Integer.toHexString(hashCode()));
        return p;
    }

    private void preparePopup() {
        final TooltipViewContainer viewContainer = new TooltipViewContainer(context);

        final View contentView = LayoutInflater.from(context)
                .inflate(R.layout.tooltip_textview, viewContainer, false);

        mTextView = new MaterialTextView(new ContextThemeWrapper(context, R.style.ToolTipTextStyle));
        ((ViewGroup) contentView).addView(mTextView);

        if (mDrawable != null) mTextView.setBackground(mDrawable);
        if (mShowArrow) mTextView.setPadding(mPadding, mPadding, mPadding, mPadding);
        else mTextView.setPadding(mPadding / 2, mPadding / 2, mPadding / 2, mPadding / 2);
        mTextView.setText(mText instanceof Spannable ? mText :
                HtmlCompat.fromHtml(String.valueOf(mText), HtmlCompat.FROM_HTML_MODE_COMPACT));

        viewContainer.addView(contentView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewContainer.setMeasureAllChildren(true);
        viewContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        mTextView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(final View v) {
                mHandler.removeCallbacks(activateRunnable);
                mHandler.postDelayed(activateRunnable, 0);
            }

            @Override
            public void onViewDetachedFromWindow(final View v) {
                if (v != null) v.removeOnAttachStateChangeListener(this);
                removeCallbacks();
            }
        });

        mContentView = contentView;
        mPopupView = viewContainer;
    }

    @Nullable
    private Positions findPosition(final View parent, @Nullable final View anchor, final Point offset, final ArrayList<Gravity> gravities,
                                   final WindowManager.LayoutParams params, final boolean fitToScreen) {
        if (mPopupView == null || gravities.isEmpty()) return null;

        final Gravity gravity = gravities.remove(0);

        final Rect displayFrame = new Rect();
        final int[] anchorPosition = {0, 0};

        parent.getWindowVisibleDisplayFrame(displayFrame);

        if (anchor != null) {
            anchor.getLocationOnScreen(anchorPosition);

            if (gravity == Gravity.LEFT) {
                anchorPosition[1] += anchor.getHeight() / 2;
            } else if (gravity == Gravity.RIGHT) {
                anchorPosition[0] += anchor.getWidth();
                anchorPosition[1] += anchor.getHeight() / 2;
            } else if (gravity == Gravity.TOP) {
                anchorPosition[0] += anchor.getWidth() / 2;
            } else if (gravity == Gravity.BOTTOM) {
                anchorPosition[0] += anchor.getWidth() / 2;
                anchorPosition[1] += anchor.getHeight();
            } else if (gravity == Gravity.CENTER) {
                anchorPosition[0] += anchor.getWidth() / 2;
                anchorPosition[1] += anchor.getHeight() / 2;
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
            arrowPosition.y = h / 2 - mPadding / 2;
        } else if (gravity == Gravity.TOP) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1] - h;
            arrowPosition.x = w / 2 - mPadding / 2;
        } else if (gravity == Gravity.RIGHT) {
            contentPosition.x = anchorPosition[0];
            contentPosition.y = anchorPosition[1] - h / 2;
            arrowPosition.y = h / 2 - mPadding / 2;
        } else if (gravity == Gravity.BOTTOM) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1];
            arrowPosition.x = w / 2 - mPadding / 2;
        } else if (gravity == Gravity.CENTER) {
            contentPosition.x = anchorPosition[0] - w / 2;
            contentPosition.y = anchorPosition[1] - h / 2;
        }

        if (fitToScreen) {
            final Rect finalRect = new Rect(contentPosition.x, contentPosition.y,
                    contentPosition.x + w, contentPosition.y + h);
            if (!displayFrame.contains(Math.round(finalRect.left + mSizeTolerance), Math.round(finalRect.top + mSizeTolerance),
                    Math.round(finalRect.right - mSizeTolerance), Math.round(finalRect.bottom - mSizeTolerance))) {
                return findPosition(parent, anchor, offset, gravities, params, true);
            }
        }

        return new Positions(new PointF(arrowPosition), new PointF(contentPosition),
                gravity, params);
    }

    public float getOffsetY() {
        return mCurrentPosition == null ? 0f : mCurrentPosition.mOffsetY;
    }

    private void invokePopup(final Positions positions) {
        if (positions != null) {
            isShowing = true;
            mCurrentPosition = positions;

            if (mHasAnchorView && mAnchorView.get() != null) setupListeners(mAnchorView.get());

            mDrawable.setAnchor(positions.gravity, !mShowArrow ? 0 : mPadding / 2,
                    !mShowArrow ? null : new PointF(positions.getArrowPointX(), positions.getArrowPointY()));

            offsetBy();

            positions.params.packageName = context.getPackageName();
            mPopupView.setFitsSystemWindows(true);
            windowManager.addView(mPopupView, positions.params);
            fadeIn();
        } else
            tooltipFunctions.doOnFailure(this);
    }

    void offsetBy() {
        if (isShowing && mPopupView != null && mCurrentPosition != null) {
            mContentView.setTranslationX(mCurrentPosition.getContentPointX());
            mContentView.setTranslationY(mCurrentPosition.getContentPointY());
        }
    }

    public void offsetTo(final float xoff, final float yoff) {
        if (isShowing && mPopupView != null && mCurrentPosition != null) {
            mCurrentPosition.offsetTo(xoff, yoff);
            mContentView.setTranslationX(mCurrentPosition.getContentPointX());
            mContentView.setTranslationY(mCurrentPosition.getContentPointY());
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
        if (isShowing || (mHasAnchorView && mAnchorView.get() == null)) return;

        isVisible = false;

        WindowManager.LayoutParams params = createPopupLayoutParams(parent.getWindowToken());
        preparePopup();

        final ArrayList<Gravity> gravities = new ArrayList<>();
        Collections.addAll(gravities, mGravities);
        gravities.remove(gravity);
        gravities.add(0, gravity);

        if (tooltipFunctions != null) tooltipFunctions.doOnPrepare(this);

        invokePopup(findPosition(parent, mAnchorView.get(),
                mAnchorPoint, gravities, params, fitToScreen));
    }

    void hide() {
        if (isShowing) fadeOut();
    }

    public void dismiss() {
        if (isShowing && mPopupView != null) {
            removeCallbacks();
            windowManager.removeView(mPopupView);
            mPopupView = null;
            isShowing = false;
            isVisible = false;

            if (tooltipFunctions != null) tooltipFunctions.doOnHidden(this);
        }
    }

    private void removeCallbacks() {
        mHandler.removeCallbacks(hideRunnable);
        mHandler.removeCallbacks(activateRunnable);
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
            if (isShowing && isVisible && mActivated && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
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
            if (!isShowing || !isVisible || !mActivated) return false;
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