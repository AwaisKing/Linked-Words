package awais.clans;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

import static awais.clans.FloatingActionButton.SIZE_NORMAL;
import static awais.clans.FloatingActionButton.shadowRadius;
import static awais.clans.FloatingActionButton.shadowYOffset;

public class FloatingActionMenu extends ViewGroup {
    private static final int ANIMATION_DURATION = 300;
    private static final float CLOSED_PLUS_ROTATION = 0;
    private static final float OPENED_PLUS_ROTATION_LEFT = -135;
    private final int mButtonSpacing = Utils.dpToPx(4);
    private final int mLabelsPaddingTop = mButtonSpacing;
    private final int mLabelsPaddingLeft = Utils.dpToPx(8);
    private final int labelMarginFix = mLabelsPaddingLeft;
    private final int mLabelsColorNormal = getWindowBackground(getContext());
    private final int mLabelsColorPressed = mLabelsColorNormal;
    private final float mLabelsTextSize;
    private final Context context;
    private final Handler uiHandler;
    private final ImageView imageToggle;
    private final AnimatorSet mOpenAnimatorSet = new AnimatorSet(), mCloseAnimatorSet = new AnimatorSet(), mIconToggleSet = new AnimatorSet();
    private final ValueAnimator mShowBackgroundAnimator, mHideBackgroundAnimator;
    private final ColorStateList mLabelsTextColor = ColorStateList.valueOf(0xFF808080);
    private final FloatingActionButton menuButton;
    //////////////////////////////////////////////////////////////////////////////////////////
    private int mMaxButtonWidth = 0, mButtonsCount;
    private boolean menuOpened, mIsMenuOpening;

    public FloatingActionMenu(final Context context) {
        this(context, null);
    }

