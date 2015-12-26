package org.softeg.slartus.forpdaplus.devdb.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.base.BaseRecyclerViewHolder;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;

import java.util.List;

import butterknife.Bind;

/**
 * Created by isanechek on 19.12.15.
 */
public class FirmwareAdapter extends RecyclerView.Adapter<FirmwareAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_firmware_item;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<FirmwareModel> mModels;

    public FirmwareAdapter(Context context, List<FirmwareModel> models) {
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
        final FirmwareModel obj = mModels.get(position);
        holder.textBody.setText(obj.getFirmwareTitle());
        holder.date.setText(obj.getFirmwareDate());
        holder.description.setText(obj.getFirmwareDescription());

        // это временно!!!
        holder.textBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DevDbUtils.showUrl(mContext, obj.getFirmwareLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public static class ViewHolder extends BaseRecyclerViewHolder {

        @Bind(R.id.devDbFirmwareTitle)
        TextView textBody;
        @Bind(R.id.devDbFirmwareDate)
        TextView date;
        @Bind(R.id.devDbFirmwareDescription)
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
