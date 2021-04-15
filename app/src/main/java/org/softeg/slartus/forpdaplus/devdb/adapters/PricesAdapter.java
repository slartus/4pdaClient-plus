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
import org.softeg.slartus.forpdaplus.devdb.model.PricesModel;

import java.util.List;


/**
 * Created by isanechek on 19.12.15.
 */
public class PricesAdapter extends RecyclerView.Adapter<PricesAdapter.ViewHolder> {

    private static final int LAYOUT = R.layout.dev_db_prices_item;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<PricesModel> mModels;

    public PricesAdapter(Context context, List<PricesModel> models) {
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
        final PricesModel obj = mModels.get(position);
        holder.textBody.setText(obj.getPricesTitle());
        holder.date.setText(obj.getPricesDate());
        holder.description.setText(obj.getPricesDescription());

        // Глаза болью наполнялись,
        // И пальцы рук хотелось себе ломать.
        // Но жизнь печальна, много боли.
        // И лучше для этой сутиэйшен решения не сыскать. (c) iSanechek
        holder.textBody.setOnClickListener(v -> DevDbUtils.showUrl(mContext, obj.getPricesLink()));
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
            textBody=itemView.findViewById(R.id.devDbPricesText);
            date=itemView.findViewById(R.id.devDbPricesDate);
            description=itemView.findViewById(R.id.devDbPricesDescription);
        }
    }
}
