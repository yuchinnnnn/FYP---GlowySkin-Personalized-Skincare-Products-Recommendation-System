package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FaceOutlineView extends View {

    private Paint paint;

    public FaceOutlineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.holo_red_light));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Adjust these values to fit the user's face shape and size
        int left = getWidth() / 5;
        int top = getHeight() / 6;
        int right = 4 * getWidth() / 4;
        int bottom = 3 * getHeight() / 4;

        // Define an offset to shift the oval to the left
        int horizontalOffset = 80; // Adjust this value to move the oval left

        // Move both left and right by the offset to the left
        left -= horizontalOffset;
        right -= horizontalOffset;

        // Draw the oval
        canvas.drawOval(left, top, right, bottom, paint);
    }

}
