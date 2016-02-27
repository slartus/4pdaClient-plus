package org.softeg.slartus.forpdaplus.listfragments.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.softeg.slartus.forpdaapi.News;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.imageview.MaterialImageLoading;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by slartus on 20.02.14.
 */
public class NewsListAdapter extends BaseAdapter {
    private ArrayList<News> newsList;

    final LayoutInflater inflater;
    private ImageLoader imageLoader;
    private Boolean mLoadImages;
    private int mNewsListRowId;

    public NewsListAdapter(Context context, ArrayList<News> newsList, ImageLoader imageLoader) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNewsListRowId = Preferences.News.List.getNewsListViewId();
        this.imageLoader = imageLoader;
        this.newsList = newsList;
        mLoadImages = Preferences.News.List.isLoadImages();
    }

    @Override
    public void notifyDataSetChanged() {

        mLoadImages = Preferences.News.List.isLoadImages();
        mNewsListRowId = Preferences.News.List.getNewsListViewId();
        super.notifyDataSetChanged();
    }

    public void setData(ArrayList<News> data) {
        this.newsList = data;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int p1) {
        return newsList.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return p1;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null || view.getId() != mNewsListRowId) {
            Log.e("kek", "new view holder "+view+" : "+mNewsListRowId);
            view = inflater.inflate(mNewsListRowId, parent, false);
            holder = new ViewHolder(view);
            assert view != null;
            view.setId(mNewsListRowId);
            if (holder.image_panel != null)
                holder.image_panel.setVisibility(mLoadImages ? View.VISIBLE : View.GONE);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            if (holder.image_panel != null)
                holder.image_panel.setVisibility(mLoadImages ? View.VISIBLE : View.GONE);
        }
        final News data = newsList.get(position);
        if (holder.textComments != null)
            holder.textComments.setText(String.valueOf(data.getCommentsCount()));
        if (holder.textAuthor != null)
            holder.textAuthor.setText(data.getAuthor());
        if (holder.textDate != null)
            holder.textDate.setText(data.getNewsDate());
        if (holder.textDescription != null)
            holder.textDescription.setText(data.getDescription());
        if (holder.textTitle != null)
            holder.textTitle.setText(data.getTitle());

        if(data.getImgUrl()==null)
            holder.imageImage.setVisibility(View.GONE);
        else
            holder.imageImage.setVisibility(View.VISIBLE);
        if (holder.image_panel != null && data.getImgUrl() != null && mLoadImages) {
            imageLoader.displayImage(data.getImgUrl().toString(), new ImageViewAware(holder.imageImage, false));
        }
        if (data.getTagTitle() != null && holder.textTag != null) {
            if(data.getTagTitle().equals("")){
                holder.textTag.setVisibility(View.GONE);
            }else {
                holder.textTag.setVisibility(View.VISIBLE);
                holder.textTag.setText(data.getTagTitle());
            }

        }
        if (data.getSourceTitle() != null && holder.textSource != null) {
            holder.textSource.setVisibility(View.VISIBLE);
            holder.textSource.setText("Источник: ".concat(data.getSourceTitle().toString()));
        }

        return view;
    }
    static class ViewHolder {
        @Bind(R.id.image_panel) View image_panel;
        @Bind(R.id.imageImage) ImageView imageImage;
        @Bind(R.id.textTitle) TextView textTitle;
        @Bind(R.id.textDate) TextView textDate;
        @Bind(R.id.textDescription) TextView textDescription;
        @Bind(R.id.textAvtor) TextView textAuthor;
        @Bind(R.id.textTag) TextView textTag;
        @Bind(R.id.textComments) TextView textComments;
        @Bind(R.id.textSource) TextView textSource;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

