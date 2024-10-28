package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class AspectRatioFrameLayout extends FrameLayout {
    private float aspectRatio = 1.0f; // Default aspect ratio

    // Constructor with Context and AttributeSet
    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(float aspectRatio) {
        if (this.aspectRatio != aspectRatio) {
            this.aspectRatio = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width == 0 || height == 0) {
            setMeasuredDimension(width, height);
        } else {
            int newHeight = (int) (width / aspectRatio);
            setMeasuredDimension(width, newHeight);
        }
    }
}

