package org.softeg.slartus.forpdaplus.classes.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.05.13
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
public class ExtBitmap {

    /**
     * Helper Functions
     *
     * @throws java.io.IOException
     */
    public static Bitmap getBitmapFromAsset(Context context, String strName) throws IOException {
        AssetManager assetManager = context.getAssets();
        // BufferedInputStream buf = new BufferedInputStream(assetManager.open(strName));
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }
}
