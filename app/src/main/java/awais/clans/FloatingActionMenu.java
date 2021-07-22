package awais.clans;

import static awais.clans.FloatingActionButton.SIZE_NORMAL;

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
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;

public final class FloatingActionMenu extends ViewGroup {
    private static final int ANIMATION_DURATION = 300;

    private final int buttonSpacing, labelsPaddingLeft, labelsPaddingTop, labelMarginFix;
    private final int shadowRadius, shadowYOffset;

    private final float labelsTextSize;
    private final Context context;
    private final Handler uiHandler;
    private final ImageView imageToggle;
    private final AnimatorSet iconToggleSet = new AnimatorSet();
    private final ValueAnimator backgroundShowAnimator, backgroundHideAnimator;
    private final ColorStateList labelsTextColor = ColorStateList.valueOf(0xDE000000);
    private final FloatingActionButton menuButton;
    //////////////////////////////////////////////////////////////////////////////////////////
    private boolean menuOpened, isMenuOpening, isMenuClosing;
    private int btnCount, maxButtonWidth = 0, fabIdx = 0;
    private MenuItemSelector menuItemSelector;
    private final OnClickListener onClickListener = v -> {
        final Object tag = v.getTag(R.id.fab_index);
        if (menuItemSelector != null && tag != null)
            menuItemSelector.onMenuItemClick((FloatingActionButton) v, (int) tag);
    };

    public FloatingActionMenu(final Context context) {
        this(context, null);
    }

