package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.softeg.slartus.forpdaplus.classes.TouchImage.TouchImageView;
import org.softeg.slartus.forpdaplus.classes.common.ExtDisplay;
import org.softeg.slartus.forpdaplus.common.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by slinkin on 10.06.13.
 */
public class MyImageView extends LinearLayout {
    private WindowManager mWindowManager;

    public MyImageView(Context context, WindowManager windowManager) {
        super(context);
        mWindowManager = windowManager;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_view_activity, null);
        addView(view);


        mSpinner = (ProgressBar) findViewById(R.id.progress);

        mImage = (TouchImageView) findViewById(R.id.image);
        mImage.setClickable(true);
    }

    private static final int COMPLETE = 0;
    private static final int FAILED = 1;
    private static final String URL_KEY = "url";
    private TouchImageView mImage;
    private ProgressBar mSpinner;
    private Drawable mDrawable;
    private Bitmap mBitmap;
    private String mUrl;
    private Point mDisplaySize;

    /**
     * Callback that is received once the image has been downloaded
     */
    private final Handler imageLoadedHandler = new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case COMPLETE:

                        // mImage.setDrawable(mDrawable, width, height);
                        mImage.setImage(mBitmap, mDisplaySize.x, mDisplaySize.y);
                        mImage.setVisibility(View.VISIBLE);
                        mSpinner.setVisibility(View.GONE);
                        break;
                    case FAILED:
                        mSpinner.setVisibility(View.GONE);
                        Bundle data = msg.getData();
                        Log.e(getContext(), data.getString("message"), (Throwable) data.getSerializable("exception"));
                    default:
                        // Could change image here to a 'failed' image
                        // otherwise will just keep on spinning
                        break;
                }
            } catch (Exception ex) {
                mSpinner.setVisibility(View.GONE);
                Log.e(getContext(), "Ошибка загрузки изображения по адресу: " + mUrl, ex);
            }

            return true;
        }
    });


    /**
     * Set's the view's drawable, this uses the internet to retrieve the image
     * don't forget to add the correct permissions to your manifest
     *
     * @param imageUrl the url of the image you wish to load
     */
    public void setImageDrawable(final String imageUrl) {
        mDrawable = null;
        mSpinner.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.GONE);
        Display display = mWindowManager.getDefaultDisplay();
        mDisplaySize = ExtDisplay.getDisplaySize(display);

        new Thread() {
            public void run() {

                try {
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    //mDrawable = Drawable.createFromStream(httpHelper.getImageStream(imageUrl), "name");
                    mBitmap = getBitmap(imageUrl);
                    imageLoadedHandler.sendEmptyMessage(COMPLETE);

                } catch (OutOfMemoryError e) {
                    Bundle data = new Bundle();
                    data.putSerializable("exception", e);
                    data.putString("message", "Нехватка памяти: " + mUrl);
                    Message message = new Message();
                    message.what = FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);
                } catch (Exception e) {
                    Bundle data = new Bundle();
                    data.putSerializable("exception", e);
                    data.putString("message", "Ошибка загрузки изображения по адресу: " + mUrl);
                    Message message = new Message();
                    message.what = FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);

                }
            }


        }.start();
    }

    private String downloadImage(String imageUrl) throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        try {
            File file = File.createTempFile("temp_image", ".tmp");


            long total = 0;


            String url = imageUrl;
            HttpEntity entity = httpHelper.getDownloadResponse(url, total);

            long fileLength = entity.getContentLength() + total;

            int count;
            int percent = 0;
            int prevPercent = 0;

            Date lastUpdateTime = new Date();
            Boolean first = true;

            InputStream in = entity.getContent();
            FileOutputStream output = new FileOutputStream(file, true);

            byte data[] = new byte[1024];
            try {
                while ((count = in.read(data)) != -1) {
                    output.write(data, 0, count);
                    total += count;

                    percent = (int) ((float) total / fileLength * 100);

                    long diffInMs = new Date().getTime() - lastUpdateTime.getTime();
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                    if ((percent != prevPercent && diffInSec > 1) || first) {
                        lastUpdateTime = new Date();
                        first = false;
                    }
                    prevPercent = percent;
                }

            } finally {
                output.flush();
                output.close();
                in.close();
            }
            return file.getPath();
        } finally {
            httpHelper.close();
        }

    }

    private Bitmap getBitmap(String imageUrl) throws Exception {
        String tempPath = downloadImage((imageUrl));
        try {

            int width = 0;
            int height = 0;
            BitmapFactory.Options o = null;
            InputStream in = null;
            try {
                in = new FileInputStream(tempPath);
                o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, o);

                width = o.outWidth; //исходная ширина
                height = o.outHeight; //исходная высота
            } finally {
                if (in != null)
                    in.close();
            }

            int reqWidth = mDisplaySize.x; //Нужная ширина
            int reqHeight = mDisplaySize.y; //Нужная высота

            int inSampleSize = 1; //кратность уменьшения
//            int origSize = origWidth * origHeight * bytesPerPixel;
//высчитываем кратность уменьшения

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }


            o.inJustDecodeBounds = false;

            o.inSampleSize = inSampleSize;

            in = new FileInputStream(tempPath); //Ваш InputStream. Важно - открыть его нужно еще раз, т.к второй раз читать из одного и того же InputStream не разрешается (Проверено на ByteArrayInputStream и FileInputStream).
            return BitmapFactory.decodeStream(in, null, o); //Полученный Bitmap
        } catch (Throwable ex) {
            Log.e(null, ex);
            return null;
        } finally {
            try {
                new File(tempPath).delete();
            } catch (Exception ignoredEx) {
                Log.e(null, ignoredEx);
            }
        }
    }

    private static Drawable getDrawableFromUrl(final String url) throws Throwable {
        HttpHelper httpHelper = new HttpHelper();
        return Drawable.createFromStream(httpHelper.getImageStream(url), "name");
    }

}
