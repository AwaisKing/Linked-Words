package awais.sephiroth.uigestures;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.LinkedHashSet;

public final class UIGestureRecognizerDelegate {
    private final LinkedHashSet<UIGestureRecognizer> mSet = new LinkedHashSet<>();
    private boolean isEnabled = true;

    public void setEnabled(final boolean enabled) {
        isEnabled = enabled;
        for (final UIGestureRecognizer uiGestureRecognizer : mSet) uiGestureRecognizer.setEnabled(enabled);
    }

    public void addGestureRecognizer(@NonNull final UIGestureRecognizer recognizer) {
        recognizer.delegate = this;
        mSet.add(recognizer);
    }

    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;
        if (isEnabled) for (final UIGestureRecognizer recognizer : mSet) handled |= recognizer.onTouchEvent(event);
        return handled;
    }

    boolean shouldRecognizeSimultaneouslyWithGestureRecognizer(final UIGestureRecognizer recognizer) {
        if (mSet.size() == 1) return true;

        boolean result = true;
        for (final UIGestureRecognizer other : mSet) {
            if (other != recognizer)
                // todo maybe set to true as before
                result = result & (!recognizer.hasBeganFiringEvents() | other.hasBeganFiringEvents());
        }

        return result;
    }
}