    public FloatingActionMenu(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        this.uiHandler = new Handler(Looper.getMainLooper());

        final Resources resources = context.getResources();
        labelsPaddingLeft = labelMarginFix = (int) resources.getDimension(R.dimen.dialog_corners);
        labelsPaddingTop = buttonSpacing = (int) resources.getDimension(R.dimen.dialog_padding);
        labelsTextSize = resources.getDimension(R.dimen.search_text_small);
        shadowRadius = (int) resources.getDimension(R.dimen.search_divider);
        shadowYOffset = (int) resources.getDimension(R.dimen.ttlm_default_elevation);

        final int maxAlpha = ResourcesCompat.getColor(resources, R.color.floating_background, null) >>> 24;
        backgroundShowAnimator = ValueAnimator.ofInt(0, maxAlpha);
        backgroundHideAnimator = ValueAnimator.ofInt(maxAlpha, 0);

        final ValueAnimator.AnimatorUpdateListener updateListener = animation ->
                setBackgroundColor(Color.argb((Integer) animation.getAnimatedValue(), 0, 0, 0));
        backgroundShowAnimator.setDuration(ANIMATION_DURATION).addUpdateListener(updateListener);
        backgroundHideAnimator.setDuration(ANIMATION_DURATION).addUpdateListener(updateListener);

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

        final ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(imageToggle, "scaleX", 1.0f, 0.2f).setDuration(200);
        final ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(imageToggle, "scaleY", 1.0f, 0.2f).setDuration(200);
        final ObjectAnimator scaleInX = ObjectAnimator.ofFloat(imageToggle, "scaleX", 0.2f, 1.0f).setDuration(200);
        final ObjectAnimator scaleInY = ObjectAnimator.ofFloat(imageToggle, "scaleY", 0.2f, 1.0f).setDuration(200);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                imageToggle.setImageResource(menuOpened ? R.drawable.ic_close : R.drawable.ic_options);
            }
        });
        iconToggleSet.play(scaleOutX).with(scaleOutY);
        iconToggleSet.play(scaleInX).with(scaleInY).after(scaleOutX);
        iconToggleSet.setInterpolator(new OvershootInterpolator(3.5f));

        menuButton.setShowAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_up));
        menuButton.setHideAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_down));
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int maxLabelWidth = 0, maxButtonHeight = 0, height = 0;

        measureChildWithMargins(imageToggle, widthMeasureSpec, 0, heightMeasureSpec, 0);
        for (int i = 0; i < btnCount; i++) {
            final View child = getChildAt(i);
            if (child != imageToggle && child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                maxButtonWidth = Math.max(maxButtonWidth, child.getMeasuredWidth());
                maxButtonHeight = Math.max(maxButtonHeight, child.getMeasuredHeight());
            }
        }

        for (int i = 0; i < btnCount; i++) {
            int usedWidth = 0;
            final View child = getChildAt(i);

            if (child != imageToggle && child.getVisibility() != GONE) {
                final int measuredWidth = child.getMeasuredWidth();
                usedWidth += measuredWidth;
                height += child.getMeasuredHeight();

                final Label label = (Label) child.getTag(R.id.fab_label);
                if (label != null) {
                    final int labelOffset = maxButtonWidth - measuredWidth;
                    final int labelUsedWidth = measuredWidth + shadowRadius + labelOffset;
                    measureChildWithMargins(label, widthMeasureSpec, labelUsedWidth, heightMeasureSpec, 0);
                    usedWidth += label.getMeasuredWidth();
                    maxLabelWidth = Math.max(maxLabelWidth, usedWidth + labelOffset);
                }
            }
        }

        final LayoutParams layoutParams = getLayoutParams();
        int width = Math.max(maxButtonWidth, maxLabelWidth) + getPaddingLeft() + getPaddingRight();
        height += buttonSpacing * (btnCount - 1) + getPaddingTop() + getPaddingBottom();
        height = adjustForOvershoot(height);

        if (layoutParams.width == LayoutParams.MATCH_PARENT)
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        if (layoutParams.height == LayoutParams.MATCH_PARENT)
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        final int buttonsHorizontalCenter = right - left - maxButtonWidth / 2 - getPaddingRight();

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

        int nextY = menuButtonTop + menuButtonMeasuredHeight + buttonSpacing;

        for (int i = btnCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child != imageToggle) {
                final FloatingActionButton fab = (FloatingActionButton) child;

                if (fab.getVisibility() != GONE) {
                    final int fabMeasuredWidth = fab.getMeasuredWidth();
                    final int fabMeasuredHeight = fab.getMeasuredHeight();

                    final int childY = nextY - fabMeasuredHeight - buttonSpacing;

                    if (fab != menuButton) {
                        final int childX = buttonsHorizontalCenter - fabMeasuredWidth / 2;
                        fab.layout(childX, childY, childX + fabMeasuredWidth, childY + fabMeasuredHeight);
                        if (!isMenuOpening) fab.hide(false);
                    }

                    final View label = (View) fab.getTag(R.id.fab_label);
                    if (label != null) {
                        final int labelMeasuredWidth = label.getMeasuredWidth();
                        final int labelMeasuredHeight = label.getMeasuredHeight();

                        final int labelsOffset = maxButtonWidth / 2;
                        final int labelXNearButton = buttonsHorizontalCenter - labelsOffset - labelMarginFix;
                        final int labelTop = childY + (fabMeasuredHeight - labelMeasuredHeight) / 2;

                        label.layout(labelXNearButton - labelMeasuredWidth,
                                labelTop,
                                labelXNearButton,
                                labelTop + labelMeasuredHeight);

                        if (!isMenuOpening) label.setVisibility(INVISIBLE);
                    }

                    nextY = childY - buttonSpacing;
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
        btnCount = getChildCount();

        for (int i = 0; i < btnCount; i++) {
            final View child = getChildAt(i);
            if (child != imageToggle) {
                final FloatingActionButton fab = (FloatingActionButton) child;
                if (fab.getTag(R.id.fab_label) == null) {
                    if (fab == menuButton) {
                        menuButton.setOnClickListener(v -> toggle());
                    } else {
                        addLabel(fab);
                        fab.setOnClickListener(onClickListener);
                    }
                }
            }
        }
    }

    private void addLabel(@NonNull final FloatingActionButton fab) {
        final String labelText = fab.getLabelText();

        if (!Utils.isEmpty(labelText)) {
            final Label label = new Label(context, fab, AnimationUtils.loadAnimation(context, R.anim.fab_slide_in_from_right),
                    AnimationUtils.loadAnimation(context, R.anim.fab_slide_out_to_right));
            label.setClickable(true);
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelsTextSize);
            label.setTextColor(labelsTextColor);
            label.setMaxLines(-1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setSingleLine(true);
            label.updateBackground();

            label.setText(labelText);
            label.setPadding(labelsPaddingLeft + shadowRadius, labelsPaddingTop + shadowRadius + shadowYOffset,
                    labelsPaddingLeft, labelsPaddingTop);

            addView(label);
            fab.setTag(R.id.fab_index, fabIdx++);
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
        if (action == MotionEvent.ACTION_DOWN) handled = menuOpened;
        else if (action == MotionEvent.ACTION_UP) {
            close();
            handled = true;
        }
        return !isMenuClosing && handled;
    }

    public boolean isOpened() {
        return menuOpened;
    }

    public void setOpened(final boolean menuOpened) {
        if (menuOpened != this.menuOpened) {
            this.menuOpened = menuOpened;
            if (iconToggleSet != null) iconToggleSet.start();
        }
    }

    public void toggle() {
        if (menuOpened) close();
        else open();
    }

    public void open() {
        if (!menuOpened) {
            backgroundShowAnimator.start();

            isMenuOpening = true;
            isMenuClosing = false;

            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    final FloatingActionButton fab = (FloatingActionButton) child;
                    uiHandler.post(() -> {
                        if (!menuOpened) {
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
        if (menuOpened) {
            backgroundHideAnimator.start();

            isMenuClosing = true;
            isMenuOpening = false;

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {

                    final FloatingActionButton fab = (FloatingActionButton) child;
                    uiHandler.post(() -> {
                        if (menuOpened) {
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

    public FloatingActionMenu setMenuItemSelector(final MenuItemSelector menuItemSelector) {
        this.menuItemSelector = menuItemSelector;
        return this;
    }

    public void setMenuToggleListener(final FloatingActionButton.OnMenuToggleListener toggleListener) {
        if (menuButton != null) menuButton.setMenuToggleListener(toggleListener, this);
    }
}