package org.softeg.slartus.forpdaplus.listfragments.adapters;/*
 * Created by slinkin on 13.03.14.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.softeg.slartus.forpdaapi.devdb.DevModel;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

public class DevDbModelsAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private ArrayList<DevModel> mData;
    private final ImageLoader imageLoader;
    public DevDbModelsAdapter(Context context, ArrayList<DevModel> data) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        imageLoader=ImageLoader.getInstance();
    }

    public void setData(ArrayList<DevModel> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public android.view.View getView(int position, android.view.View view, android.view.ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.dev_item, parent, false);
            holder = new ViewHolder();

            assert view != null;
            holder.imageView = view.findViewById(R.id.imageView);
            holder.progressView = view.findViewById(R.id.progressView);
            holder.titleTextView = view.findViewById(R.id.titleTextView);
            holder.descTextView = view.findViewById(R.id.descTextView);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        DevModel item = mData.get(position);

        holder.titleTextView.setText(item.getMain());
        holder.descTextView.setText(item.getDescription());

        imageLoader.displayImage(item.getImgUrl(), holder.imageView, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String p1, View p2) {
                p2.setVisibility(View.INVISIBLE);
                holder.progressView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String p1, View p2, FailReason p3) {
                holder.progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String p1, View p2, Bitmap p3) {
                p2.setVisibility(View.VISIBLE);
                holder.progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String p1, View p2) {

            }
        });
        return view;
    }

    class ViewHolder {
        ImageView imageView;
        View progressView;
        TextView titleTextView;
        TextView descTextView;
    }
}
