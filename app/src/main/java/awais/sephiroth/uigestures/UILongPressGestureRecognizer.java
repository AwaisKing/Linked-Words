package awais.sephiroth.uigestures;

import android.graphics.PointF;
import android.os.Message;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public final class UILongPressGestureRecognizer extends UIGestureRecognizer {
    // request to change the current state to Failed
    private static final int MESSAGE_FAILED = 1;
    // request to change the current state to Possible
    private static final int MESSAGE_RESET = 2;
    // we handle the action_pointer_up received in the onTouchEvent with a delay
    // in order to check how many fingers were actually down when we're checking them
    // in the action_up.
    private static final int MESSAGE_POINTER_UP = 3;
    // post handle the long press event
    private static final int MESSAGE_LONG_PRESS = 4;

    private int numberOfTouches = 0, mNumTaps = 1;
    private long longPressTimeout = Math.max(LONG_PRESS_TIMEOUT, DOUBLE_TAP_TIMEOUT);
    private boolean mStarted, mBegan, mAlwaysInTapRegion = false;

    private final int allowableMovement, scaledTouchSlop;
    private final PointF mStartLocation = new PointF(), mDownFocusLocation = new PointF();

    public UILongPressGestureRecognizer(final int scaledTouchSlop) {
        super();

        this.mStarted = false;
        this.mBegan = false;

        this.scaledTouchSlop = scaledTouchSlop;
        this.allowableMovement = scaledTouchSlop;
    }

    @Override
    protected void removeMessages() {
        removeMessages(MESSAGE_FAILED, MESSAGE_RESET, MESSAGE_POINTER_UP, MESSAGE_LONG_PRESS);
    }

    @Override
    protected void handleMessage(@NonNull final Message msg) {
        if (msg.what == MESSAGE_RESET) handleReset();
        else if (msg.what == MESSAGE_FAILED) handleFailed();
        else if (msg.what == MESSAGE_POINTER_UP) numberOfTouches = msg.arg1;
        else if (msg.what == MESSAGE_LONG_PRESS) handleLongPress();
    }

    @Override
    public boolean hasBeganFiringEvents() {
        return super.hasBeganFiringEvents() && inState(State.BEGAN, State.CHANGED);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);

        if (!isEnabled) return false;

        final int action = event.getActionMasked();
        final int count = event.getPointerCount();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                removeMessages();

                mAlwaysInTapRegion = true;
                numberOfTouches = count;
                mBegan = false;

                if (!mStarted) {
                    state = State.POSSIBLE;
                    setBeginFiringEvents(false);
                    mNumTaps = 1;
                    mStarted = true;
                } else {
                    mNumTaps++;

                    // if second tap is too far wawy from the first and only 1 finger is required
                }

                if (mNumTaps == 1) {
                    mHandler.sendEmptyMessageAtTime(MESSAGE_LONG_PRESS, event.getDownTime() + longPressTimeout);
                } else {
                    delayedFail();
                }

                mDownFocusLocation.set(mCurrentLocation);
                mStartLocation.set(mCurrentLocation);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (state == State.POSSIBLE && mStarted) {
                    removeMessages(MESSAGE_POINTER_UP);
                    numberOfTouches = count;

                    if (numberOfTouches > 1) {
                        removeMessages();
                        state = State.FAILED;
                    }

                    mDownFocusLocation.set(mCurrentLocation);
                    computeFocusPoint(event, mStartLocation);

                } else if (inState(State.BEGAN, State.CHANGED) && mStarted) {
                    numberOfTouches = count;
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                if (state == State.POSSIBLE && mStarted) {
                    removeMessages(MESSAGE_POINTER_UP);

                    mDownFocusLocation.set(mCurrentLocation);

                    final Message message = mHandler.obtainMessage(MESSAGE_POINTER_UP);
                    message.arg1 = numberOfTouches - 1;
                    mHandler.sendMessageDelayed(message, UIGestureRecognizer.TAP_TIMEOUT);

                    computeFocusPoint(event, mStartLocation);

                } else if (inState(State.BEGAN, State.CHANGED)) {
                    if (numberOfTouches - 1 < 1) {
                        final boolean began = hasBeganFiringEvents();
                        state = State.ENDED;

                        if (began) fireActionEvent();

                        setBeginFiringEvents(false);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (state == State.POSSIBLE && mStarted) {
                    if (mAlwaysInTapRegion) {
                        final float distance = UIGestureRecognizer.distance(mCurrentLocation, mDownFocusLocation);

                        if (distance > allowableMovement) {
                            mAlwaysInTapRegion = false;
                            removeMessages();
                            state = State.FAILED;
                        }
                    }
                } else if (state == State.BEGAN) {
                    if (!mBegan) {
                        final float distance = UIGestureRecognizer.distance(mCurrentLocation, mDownFocusLocation);

                        if (distance > scaledTouchSlop) {
                            mBegan = true;

                            if (hasBeganFiringEvents()) {
                                state = State.CHANGED;
                                fireActionEvent();
                            }
                        }
                    }
                } else if (state == State.CHANGED) {
                    if (hasBeganFiringEvents()) fireActionEvent();
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                removeMessages(MESSAGE_RESET, MESSAGE_POINTER_UP, MESSAGE_LONG_PRESS);

                if (state == State.POSSIBLE && mStarted) {
                    if (numberOfTouches != 1) {
                        mStarted = false;
                        removeMessages();
                        state = State.FAILED;
                        postReset();
                    } else {
                        if (mNumTaps < 1) {
                            removeMessages(MESSAGE_FAILED);
                            delayedFail();
                        } else {
                            mNumTaps = 1;
                            mStarted = false;
                            removeMessages();
                            state = State.FAILED;
                        }
                    }
                } else if (inState(State.BEGAN, State.CHANGED)) {
                    mNumTaps = 1;
                    mStarted = false;
                    final boolean began = hasBeganFiringEvents();
                    state = State.ENDED;
                    if (began) fireActionEvent();
                    postReset();
                } else {
                    mStarted = false;
                    postReset();
                }
                setBeginFiringEvents(false);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                removeMessages();
                mStarted = false;
                mNumTaps = 1;
                state = State.CANCELLED;
                postReset();
            }
            break;
        }

        if (state == State.POSSIBLE) return cancelsTouchesInView;
        else if (state == State.ENDED) return cancelsTouchesInView;

        return !cancelsTouchesInView;
    }

    @Override
    public void reset() {
        super.reset();
        handleReset();
    }

    private void postReset() {
        mHandler.sendEmptyMessage(MESSAGE_RESET);
    }

    private void delayedFail() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_FAILED, DOUBLE_TAP_TIMEOUT);
    }

    private void handleFailed() {
        removeMessages();
        state = State.FAILED;
        setBeginFiringEvents(false);
        mStarted = false;
    }

    private void handleReset() {
        state = State.POSSIBLE;
        mStarted = false;
    }

    private void handleLongPress() {
        removeMessages(MESSAGE_FAILED);

        if (state == State.POSSIBLE && mStarted) {
            if (numberOfTouches == 1) {
                state = State.BEGAN;
                fireActionEventIfCanRecognizeSimultaneously();
            } else {
                state = State.FAILED;
                setBeginFiringEvents(false);
                mStarted = false;
                mNumTaps = 1;
            }
        }
    }

    private void fireActionEventIfCanRecognizeSimultaneously() {
        if (inState(State.CHANGED, State.ENDED)) {
            setBeginFiringEvents(true);
            fireActionEvent();
        } else if (delegate.shouldRecognizeSimultaneouslyWithGestureRecognizer(this)) {
            setBeginFiringEvents(true);
            fireActionEvent();
        }
    }

    public void setLongPressTimeout(final long longPressTimeout) {
        this.longPressTimeout = longPressTimeout;
    }
}