package awais.sephiroth.uigestures;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedHashSet;

public final class UIGestureRecognizerDelegate {
    private final LinkedHashSet<UIGestureRecognizer> gestureRecognizers = new LinkedHashSet<>();
    private boolean isEnabled = true;

    public void setEnabled(final boolean enabled) {
        isEnabled = enabled;
        for (final UIGestureRecognizer uiGestureRecognizer : gestureRecognizers) uiGestureRecognizer.setEnabled(enabled);
    }

    public void addGestureRecognizer(@NonNull final UIGestureRecognizer recognizer) {
        recognizer.delegate = this;
        gestureRecognizers.add(recognizer);
    }

    @SafeVarargs
    public final boolean onTouchEvent(final MotionEvent event, final boolean useOR,
                                      final Class<? extends UIGestureRecognizer>... recognizerTypesToExclude) {
        boolean handled = false;

        if (isEnabled) {
            boolean isFirst = true;
            final boolean excludeNotEmpty = recognizerTypesToExclude != null;
            final LinkedHashSet<Class<? extends UIGestureRecognizer>> exludedRecognizers = excludeNotEmpty ?
                    new LinkedHashSet<>(Arrays.asList(recognizerTypesToExclude)) : null;

            for (final UIGestureRecognizer recognizer : gestureRecognizers) {
                final boolean exludeEvent = excludeNotEmpty && exludedRecognizers.contains(recognizer.getClass());
                if (exludeEvent) continue;

                if (useOR) handled |= recognizer.onTouchEvent(event);
                else if (isFirst) handled = recognizer.onTouchEvent(event);
                else handled &= recognizer.onTouchEvent(event);
                isFirst = false;
            }
        }
        return handled;
    }

    boolean shouldRecognizeSimultaneouslyWithGestureRecognizer(final UIGestureRecognizer recognizer) {
        if (gestureRecognizers.size() == 1) return true;

        boolean result = true;
        for (final UIGestureRecognizer other : gestureRecognizers) {
            if (other != recognizer)
                result = result & (!recognizer.hasBeganFiringEvents() | other.hasBeganFiringEvents());
        }

        return result;
    }
}