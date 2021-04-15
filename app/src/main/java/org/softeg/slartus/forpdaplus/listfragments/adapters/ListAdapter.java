package org.softeg.slartus.forpdaplus.listfragments.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/*
 * Created by slartus on 21.02.14.
 */
public class ListAdapter extends BaseAdapter implements Filterable {
    private final LayoutInflater mInflater;
    protected ArrayList<? extends IListItem> mData;
    private final boolean showSubMain;


    public ListAdapter(Context context, ArrayList<? extends IListItem> data, boolean showSubMain) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        this.showSubMain = showSubMain;
    }

    public void setData(ArrayList<? extends IListItem> data) {
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

    private final int ITEM_TYPE = 0;
    private final int ITEM_NEW_TYPE = 1;
    private final int ITEM_OLD_TYPE = 2;

    private final int ITEM_PROGRESS_TYPE = 3;
    private final int ITEM_PROGRESS_NEW_TYPE = 4;
    private final int ITEM_PROGRESS_OLD_TYPE = 5;

    @Override
    public int getItemViewType(int position) {
        IListItem topic = mData.get(position);
        if (topic.isInProgress()) {
            switch (topic.getState()) {
                case IListItem.STATE_GREEN:
                    return ITEM_PROGRESS_NEW_TYPE;
                case IListItem.STATE_RED:
                    return ITEM_PROGRESS_OLD_TYPE;
                default:
                    return ITEM_PROGRESS_TYPE;
            }
        } else {
            switch (topic.getState()) {
                case IListItem.STATE_GREEN:
                    return ITEM_NEW_TYPE;
                case IListItem.STATE_RED:
                    return ITEM_OLD_TYPE;
                default:
                    return ITEM_TYPE;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    @Override
    public android.view.View getView(int position, android.view.View view, android.view.ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            switch (getItemViewType(position)) {
                case ITEM_TYPE:
                    view = mInflater.inflate(R.layout.list_item, parent, false);
                    break;
                case ITEM_NEW_TYPE:
                    view = mInflater.inflate(R.layout.list_item_green, parent, false);
                    break;
                case ITEM_OLD_TYPE:
                    view = mInflater.inflate(R.layout.list_item_red, parent, false);
                    break;
                case ITEM_PROGRESS_TYPE:
                    view = mInflater.inflate(R.layout.list_item_progress, parent, false);
                    break;
                case ITEM_PROGRESS_NEW_TYPE:
                    view = mInflater.inflate(R.layout.list_item_progress_green, parent, false);
                    break;
                case ITEM_PROGRESS_OLD_TYPE:
                    view = mInflater.inflate(R.layout.list_item_progress_red, parent, false);
                    break;
            }

            holder = new ViewHolder();
            assert view != null;
            holder.Flag = view.findViewById(R.id.imgFlag);
            holder.TopLeft = view.findViewById(R.id.txtTopLeft);
            holder.TopRight = view.findViewById(R.id.txtTopRight);
            holder.Main = view.findViewById(R.id.txtMain);
            holder.SubMain = view.findViewById(R.id.txtSubMain);
            holder.progress = view.findViewById(R.id.progressBar);
            holder.isPinned = view.findViewById(R.id.pinned);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        IListItem topic = mData.get(position);
        holder.TopLeft.setText(topic.getTopLeft());
        holder.TopRight.setText(topic.getTopRight());
        holder.Main.setText(topic.getMain());

        if (showSubMain && TextUtils.isEmpty(topic.getSubMain())) {
            holder.SubMain.setVisibility(View.GONE);
        } else {
            holder.SubMain.setVisibility(View.VISIBLE);
            holder.SubMain.setText(topic.getSubMain());
        }
        setVisibility(holder.progress, topic.isInProgress() ? View.VISIBLE : View.INVISIBLE);
        try {
            setVisibility(holder.isPinned, ((FavTopic) topic).isPinned() ? View.VISIBLE : View.GONE);
        } catch (ClassCastException ex) {
        }

        switch (topic.getState()) {
            case IListItem.STATE_GREEN:
                if (App.getInstance().getPreferences().getBoolean("oldIndicator", false)) {
                    setVisibility(holder.Flag, View.VISIBLE);
                    holder.Flag.setBackgroundColor(App.getContext().getResources().getColor(R.color.new_flag));
                }
                break;
            case IListItem.STATE_RED:
                setVisibility(holder.Flag, View.VISIBLE);
                holder.Flag.setBackgroundColor(App.getContext().getResources().getColor(R.color.old_flag));
                break;
            default:
                setVisibility(holder.Flag, View.INVISIBLE);
        }
        return view;
    }

    private void setVisibility(View v, int visibility) {
        if (v.getVisibility() != visibility)
            v.setVisibility(visibility);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mData = (ArrayList<IListItem>) results.values;
                ListAdapter.super.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<IListItem> FilteredArrayNames = new ArrayList<IListItem>();

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < mData.size(); i++) {
                    IListItem item = mData.get(i);
                    if (item.getMain().toString().contains(constraint)) {
                        FilteredArrayNames.add(item);
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }
        };

        return filter;
    }


    @Override
    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();

    }

    class ViewHolder {
        LinearLayout Flag;
        View progress;
        TextView TopLeft;
        TextView TopRight;
        TextView Main;
        TextView SubMain;
        View isPinned;
    }
}