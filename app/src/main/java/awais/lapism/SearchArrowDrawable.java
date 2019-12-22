package awais.lapism;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;

class SearchArrowDrawable extends DrawerArrowDrawable {
    static final float STATE_ARROW = 0.0f;
    static final float STATE_HAMBURGER = 1.0f;
    private static final Property<SearchArrowDrawable, Float> PROGRESS = new Property<SearchArrowDrawable, Float>(Float.class, "progress") {
        @Override
        public void set(@NonNull SearchArrowDrawable object, Float value) {
            object.setProgress(value);
        }

        @NonNull
        @Override
        public Float get(@NonNull SearchArrowDrawable object) {
            return object.getProgress();
        }
    };

    SearchArrowDrawable(Context context) {
        super(context);
    }

    void animate(float state) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(this, PROGRESS, state,
                state == STATE_ARROW ? STATE_HAMBURGER : STATE_ARROW);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(317);
        anim.start();
    }
}