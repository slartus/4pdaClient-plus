package org.softeg.slartus.forpdaplus.controls.imageview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/*
 * Created by slinkin on 07.11.2014.
 */
public class HackyViewPager extends ViewPager {

    private boolean isLocked;

    public HackyViewPager(Context context) {
        super(context);
        isLocked = false;
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isLocked = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return false;
        //return super.onInterceptTouchEvent(ev);
        if (!isLocked) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return false;
//       return super.onTouchEvent(event);
        if (!isLocked) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public void toggleLock() {
        isLocked = !isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isLocked() {
        return isLocked;
    }

}
