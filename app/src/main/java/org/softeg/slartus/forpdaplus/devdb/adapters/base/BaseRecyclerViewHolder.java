package org.softeg.slartus.forpdaplus.devdb.adapters.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by isanechek on 24.11.15.
 */
public class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {
    public BaseRecyclerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}