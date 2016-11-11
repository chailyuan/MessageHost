package com.gaga.messagehost.scope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// Scope

public class ZZScope extends View {
    private int width;
    private int height;

    public static final int SCOPERESERVE_XCALE = 100;
    public static final int SCOPERESERVE_YSCALE = 50;

    private Path path;
    private Canvas cb;
    private Paint paint;
    private Bitmap bitmap;
    private Bitmap graticule;

    protected float index;

    protected int middleOfZoom;//缩放的中间位置
    protected int oldNumEveryPage;

    //mine
    //每页显示500的数据的话，每小格显示10个，singleSize = dataStepX * 10;
    protected float dataStepX;//每个数据所跨越的像素点数
    protected float dataStepY;//每个数据所跨越的像素点数
    protected float singleSize;//每一小格所跨越的像素点数
    protected float dataStart;//数据开始的像素点

    //缩放标志
    protected boolean ZOOM = false;


    //缩放示波器
    float oldXLeft,oldXRight;//按下去的时候的坐标
    float newXLeft,newXRight;//移动时候的坐标

    protected ZZShowScopeActivity main;
    private long middleOfZoomFromOffset;

    // Scope
    public ZZScope(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create path and paint

        path = new Path();
        paint = new Paint();
    }

    // On size changed

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get dimensions

        width = w;
        height = h;

        singleSize = ( (float) (width- SCOPERESERVE_XCALE) * 10.0f) / 500;
        dataStepX = singleSize/10.0f;
        dataStart = 0;

        dataStepY = (float) (h-SCOPERESERVE_YSCALE)/256.0f;

        // Create a bitmap for trace storage

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cb = new Canvas(bitmap);

        // Create a bitmap for the graticule

        graticule = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(graticule);

        // Black background

        canvas.drawColor(Color.BLACK);

        // Set up paint

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(255, 0, 63, 0));

        // Draw graticule

        for (float i = 0; i < width; i += singleSize*5)
            canvas.drawLine(i, 0, i, height, paint);

        canvas.translate(0, height / 2);

        for (int i = 0; i < height / 2; i += singleSize*5) {
            canvas.drawLine(0, i, width, i, paint);
            canvas.drawLine(0, -i, width, -i, paint);
        }

        // Draw the graticule on the bitmap

        cb.drawBitmap(graticule, 0, 0, null);

        //更换坐标原点
        cb.translate(0, height / 2);
    }

    private int max;

    // On draw

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {

        //清空原先的显示内容，只显示基本的网格
        cb.drawBitmap(graticule, 0, -height / 2, null);

        dataStepX = ( (float) (width- SCOPERESERVE_XCALE)) / ZZShowScopeActivity.numEveryPage;

        //重画路径
        path.rewind();
        path.moveTo(0, 0);

        for (int i=0; i<main.numRead+1 && i* dataStepX<width-SCOPERESERVE_XCALE; i++){
            float x = (float)i* dataStepX;
            float y = -(float) main.data[i] * dataStepY;
            path.lineTo(x,y);
        }

        // Green trace
        //画路径
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        cb.drawPath(path, paint);

        // Draw index
        //点击scope后显示标尺
        if (index > 0 && index < width- SCOPERESERVE_XCALE) {
            // Yellow index
            paint.setColor(Color.YELLOW);

            paint.setAntiAlias(false);
            cb.drawLine(index, -height / 2, index, height / 2, paint);

            paint.setAntiAlias(true);
            paint.setTextSize(height / 24);
            paint.setTextAlign(Paint.Align.LEFT);

            // Get value
            int i = Math.round(index / dataStepX);

            float x = (float) i * dataStepX;
            float y = -main.data[i] *dataStepY;

            // Draw value
            String s = String.format("%d",
                    main.data[i]);
            cb.drawText(s, x, y, paint);

            paint.setTextAlign(Paint.Align.CENTER);

            // Draw time value
            s = String.format("%d",i+main.curOffset);

            cb.drawText(s, x, height / 2, paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    // On touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        //读取手指数
        int pointerCount = event.getPointerCount();

        switch (event.getAction()&MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (pointerCount==1){
                    index = x;
                }
                break;
            //第二个手指按下
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount==2){
                    ZOOM = true;

                    float x1 = event.getX(0);
                    float x2 = event.getX(1);
                    oldXLeft = Math.min(x1,x2);
                    oldXRight = Math.max(x1,x2);
                    middleOfZoom = Math.round(((oldXLeft+oldXRight)/2) / dataStepX);
                    oldNumEveryPage = ZZShowScopeActivity.numEveryPage;
                    middleOfZoomFromOffset = main.curOffset+middleOfZoom;

                    index = (oldXLeft+oldXRight)/2;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerCount==1&& !ZOOM){
                    main.curOffset -= (int)((x-index)/dataStepX);
                    index = x;
                    main.readDataSetting(ZZShowScopeActivity.GOCURRPAGE);
                }

                if (pointerCount==2){
                    float x1 = event.getX(0);
                    float x2 = event.getX(1);
                    newXLeft = Math.min(x1,x2);
                    newXRight = Math.max(x1,x2);

                    ZZShowScopeActivity.numEveryPage =
                            (int)((oldXRight-oldXLeft)/(newXRight-newXLeft)*oldNumEveryPage);
                    if (ZZShowScopeActivity.numEveryPage>500){
                        ZZShowScopeActivity.numEveryPage = 500;
                    }
                    int numLeft = (int)(((oldXRight-oldXLeft)/(newXRight-newXLeft))*middleOfZoom);
                    main.curOffset = middleOfZoomFromOffset - numLeft;
                    main.readDataSetting(ZZShowScopeActivity.GOCURRPAGE);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (ZOOM){
                    ZOOM = false;
                }
                break;
        }
        invalidate();
        return true;
    }
}
