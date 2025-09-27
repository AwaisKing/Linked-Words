package awais.clans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import awais.backworddictionary.R;

public final class Label extends MaterialTextView {
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(@NonNull final MotionEvent e) {
            onActionDown(true);
            if (fab != null) fab.onActionDown();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(@NonNull final MotionEvent e) {
            onActionUp();
            if (fab != null) fab.onActionUp();
            return super.onSingleTapUp(e);
        }
    });
    private final FloatingActionButton fab;
    private final Animation showAnimation, hideAnimation;
    private final int cornerRadius, shadowRadius, shadowYOffset;

    private Drawable backgroundDrawable;
    private int rawWidth, rawHeight;

    public Label(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, null, null, null);
    }

    public Label(@NonNull final Context context, @NonNull final FloatingActionButton fab, final Animation showAnimation, final Animation hideAnimation) {
        this(context, null, fab, showAnimation, hideAnimation);
    }

    public Label(@NonNull final Context context, @Nullable final AttributeSet attrs, @Nullable final FloatingActionButton fab,
                 final Animation showAnimation, final Animation hideAnimation) {
        super(context, attrs);

        final Resources resources = context.getResources();
        this.cornerRadius = (int) resources.getDimension(R.dimen.fab_label_corner_size);
        this.shadowRadius = (int) resources.getDimension(R.dimen.search_divider);
        this.shadowYOffset = (int) resources.getDimension(R.dimen.ttlm_default_elevation);

        this.fab = fab;
        this.showAnimation = showAnimation;
        this.hideAnimation = hideAnimation;

        if (fab != null) setOnClickListener(fab.getOnClickListener());
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        if (rawWidth == 0) rawWidth = measuredWidth;
        if (rawHeight == 0) rawHeight = measuredHeight;
        setMeasuredDimension(measuredWidth + shadowRadius, measuredHeight + shadowRadius + shadowYOffset);
    }

    void updateBackground() {
        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createRectDrawable(0xffffffff));
        drawable.addState(new int[]{}, createRectDrawable(0xfffafafa));

        final RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][]{{}}, new int[]{0x33000000}), drawable, null);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(final View view, final Outline outline) {
                // outline.setOval(0, 0, view.getWidth(), view.getHeight());
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        setClipToOutline(true);
        backgroundDrawable = ripple;

        final LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{new Shadow(), backgroundDrawable});
        layerDrawable.setLayerInset(1, shadowRadius, shadowRadius + shadowYOffset, shadowRadius, shadowRadius + shadowYOffset);

        setBackground(layerDrawable);
    }

    @NonNull
    private Drawable createRectDrawable(final int color) {
        final RoundRectShape shape = new RoundRectShape(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                                                                    cornerRadius, cornerRadius}, null, null);
        final ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    void onActionDown(final boolean isLabel) {
        if (backgroundDrawable instanceof StateListDrawable) {
            final StateListDrawable drawable = (StateListDrawable) backgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_pressed});
        } else if (backgroundDrawable instanceof RippleDrawable) {
            final int measuredWidth = getMeasuredWidth();
            final RippleDrawable ripple = (RippleDrawable) backgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(isLabel ? measuredWidth >> 1 : measuredWidth, getMeasuredHeight() >> 1);
            ripple.setVisible(true, true);
        }
    }

    void onActionUp() {
        if (backgroundDrawable instanceof RippleDrawable) {
            final RippleDrawable ripple = (RippleDrawable) backgroundDrawable;
            ripple.setHotspot(getMeasuredWidth() >> 1, getMeasuredHeight() >> 1);
            ripple.setVisible(true, true);
        }
        backgroundDrawable.setState(new int[]{});
    }

    void show() {
        if (showAnimation != null) {
            hideAnimation.cancel();
            startAnimation(showAnimation);
        }
        setVisibility(VISIBLE);
    }

    void hide() {
        if (hideAnimation != null) {
            showAnimation.cancel();
            startAnimation(hideAnimation);
        }
        setVisibility(INVISIBLE);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(final MotionEvent event) {
        if (fab != null && fab.getOnClickListener() != null && fab.isEnabled()) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                onActionUp();
                fab.onActionUp();
            }
            gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private class Shadow extends Drawable {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);

        private Shadow() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xfffafafa);

            mErase.setXfermode(PORTER_DUFF_CLEAR);

            if (!isInEditMode()) {
                mPaint.setShadowLayer(shadowRadius, 0, shadowYOffset, 0x66000000);
            }
        }

        @Override
        public void draw(@NonNull final Canvas canvas) {
            final RectF shadowRect = new RectF(shadowRadius, shadowRadius + shadowYOffset, rawWidth, rawHeight);
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, mPaint);
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, mErase);
        }

        @Override
        public void setAlpha(final int alpha) {}

        @Override
        public void setColorFilter(@Nullable final ColorFilter colorFilter) {}

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}