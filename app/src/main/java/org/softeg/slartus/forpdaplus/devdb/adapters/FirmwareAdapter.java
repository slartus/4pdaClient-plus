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
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;

import java.util.List;


/**
 * Created by isanechek on 19.12.15.
 */
public class FirmwareAdapter extends RecyclerView.Adapter<FirmwareAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_firmware_item;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<FirmwareModel> mModels;

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
        holder.textBody.setOnClickListener(v -> DevDbUtils.showUrl(mContext, obj.getFirmwareLink()));
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
            textBody = itemView.findViewById(R.id.devDbFirmwareTitle);
            date = itemView.findViewById(R.id.devDbFirmwareDate);
            description = itemView.findViewById(R.id.devDbFirmwareDescription);
        }
    }
}
