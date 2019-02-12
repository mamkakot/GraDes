package com.example.dmitry.dinamic_creating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DrawLine extends View {
    float startX, endX, startY, endY;
    Paint paint = new Paint();
    public View firstBtn, secondBtn;

    public DrawLine(Context context) {
        super(context);
    }

    public DrawLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(startX, startY, endX, endY, paint);
    }

    @SuppressLint("ResourceType")
    public void setCoords(View firstButton, View secondButton) {
        startX = firstButton.getX() + (firstButton.getWidth() >> 1);
        startY = firstButton.getY() + (firstButton.getHeight() >> 1);
        endX = secondButton.getX() + (secondButton.getWidth() >> 1);
        endY = secondButton.getY() + (secondButton.getHeight() >> 1);
        paint.setStrokeWidth(5);
        if (firstButton.getId() > secondButton.getId()) { secondBtn = firstButton; firstBtn = secondButton; }
        else { firstBtn = firstButton; secondBtn = secondButton; }
    }

    public void draw() {
        invalidate();
        requestLayout();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }
}
