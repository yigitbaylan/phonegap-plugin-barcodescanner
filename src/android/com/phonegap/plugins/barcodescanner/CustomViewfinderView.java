package com.phonegap.plugins.barcodescanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.List;

public class CustomViewfinderView extends ViewfinderView {
    public CustomViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewSize == null) {
            return;
        }

        final Rect frame = framingRect;
        final Size previewSize = this.previewSize;

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        //initialize new paint in the constructor
        String myPassedColor = "#FFFFFF";
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        borderPaint.setColor(Color.parseColor(myPassedColor));

        //inside onDraw
        int distance = (frame.bottom - frame.top) / 4;
        int thickness = 15;
        float corners = 20;
        int correctionMargin = 10;

        canvas.drawRect(0, 0, width, frame.top - 10, paint);
        canvas.drawRect(0, frame.top -  10, frame.left - 10, frame.bottom + 10, paint);
        canvas.drawRect(frame.right + 10, frame.top - 10, width, frame.bottom + 10, paint);
        canvas.drawRect(0, frame.bottom + 10, width, height, paint);

        //top left corner
        canvas.drawRoundRect(frame.left - thickness, frame.top - thickness, distance + frame.left, frame.top, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.left - thickness, frame.top - correctionMargin , frame.left, distance + frame.top, corners, corners, borderPaint);
        //top right corner
        canvas.drawRoundRect(frame.right - distance, frame.top - thickness, frame.right + thickness, frame.top, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.right, frame.top - correctionMargin, frame.right + thickness, distance + frame.top, corners, corners, borderPaint);

        //bottom left corner
        canvas.drawRoundRect(frame.left - thickness, frame.bottom, distance + frame.left, frame.bottom + thickness, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.left - thickness, frame.bottom - distance, frame.left, frame.bottom + correctionMargin, corners, corners, borderPaint);

        //bottom right corner
        canvas.drawRoundRect(frame.right - distance, frame.bottom, frame.right + thickness, frame.bottom + thickness, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.right, frame.bottom - distance, frame.right + thickness, frame.bottom + correctionMargin, corners, corners, borderPaint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            // If wanted, draw a red "laser scanner" line through the middle to show decoding is active
            if (laserVisibility) {
                paint.setColor(laserColor);

                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

                final int middle = frame.height() / 2 + frame.top;
                canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
            }

            final float scaleX = this.getWidth() / (float) previewSize.width;
            final float scaleY = this.getHeight() / (float) previewSize.height;

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                            (int) (point.getX() * scaleX),
                            (int) (point.getY() * scaleY),
                            radius, paint
                    );
                }
                lastPossibleResultPoints.clear();
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (final ResultPoint point : possibleResultPoints) {
                    canvas.drawCircle(
                            (int) (point.getX() * scaleX),
                            (int) (point.getY() * scaleY),
                            POINT_SIZE, paint
                    );
                }

                // swap and clear buffers
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }
}
