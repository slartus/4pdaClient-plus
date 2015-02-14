package org.softeg.slartus.forpdaplus.classes.TouchImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

    private static final String TAG = "Touch";
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    Context context;

    public TouchImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public TouchImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        super.setClickable(true);
        this.context = context;

        matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {


            public boolean onTouch(View v, MotionEvent rawEvent) {
                WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);


                // Handle touch events here...
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        Log.d(TAG, "mode=DRAG");
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        Log.d(TAG, "oldDist=" + oldDist);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                            Log.d(TAG, "mode=ZOOM");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        int xDiff = (int) Math.abs(event.getX() - start.x);
                        int yDiff = (int) Math.abs(event.getY() - start.y);
                        if (xDiff < 8 && yDiff < 8) {
                            performClick();
                        }
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        Log.d(TAG, "mode=NONE");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            // ...
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            Log.d(TAG, "newDist=" + newDist);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }

                setImageMatrix(matrix);
                return true; // indicate event was handled
            }

        });
    }

    public void setDrawable(Drawable drawable, int displayWidth, int displayHeight) {
        super.setImageDrawable(drawable);
        int drawableHeight = drawable.getIntrinsicHeight();
        int drawableWidth = drawable.getIntrinsicWidth();
        //Fit to screen.
        float scale;
        if ((displayHeight / drawableHeight) >= (displayWidth / drawableWidth)) {
            scale = (float) displayWidth / (float) drawableWidth;
        } else {
            scale = (float) displayHeight / (float) drawableHeight;
        }

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(scale, scale, mid.x, mid.y);
        setImageMatrix(matrix);


        // Center the image
        float redundantYSpace = (float) displayHeight - (scale * (float) drawableHeight);
        float redundantXSpace = (float) displayWidth - (scale * (float) drawableWidth);

        redundantYSpace /= (float) 2;
        redundantXSpace /= (float) 2;


        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }

    public void setImage(Bitmap bm, int displayWidth, int displayHeight) {
        super.setImageBitmap(bm);

        //Fit to screen.
        float scale;
        if ((displayHeight / bm.getHeight()) >= (displayWidth / bm.getWidth())) {
            scale = (float) displayWidth / (float) bm.getWidth();
        } else {
            scale = (float) displayHeight / (float) bm.getHeight();
        }

        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postScale(scale, scale, mid.x, mid.y);
        setImageMatrix(matrix);


        // Center the image
        float redundantYSpace = (float) displayHeight - (scale * (float) bm.getHeight());
        float redundantXSpace = (float) displayWidth - (scale * (float) bm.getWidth());

        redundantYSpace /= (float) 2;
        redundantXSpace /= (float) 2;


        savedMatrix.set(matrix);
        matrix.set(savedMatrix);
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        setImageMatrix(matrix);
    }


    /**
     * Show an event in the LogCat view, for debugging
     */
    private void dumpEvent(WrapMotionEvent event) {
        // ...
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            
                sb.append("(pid ").append(
                        action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            

            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(WrapMotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, WrapMotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}