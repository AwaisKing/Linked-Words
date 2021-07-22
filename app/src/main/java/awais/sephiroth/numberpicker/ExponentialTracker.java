package awais.sephiroth.numberpicker;

import android.graphics.PointF;
import android.os.Handler;
import android.util.DisplayMetrics;

final class ExponentialTracker {
    private static final long MAX_TIME_DELAY = 200L;
    private static final long MIN_TIME_DELAY = 16L;

    protected float minDistance = 0f;

    private boolean started = false;
    private float downPosition = 0f;
    private long time = 1000L;
    private int direction = 0;

    private final PointF minPoint = new PointF(0f, 0f);
    private final HorizontalNumberPicker numberPicker;
    private final int maxDistance;
    private final ProgressCallback callback;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!started) return;

            final int progress = numberPicker.getProgress();
            if (direction > 0)
                callback.onProgressCallback(progress + 1);
            else if (direction < 0)
                callback.onProgressCallback(progress - 1);

            if (started) handler.postDelayed(this, time);
        }
    };

    ExponentialTracker(final HorizontalNumberPicker numberPicker, final int maxDistance,
                       final ProgressCallback callback) {
        this.numberPicker = numberPicker;
        this.maxDistance = maxDistance;
        this.callback = callback;
    }

    public void begin(final float x) {
        calcDistance();
        downPosition = x;
        minPoint.set(-minDistance, -minDistance);
        started = true;
        direction = 0;
        time = MAX_TIME_DELAY;
        handler.post(runnable);
    }

    void addMovement(final float x) {
        final float perc = Math.max(-minDistance, Math.min(x - downPosition, minDistance)) / minDistance;
        direction = perc > 0 ? 1 : perc < 0 ? -1 : 0;
        time = (long) (MAX_TIME_DELAY - ((float) (MAX_TIME_DELAY - MIN_TIME_DELAY) * Math.abs(perc)));
    }

    public void end() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    private void calcDistance() {
        final int[] loc = {0, 0};
        final DisplayMetrics metrics = numberPicker.getResources().getDisplayMetrics();
        numberPicker.getLocationOnScreen(loc);
        loc[0] += numberPicker.getWidth() / 2;
        loc[1] += numberPicker.getHeight() / 2;

        minDistance = (float) Math.min(maxDistance, Math.min(loc[0], metrics.widthPixels - loc[0]));
    }
}