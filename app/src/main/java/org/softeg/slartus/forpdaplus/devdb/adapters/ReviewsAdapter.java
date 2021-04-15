package org.softeg.slartus.forpdaplus.devdb.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.base.BaseRecyclerViewHolder;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.model.ReviewsModel;

import java.util.List;


public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_reviews_item;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<ReviewsModel> mModels;
    private final ImageLoader imageLoader;

    public ReviewsAdapter(Context context, List<ReviewsModel> models, ImageLoader imageLoader) {
        super();
        this.mContext = context;
        this.mModels = models;
        this.imageLoader = imageLoader;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(LAYOUT, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ReviewsModel obj = mModels.get(position);
        holder.textBody.setText(obj.getReviewTitle());
        holder.date.setText(obj.getReviewDate());

        holder.textBody.setOnClickListener(v -> DevDbUtils.showUrl(mContext, obj.getReviewLink()));

        //Picasso.with(mContext).load(obj.getReviewImgLink()).into(holder.image);
        imageLoader.displayImage(obj.getReviewImgLink(), holder.image);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public static class ViewHolder extends BaseRecyclerViewHolder {

        TextView textBody;
        TextView date;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            textBody=itemView.findViewById(R.id.devDbReviewsText);
            date=itemView.findViewById(R.id.devDbReviewsDate);
            image=itemView.findViewById(R.id.devDbReviewsIV);
        }
    }
}
