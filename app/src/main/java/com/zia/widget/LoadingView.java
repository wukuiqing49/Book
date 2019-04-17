package com.zia.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.zia.bookdownloader.R;

public class LoadingView extends View {
    private float mRotationRadius;//大圆(里面包含很多小圆)的半径
    private float mCircleRadius;//每一个小圆的半径
    private int mCircleColors;//小圆颜色列表，在initialize方法里面初始化
    private long mRotationDuration = 1600;//大圆和小圆旋转时间
    private int mSplashBgColor = Color.WHITE;//整体的背景颜色
    /**
     * 参数，保存了一些绘制状态，会被动态的改变
     */
    //空心圆的初始半径
    private float mHoleRadius = 0f;
    //当前大圆旋转角度
    private float mCurrentRotationAngle = 0f;
    //当前大圆的半径
    private float mCurrentRotationRadius = mRotationRadius;
    //绘制圆的画笔
    private Paint mpaint = new Paint();
    //绘制背景的画笔
    private Paint mPaintBackground = new Paint();
    //屏幕中心点坐标
    private float mCenterX;
    private float mCenterY;
    //屏幕对角线一半
    private float mDiagonaalDist;
    private ValueAnimator valueAnimator;

    /**
     * 如果要写再xml里面就需要三个构造函数
     *
     * @param context
     */
    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mpaint.setAntiAlias(true);//初始化画小圆的画笔
        mPaintBackground.setAntiAlias(true);//初始化画背景的画笔
        mPaintBackground.setStyle(Paint.Style.STROKE);//设置样式
        mPaintBackground.setColor(mSplashBgColor);//设置背景画笔颜色
        mCircleColors = getContext().getResources().getColor(R.color.colorPrimary);//设置小圆的颜色
    }

    /**
     * 计算屏幕中心点
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2f;
        mCenterY = h / 2f;
        //屏幕对角线一半
        mDiagonaalDist = (float) Math.sqrt((w * w + h * h) / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRotationRadius == 0) {
            mRotationRadius = Math.max(getWidth(), getHeight()) / 2f;
            mCircleRadius = mRotationRadius / 9f;
        }
        //绘制动画第一个动画
        if (mstate == null) {
            mstate = new RotationState();
        }
        mstate.drawState(canvas);
        super.onDraw(canvas);
    }

    public void start() {
        post(new Runnable() {
            @Override
            public void run() {
                mstate.start();
            }
        });
    }

    public void cancel() {
        mstate.cancel();
    }


    private abstract class SplashState {
        public abstract void drawState(Canvas canvas);

        public abstract void start();

        public abstract void cancel();
    }

    //这里利用一个设计模式：策略模式
    private SplashState mstate = new RotationState();

    /**
     * 第一个动画：小圆旋转动画
     * 要素：不断的绘制小圆--》控制左边---- 旋转的角度、公转的半径
     */
    private class RotationState extends SplashState {
        @Override
        public void start() {
            cancel();
            //动画初始化
            //花1600ms，计算某个时间当前的角度是多少：0~2π中的某个值
            valueAnimator = ValueAnimator.ofFloat(0, (float) Math.PI * 2);
            valueAnimator.setInterpolator(new LinearInterpolator());//设置插值器，主要是为了小圆点的旋转时间平均，让动画没有停顿。
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {//回调监听
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //得到某个时间点计算的结果 ----这个时间带你当前大圆旋转的角度
                    mCurrentRotationAngle = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator.setDuration(mRotationDuration);//动画时间
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//不断重复
            valueAnimator.start();//启动
        }

        @Override
        public void cancel() {
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }

        @Override
        public void drawState(Canvas canvas) {
            //绘制动画
            //1擦黑板
            drawBackground(canvas);
            //2.画小圆
            drawCircle(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (mHoleRadius > 0f) {
            //得到画笔的宽度 = 对角线/2-空心圆的半径
            float strokewidth = mDiagonaalDist - mHoleRadius;
            mPaintBackground.setStrokeWidth(strokewidth);//设置画笔的宽度
            //画圆的半径 = 空心圆半径+画笔宽度/2
            float radius = mHoleRadius + strokewidth / 2;
            //空心圆的半径越来越大  画笔的宽度就越来也小
            canvas.drawCircle(mCenterX, mCenterY, radius, mPaintBackground);
        }
    }

    /**
     * 画六个小圆
     */
    private void drawCircle(Canvas canvas) {
        //得到每个小圆的间隔角度
        float rotationAngle = (float) (2 * Math.PI / 6);
        //画6个小圆
        for (int i = 0; i < 6; i++) {
            /**
             * 计算角度
             * x = r*cos(a)
             * y = r*sin(a);
             */
            double angle = i * rotationAngle + mCurrentRotationAngle;//不断的更新圆点的坐标位置
            //因为最开始的计算是再屏幕的左上角，计算出每个圆点位置之后加上中心点X或者Y坐标就可以了
            float cx = (float) (mCurrentRotationRadius * Math.cos(angle) + mCenterX);
            float cy = (float) (mCurrentRotationRadius * Math.sin(angle) + mCenterY);
            mpaint.setColor(mCircleColors);//设置小圆的颜色
            canvas.drawCircle(cx, cy, mCircleRadius, mpaint);
        }
    }
}