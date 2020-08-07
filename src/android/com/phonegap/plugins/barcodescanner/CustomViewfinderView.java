package com.phonegap.plugins.barcodescanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.List;

public class CustomViewfinderView extends ViewfinderView {
    public CustomViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private int customLaserColor = Color.parseColor("#FF0000");
    private int borderColor = Color.parseColor("#FFFFFF");
    private int borderThickness = 15;
    private int divideDistanceWith = 4;

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

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        borderPaint.setColor(borderColor);

        //inside onDraw
        int distance = (frame.bottom - frame.top) / divideDistanceWith;
        float corners = borderThickness + 5;
        int correctionMargin = borderThickness - 3;
        int correctionMarginForBackground = divideDistanceWith == 1 ? 0 : borderThickness / 2;

        canvas.drawRect(0, 0, width, frame.top - correctionMarginForBackground, paint);
        canvas.drawRect(0, frame.top -  correctionMarginForBackground, frame.left - correctionMarginForBackground, frame.bottom + correctionMarginForBackground, paint);
        canvas.drawRect(frame.right + correctionMarginForBackground, frame.top - correctionMarginForBackground, width, frame.bottom + correctionMarginForBackground, paint);
        canvas.drawRect(0, frame.bottom + correctionMarginForBackground, width, height, paint);

        //top left corner
        canvas.drawRoundRect(frame.left - borderThickness, frame.top - borderThickness, distance + frame.left, frame.top, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.left - borderThickness, frame.top - correctionMargin , frame.left, distance + frame.top, corners, corners, borderPaint);

        //top right corner
        canvas.drawRoundRect(frame.right - distance, frame.top - borderThickness, frame.right + borderThickness, frame.top, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.right, frame.top - correctionMargin, frame.right + borderThickness, distance + frame.top, corners, corners, borderPaint);

        //bottom left corner
        canvas.drawRoundRect(frame.left - borderThickness, frame.bottom, distance + frame.left, frame.bottom + borderThickness, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.left - borderThickness, frame.bottom - distance, frame.left, frame.bottom + correctionMargin, corners, corners, borderPaint);

        //bottom right corner
        canvas.drawRoundRect(frame.right - distance, frame.bottom, frame.right + borderThickness, frame.bottom + borderThickness, corners, corners, borderPaint);
        canvas.drawRoundRect(frame.right, frame.bottom - distance, frame.right + borderThickness, frame.bottom + correctionMargin, corners, corners, borderPaint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            // If wanted, draw a red "laser scanner" line through the middle to show decoding is active
            if (laserVisibility) {
                paint.setColor(customLaserColor);

//                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

                final int middle = frame.height() / 2 + frame.top;
                canvas.drawRect(frame.left - correctionMarginForBackground, middle - 1, frame.right + correctionMarginForBackground, middle + 2, paint);
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

    public void setDivideDistanceWith(int size) {
        if(size != 0){
            divideDistanceWith = size;
        }
        else {
            divideDistanceWith = 1;
        }
    }

    public void setBorderThickness(int size) {
        borderThickness = size;
    }

    public void setBorderColor(String color) {
        try{
            int newColor = Color.parseColor(color);
            borderColor = newColor;
        }
        catch (Exception e){
            borderColor = Color.parseColor("#FFFFFF");
        }
    }

    public void setLaserColor(String color) {
        try{
            int newColor = Color.parseColor(color);
            customLaserColor = newColor;
        }
        catch (Exception e){
            customLaserColor = laserColor;
        }
    }
}
