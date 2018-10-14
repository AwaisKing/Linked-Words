package com.keiferstone.nonet;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BannerView extends AppCompatTextView {
    private static final int SHOWN = 100;
    private static final int SHOWING = 101;
    private static final int HIDDEN = 102;
    private static final int HIDING = 103;
    @BannerView.State private int state;

    public BannerView(Context context) {
        super(context);
        init();
    }

    public BannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        state = HIDDEN;
    }

    public void show() {
        if (state == HIDING || state == HIDDEN) {
            state = SHOWING;
            clearAnimation();
            setVisibility(VISIBLE);
            setTranslationY(-getHeight());
            animate().translationY(0).setInterpolator(new FastOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animation) {}
                        @Override public void onAnimationEnd(Animator animation) { state = SHOWN; }
                        @Override public void onAnimationCancel(Animator animation) {}
                        @Override public void onAnimationRepeat(Animator animation) {}
                    }).start();
        }
    }

    public void hide() {
        if (state == SHOWN || state == SHOWING) {
            state = HIDING;
            clearAnimation();
            animate().translationY(-getHeight()).setInterpolator(new FastOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animation) {}
                        @Override public void onAnimationEnd(Animator animation) {
                            setVisibility(GONE);
                            state = HIDDEN;
                        }
                        @Override public void onAnimationCancel(Animator animation) {}
                        @Override public void onAnimationRepeat(Animator animation) {}
                    }).start();
        }
    }

    @Retention( RetentionPolicy.SOURCE )
    @IntDef( {SHOWN, SHOWING, HIDDEN, HIDING} )
    private @interface State {}
}
