package awais.clans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.res.ResourcesCompat;

import java.lang.ref.WeakReference;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

public final class FloatingActionButton extends AppCompatImageButton implements View.OnClickListener {
    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;
    static final int shadowRadius = Utils.dpToPx(1), shadowYOffset = Utils.dpToPx(2), iconSize = Utils.dpToPx(24f);
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static OnMenuToggleListener menuToggleListener;
    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(final MotionEvent e) {
            final Label label = (Label) getTag(R.id.fab_label);
            if (label != null) label.onActionDown(false);
            onActionDown();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            final Label label = (Label) getTag(R.id.fab_label);
            if (label != null) label.onActionUp();
            onActionUp();
            return super.onSingleTapUp(e);
        }
    });
    private final Resources resources;
    private String labelText;
    private Drawable iconDrawable;
    private OnClickListener clickListener;
    private StateListDrawable backgroundDrawable;
    private Animation showAnimation, hideAnimation;
    private static WeakReference<FloatingActionMenu> floatingActionMenu;
    private int colorNormal, colorPressed;
    private float originalX = -1, originalY = -1;
    private boolean shouldUpdateButtonPosition, buttonPositionSaved;
    int fabSize = SIZE_MINI;

    public FloatingActionButton(@NonNull final Context context) {
        this(context, null);
    }

    public FloatingActionButton(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        resources = context.getResources();

        if (attrs != null) {
            for (int i = 0; i < attrs.getAttributeCount(); ++i) {
                if ("text".equals(attrs.getAttributeName(i))) {
                    final int strValue = attrs.getAttributeResourceValue(i, 0);
                    if (strValue != 0) labelText = resources.getString(strValue);
                    break;
                }
            }
        }

        colorNormal = ResourcesCompat.getColor(resources, R.color.mini_fab_color, null);
        colorPressed = ResourcesCompat.getColor(resources, R.color.fab_label_ripple_color, null);

        showAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up);
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down);

        final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                final boolean isShown = animation == showAnimation;
                if (floatingActionMenu != null && floatingActionMenu.get() != null) floatingActionMenu.get().setOpened(isShown);
                if (menuToggleListener != null) menuToggleListener.onMenuToggle(isShown);
            }

            @Override
            public void onAnimationStart(final Animation animation) { }

            @Override
            public void onAnimationRepeat(final Animation animation) { }
        };
        showAnimation.setAnimationListener(animationListener);
        hideAnimation.setAnimationListener(animationListener);

        // updateBackground();
        setClickable(true);
    }

    @SuppressLint("PrivateResource")
    private int getCircleSize() {
        return fabSize == SIZE_NORMAL ? Utils.dpToPx(64) : resources.getDimensionPixelSize(R.dimen.design_fab_size_mini);
    }

    private int calculateMeasuredWidth() {
        return getCircleSize() + shadowRadius * 2;
    }

    private int calculateMeasuredHeight() {
        return getCircleSize() + shadowRadius * 2 + shadowYOffset * 2;
    }

    private float calculateCenterX() {
        return (float) (getMeasuredWidth() / 2);
    }

    private float calculateCenterY() {
        return (float) (getMeasuredHeight() / 2);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        saveButtonOriginalPosition();

        if (shouldUpdateButtonPosition) {
            setX(originalX);
            setY(originalY);
            shouldUpdateButtonPosition = false;
        }

        super.onSizeChanged(w, h, oldw, oldh);
        updateBackground();
    }

    void updateBackground() {
        final Drawable iconDrawable = getIconDrawable();
        if (fabSize == SIZE_MINI) iconDrawable.setColorFilter(new PorterDuffColorFilter(0xFF2196F3, PorterDuff.Mode.SRC_ATOP));

        final LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                new Shadow(),
                createFillDrawable(),
                iconDrawable
        });

        int iconSize = -1;
        if (iconDrawable != null) iconSize = Math.max(iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());
        final int iconOffset = (getCircleSize() - (iconSize > 0 ? iconSize : FloatingActionButton.iconSize)) / 2;
        final int circleInsetVertical = shadowRadius + shadowYOffset;

        layerDrawable.setLayerInset(2, shadowRadius + iconOffset, circleInsetVertical + iconOffset,
                shadowRadius + iconOffset, circleInsetVertical + iconOffset);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) setBackground(layerDrawable);
        else setBackgroundDrawable(layerDrawable);
    }

    protected Drawable getIconDrawable() {
        return iconDrawable != null ? iconDrawable : new ColorDrawable(Color.TRANSPARENT);
    }

    @NonNull
    private Drawable createFillDrawable() {
        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createCircleDrawable(0xFFAAAAAA));
        drawable.addState(new int[]{android.R.attr.state_pressed}, createCircleDrawable(colorPressed));
        drawable.addState(new int[]{}, createCircleDrawable(colorNormal));
        backgroundDrawable = drawable;
        return backgroundDrawable;
    }

    @NonNull
    private Drawable createCircleDrawable(final int color) {
        final CircleDrawable shapeDrawable = new CircleDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private void saveButtonOriginalPosition() {
        if (!buttonPositionSaved) {
            if (originalX == -1) originalX = getX();
            if (originalY == -1) originalY = getY();
            buttonPositionSaved = true;
        }
    }

    void playShowAnimation() {
        hideAnimation.cancel();
        startAnimation(showAnimation);
    }

    void playHideAnimation() {
        showAnimation.cancel();
        startAnimation(hideAnimation);
    }

    OnClickListener getOnClickListener() {
        return clickListener;
    }

    Label getLabelView() {
        return (Label) getTag(R.id.fab_label);
    }

    void setColors(final int colorNormal, final int colorPressed) {
        if (this.colorNormal != colorNormal) this.colorNormal = colorNormal;
        if (this.colorPressed != colorPressed) this.colorPressed = colorPressed;
    }

    void onActionDown() {
        if (backgroundDrawable != null)
            backgroundDrawable.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
    }

    void onActionUp() {
        if (backgroundDrawable != null) backgroundDrawable.setState(new int[]{android.R.attr.state_enabled});
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (clickListener != null && isEnabled()) {
            final Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                final int action = event.getAction();
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    label.onActionUp();
                    onActionUp();
                }
                gestureDetector.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        if (iconDrawable != drawable) {
            iconDrawable = drawable;
            updateBackground();
        }
    }

    @Override
    public void setImageResource(final int resId) {
        final Drawable drawable = ResourcesCompat.getDrawable(resources, resId, null);
        if (iconDrawable != drawable) {
            iconDrawable = drawable;
            updateBackground();
        }
    }

    @Override
    public Drawable getDrawable() {
        return iconDrawable;
    }

    @Override
    public void onClick(final View v) {
        if (clickListener != null) clickListener.onClick(this);
    }

    @Override
    public void setOnClickListener(final OnClickListener clickListener) {
        super.setOnClickListener(clickListener);
        this.clickListener = clickListener;
        final View label = (View) getTag(R.id.fab_label);
        if (label != null) label.setOnClickListener(this);
    }

    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }

    public void show(final boolean animate) {
        if (isHidden()) {
            if (animate) playShowAnimation();
            super.setVisibility(VISIBLE);
        }
    }

    public void hide(final boolean animate) {
        if (!isHidden()) {
            if (animate) playHideAnimation();
            super.setVisibility(INVISIBLE);
        }
    }

    public void setLabelText(final String text) {
        labelText = text;
        final TextView labelView = getLabelView();
        if (labelView != null) labelView.setText(text);
    }

    public String getLabelText() {
        return labelText;
    }

    public void setShowAnimation(final Animation showAnimation) {
        this.showAnimation = showAnimation;
    }

    public void setHideAnimation(final Animation hideAnimation) {
        this.hideAnimation = hideAnimation;
    }

    @Override
    public void setElevation(final float elevation) {
        super.setElevation(elevation);
        updateBackground();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        final Label label = (Label) getTag(R.id.fab_label);
        if (label != null) label.setEnabled(enabled);
    }

    @Override
    public void setVisibility(final int visibility) {
        super.setVisibility(visibility);
        final Label label = (Label) getTag(R.id.fab_label);
        if (label != null) label.setVisibility(visibility);
    }

    private final class Shadow extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint erase = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float radius = getCircleSize() >> 1;

        private Shadow() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colorNormal);
            erase.setXfermode(PORTER_DUFF_CLEAR);
            if (!isInEditMode()) paint.setShadowLayer(shadowRadius, 0, shadowYOffset, 0x66000000);

        }

        @Override
        public void draw(@NonNull final Canvas canvas) {
            final float centerX = calculateCenterX();
            final float centerY = calculateCenterY();
            canvas.drawCircle(centerX, centerY, radius, paint);
            canvas.drawCircle(centerX, centerY, radius, erase);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }

        @Override
        public void setAlpha(final int alpha) { }

        @Override
        public void setColorFilter(@Nullable final ColorFilter colorFilter) { }

    }

    private final class CircleDrawable extends ShapeDrawable {
        private final int circleInsetVertical = shadowRadius + shadowYOffset;

        private CircleDrawable(final Shape s) {
            super(s);
        }

        @Override
        public void draw(final Canvas canvas) {
            setBounds(shadowRadius, circleInsetVertical, calculateMeasuredWidth()
                    - shadowRadius, calculateMeasuredHeight() - circleInsetVertical);
            super.draw(canvas);
        }
    }

    public void setMenuToggleListener(final OnMenuToggleListener listener, final FloatingActionMenu actionMenu) {
        floatingActionMenu = new WeakReference<>(actionMenu);
        if (listener != null && menuToggleListener == null) menuToggleListener = listener;
    }

    public interface OnMenuToggleListener {
        void onMenuToggle(final boolean opened);
    }
}