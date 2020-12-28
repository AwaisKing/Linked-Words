package awais.sephiroth.uigestures;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

public abstract class UIGestureRecognizer {
    protected static final int TIMEOUT_DELAY_MILLIS = 5;
    protected static final long LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    protected static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    protected static final long TAP_TIMEOUT = ViewConfiguration.getTapTimeout();

    protected final PointF mDownLocation = new PointF(), mCurrentLocation = new PointF(), mPreviousDownLocation = new PointF();
    protected final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            UIGestureRecognizer.this.handleMessage(msg);
        }
    };
    protected int mNumberOfTouches = 0;
    protected boolean isEnabled = true, cancelsTouchesInView = true;
    protected State state = null;
    protected ActionListener actionListener = null;
    protected UIGestureRecognizerDelegate delegate = null;
    private boolean mBeganFiringEvents = false;

    protected abstract void removeMessages();
    protected abstract void handleMessage(final Message msg);

    public State getState() {
        return state;
    }

    public void setEnabled(final boolean enabled) {
        if (isEnabled != enabled) {
            isEnabled = enabled;
            if (!enabled) reset();
        }
    }

    public void setActionListener(final ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setCancelsTouchesInView(final boolean cancelsTouchesInView) {
        this.cancelsTouchesInView = cancelsTouchesInView;
    }

    public float getDownLocationX() {
        return mDownLocation.x;
    }

    public float getCurrentLocationX() {
        return mCurrentLocation.x;
    }

    public void reset() {
        state = null;
        setBeginFiringEvents(false);
        removeMessages();
    }

    public boolean hasBeganFiringEvents() {
        return mBeganFiringEvents;
    }

    protected void setBeginFiringEvents(final boolean value) {
        mBeganFiringEvents = value;
    }

    protected void removeMessages(@NonNull final int... messages) {
        for (final int message : messages) mHandler.removeMessages(message);
    }

    protected void fireActionEvent() {
        actionListener.onAction(this);
    }

    public boolean onTouchEvent(final MotionEvent event) {
        final MotionEvent obtain = MotionEvent.obtain(event);

        // action down
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mPreviousDownLocation.set(mDownLocation);
            mDownLocation.set(event.getX(), event.getY());
        }

        if (obtain != null) obtain.recycle();

        // compute current location
        mNumberOfTouches = computeFocusPoint(event, mCurrentLocation);
        return false;
    }

    boolean inState(@NonNull final State... states) {
        for (final State state : states) if (state == this.state) return true;
        return false;
    }

    @Override
    protected void finalize() { }

    static int computeFocusPoint(@NonNull final MotionEvent event, final PointF out) {
        final int actionMasked = event.getActionMasked();
        final int count = event.getPointerCount();
        final boolean pointerUp = actionMasked == MotionEvent.ACTION_POINTER_UP;

        final int skipIndex = pointerUp ? event.getActionIndex() : -1;
        // Determine focal point
        float sumX = 0f, sumY = 0f;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += event.getX(i);
            sumY += event.getY(i);
        }

        final int div = pointerUp ? count - 1 : count;
        out.x = sumX / div;
        out.y = sumY / div;
        return pointerUp ? count - 1 : count;
    }

    public enum State {
        POSSIBLE,
        BEGAN,
        CHANGED,
        FAILED,
        CANCELLED,
        ENDED,
    }
}