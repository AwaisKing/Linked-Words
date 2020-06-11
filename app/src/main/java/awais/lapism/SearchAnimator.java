package awais.lapism;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;

import awais.backworddictionary.R;

final class SearchAnimator {
    static void fadeIn(@NonNull final View view) {
        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        view.setAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }

    static void fadeOut(@NonNull final View view) {
        final Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        view.setAnimation(anim);
        view.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void revealOpen(final View view, int cx, @NonNull final Context context, final SearchEditText editText,
                           final MaterialSearchView.OnOpenCloseListener listener) {
        final Resources resources = context.getResources();
        if (cx <= 0) {
            final int padding = resources.getDimensionPixelSize(R.dimen.search_reveal);
            cx = SearchUtils.isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        final int cy = resources.getDimensionPixelSize(R.dimen.search_height) >> 1;

        if (cx != 0 && cy != 0) {
            final Point displaySize = new Point();
            final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getSize(displaySize);
                float finalRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

                final Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0.0f, finalRadius);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(300);

                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    final public void onAnimationStart(Animator animation) {
                        if (listener != null) listener.onOpen();
                    }

                    @Override
                    final public void onAnimationEnd(Animator animation) {
                        editText.requestFocus();
                    }
                });

                view.setVisibility(View.VISIBLE);
                anim.start();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void revealClose(final View view, int cx, @NonNull final Context context, final SearchEditText editText,
                            final MaterialSearchView searchView, final MaterialSearchView.OnOpenCloseListener listener) {
        final Resources resources = context.getResources();
        if (cx <= 0) {
            final int padding = resources.getDimensionPixelSize(R.dimen.search_reveal);
            cx = SearchUtils.isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        final int cy = resources.getDimensionPixelSize(R.dimen.search_height) >> 1;

        if (cx != 0 && cy != 0) {
            final Point displaySize = new Point();
            final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getSize(displaySize);
                float initialRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

                final Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0.0f);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(300);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(final Animator animation) {
                        editText.clearFocus();
                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        view.setVisibility(View.GONE);
                        searchView.setVisibility(View.GONE);
                        if (listener != null) listener.onClose();
                    }
                });
                anim.start();
            }
        }
    }

    static void fadeOpen(@NonNull final View view, final SearchEditText editText, final MaterialSearchView.OnOpenCloseListener listener) {
        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                if (listener != null) listener.onOpen();
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                editText.requestFocus();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {}
        });

        view.setAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }

    static void fadeClose(@NonNull final View view, final SearchEditText editText, final MaterialSearchView searchView,
                          final MaterialSearchView.OnOpenCloseListener listener) {
        final Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                editText.clearFocus();
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                view.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                if (listener != null) listener.onClose();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {}
        });

        view.setAnimation(anim);
        view.setVisibility(View.GONE);
    }
}