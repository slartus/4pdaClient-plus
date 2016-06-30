package org.softeg.slartus.forpdaplus.devdb.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.base.BaseRecyclerViewHolder;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.helpers.DialogHelper;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;

import java.util.List;

import butterknife.BindView;

/**
 * Created by isanechek on 18.11.15.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_comments_item;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    private List<CommentsModel> mModels;

    public CommentsAdapter(Context context, List<CommentsModel> models) {
        super();
        this.mContext = context;
        this.mModels = models;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(LAYOUT, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CommentsModel obj = mModels.get(position);
        holder.devDbCommentsRatingNum.setText(obj.getCommentRatingNum());
//        holder.devDbCommentsRatingText.setText(obj.getCommentRatingText());
        holder.devDbCommentsUserName.setText(obj.getCommentUserName());
        holder.devDbCommentsDatePost.setText(obj.getCommentDate());
        holder.devDbCommentsBodeText.setText(obj.getCommentText());

        // пока так, как нибудь потом перенесу во фрагмент
        holder.devDbCommentsBodeText.setOnClickListener(v -> DialogHelper.showCommentDialog(mContext, obj.getCommentText(), obj.getCommentUserName()));

        holder.mFrameLayout.setOnClickListener(v -> {
//                DialogHelper.showRatingDialog(mContext, obj.getRatingList());
        });
        holder.devDbCommentsUserName.setOnClickListener(v -> DevDbUtils.showUrl(mContext, obj.getCommentUserLink()));

    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public static class ViewHolder extends BaseRecyclerViewHolder {

        @BindView(R.id.devDbCommentsRatingNum)
        TextView devDbCommentsRatingNum;
        //        @Bind(R.id.devDbCommentsRatingText)
//        TextView devDbCommentsRatingText;
        @BindView(R.id.devDbCommentsUserName)
        TextView devDbCommentsUserName;
        @BindView(R.id.devDbCommentsDatePost)
        TextView devDbCommentsDatePost;
        //        @Bind(R.id.devDbCommentsRating)
//        TextView devDbCommentsRating;
        @BindView(R.id.devDbCommentsBodeText)
        TextView devDbCommentsBodeText;
        @BindView(R.id.devDbRatingCon)
        FrameLayout mFrameLayout;


        public ViewHolder(View itemView) {
            super(itemView);
        }

    }
}
