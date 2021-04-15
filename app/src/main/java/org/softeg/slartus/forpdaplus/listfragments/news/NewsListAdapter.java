package org.softeg.slartus.forpdaplus.listfragments.news;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.squareup.picasso.Picasso;

import org.softeg.slartus.forpdaapi.News;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;

/**
 * Created by slartus on 20.02.14.
 */
public class NewsListAdapter extends BaseAdapter {
    private ArrayList<News> newsList;

    final LayoutInflater inflater;
    private final ImageLoader imageLoader;
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
        final ViewHolder holder;
        View rowView = view;
        if (rowView == null || rowView.getId() != mNewsListRowId) {
            rowView = inflater.inflate(mNewsListRowId, null);
            holder = new ViewHolder();
            assert rowView != null;
            rowView.setId(mNewsListRowId);
            holder.image_panel = rowView.findViewById(R.id.image_panel);
            if (holder.image_panel != null)
                holder.image_panel.setVisibility(mLoadImages ? View.VISIBLE : View.GONE);
            holder.imageImage = rowView.findViewById(R.id.imageImage);

            holder.textSource = rowView.findViewById(R.id.textSource);
            holder.textComments = rowView.findViewById(R.id.textComments);
            holder.textTag = rowView.findViewById(R.id.textTag);

            holder.textAuthor = rowView.findViewById(R.id.textAvtor);
            holder.textDate = rowView.findViewById(R.id.textDate);
            holder.textDescription = rowView.findViewById(R.id.textDescription);
            holder.textTitle = rowView.findViewById(R.id.textTitle);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
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
            holder.textSource.setText(App.getContext().getString(R.string.source).concat(": ").concat(data.getSourceTitle().toString()));
        }

        return rowView;
    }
    public class ViewHolder {
        public View image_panel;
        public ImageView imageImage;
        public TextView textTitle;
        public TextView textDate;
        public TextView textDescription;
        public TextView textAuthor;
        public TextView textTag;
        public TextView textComments;
        public TextView textSource;
    }
}

