package com.gaga.messagehost.scope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ZZYScale extends View {
    private static final int WIDTH_FRACTION = 24;

    private int width;
    private int height;

    protected float index;
    protected float singleSize=20;


    private Matrix matrix;
    private Paint paint;
    private Path thumb;

    public ZZYScale(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create paint
        matrix = new Matrix();
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get offered dimension
        int h = MeasureSpec.getSize(heightMeasureSpec);

        // Set wanted dimensions
        setMeasuredDimension(h / WIDTH_FRACTION, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get actual dimensions
        width = w;
        height = h;

        // Create a path for the thumb
        thumb = new Path();

        thumb.moveTo(-1, -1);
        thumb.lineTo(-1, 1);
        thumb.lineTo(1, 1);
        thumb.lineTo(2, 0);
        thumb.lineTo(1, -1);
        thumb.close();

        // Create a matrix to scale the thumb
        matrix.setScale(width / 4, width / 4);

        // Scale the thumb
        thumb.transform(matrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        canvas.translate(0, height / 2);

        // Draw scale ticks
        for (float i = 0; i < height / 2; i += singleSize) {
            canvas.drawLine(width * 2 / 3, i, width, i, paint);
            canvas.drawLine(width * 2 / 3, -i, width, -i, paint);
        }

        for (float i = 0; i < height / 2; i += singleSize * 5.0) {
            canvas.drawLine(width / 3, i, width, i, paint);
            canvas.drawLine(width / 3, -i, width, -i, paint);
        }

        // Draw sync level thumb if not zero
        if (index != 0) {
            canvas.translate(width / 3, index);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(thumb, paint);
        }
    }

    // On touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Set the index from the touch dimension
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                index = y - (height / 2);
                break;

            case MotionEvent.ACTION_MOVE:
                index = y - (height / 2);
                break;

            case MotionEvent.ACTION_UP:
                index = y - (height / 2);
                break;
        }
        invalidate();
        return true;
    }
}
