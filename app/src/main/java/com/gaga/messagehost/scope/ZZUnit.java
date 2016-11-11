package com.gaga.messagehost.scope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class ZZUnit extends View {
    private int width;
    private int height;

    private Paint paint;

    public ZZUnit(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create paint
        paint = new Paint();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get dinemsions
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStrokeWidth(2);

        // Draw half a tick
        canvas.drawLine(width, 0, width, height / 3, paint);

        // Set up paint
        paint.setAntiAlias(true);
        paint.setTextSize(height * 2 / 3);
        paint.setTextAlign(Align.CENTER);

        canvas.drawText("poi",width,height-(height/6),paint);
    }
}
