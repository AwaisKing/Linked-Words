package awais.backworddictionary.adapters.animation;

import android.animation.TimeInterpolator;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class MagicAnimator extends RecyclerView.ItemAnimator {
    private static final TimeInterpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();

    private static void log(final String method, final RecyclerView.ViewHolder viewHolder, final ItemHolderInfo preLayoutInfo,
                            final ItemHolderInfo postLayoutInfo) {
        Log.d("AWAISKING_APP", method + ": " + viewHolder
                + " -- [" + (preLayoutInfo != null ? preLayoutInfo.changeFlags + ", " + preLayoutInfo.left + ", " + preLayoutInfo.top + ", " + preLayoutInfo.right + ", " + preLayoutInfo.bottom : null)
                + "] -- [" + (postLayoutInfo != null ? postLayoutInfo.changeFlags + ", " + postLayoutInfo.left + ", " + postLayoutInfo.top + ", " + postLayoutInfo.right + ", " + postLayoutInfo.bottom : null) + ']'
        );
    }

    @Override
    public boolean animateAppearance(@NonNull final RecyclerView.ViewHolder viewHolder, @Nullable final ItemHolderInfo preLayoutInfo,
                                     @NonNull final ItemHolderInfo postLayoutInfo) {
        log("animateAppearance", viewHolder, preLayoutInfo, postLayoutInfo);
        return false;
    }

    @Override
    public boolean animatePersistence(@NonNull final RecyclerView.ViewHolder viewHolder, @NonNull final ItemHolderInfo preLayoutInfo,
                                      @NonNull final ItemHolderInfo postLayoutInfo) {
        log("animatePersistence", viewHolder, preLayoutInfo, postLayoutInfo);
        return false;
    }

    @Override
    public boolean animateDisappearance(@NonNull final RecyclerView.ViewHolder viewHolder, @NonNull final ItemHolderInfo preLayoutInfo,
                                        @Nullable final ItemHolderInfo postLayoutInfo) {
        log("animateDisappearance", viewHolder, preLayoutInfo, postLayoutInfo);
        return false;
    }

    @Override
    public boolean animateChange(@NonNull final RecyclerView.ViewHolder oldHolder, @NonNull final RecyclerView.ViewHolder newHolder,
                                 @NonNull final ItemHolderInfo preLayoutInfo, @NonNull final ItemHolderInfo postLayoutInfo) {
        //log("animateDisappearance", viewHolder, preLayoutInfo, postLayoutInfo);
        return false;
    }

    @Override
    public void runPendingAnimations() {

    }

    @Override
    public void endAnimation(@NonNull final RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
