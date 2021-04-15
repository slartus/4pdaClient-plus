package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaplus.classes.common.ExtBitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class BbCodesBasePanel {
    protected Context mContext;
    private final IGetBitmap m_GetBitmapFunc;
    protected LinearLayout lnrBbCodes;
    protected EditText txtPost;

    public interface IGetBitmap {
        Bitmap getBitmap(Context context, String filePath) throws IOException;
    }

    public Context getContext() {
        return mContext;
    }

    public BbCodesBasePanel(Context context, Gallery gallery, EditText editText) {
        this(context, gallery, editText, new IGetBitmap() {
            @Override
            public Bitmap getBitmap(Context context, String filePath) throws IOException {
                return ExtBitmap.getBitmapFromAsset(context, filePath);
            }
        });
    }

    public BbCodesBasePanel(Context context, Gallery gallery, EditText editText,
                            IGetBitmap getBitmap) {
        mContext = context;
        m_GetBitmapFunc = getBitmap;
        initVars();
        gallery.setAdapter(new ImageAdapter(context, getImages()));
        txtPost = editText;

        gallery.setSelection(3, true);
    }

    protected void initVars() {

    }

    protected abstract BbImage[] getImages();


    /**
     * Helper Functions
     *
     * @throws IOException
     */
    protected Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = mContext.getAssets();
        // BufferedInputStream buf = new BufferedInputStream(assetManager.open(strName));
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }


    public class ImageAdapter extends BaseAdapter {
        /**
         * The parent context
         */
        private final Context myContext;
        private final float m_Density;
        // Put some images to project-folder: /res/drawable/
        // format: jpg, gif, png, bmp, ...

        private BbImage[] m_Images = null;

        /**
         * Simple Constructor saving the 'parent' context.
         */
        public ImageAdapter(Context c, BbImage[] images) {
            this.myContext = c;
            m_Images = images;
            m_Density = mContext.getResources().getDisplayMetrics().density;
        }

        // inherited abstract methods - must be implemented
        // Returns count of images, and individual IDs
        public int getCount() {
            return m_Images.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }


        // Returns a new ImageView to be displayed,
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // Get a View to display image data
                convertView = new ImageView(this.myContext);

                ((ImageView) convertView).setScaleType(ImageView.ScaleType.FIT_END);
                // Set the Width & Height of the individual images
                convertView.setLayoutParams(new Gallery.LayoutParams((int) (m_Density * 30), (int) (m_Density * 30)));
            }

            try {
                ((ImageView) convertView).setImageBitmap(m_GetBitmapFunc.getBitmap(mContext, m_Images[position].FilePath));
                convertView.setTag(m_Images[position]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }
}
