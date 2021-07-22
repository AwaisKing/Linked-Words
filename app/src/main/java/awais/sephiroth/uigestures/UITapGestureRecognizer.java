package awais.sephiroth.uigestures;

import android.graphics.PointF;
import android.os.Message;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import awais.backworddictionary.helpers.Utils;

public final class UITapGestureRecognizer extends UIGestureRecognizer {
    // request to change the current state to Failed
    private static final int MESSAGE_FAILED = 1;
    // request to change the current state to Possible
    private static final int MESSAGE_RESET = 2;
    // we handle the action_pointer_up received in the onTouchEvent with a delay
    // in order to check how many fingers were actually down when we're checking them
    // in the action_up.
    private static final int MESSAGE_POINTER_UP = 3;
    // a long press will make this gesture to fail
    private static final int MESSAGE_LONG_PRESS = 4;

    private final int scaledTouchSlop;
    private final long tapTimeout = TAP_TIMEOUT;
    private final PointF mDownFocus = new PointF(), mDownCurrentLocation = new PointF();
    private int mNumTaps = 0;
    private boolean mStarted, mAlwaysInTapRegion = false;

    public UITapGestureRecognizer(final int scaledTouchSlop) {
        super();
        this.mStarted = false;
        this.scaledTouchSlop = scaledTouchSlop;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);

        if (!isEnabled) return false;

        final int action = event.getActionMasked();
        final int count = event.getPointerCount();
        if (action == MotionEvent.ACTION_DOWN) {
            removeMessages();
            mAlwaysInTapRegion = true;
            mNumberOfTouches = count;

            state = State.POSSIBLE;
            setBeginFiringEvents(false);

            if (!mStarted) {
                mNumTaps = 0;
                mStarted = true;
            }

            mHandler.sendEmptyMessageDelayed(MESSAGE_LONG_PRESS, tapTimeout + TIMEOUT_DELAY_MILLIS);

            mNumTaps++;
            mDownFocus.set(mCurrentLocation);
            mDownCurrentLocation.set(mCurrentLocation);
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (state == State.POSSIBLE && mStarted) {
                removeMessages(MESSAGE_POINTER_UP);
                mNumberOfTouches = count;
                if (mNumberOfTouches > 1) state = State.FAILED;
                mDownFocus.set(mCurrentLocation);
                mDownCurrentLocation.set(mCurrentLocation);
            }
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            if (state == State.POSSIBLE && mStarted) {
                removeMessages(MESSAGE_FAILED, MESSAGE_RESET, MESSAGE_POINTER_UP);
                mDownFocus.set(mCurrentLocation);

                final Message message = mHandler.obtainMessage(MESSAGE_POINTER_UP);
                message.arg1 = mNumberOfTouches - 1;
                mHandler.sendMessageDelayed(message, UIGestureRecognizer.TAP_TIMEOUT);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            // if taps and touches > 1 then we need to be less strict
            if (state == State.POSSIBLE && mStarted && mAlwaysInTapRegion && Utils.distance(mDownFocus, mCurrentLocation) > scaledTouchSlop) {
                mDownCurrentLocation.set(mCurrentLocation);
                mAlwaysInTapRegion = false;
                removeMessages();
                state = State.FAILED;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            removeMessages(MESSAGE_RESET, MESSAGE_POINTER_UP, MESSAGE_LONG_PRESS);
            if (state != State.POSSIBLE || !mStarted) handleReset();
            else if (mNumberOfTouches != 1) handleFailed();
            else if (mNumTaps < 1) delayedFail();
            else {
                state = State.ENDED;
                fireActionEventIfCanRecognizeSimultaneously();
                postReset();
                mStarted = false;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            removeMessages();
            mStarted = false;
            state = State.CANCELLED;
            setBeginFiringEvents(false);
            postReset();
        }

        if (state == State.POSSIBLE) return !cancelsTouchesInView;
        else if (state == State.ENDED) return !cancelsTouchesInView;

        return cancelsTouchesInView;
    }

    @Override
    protected void handleMessage(@NonNull final Message msg) {
        if (msg.what == MESSAGE_RESET) handleReset();
        else if (msg.what == MESSAGE_POINTER_UP) mNumberOfTouches = msg.arg1;
        else if (msg.what == MESSAGE_FAILED || msg.what == MESSAGE_LONG_PRESS) handleFailed();
    }

    @Override
    public void reset() {
        super.reset();
        handleReset();
    }

    @Override
    public boolean hasBeganFiringEvents() {
        return super.hasBeganFiringEvents() && inState(State.ENDED);
    }

    @Override
    protected void removeMessages() {
        removeMessages(MESSAGE_FAILED, MESSAGE_RESET, MESSAGE_POINTER_UP, MESSAGE_LONG_PRESS);
    }

    private void handleReset() {
        state = State.POSSIBLE;
        setBeginFiringEvents(false);
        mStarted = false;
    }

    private void postReset() {
        mHandler.sendEmptyMessage(MESSAGE_RESET);
    }

    private void fireActionEventIfCanRecognizeSimultaneously() {
        if (delegate.shouldRecognizeSimultaneouslyWithGestureRecognizer(this)) {
            setBeginFiringEvents(true);
            fireActionEvent();
        }
    }

    private void handleFailed() {
        state = State.FAILED;
        setBeginFiringEvents(false);
        removeMessages();
        mStarted = false;
    }

    private void delayedFail() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_FAILED, DOUBLE_TAP_TIMEOUT);
    }
}