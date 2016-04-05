package com.kindabear.radiople.view.sizingimageview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import de.hdodenhof.circleimageview.CircleImageView;

public class CircleSizingImageView extends CircleImageView {

    private final static String TAG = "PodpleImageView";

    public CircleSizingImageView(Context context) {
        super(context);
    }

    public CircleSizingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleSizingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnSizeChangedListener(final OnSizeChangedListener listener) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                listener.onSizeChanged(getWidth(), getHeight());
            }
        });
    }

}
