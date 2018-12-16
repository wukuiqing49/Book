package com.zia.page.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zia on 2018/12/16.
 */
public class BatteryView extends View {

    private Paint paint;
    private Rect rect = new Rect();
    private Rect rect2 = new Rect();
    private Rect rect3 = new Rect();
    private float mPower = 0;
    private @ColorInt
    int color = Color.WHITE;

    public void setColor(int color) {
        this.color = color;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int battery_head_width = 3;
        int battery_head_height = 7;

        int battery_inside_margin = 3;

        int battery_left = 0;
        int battery_top = 0;
        int battery_width = getWidth() - battery_head_width;
        int battery_height = getHeight();

        //先画外框
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        rect.set(battery_left, battery_top, battery_left + battery_width, battery_top + battery_height);
        canvas.drawRect(rect, paint);

        paint.setStyle(Paint.Style.FILL);
        //画电量
        if (mPower != 0) {
            int p_left = battery_left + battery_inside_margin;
            int p_top = battery_top + battery_inside_margin;
            int p_right = p_left - battery_inside_margin + (int) ((battery_width - battery_inside_margin) * mPower);
            int p_bottom = p_top + battery_height - battery_inside_margin * 2;
            rect2.set(p_left, p_top, p_right, p_bottom);
            canvas.drawRect(rect2, paint);
        }

        //画电池头
        int h_left = battery_left + battery_width;
        int h_top = battery_top + battery_height / 2 - battery_head_height / 2;
        int h_right = h_left + battery_head_width;
        int h_bottom = h_top + battery_head_height;
        rect3.set(h_left, h_top, h_right, h_bottom);
        canvas.drawRect(rect3, paint);
    }

    public void setPower(float powerPercent) {
        mPower = powerPercent;
        if (mPower < 0) {
            mPower = 0;
        }
        postInvalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public BatteryView(Context context) {
        super(context);
        init();
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
}
