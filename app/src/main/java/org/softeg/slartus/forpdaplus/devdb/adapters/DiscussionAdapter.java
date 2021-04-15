package org.softeg.slartus.forpdaplus.devdb.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.base.BaseRecyclerViewHolder;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.model.DiscussionModel;

import java.util.List;


/**
 * Created by isanechek on 19.12.15.
 */
public class DiscussionAdapter  extends RecyclerView.Adapter<DiscussionAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_disscussion_item;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<DiscussionModel> mModels;

    public DiscussionAdapter(Context context, List<DiscussionModel> models) {
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
        final DiscussionModel obj = mModels.get(position);
        holder.textBody.setText(obj.getDiscussionTitle());
        holder.date.setText(obj.getDiscussionDate());
        holder.description.setText(obj.getDiscussionDescription());

        holder.textBody.setOnClickListener(v -> DevDbUtils.showUrl(mContext, obj.getDiscussionLink()));

    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public static class ViewHolder extends BaseRecyclerViewHolder {

        TextView textBody;
        TextView date;
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            textBody=itemView.findViewById(R.id.devDbDiscussionText);
            date=itemView.findViewById(R.id.devDbDiscussionDate);
            description=itemView.findViewById(R.id.devDbDiscussionDescription);
        }
    }
}
