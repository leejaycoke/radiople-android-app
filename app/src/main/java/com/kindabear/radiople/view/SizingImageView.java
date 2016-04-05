package com.kindabear.radiople.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.kindabear.radiople.view.sizingimageview.OnSizeChangedListener;

public class SizingImageView extends ImageView {

    private final static String TAG = "PodpleImageView";

    public SizingImageView(Context context) {
        super(context);
    }

    public SizingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
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
