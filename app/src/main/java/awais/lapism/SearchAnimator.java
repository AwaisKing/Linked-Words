package awais.lapism;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import awais.backworddictionary.R;

class SearchAnimator {
    static void fadeIn(View view) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(317);

        view.setAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }

    static void fadeOut(View view) {
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(317);

        view.setAnimation(anim);
        view.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void revealOpen(View view, int cx, int duration, Context context, final SearchEditText editText,
                           final boolean shouldClearOnOpen, final MaterialSearchView.OnOpenCloseListener listener) {

        if (cx <= 0) {
            int padding = context.getResources().getDimensionPixelSize(R.dimen.search_reveal);
            cx = SearchUtils.isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        int cy = context.getResources().getDimensionPixelSize(R.dimen.search_height) / 2;

        if (cx == 0 || cy == 0) return;

        Point displaySize = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;
        windowManager.getDefaultDisplay().getSize(displaySize);
        float finalRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0.0f, finalRadius);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(duration);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onOpen();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (shouldClearOnOpen && editText.length() > 0 && editText.getText() != null)
                    editText.getText().clear();
                editText.requestFocus();
            }
        });

        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void revealClose(final View view, int cx, int duration, Context context, final SearchEditText editText,
                            final boolean shouldClearOnClose, final MaterialSearchView searchView, final MaterialSearchView.OnOpenCloseListener listener) {

        if (cx <= 0) {
            int padding = context.getResources().getDimensionPixelSize(R.dimen.search_reveal);
            cx = SearchUtils.isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        int cy = context.getResources().getDimensionPixelSize(R.dimen.search_height) / 2;

        if (cx == 0 || cy == 0) return;

        Point displaySize = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;
        windowManager.getDefaultDisplay().getSize(displaySize);
        float initialRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(duration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (shouldClearOnClose && editText.length() > 0 && editText.getText() != null)
                    editText.getText().clear();
                editText.clearFocus();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                if (listener != null) listener.onClose();
            }
        });
        anim.start();
    }

    static void fadeOpen(View view, final SearchEditText editText,
                         final MaterialSearchView.OnOpenCloseListener listener) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(317);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (listener != null) listener.onOpen();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                editText.requestFocus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.setAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }

    static void fadeClose(final View view, final SearchEditText editText, final MaterialSearchView searchView,
                          final MaterialSearchView.OnOpenCloseListener listener) {
        final Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(317);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                editText.clearFocus();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                if (listener != null) listener.onClose();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.setAnimation(anim);
        view.setVisibility(View.GONE);
    }
}