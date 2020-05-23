package com.junt.videorecorderlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * description :
 *
 * @author Junt
 * @date :2019/5/8 10:05
 */
public class CustomProgressBar extends View {

    private float viewWidth;
    private float viewHeight;
    private Paint backgroundPaint;
    private RectF backRectF;
    private Paint progressPaint;
    private RectF progressRecf;
    private Paint textPaint;
    private RectF textRecf;
    private int progress = 0;
    private int textBaseLine;
    //圆形进度条
    private float radius = 100;
    private float currentAngle;
    //圆环宽度
    private float ringWidth = 10;
    //水平进度条
    private Paint bubblePaint;
    private RectF bubbleRecf;
    private float bubbleWidth = 70;
    private float bubbleHeight = 50;
    private float triangleHeight = 20;
    private float barHeight = 20;
    private Path trianglePath;
    private Paint trianglePaint;
    //进度条类型
    private final int STYLE_HORIZONTAL = 0;
    private final int STYLE_RING = 1;
    private int style;


    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressBar);
        style = typedArray.getInt(R.styleable.CustomProgressBar_style, STYLE_RING);
        ringWidth=typedArray.getInt(R.styleable.CustomProgressBar_ringWidth,10);
        typedArray.recycle();
        init();
    }

    /**
     * 初始化画笔工具
     */
    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backRectF = new RectF();
        progressRecf = new RectF();

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.GREEN);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        if (style == STYLE_HORIZONTAL) {
            viewHeight = bubbleHeight + triangleHeight + barHeight;

            bubblePaint = new Paint();
            bubblePaint.setStyle(Paint.Style.FILL);
            bubblePaint.setAntiAlias(true);
            bubblePaint.setColor(Color.parseColor("#FF6900"));

            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(bubbleWidth / 3);

            trianglePath = new Path();
            trianglePaint = new Paint();
            trianglePaint.setStyle(Paint.Style.FILL);
            trianglePaint.setAntiAlias(true);
            trianglePaint.setColor(Color.parseColor("#FF6900"));

            bubbleRecf = new RectF();
        } else {
            viewHeight = 200;
            textPaint.setColor(Color.BLACK);
            backgroundPaint.setStyle(Paint.Style.STROKE);
            backgroundPaint.setStrokeWidth(ringWidth);
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeWidth(ringWidth);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        if (style == STYLE_HORIZONTAL) {
            calculateHorizontalParam();
        } else if (style == STYLE_RING) {
            calculateRingParam();
        }
    }


    /**
     * 初始化Recf等(圆环进度条)
     */
    private void calculateRingParam() {
        radius = viewHeight / 2 - backgroundPaint.getStrokeWidth();
        textPaint.setTextSize(radius / 3);

        currentAngle = (float) progress / 100 * 360;
        backRectF.left = viewWidth / 2 - radius;
        backRectF.top = ringWidth;
        backRectF.right = viewWidth / 2 + radius;
        backRectF.bottom = viewHeight - ringWidth;

        progressRecf = backRectF;

        textRecf = backRectF;
        Paint.FontMetricsInt fontMetricsInt = textPaint.getFontMetricsInt();
        textBaseLine = (int) ((textRecf.top + textRecf.bottom - fontMetricsInt.top - fontMetricsInt.bottom) / 2);
    }

    /**
     * 初始化Recf等(水平进度条)
     */
    private void calculateHorizontalParam() {
        float currentPos = (float) progress / 100 * (viewWidth - bubbleWidth) + bubbleWidth / 2;

        backRectF.left = bubbleWidth / 2;
        backRectF.top = viewHeight - barHeight;
        backRectF.right = viewWidth - bubbleWidth / 2;
        backRectF.bottom = viewHeight;

        progressRecf.left = bubbleWidth / 2;
        progressRecf.top = viewHeight - barHeight;
        progressRecf.right = currentPos;
        progressRecf.bottom = viewHeight;

        bubbleRecf.left = currentPos - bubbleWidth / 2;
        bubbleRecf.top = 0;
        bubbleRecf.right = currentPos + bubbleWidth / 2;
        bubbleRecf.bottom = bubbleHeight;

        trianglePath.reset();
        trianglePath.moveTo(bubbleRecf.centerX() - triangleHeight / 2, bubbleHeight);
        trianglePath.lineTo(bubbleRecf.centerX() + triangleHeight / 2, bubbleHeight);
        trianglePath.lineTo(bubbleRecf.centerX(), bubbleHeight + triangleHeight);

        textRecf = bubbleRecf;
        Paint.FontMetricsInt fontMetricsInt = textPaint.getFontMetricsInt();
        textBaseLine = (int) ((textRecf.top + textRecf.bottom - fontMetricsInt.top - fontMetricsInt.bottom) / 2);
    }


    /**
     * 设置进度
     */
    public void setProgress(int progress) {
        this.progress = progress;
        if (style == STYLE_HORIZONTAL) {
            calculateHorizontalParam();
        } else if (style == STYLE_RING) {
            currentAngle = (float) progress / 100 * 360;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (style == STYLE_HORIZONTAL) {
            drawHorizontalBack(canvas);
            drawHorizontalProgress(canvas);
            drawHorizontalBubble(canvas);
        } else if (style == STYLE_RING) {
            drawRingBack(canvas);
            drawRingProgress(canvas);
//            drawRingText(canvas);
        }
    }

    /**
     * 绘制圆环进度文字
     */
    private void drawRingText(Canvas canvas) {
        canvas.drawText(progress + "%", textRecf.centerX(), textBaseLine, textPaint);
    }

    /**
     * 绘制圆环进度
     */
    private void drawRingProgress(Canvas canvas) {
        canvas.drawArc(progressRecf, -90, currentAngle, false, progressPaint);
    }

    /**
     * 绘制背景（圆环进度条）
     */
    private void drawRingBack(Canvas canvas) {
        canvas.drawArc(backRectF, 0, 360, false, backgroundPaint);
    }

    /**
     * 绘制进度气泡（水平进度条）
     */
    private void drawHorizontalBubble(Canvas canvas) {
        canvas.drawRoundRect(bubbleRecf, bubbleHeight / 4, bubbleHeight / 4, bubblePaint);
        canvas.drawPath(trianglePath, trianglePaint);
        canvas.drawText(progress + "%", textRecf.centerX(), textBaseLine, textPaint);
    }

    /**
     * 绘制背景（水平进度条）
     */
    private void drawHorizontalBack(Canvas canvas) {
        canvas.drawRoundRect(backRectF, barHeight / 2, barHeight / 2, backgroundPaint);
    }

    /**
     * 绘制进度条（水平进度条）
     */
    private void drawHorizontalProgress(Canvas canvas) {
        canvas.drawRoundRect(progressRecf, barHeight / 2, barHeight / 2, progressPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(measureWidth(widthMode, widthSize), measureHeight(heightMode, heightSize));
    }

    /**
     * 测量宽度
     */
    private int measureWidth(int mode, int width) {
        int mWidth = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mWidth = width;
                break;
        }
        return mWidth;
    }

    /**
     * 测量高度
     */
    private int measureHeight(int mode, int height) {
        int mHeight = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                mHeight = (int) viewHeight;
                break;
            case MeasureSpec.EXACTLY:
                viewHeight = mHeight = height;
                break;
        }
        return mHeight;
    }
}
