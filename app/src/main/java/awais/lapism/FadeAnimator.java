package awais.lapism;

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

public final class FadeAnimator extends SimpleItemAnimator {
    private final Interpolator interpolator = new DecelerateInterpolator();
    private final ArrayList<ViewHolder> pendingAdditions = new ArrayList<>();
    private final ArrayList<ViewHolder> changeAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> removeAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> pendingRemovals = new ArrayList<>();
    private final ArrayList<ViewHolder> moveAnimations = new ArrayList<>();
    private final ArrayList<ViewHolder> addAnimations = new ArrayList<>();
    private final ArrayList<ChangeInfo> pendingChanges = new ArrayList<>();
    private final ArrayList<MoveInfo> pendingMoves = new ArrayList<>();
    private final ArrayList<ArrayList<ViewHolder>> additionsList = new ArrayList<>();
    private final ArrayList<ArrayList<ChangeInfo>> changesList = new ArrayList<>();
    private final ArrayList<ArrayList<MoveInfo>> movesList = new ArrayList<>();

    public FadeAnimator() {
        super();
        setSupportsChangeAnimations(false);
    }

    @Override
    public void runPendingAnimations() {
        final boolean movesPending = !pendingMoves.isEmpty();
        final boolean changesPending = !pendingChanges.isEmpty();
        final boolean removalsPending = !pendingRemovals.isEmpty();
        final boolean additionsPending = !pendingAdditions.isEmpty();

        if (movesPending || changesPending || removalsPending || additionsPending) {
            for (final ViewHolder holder : pendingRemovals) {
                ViewCompat.animate(holder.itemView).alpha(0).setDuration(getRemoveDuration()).setInterpolator(interpolator)
                        .setStartDelay(Math.abs(holder.getOldPosition() * getRemoveDuration() >> 2))
                        .setListener(new DefaultRemoveVpaListener(holder)).start();
                removeAnimations.add(holder);
            }
            pendingRemovals.clear();

            if (movesPending) {
                final ArrayList<MoveInfo> moves = new ArrayList<>(pendingMoves);
                movesList.add(moves);
                pendingMoves.clear();

                final Runnable mover = () -> {
                    if (movesList.remove(moves)) {
                        for (MoveInfo moveInfo : moves) {
                            final int deltaX = moveInfo.toX - moveInfo.fromX;
                            final int deltaY = moveInfo.toY - moveInfo.fromY;

                            if (deltaX != 0) ViewCompat.animate(moveInfo.holder.itemView).translationX(0);
                            if (deltaY != 0) ViewCompat.animate(moveInfo.holder.itemView).translationY(0);

                            // TODO: make EndActions end listeners instead, since end actions aren't called when
                            //  vpas are canceled (and can't end them. why?) need listener functionality in VPACompat for this.
                            moveAnimations.add(moveInfo.holder);

                            final ViewPropertyAnimatorCompat animation = ViewCompat.animate(moveInfo.holder.itemView);
                            animation.setDuration(getMoveDuration()).setListener(new ViewPropertyAnimatorListener() {
                                @Override
                                public void onAnimationStart(final View view) {
                                    dispatchMoveStarting(moveInfo.holder);
                                }

                                @Override
                                public void onAnimationCancel(final View view) {
                                    if (deltaX != 0) view.setTranslationX(0);
                                    if (deltaY != 0) view.setTranslationY(0);
                                }

                                @Override
                                public void onAnimationEnd(final View view) {
                                    animation.setListener(null);
                                    dispatchMoveFinished(moveInfo.holder);
                                    moveAnimations.remove(moveInfo.holder);
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
                final ArrayList<ChangeInfo> changes = new ArrayList<>(pendingChanges);

                changesList.add(changes);
                pendingChanges.clear();

                final Runnable changer = () -> {
                    if (changesList.remove(changes)) {
                        for (final ChangeInfo changeInfo : changes) {
                            final View view = changeInfo.oldHolder == null ? null : changeInfo.oldHolder.itemView;
                            final View newView = changeInfo.newHolder == null ? null : changeInfo.newHolder.itemView;

                            if (view != null) {
                                changeAnimations.add(changeInfo.oldHolder);
                                final ViewPropertyAnimatorCompat oldViewAnim = ViewCompat.animate(view).setDuration(getChangeDuration());
                                oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX).translationY(changeInfo.toY - changeInfo.fromY)
                                        .alpha(0).setListener(new ViewPropertyAnimatorListener() {
                                    @Override
                                    public void onAnimationStart(final View view) {
                                        dispatchChangeStarting(changeInfo.oldHolder, true);
                                    }

                                    @Override
                                    public void onAnimationEnd(final View view) {
                                        oldViewAnim.setListener(null);
                                        view.setAlpha(1);
                                        view.setTranslationX(0);
                                        view.setTranslationY(0);
                                        dispatchChangeFinished(changeInfo.oldHolder, true);
                                        changeAnimations.remove(changeInfo.oldHolder);
                                        dispatchFinishedWhenDone();
                                    }

                                    @Override
                                    public void onAnimationCancel(final View view) {}
                                }).start();
                            }
                            if (newView != null) {
                                changeAnimations.add(changeInfo.newHolder);

                                final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
                                newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration()).alpha(1)
                                        .setListener(new ViewPropertyAnimatorListener() {
                                            @Override
                                            public void onAnimationStart(final View view) {
                                                dispatchChangeStarting(changeInfo.newHolder, false);
                                            }

                                            @Override
                                            public void onAnimationEnd(final View view) {
                                                newViewAnimation.setListener(null);
                                                newView.setAlpha(1);
                                                newView.setTranslationX(0);
                                                newView.setTranslationY(0);
                                                dispatchChangeFinished(changeInfo.newHolder, false);
                                                changeAnimations.remove(changeInfo.newHolder);
                                                dispatchFinishedWhenDone();
                                            }

                                            @Override
                                            public void onAnimationCancel(final View view) {}
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
                final ArrayList<ViewHolder> additions = new ArrayList<>(pendingAdditions);
                additionsList.add(additions);
                pendingAdditions.clear();

                final Runnable adder = () -> {
                    if (additionsList.remove(additions)) {
                        for (final ViewHolder holder : additions) {
                            ViewCompat.animate(holder.itemView).alpha(1).setDuration(getAddDuration())
                                    .setInterpolator(interpolator).setListener(new DefaultAddVpaListener(holder))
                                    .setStartDelay(Math.abs(holder.getBindingAdapterPosition() * getAddDuration() >> 2)).start();
                            addAnimations.add(holder);
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
    }

    @Override
    public boolean animateRemove(final ViewHolder holder) {
        endAnimation(holder);
        cleanup(holder.itemView);
        pendingRemovals.add(holder);
        return true;
    }

    @Override
    public boolean animateAdd(final ViewHolder holder) {
        endAnimation(holder);
        cleanup(holder.itemView);
        holder.itemView.setAlpha(0);
        pendingAdditions.add(holder);
        return true;
    }

    @Override
    public boolean animateMove(@NonNull final ViewHolder holder, int fromX, int fromY, final int toX, final int toY) {
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

        pendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    @Override
    public boolean animateChange(final ViewHolder oldHolder, final ViewHolder newHolder, final int fromX, final int fromY, final int toX, final int toY) {
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

        pendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    @Override
    public void endAnimation(@NonNull final ViewHolder item) {
        ViewCompat.animate(item.itemView).cancel();

        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (int i = pendingMoves.size() - 1; i >= 0; i--) {
            final MoveInfo moveInfo = pendingMoves.get(i);
            if (moveInfo.holder == item) {
                item.itemView.setTranslationY(0);
                item.itemView.setTranslationX(0);
                dispatchMoveFinished(item);
                pendingMoves.remove(i);
            }
        }

        endChangeAnimation(pendingChanges, item);
        if (pendingRemovals.remove(item)) {
            cleanup(item.itemView);
            dispatchRemoveFinished(item);
        }
        if (pendingAdditions.remove(item)) {
            cleanup(item.itemView);
            dispatchAddFinished(item);
        }

        for (int i = changesList.size() - 1; i >= 0; i--) {
            final ArrayList<ChangeInfo> changes = changesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) changesList.remove(i);
        }

        for (int i = movesList.size() - 1; i >= 0; i--) {
            final ArrayList<MoveInfo> moves = movesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                final MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    item.itemView.setTranslationY(0);
                    item.itemView.setTranslationX(0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) movesList.remove(i);
                    break;
                }
            }
        }

        for (int i = additionsList.size() - 1; i >= 0; i--) {
            final ArrayList<ViewHolder> additions = additionsList.get(i);
            if (additions.remove(item)) {
                cleanup(item.itemView);
                dispatchAddFinished(item);
                if (additions.isEmpty()) additionsList.remove(i);
            }
        }

        removeAnimations.remove(item);
        addAnimations.remove(item);
        changeAnimations.remove(item);
        moveAnimations.remove(item);

        dispatchFinishedWhenDone();
    }

    @Override
    public boolean isRunning() {
        return !pendingAdditions.isEmpty() || !pendingChanges.isEmpty() || !pendingMoves.isEmpty() ||
                !pendingRemovals.isEmpty() || !moveAnimations.isEmpty() || !removeAnimations.isEmpty() ||
                !addAnimations.isEmpty() || !changeAnimations.isEmpty() || !movesList.isEmpty() ||
                !additionsList.isEmpty() || !changesList.isEmpty();
    }

    @Override
    public void endAnimations() {
        int count = pendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            final MoveInfo item = pendingMoves.get(i);
            item.holder.itemView.setTranslationY(0);
            item.holder.itemView.setTranslationX(0);
            dispatchMoveFinished(item.holder);
            pendingMoves.remove(i);
        }

        count = pendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            final ViewHolder item = pendingRemovals.get(i);
            dispatchRemoveFinished(item);
            pendingRemovals.remove(i);
        }

        count = pendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            final ViewHolder item = pendingAdditions.get(i);
            cleanup(item.itemView);
            dispatchAddFinished(item);
            pendingAdditions.remove(i);
        }

        count = pendingChanges.size();
        for (int i = count - 1; i >= 0; i--)
            endChangeAnimationIfNecessary(pendingChanges.get(i));

        pendingChanges.clear();
        if (isRunning()) {
            int listCount = movesList.size();
            for (int i = listCount - 1; i >= 0; i--) {
                final ArrayList<MoveInfo> moves = movesList.get(i);
                count = moves.size();
                for (int j = count - 1; j >= 0; j--) {
                    final MoveInfo moveInfo = moves.get(j);
                    moveInfo.holder.itemView.setTranslationY(0);
                    moveInfo.holder.itemView.setTranslationX(0);
                    dispatchMoveFinished(moveInfo.holder);
                    moves.remove(j);
                    if (moves.isEmpty()) movesList.remove(moves);
                }
            }

            listCount = additionsList.size();
            for (int i = listCount - 1; i >= 0; i--) {
                final ArrayList<ViewHolder> additions = additionsList.get(i);
                count = additions.size();
                for (int j = count - 1; j >= 0; j--) {
                    final ViewHolder item = additions.get(j);
                    item.itemView.setAlpha(1);

                    dispatchAddFinished(item);

                    //this check prevent exception when removal already happened during finishing animation
                    if (j < additions.size()) additions.remove(j);
                    if (additions.isEmpty()) additionsList.remove(additions);
                }
            }

            listCount = changesList.size();
            for (int i = listCount - 1; i >= 0; i--) {
                final ArrayList<ChangeInfo> changes = changesList.get(i);
                count = changes.size();
                for (int j = count - 1; j >= 0; j--) {
                    endChangeAnimationIfNecessary(changes.get(j));
                    if (changes.isEmpty()) changesList.remove(changes);
                }
            }

            cancelAll(removeAnimations);
            cancelAll(moveAnimations);
            cancelAll(addAnimations);
            cancelAll(changeAnimations);

            dispatchAnimationsFinished();
        }
    }

    private void endChangeAnimationIfNecessary(@NonNull final ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        if (changeInfo.newHolder != null) endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
    }

    private boolean endChangeAnimationIfNecessary(@NonNull final ChangeInfo changeInfo, final ViewHolder item) {
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

    private void endChangeAnimation(@NonNull final List<ChangeInfo> infoList, final ViewHolder item) {
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

    private void cancelAll(@NonNull final List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--)
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
    }

    private static void cleanup(@NonNull final View v) {
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

    private final class DefaultAddVpaListener implements ViewPropertyAnimatorListener {
        private final RecyclerView.ViewHolder viewHolder;

        DefaultAddVpaListener(final RecyclerView.ViewHolder holder) {
            viewHolder = holder;
        }

        @Override
        public void onAnimationStart(final View view) {
            dispatchAddStarting(viewHolder);
        }

        @Override
        public void onAnimationCancel(final View view) {
            cleanup(view);
        }

        @Override
        public void onAnimationEnd(final View view) {
            cleanup(view);
            dispatchAddFinished(viewHolder);
            addAnimations.remove(viewHolder);
            dispatchFinishedWhenDone();
        }
    }

    private final class DefaultRemoveVpaListener implements ViewPropertyAnimatorListener {
        private final RecyclerView.ViewHolder viewHolder;

        private DefaultRemoveVpaListener(final RecyclerView.ViewHolder holder) {
            viewHolder = holder;
        }

        @Override
        public void onAnimationStart(final View view) {
            dispatchRemoveStarting(viewHolder);
        }

        @Override
        public void onAnimationCancel(final View view) {
            cleanup(view);
        }

        @Override
        public void onAnimationEnd(final View view) {
            cleanup(view);
            dispatchRemoveFinished(viewHolder);
            removeAnimations.remove(viewHolder);
            dispatchFinishedWhenDone();
        }
    }

    private final static class MoveInfo {
        private final ViewHolder holder;
        private final int fromX;
        private final int fromY;
        private final int toX;
        private final int toY;

        private MoveInfo(final ViewHolder holder, final int fromX, final int fromY, final int toX, final int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private final static class ChangeInfo {
        private ViewHolder oldHolder, newHolder;
        private int fromX, fromY, toX, toY;

        private ChangeInfo(final ViewHolder oldHolder, final ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(final ViewHolder oldHolder, final ViewHolder newHolder, final int fromX, final int fromY, final int toX,
                           final int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}