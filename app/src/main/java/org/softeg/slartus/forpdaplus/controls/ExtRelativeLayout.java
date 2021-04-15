package org.softeg.slartus.forpdaplus.controls;/*
 * Created by slinkin on 14.05.2014.
 */

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


public class ExtRelativeLayout extends RelativeLayout {
    private final int[] mInsets = new int[4];

    public ExtRelativeLayout(Context context) {
        super(context);
    }

    public ExtRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public final int[] getInsets() {
        return mInsets;
    }

    @Override
    protected final boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Intentionally do not modify the bottom inset. For some reason,
            // if the bottom inset is modified, window resizing stops working.
            // TODO: Figure out why.

            mInsets[0] = insets.left;
            mInsets[1] = insets.top;
            mInsets[2] = insets.right;
         //   mInsets[3] = insets.bottom;

            insets.left = 0;
            insets.top = 0;
            insets.right = 0;
        //    insets.bottom = 0;
        }

        return super.fitSystemWindows(insets);
    }
}
