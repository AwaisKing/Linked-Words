package awais.sephiroth.xtooltip;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.ObjectsCompat;

import awais.backworddictionary.R;

public final class TooltipTextDrawable extends Drawable {
    public static final float ARROW_RATIO_DEFAULT = 1.4f;

    private final Rect outlineRect = new Rect();
    private final RectF rectF = new RectF();
    private final PointF tmpPoint = new PointF();
    private final Path path = new Path();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float radius;
    private int padding = 0, arrowWeight = 0;
    private PointF point = null;
    private Tooltip.Gravity gravity = null;

    public TooltipTextDrawable(@NonNull final Context context) {
        radius = context.getResources().getDimension(R.dimen.ttlm_default_radius);
        bgPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.drawPath(path, bgPaint);
    }

    void setAnchor(final Tooltip.Gravity gravity, final int padding, final PointF point) {
        if (gravity != this.gravity || padding != this.padding || !ObjectsCompat.equals(this.point, point)) {
            this.gravity = gravity;
            this.padding = padding;
            this.arrowWeight = (int) ((float) padding / ARROW_RATIO_DEFAULT);
            this.point = point == null ? null : new PointF(point.x, point.y);
            final Rect bounds = getBounds();
            if (!bounds.isEmpty()) {
                calculatePath(bounds);
                invalidateSelf();
            }
        }
    }

    private void calculatePath(@NonNull final Rect outBounds) {
        final int left = outBounds.left + padding;
        final int top = outBounds.top + padding;
        final int right = outBounds.right - padding;
        final int bottom = outBounds.bottom - padding;

        final float maxY = bottom - radius;
        final float maxX = right - radius;
        final float minY = top + radius;
        final float minX = left + radius;

        if (null != point && null != gravity) {
            calculatePathWithGravity(outBounds, left, top, right, bottom, maxY, maxX, minY, minX);
        } else {
            rectF.set((float) left, (float) top, (float) right, (float) bottom);
            path.addRoundRect(rectF, radius, radius, Path.Direction.CW);
        }
    }

    private void calculatePathWithGravity(final Rect outBounds, final int left, final int top, final int right, final int bottom,
                                          final float maxY, final float maxX, final float minY, final float minX) {
        if (gravity == Tooltip.Gravity.LEFT || gravity == Tooltip.Gravity.RIGHT) {
            if (maxY - minY < arrowWeight * 2) {
                arrowWeight = (int) Math.floor((maxY - minY) / 2);
            }
        } else if (gravity == Tooltip.Gravity.BOTTOM || gravity == Tooltip.Gravity.TOP) {
            if (maxX - minX < arrowWeight * 2) {
                arrowWeight = (int) Math.floor((maxX - minX) / 2);
            }
        }

        final boolean drawPoint =
                isDrawPoint(left, top, right, bottom, maxY, maxX, minY, minX, tmpPoint, point, gravity, arrowWeight);

        clampPoint(left, top, right, bottom, tmpPoint);

        path.reset();

        // top/left
        path.moveTo(left + radius, (float) top);

        if (drawPoint && gravity == Tooltip.Gravity.BOTTOM) {
            path.lineTo(left + tmpPoint.x - arrowWeight, (float) top);
            path.lineTo(left + tmpPoint.x, (float) outBounds.top);
            path.lineTo(left + tmpPoint.x + arrowWeight, (float) top);
        }

        // top/right
        path.lineTo(right - radius, (float) top);
        path.quadTo((float) right, (float) top, (float) right, top + radius);

        if (drawPoint && gravity == Tooltip.Gravity.LEFT) {
            path.lineTo((float) right, top + tmpPoint.y - arrowWeight);
            path.lineTo((float) outBounds.right, top + tmpPoint.y);
            path.lineTo((float) right, top + tmpPoint.y + arrowWeight);
        }

        // bottom/right
        path.lineTo((float) right, bottom - radius);
        path.quadTo((float) right, (float) bottom, right - radius, (float) bottom);

        if (drawPoint && gravity == Tooltip.Gravity.TOP) {
            path.lineTo(left + tmpPoint.x + arrowWeight, (float) bottom);
            path.lineTo(left + tmpPoint.x, (float) outBounds.bottom);
            path.lineTo(left + tmpPoint.x - arrowWeight, (float) bottom);
        }

        // bottom/left
        path.lineTo(left + radius, (float) bottom);
        path.quadTo((float) left, (float) bottom, (float) left, bottom - radius);

        if (drawPoint && gravity == Tooltip.Gravity.RIGHT) {
            path.lineTo((float) left, top + tmpPoint.y + arrowWeight);
            path.lineTo((float) outBounds.left, top + tmpPoint.y);
            path.lineTo((float) left, top + tmpPoint.y - arrowWeight);
        }

        // top/left
        path.lineTo((float) left, top + radius);
        path.quadTo((float) left, (float) top, left + radius, (float) top);
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        calculatePath(bounds);
    }

    @Override
    public int getAlpha() {
        return bgPaint.getAlpha();
    }

    @Override
    public void setAlpha(final int alpha) {
        bgPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable final ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void getOutline(@NonNull final Outline outline) {
        copyBounds(outlineRect);
        outlineRect.inset(padding, padding);
        outline.setRoundRect(outlineRect, radius);
        if (getAlpha() < 255) outline.setAlpha(0f);
        //outline.setAlpha(getAlpha() / ALPHA_MAX);
    }

    private static boolean isDrawPoint(final int left, final int top, final int right, final int bottom, final float maxY, final float maxX,
                                       final float minY, final float minX, @NonNull final PointF tmpPoint, @NonNull final PointF point,
                                       final Tooltip.Gravity gravity, final int arrowWeight) {
        boolean drawPoint = false;
        tmpPoint.set(point.x, point.y);

        if (gravity == Tooltip.Gravity.RIGHT || gravity == Tooltip.Gravity.LEFT) {
            if (top <= tmpPoint.y && tmpPoint.y <= bottom) {
                if (top + tmpPoint.y + arrowWeight > maxY) {
                    tmpPoint.y = (maxY - (float) arrowWeight - (float) top);
                } else if (top + tmpPoint.y - arrowWeight < minY) {
                    tmpPoint.y = (minY + arrowWeight - top);
                }
                drawPoint = true;
            }
        } else if (left <= tmpPoint.x && tmpPoint.x <= right)
            if (left <= tmpPoint.x && tmpPoint.x <= right) {
                if (left + tmpPoint.x + arrowWeight > maxX) {
                    tmpPoint.x = (maxX - (float) arrowWeight - (float) left);
                } else if (left + tmpPoint.x - arrowWeight < minX) {
                    tmpPoint.x = (minX + arrowWeight - left);
                }
                drawPoint = true;
            }
        return drawPoint;
    }

    private static void clampPoint(final int left, final int top, final int right, final int bottom, @NonNull final PointF tmpPoint) {
        if (tmpPoint.y < top) {
            tmpPoint.y = (float) top;
        } else if (tmpPoint.y > bottom) {
            tmpPoint.y = (float) bottom;
        }
        if (tmpPoint.x < left) {
            tmpPoint.x = (float) left;
        }
        if (tmpPoint.x > right) {
            tmpPoint.x = (float) right;
        }
    }
}