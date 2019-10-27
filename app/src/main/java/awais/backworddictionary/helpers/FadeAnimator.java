package awais.backworddictionary.helpers;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;
import java.util.List;

public class FadeAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private final ArrayList<ViewHolder> mPendingAdditions = new ArrayList<>();
    private final ArrayList<ViewHolder> mChangeAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> mRemoveAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> mPendingRemovals = new ArrayList<>();
    private final ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> mAddAnimations = new ArrayList<>();
    private final ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();
    private final ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private final ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList<>();
    private final ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();
    private final ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();

    public FadeAnimator() {
        super();
        setSupportsChangeAnimations(false);
    }

    private static void cleanup(@NonNull View v) {
        v.setAlpha(1);
        v.setScaleY(1);
        v.setScaleX(1);
        v.setTranslationY(0);
        v.setTranslationX(0);
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setPivotY(v.getMeasuredHeight() >> 1);
        v.setPivotX(v.getMeasuredWidth() >> 1);
        ViewCompat.animate(v).setInterpolator(null).setStartDelay(0);
    }

    @Override
    public void runPendingAnimations() {
        final boolean removalsPending = !mPendingRemovals.isEmpty(), movesPending = !mPendingMoves.isEmpty(),
                changesPending = !mPendingChanges.isEmpty(), additionsPending = !mPendingAdditions.isEmpty();

        if (!removalsPending && !movesPending && !additionsPending && !changesPending) return;

        for (final ViewHolder holder : mPendingRemovals) {
            ViewCompat.animate(holder.itemView).alpha(0).setDuration(getRemoveDuration()).setInterpolator(mInterpolator)
                    .setStartDelay(Math.abs(holder.getOldPosition() * getRemoveDuration() >> 2))
                    .setListener(new DefaultRemoveVpaListener(holder)).start();
            mRemoveAnimations.add(holder);
        }
        mPendingRemovals.clear();

        if (movesPending) {
            final ArrayList<MoveInfo> moves = new ArrayList<>(mPendingMoves);
            mMovesList.add(moves);
            mPendingMoves.clear();

            final Runnable mover = () -> {
                if (mMovesList.remove(moves)) {
                    for (MoveInfo moveInfo : moves) {
                        final int deltaX = moveInfo.toX - moveInfo.fromX;
                        final int deltaY = moveInfo.toY - moveInfo.fromY;

                        if (deltaX != 0) ViewCompat.animate(moveInfo.holder.itemView).translationX(0);
                        if (deltaY != 0) ViewCompat.animate(moveInfo.holder.itemView).translationY(0);

                        // TODO: make EndActions end listeners instead, since end actions aren't called when
                        //  vpas are canceled (and can't end them. why?) need listener functionality in VPACompat for this.
                        mMoveAnimations.add(moveInfo.holder);

                        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(moveInfo.holder.itemView);
                        animation.setDuration(getMoveDuration()).setListener(new ViewPropertyAnimatorListener() {
                            @Override
                            public void onAnimationStart(View view) {
                                dispatchMoveStarting(moveInfo.holder);
                            }

                            @Override
                            public void onAnimationCancel(View view) {
                                if (deltaX != 0) view.setTranslationX(0);
                                if (deltaY != 0) view.setTranslationY(0);
                            }

                            @Override
                            public void onAnimationEnd(View view) {
                                animation.setListener(null);
                                dispatchMoveFinished(moveInfo.holder);
                                mMoveAnimations.remove(moveInfo.holder);
                                dispatchFinishedWhenDone();
                            }
                        }).start();
                    }
                    moves.clear();
                }
            };
            if (removalsPending)
                ViewCompat.postOnAnimationDelayed(moves.get(0).holder.itemView, mover, getRemoveDuration());
            else mover.run();
        }

        if (changesPending) {
            final ArrayList<ChangeInfo> changes = new ArrayList<>(mPendingChanges);

            mChangesList.add(changes);
            mPendingChanges.clear();

            final Runnable changer = () -> {
                if (mChangesList.remove(changes)) {
                    for (final ChangeInfo changeInfo : changes) {
                        final View view = changeInfo.oldHolder == null ? null : changeInfo.oldHolder.itemView;
                        final View newView = changeInfo.newHolder == null ? null : changeInfo.newHolder.itemView;

                        if (view != null) {
                            mChangeAnimations.add(changeInfo.oldHolder);
                            final ViewPropertyAnimatorCompat oldViewAnim = ViewCompat.animate(view).setDuration(getChangeDuration());
                            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX).translationY(changeInfo.toY - changeInfo.fromY)
                                    .alpha(0).setListener(new ViewPropertyAnimatorListener() {
                                @Override
                                public void onAnimationStart(View view) {
                                    dispatchChangeStarting(changeInfo.oldHolder, true);
                                }

                                @Override
                                public void onAnimationEnd(View view) {
                                    oldViewAnim.setListener(null);
                                    view.setAlpha(1);
                                    view.setTranslationX(0);
                                    view.setTranslationY(0);
                                    dispatchChangeFinished(changeInfo.oldHolder, true);
                                    mChangeAnimations.remove(changeInfo.oldHolder);
                                    dispatchFinishedWhenDone();
                                }

                                @Override
                                public void onAnimationCancel(View view) {}
                            }).start();
                        }
                        if (newView != null) {
                            mChangeAnimations.add(changeInfo.newHolder);

                            final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
                            newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration()).alpha(1)
                                    .setListener(new ViewPropertyAnimatorListener() {
                                        @Override
                                        public void onAnimationStart(View view) {
                                            dispatchChangeStarting(changeInfo.newHolder, false);
                                        }

                                        @Override
                                        public void onAnimationEnd(View view) {
                                            newViewAnimation.setListener(null);
                                            newView.setAlpha(1);
                                            newView.setTranslationX(0);
                                            newView.setTranslationY(0);
                                            dispatchChangeFinished(changeInfo.newHolder, false);
                                            mChangeAnimations.remove(changeInfo.newHolder);
                                            dispatchFinishedWhenDone();
                                        }

                                        @Override
                                        public void onAnimationCancel(View view) {}
                                    }).start();
                        }
                    }
                    changes.clear();
                }
            };

            if (removalsPending)
                ViewCompat.postOnAnimationDelayed(changes.get(0).oldHolder.itemView, changer, getRemoveDuration());
            else changer.run();
        }

        if (additionsPending) {
            final ArrayList<ViewHolder> additions = new ArrayList<>(mPendingAdditions);
            mAdditionsList.add(additions);
            mPendingAdditions.clear();

            final Runnable adder = () -> {
                if (mAdditionsList.remove(additions)) {
                    for (final ViewHolder holder : additions) {
                        ViewCompat.animate(holder.itemView).alpha(1).setDuration(getAddDuration())
                                .setInterpolator(mInterpolator).setListener(new DefaultAddVpaListener(holder))
                                .setStartDelay(Math.abs(holder.getAdapterPosition() * getAddDuration() >> 2)).start();
                        mAddAnimations.add(holder);
                    }
                    additions.clear();
                }
            };

            if (removalsPending || movesPending || changesPending) {
                final long removeDuration = removalsPending ? getRemoveDuration() : 0,
                        moveDuration = movesPending ? getMoveDuration() : 0,
                        changeDuration = changesPending ? getChangeDuration() : 0,
                        totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
                ViewCompat.postOnAnimationDelayed(additions.get(0).itemView, adder, totalDelay);
            } else adder.run();
        }
    }

    @Override
    public boolean animateRemove(final ViewHolder holder) {
        endAnimation(holder);
        cleanup(holder.itemView);
        mPendingRemovals.add(holder);
        return true;
    }

    @Override
    public boolean animateAdd(final ViewHolder holder) {
        endAnimation(holder);
        cleanup(holder.itemView);
        holder.itemView.setAlpha(0);
        mPendingAdditions.add(holder);
        return true;
    }

    @Override
    public boolean animateMove(@NonNull final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        fromX += holder.itemView.getTranslationX();
        fromY += holder.itemView.getTranslationY();
        endAnimation(holder);

        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;

        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }

        if (deltaX != 0) holder.itemView.setTranslationX(-deltaX);
        if (deltaY != 0) holder.itemView.setTranslationY(-deltaY);

        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    @Override
    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (oldHolder == newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY);
        }

        final float prevAlpha = oldHolder.itemView.getAlpha(), prevTransX = oldHolder.itemView.getTranslationX(),
                prevTransY = oldHolder.itemView.getTranslationY();
        endAnimation(oldHolder);

        final int deltaX = (int) (toX - fromX - prevTransX);
        final int deltaY = (int) (toY - fromY - prevTransY);

        // recover prev translation state after ending animation
        oldHolder.itemView.setTranslationX(prevTransX);
        oldHolder.itemView.setTranslationY(prevTransY);
        oldHolder.itemView.setAlpha(prevAlpha);

        if (newHolder != null) {
            endAnimation(newHolder);
            newHolder.itemView.setTranslationX(-deltaX);
            newHolder.itemView.setTranslationY(-deltaY);
            newHolder.itemView.setAlpha(0);
        }

        mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    @Override
    public void endAnimation(@NonNull ViewHolder item) {
        ViewCompat.animate(item.itemView).cancel();

        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            final MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                item.itemView.setTranslationY(0);
                item.itemView.setTranslationX(0);
                dispatchMoveFinished(item);
                mPendingMoves.remove(i);
            }
        }

        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            cleanup(item.itemView);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            cleanup(item.itemView);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            final ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) mChangesList.remove(i);
        }

        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            final ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                final MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    item.itemView.setTranslationY(0);
                    item.itemView.setTranslationX(0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) mMovesList.remove(i);
                    break;
                }
            }
        }

        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            final ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                cleanup(item.itemView);
                dispatchAddFinished(item);
                if (additions.isEmpty()) mAdditionsList.remove(i);
            }
        }

        if (mRemoveAnimations.remove(item) && DEBUG)
            throw new IllegalStateException("after animation is cancelled, item should not be in mRemoveAnimations list");

        if (mAddAnimations.remove(item) && DEBUG)
            throw new IllegalStateException("after animation is cancelled, item should not be in mAddAnimations list");

        if (mChangeAnimations.remove(item) && DEBUG)
            throw new IllegalStateException("after animation is cancelled, item should not be in mChangeAnimations list");

        if (mMoveAnimations.remove(item) && DEBUG)
            throw new IllegalStateException("after animation is cancelled, item should not be in mMoveAnimations list");

        dispatchFinishedWhenDone();
    }

    @Override
    public boolean isRunning() {
        return !mPendingAdditions.isEmpty() || !mPendingChanges.isEmpty() || !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() || !mMoveAnimations.isEmpty() || !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() || !mChangeAnimations.isEmpty() || !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() || !mChangesList.isEmpty();
    }

    @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            final MoveInfo item = mPendingMoves.get(i);
            item.holder.itemView.setTranslationY(0);
            item.holder.itemView.setTranslationX(0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(i);
        }

        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            final ViewHolder item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(i);
        }

        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            final ViewHolder item = mPendingAdditions.get(i);
            cleanup(item.itemView);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }

        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--)
            endChangeAnimationIfNecessary(mPendingChanges.get(i));

        mPendingChanges.clear();
        if (!isRunning()) return;

        int listCount = mMovesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            final ArrayList<MoveInfo> moves = mMovesList.get(i);
            count = moves.size();
            for (int j = count - 1; j >= 0; j--) {
                final MoveInfo moveInfo = moves.get(j);
                moveInfo.holder.itemView.setTranslationY(0);
                moveInfo.holder.itemView.setTranslationX(0);
                dispatchMoveFinished(moveInfo.holder);
                moves.remove(j);
                if (moves.isEmpty()) mMovesList.remove(moves);
            }
        }

        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            final ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                final ViewHolder item = additions.get(j);
                item.itemView.setAlpha(1);

                dispatchAddFinished(item);

                //this check prevent exception when removal already happened during finishing animation
                if (j < additions.size()) additions.remove(j);
                if (additions.isEmpty()) mAdditionsList.remove(additions);
            }
        }

        listCount = mChangesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            final ArrayList<ChangeInfo> changes = mChangesList.get(i);
            count = changes.size();
            for (int j = count - 1; j >= 0; j--) {
                endChangeAnimationIfNecessary(changes.get(j));
                if (changes.isEmpty()) mChangesList.remove(changes);
            }
        }

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    private void endChangeAnimationIfNecessary(@NonNull ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        if (changeInfo.newHolder != null) endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
    }

    private boolean endChangeAnimationIfNecessary(@NonNull ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;

        if (changeInfo.newHolder == item) changeInfo.newHolder = null;
        else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null;
            oldItem = true;
        } else return false;

        item.itemView.setAlpha(1);
        item.itemView.setTranslationX(0);
        item.itemView.setTranslationY(0);

        dispatchChangeFinished(item, oldItem);
        return true;
    }

    private void endChangeAnimation(@NonNull List<ChangeInfo> infoList, ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            final ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item) &&
                    changeInfo.oldHolder == null && changeInfo.newHolder == null)
                infoList.remove(changeInfo);
        }
    }

    private void dispatchFinishedWhenDone() {
        if (!isRunning()) dispatchAnimationsFinished();
    }

    private void cancelAll(@NonNull List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--)
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
    }

    private static class MoveInfo {
        private final ViewHolder holder;
        private final int fromX;
        private final int fromY;
        private final int toX;
        private final int toY;

        private MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private static class ChangeInfo {
        private ViewHolder oldHolder, newHolder;
        private int fromX, fromY, toX, toY;

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX,
                int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @NonNull
        @Override
        public String toString() {
            return "ChangeInfo{oldHolder=" + oldHolder + ", newHolder=" + newHolder + ", fromX=" + fromX +
                    ", fromY=" + fromY + ", toX=" + toX + ", toY=" + toY + '}';
        }
    }

    private class DefaultAddVpaListener implements ViewPropertyAnimatorListener {
        private final RecyclerView.ViewHolder mViewHolder;

        DefaultAddVpaListener(final RecyclerView.ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchAddStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            cleanup(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            cleanup(view);
            dispatchAddFinished(mViewHolder);
            mAddAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }

    private class DefaultRemoveVpaListener implements ViewPropertyAnimatorListener {
        private final RecyclerView.ViewHolder mViewHolder;

        private DefaultRemoveVpaListener(final RecyclerView.ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchRemoveStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            cleanup(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            cleanup(view);
            dispatchRemoveFinished(mViewHolder);
            mRemoveAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }
}