    public FloatingActionMenu(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        uiHandler = new Handler();

        final Resources resources = context.getResources();
        mLabelsTextSize = resources.getDimension(R.dimen.search_text_small);

        final int maxAlpha = ResourcesCompat.getColor(resources, R.color.floating_background, null) >>> 24;
        mShowBackgroundAnimator = ValueAnimator.ofInt(0, maxAlpha);
        mHideBackgroundAnimator = ValueAnimator.ofInt(maxAlpha, 0);

        final ValueAnimator.AnimatorUpdateListener updateListener = animation ->
                setBackgroundColor(Color.argb((Integer) animation.getAnimatedValue(), 0, 0, 0));
        mShowBackgroundAnimator.setDuration(ANIMATION_DURATION).addUpdateListener(updateListener);
        mHideBackgroundAnimator.setDuration(ANIMATION_DURATION).addUpdateListener(updateListener);

        imageToggle = new ImageView(context);
        imageToggle.setImageResource(R.drawable.ic_options);

        menuButton = new FloatingActionButton(context);
        menuButton.fabSize = SIZE_NORMAL;
        menuButton.setColors(ResourcesCompat.getColor(resources, R.color.colorAccent, null),
                ResourcesCompat.getColor(resources, R.color.colorPrimary, null));
        menuButton.updateBackground();
        menuButton.setLabelText(resources.getString(R.string.options));

        addView(menuButton, super.generateDefaultLayoutParams());
        addView(imageToggle);

        final ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(imageToggle, "scaleX", 1.0f, 0.2f);
        final ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(imageToggle, "scaleY", 1.0f, 0.2f);
        final ObjectAnimator scaleInX = ObjectAnimator.ofFloat(imageToggle, "scaleX", 0.2f, 1.0f);
        final ObjectAnimator scaleInY = ObjectAnimator.ofFloat(imageToggle, "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(200);
        scaleOutY.setDuration(200);
        scaleInX.setDuration(200);
        scaleInY.setDuration(200);
        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                imageToggle.setImageResource(isOpened() ? R.drawable.ic_close : R.drawable.ic_options);
            }
        });
        mIconToggleSet.play(scaleOutX).with(scaleOutY);
        mIconToggleSet.play(scaleInX).with(scaleInY).after(scaleOutX);
        mIconToggleSet.setInterpolator(new OvershootInterpolator(3.5f));

        final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(imageToggle, "rotation", OPENED_PLUS_ROTATION_LEFT, CLOSED_PLUS_ROTATION);
        final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(imageToggle, "rotation", CLOSED_PLUS_ROTATION, OPENED_PLUS_ROTATION_LEFT);

        mOpenAnimatorSet.play(expandAnimator);
        mCloseAnimatorSet.play(collapseAnimator);
        Interpolator mOpenInterpolator = new OvershootInterpolator();
        mOpenAnimatorSet.setDuration(ANIMATION_DURATION).setInterpolator(mOpenInterpolator);
        Interpolator mCloseInterpolator = new AnticipateInterpolator();
        mCloseAnimatorSet.setDuration(ANIMATION_DURATION).setInterpolator(mCloseInterpolator);

        menuButton.setShowAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_up));
        menuButton.setHideAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_down));
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int maxLabelWidth = 0, maxButtonHeight = 0, height = 0;

        measureChildWithMargins(imageToggle, widthMeasureSpec, 0, heightMeasureSpec, 0);
        for (int i = 0; i < mButtonsCount; i++) {
            final View child = getChildAt(i);
            if (child != imageToggle && child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
                maxButtonHeight = Math.max(maxButtonHeight, child.getMeasuredHeight());
            }
        }

        for (int i = 0; i < mButtonsCount; i++) {
            int usedWidth = 0;
            final View child = getChildAt(i);

            if (child != imageToggle && child.getVisibility() != GONE) {
                final int measuredWidth = child.getMeasuredWidth();
                usedWidth += measuredWidth;
                height += child.getMeasuredHeight();

                final Label label = (Label) child.getTag(R.id.fab_label);
                if (label != null) {
                    final int labelOffset = mMaxButtonWidth - measuredWidth;
                    final int labelUsedWidth = measuredWidth + shadowRadius + labelOffset;
                    measureChildWithMargins(label, widthMeasureSpec, labelUsedWidth, heightMeasureSpec, 0);
                    usedWidth += label.getMeasuredWidth();
                    maxLabelWidth = Math.max(maxLabelWidth, usedWidth + labelOffset);
                }
            }
        }

        final LayoutParams layoutParams = getLayoutParams();
        int width = Math.max(mMaxButtonWidth, maxLabelWidth) + getPaddingLeft() + getPaddingRight();
        height += mButtonSpacing * (mButtonsCount - 1) + getPaddingTop() + getPaddingBottom();
        height = adjustForOvershoot(height);

        if (layoutParams.width == LayoutParams.MATCH_PARENT)
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        if (layoutParams.height == LayoutParams.MATCH_PARENT)
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        final int buttonsHorizontalCenter = right - left - mMaxButtonWidth / 2 - getPaddingRight();

        final int imageToggleMeasuredHeight = imageToggle.getMeasuredHeight();
        final int imageToggleMeasuredWidth = imageToggle.getMeasuredWidth();
        final int menuButtonMeasuredHeight = menuButton.getMeasuredHeight();
        final int menuButtonMeasuredWidth = menuButton.getMeasuredWidth();

        final int menuButtonTop = bottom - top - menuButtonMeasuredHeight - getPaddingBottom();
        final int menuButtonLeft = buttonsHorizontalCenter - menuButtonMeasuredWidth / 2;
        menuButton.layout(menuButtonLeft, menuButtonTop, menuButtonLeft + menuButtonMeasuredWidth, menuButtonTop + menuButtonMeasuredHeight);

        final int imageLeft = buttonsHorizontalCenter - imageToggleMeasuredWidth / 2;
        final int imageTop = menuButtonTop + menuButtonMeasuredHeight / 2 - imageToggleMeasuredHeight / 2;
        imageToggle.layout(imageLeft, imageTop, imageLeft + imageToggleMeasuredWidth, imageTop + imageToggleMeasuredHeight);

        int nextY = menuButtonTop + menuButtonMeasuredHeight + mButtonSpacing;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child != imageToggle) {
                final FloatingActionButton fab = (FloatingActionButton) child;

                if (fab.getVisibility() != GONE) {
                    final int fabMeasuredWidth = fab.getMeasuredWidth();
                    final int fabMeasuredHeight = fab.getMeasuredHeight();

                    final int childY = nextY - fabMeasuredHeight - mButtonSpacing;

                    if (fab != menuButton) {
                        final int childX = buttonsHorizontalCenter - fabMeasuredWidth / 2;
                        fab.layout(childX, childY, childX + fabMeasuredWidth, childY + fabMeasuredHeight);
                        if (!mIsMenuOpening) fab.hide(false);
                    }

                    final View label = (View) fab.getTag(R.id.fab_label);
                    if (label != null) {
                        final int labelMeasuredWidth = label.getMeasuredWidth();
                        final int labelMeasuredHeight = label.getMeasuredHeight();

                        final int labelsOffset = mMaxButtonWidth / 2;
                        final int labelXNearButton = buttonsHorizontalCenter - labelsOffset - labelMarginFix;
                        final int labelTop = childY + (fabMeasuredHeight - labelMeasuredHeight) / 2;

                        label.layout(labelXNearButton - labelMeasuredWidth,
                                labelTop,
                                labelXNearButton,
                                labelTop + labelMeasuredHeight);

                        if (!mIsMenuOpening) label.setVisibility(INVISIBLE);
                    }

                    nextY = childY - mButtonSpacing;
                }
            }
        }
    }

    private int adjustForOvershoot(final int dimension) {
        return (int) (dimension * 0.03 + dimension);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(menuButton);
        bringChildToFront(imageToggle);
        mButtonsCount = getChildCount();

        for (int i = 0; i < mButtonsCount; i++) {
            final View child = getChildAt(i);
            if (child != imageToggle) {
                final FloatingActionButton fab = (FloatingActionButton) child;
                if (fab.getTag(R.id.fab_label) == null) {
                    if (fab == menuButton) {
                        menuButton.setOnClickListener(v -> toggle());
                    } else {
                        addLabel(fab);
                    }
                }
            }
        }
    }

    private void addLabel(@NonNull final FloatingActionButton fab) {
        final String labelText = fab.getLabelText();

        if (!TextUtils.isEmpty(labelText)) {
            final Label label = new Label(context, fab, AnimationUtils.loadAnimation(context, R.anim.fab_slide_in_from_right),
                    AnimationUtils.loadAnimation(context, R.anim.fab_slide_out_to_right));
            label.setClickable(true);
            label.setColors(mLabelsColorNormal, mLabelsColorPressed);
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLabelsTextSize);
            label.setTextColor(mLabelsTextColor);
            label.setMaxLines(-1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setSingleLine(true);
            label.updateBackground();

            label.setText(labelText);

            int left = mLabelsPaddingLeft;
            int top = mLabelsPaddingTop;
            left += shadowRadius;
            top += shadowRadius + shadowYOffset;
            label.setPadding(left, top, mLabelsPaddingLeft, mLabelsPaddingTop);

            addView(label);
            fab.setTag(R.id.fab_label, label);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new MarginLayoutParams(context, attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(final LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(final LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        boolean handled = false;
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) handled = isOpened();
        else if (action == MotionEvent.ACTION_UP) {
            close();
            handled = true;
        }
        return handled;
    }

    public boolean isOpened() {
        return menuOpened;
    }

    public void setOpened(final boolean menuOpened) {
        this.menuOpened = menuOpened;
    }

    public void toggle() {
        if (isOpened()) close();
        else open();
    }

    public void open() {
        if (!isOpened()) {
            mShowBackgroundAnimator.start();

            if (mIconToggleSet != null) mIconToggleSet.start();
            else {
                mCloseAnimatorSet.cancel();
                mOpenAnimatorSet.start();
            }

            mIsMenuOpening = true;
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    final FloatingActionButton fab = (FloatingActionButton) child;
                    uiHandler.post(() -> {
                        if (!isOpened()) {
                            if (fab != menuButton) fab.show(true);

                            final Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null) label.show();
                        }
                    });
                }
            }
        }
    }

    public void close() {
        if (isOpened()) {
            mHideBackgroundAnimator.start();

            if (mIconToggleSet != null) mIconToggleSet.start();
            else {
                mCloseAnimatorSet.start();
                mOpenAnimatorSet.cancel();
            }

            mIsMenuOpening = false;
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {

                    final FloatingActionButton fab = (FloatingActionButton) child;
                    uiHandler.post(() -> {
                        if (isOpened()) {
                            if (fab != menuButton) fab.hide(true);
                            final Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null) label.hide();
                        }
                    });
                }
            }
        }
    }

    public FloatingActionMenu setMenuButtonClickListener(final OnClickListener listener) {
        menuButton.setOnClickListener(listener);
        return this;
    }

    public FloatingActionMenu setLongClickListener(final OnLongClickListener listener) {
        menuButton.setOnLongClickListener(listener);
        return this;
    }

    public void setMenuToggleListener(final FloatingActionButton.OnMenuToggleListener toggleListener) {
        if (menuButton != null) menuButton.setMenuToggleListener(toggleListener, this);
    }

    private static int getWindowBackground(@NonNull final Context context) {
        final int colorAttr = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? android.R.attr.windowBackground :
                context.getResources().getIdentifier("windowBackground", "attr", context.getPackageName());
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }
}