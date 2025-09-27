package awais.lapism;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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

    static void revealOpen(final View view, int cx, @NonNull final Context context, final SearchEditText editText,
                           final MaterialSearchView.OnOpenCloseListener listener) {
        final Resources resources = context.getResources();
        if (cx <= 0) {
            final int padding = resources.getDimensionPixelSize(R.dimen.search_reveal);
            cx = isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        final int cy = resources.getDimensionPixelSize(R.dimen.search_height) >> 1;

        if (cx == 0 || cy == 0) return;

        final Point displaySize = new Point();
        ContextCompat.getDisplayOrDefault(context).getSize(displaySize);
        final float finalRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

        final Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0.0f, finalRadius);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                if (listener != null) listener.onOpen();
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                editText.requestFocus();
            }
        });
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    static void revealClose(final View view, int cx, @NonNull final Context context, final SearchEditText editText,
                            final MaterialSearchView searchView, final MaterialSearchView.OnOpenCloseListener listener) {
        final Resources resources = context.getResources();
        if (cx <= 0) {
            final int padding = resources.getDimensionPixelSize(R.dimen.search_reveal);
            cx = isRtlLayout(context) ? padding : view.getWidth() - padding;
        }

        final int cy = resources.getDimensionPixelSize(R.dimen.search_height) >> 1;

        if (cx == 0 || cy == 0) return;

        final Point displaySize = new Point();
        ContextCompat.getDisplayOrDefault(context).getSize(displaySize);
        final float initialRadius = (float) Math.hypot(Math.max(cx, displaySize.x - cx), cy);

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

    static boolean isRtlLayout(@Nullable final Context context) {
        final Resources res = context != null ? context.getResources() : Resources.getSystem();
        return res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    /*
    if (isRTL()) {
        // The view has RTL layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_END);
    } else {
        // The view has LTR layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_START);
    }

    public static boolean isRTL() {
        return isRTL(Utils.defaultLocale);
    }

    private static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }*/
}