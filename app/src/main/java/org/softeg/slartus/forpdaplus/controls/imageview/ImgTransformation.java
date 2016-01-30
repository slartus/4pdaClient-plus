package org.softeg.slartus.forpdaplus.controls.imageview;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class ImgTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        double maxSize = ImgHelper.getMaxTextureSize() * 0.75;
        int imageWidth = source.getWidth();
        int imageHeight = source.getHeight();
        double scale = 1;
        if (imageWidth > maxSize) {
            scale = maxSize / imageWidth;
            imageWidth = (int) (imageWidth * scale);
            imageHeight = (int) (imageHeight * scale);
        }
        if (imageHeight > maxSize) {
            scale = maxSize / imageHeight;
            imageWidth = (int) (imageWidth * scale);
            imageHeight = (int) (imageHeight * scale);
        }
        Bitmap result = Bitmap.createScaledBitmap(source, imageWidth, imageHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "transformation" + " desiredWidth";
    }
}
