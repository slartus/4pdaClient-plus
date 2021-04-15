package org.softeg.slartus.forpdaplus.listfragments.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaapi.devdb.DevCatalog;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/*
 * Created by slartus on 06.03.14.
 */
public class DevDbAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private ArrayList<DevCatalog> mData;

    public DevDbAdapter(Context context, ArrayList<DevCatalog> data) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
    }

    public void setData(ArrayList<DevCatalog> data) {
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
            view = mInflater.inflate(R.layout.dev_catalog_item, parent, false);
            holder = new ViewHolder();

            assert view != null;
            holder.Main = view.findViewById(R.id.text1);
            holder.SubMain = view.findViewById(R.id.text2);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        ICatalogItem item = mData.get(position);

        holder.Main.setText(item.getTitle());
        if(item.getSubTitle() == null){
            holder.SubMain.setVisibility(View.GONE);
        }else {
            holder.SubMain.setText(item.getSubTitle());
        }


        return view;
    }

    class ViewHolder {
        TextView Main;
        TextView SubMain;
    }
}
