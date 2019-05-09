package com.example.dmitry.dinamic_creating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DrawLine extends View {
    float startX, endX, startY, endY, dp = getResources().getDisplayMetrics().density;
    Path path = new Path();
    Paint paintLine = new Paint(), paintNum = new Paint(), paintText = new Paint();
    public View firstBtn, secondBtn;
    public String number = "";
    private Rect mTextRect = new Rect();

    int btnSide;
    // TODO Переделать дизайн, выглядит убого. Подобрать какие-нибудь три сочетающихся цвета и от это плясать
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
        float sas = btnSide;
        if (startX == endX && startY == endY) {
            path.reset();
            paintLine.setStyle(Paint.Style.STROKE);
            path.addCircle(startX + sas / 2f, startY + sas / 2f, sas / 2f, Path.Direction.CW);
            canvas.drawPath(path, paintLine);

            paintText.setTextSize(32);
            paintText.getTextBounds(number, 0, number.length(), mTextRect);
            int circleColor = R.color.num_color;
            paintNum.setColor(getResources().getColor(circleColor));
            paintText.setColor(Color.BLACK);
            canvas.drawCircle(startX + sas,
                    startY + sas / 2f,
                    sas / 3f,
                    paintNum);
            canvas.drawText(number,
                    startX + sas - paintText.measureText(number) / 2f,
                    startY + sas / 2f + (mTextRect.height() >> 1),
                    paintText);
        } else {
            canvas.drawLine(startX, startY, endX, endY, paintLine);
            paintText.setTextSize(34);
            paintText.getTextBounds(number, 0, number.length(), mTextRect);
            int circleColor = R.color.num_color;
            paintNum.setColor(getResources().getColor(circleColor));
            paintText.setColor(Color.BLACK);
            canvas.drawCircle(startX + (endX - startX) / 2,
                    startY + (endY - startY) / 2,
                    btnSide / 3f,
                    paintNum);
            canvas.drawText(number,
                    startX + (endX - startX) / 2 - paintText.measureText(number) / 2f,
                    startY + (endY - startY) / 2 + (mTextRect.height() >> 1),
                    paintText);
        }
    }

    @SuppressLint("ResourceType")
    public void setCoords(View firstButton, View secondButton, int buttonSide) {
        btnSide = buttonSide;
        startX = firstButton.getX() + (buttonSide >> 1);
        startY = firstButton.getY() + (buttonSide >> 1);
        endX = secondButton.getX() + (buttonSide >> 1);
        endY = secondButton.getY() + (buttonSide >> 1);
        paintLine.setStrokeWidth(2*dp);
        if (firstButton.getId() > secondButton.getId()) {
            secondBtn = firstButton;
            firstBtn = secondButton;
        } else {
            firstBtn = firstButton;
            secondBtn = secondButton;
        }
    }

    public void draw() {
        invalidate();
        requestLayout();
    }

    public void setColor(int color) {
        paintLine.setColor(color);
    }
}
