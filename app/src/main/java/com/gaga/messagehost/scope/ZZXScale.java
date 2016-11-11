package com.gaga.messagehost.scope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ZZXScale extends View {
    private static final int HEIGHT_FRACTION = 32;

    private int width;
    private int height;

    private Paint paint;
    protected ZZShowScopeActivity main;
    //mine
    //每页显示500的数据的话，每小格显示10个，singleSize = dataStepX * 10;
    protected float dataStep;//每个数据所跨越的像素点数
    protected float singleSize;//每一小格所跨越的像素点数
    protected float dataStart;//数据开始的像素点
    protected float dataStop;//数据结束的x轴像素点
    protected float startX = 0;//坐标轴的起点

    private int[] xLabel = new int[]{100,200,300,400,500};

    public ZZXScale(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create paint
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get offered dimension
        int w = MeasureSpec.getSize(widthMeasureSpec);

        // Set wanted dimensions
        setMeasuredDimension(w, w / HEIGHT_FRACTION);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get actual dimensions
        width = w;
        height = h;

        singleSize = ( (float) (width-ZZScope.SCOPERESERVE_XCALE) * 10.0f) / 500;
        dataStep = singleSize/10.0f;
        dataStart = 0;
        dataStop = singleSize*50.0f;

        main.zzyscale.singleSize = singleSize;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {
        // Set up paint
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        // 画短棍
        for (float i = startX; i < width; i += singleSize)
            canvas.drawLine(i, 0, i, height / 4, paint);

        // 画长棍
        for (float i = startX; i < width; i += singleSize*5.0f)
            canvas.drawLine(i, 0, i, height / 3, paint);

        // Set up paint
        paint.setAntiAlias(true);
        paint.setTextSize(height * 2 / 3);
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw scale
        canvas.drawText("poi", 0, height - (height / 6), paint);
        int j=0;
        float k= (float) ZZShowScopeActivity.numEveryPage/5;
        for (int x=0;x<5;x++){
            xLabel[x] = Math.round(k*(x+1));
        }

        for (int i = (int) Math.round(singleSize * 10.0); i < width-90;
             i += (int) Math.round(singleSize * 10.0)) {
            String s = String.format("%d",xLabel[j]+main.curOffset);
            j++;
            canvas.drawText(s, i, height - (height / 8), paint);
        }

    }
}
