package com.kindabear.radiople.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoneSwipeableViewPager extends android.support.v4.view.ViewPager {

    public NoneSwipeableViewPager(Context context) {
        super(context);
    }

    public NoneSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
