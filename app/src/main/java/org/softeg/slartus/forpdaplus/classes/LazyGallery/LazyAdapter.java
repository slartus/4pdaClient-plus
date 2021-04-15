package org.softeg.slartus.forpdaplus.classes.LazyGallery;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:15
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.softeg.slartus.forpdaplus.R;

import java.io.IOException;

public class LazyAdapter extends BaseAdapter {

    private final Activity activity;
    private final String[] data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a, String[] d) throws IOException {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = ImageLoader.getInstance();
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return data[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.gallery_item, null);

        ImageView image= vi.findViewById(R.id.image);

        imageLoader.displayImage(data[position], image);
        return vi;
    }
}
