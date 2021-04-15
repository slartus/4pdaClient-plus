package org.softeg.slartus.forpdaplus.listfragments.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/**
 * Created by slartus on 04.03.14.
 */
public class CatalogCrumbsAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private ArrayList<? extends ICatalogItem> mData;

    public CatalogCrumbsAdapter(Context context, ArrayList<? extends ICatalogItem> data) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
    }

    public void setData(ArrayList<? extends ICatalogItem> data) {
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
            view = mInflater.inflate(R.layout.catalog_crumb_item, parent, false);
            assert view != null;
            holder = new ViewHolder();
            holder.Main = view.findViewById(R.id.text1);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        ICatalogItem item = mData.get(position);
        holder.Main.setText(item.getTitle());


        return view;
    }

    class ViewHolder {
        TextView Main;

    }
}
