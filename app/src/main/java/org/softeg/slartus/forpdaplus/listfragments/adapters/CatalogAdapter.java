package org.softeg.slartus.forpdaplus.listfragments.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/**
 * Created by slinkin on 21.02.14.
 */
public class CatalogAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private ArrayList<? extends ICatalogItem> mData;

    public CatalogAdapter(Context context, ArrayList<? extends ICatalogItem> data) {
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
            view = mInflater.inflate(R.layout.catalog_item, parent, false);
            holder = new ViewHolder();

            holder.Main = view.findViewById(R.id.text1);
            holder.SubMain = view.findViewById(R.id.text2);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        ICatalogItem item = mData.get(position);

        holder.Main.setText(item.getTitle());
        holder.SubMain.setText(item.getSubTitle());

        return view;
    }

    class ViewHolder {
        TextView Main;
        TextView SubMain;
    }
}
