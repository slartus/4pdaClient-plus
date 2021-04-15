package org.softeg.slartus.forpdaplus.listfragments.adapters;/*
 * Created by slinkin on 10.04.2014.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.BaseExpandableListFragment;

import java.util.ArrayList;

public class ExpandableMyListAdapter extends BaseExpandableListAdapter {
    final LayoutInflater mInflater;
    private final ArrayList<BaseExpandableListFragment.ExpandableGroup> mData;

    public ExpandableMyListAdapter(Context context, ArrayList<BaseExpandableListFragment.ExpandableGroup> mData) {
        this.mData = mData;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).getChildren().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mData.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition * 100;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 100 + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_drawer_menu_group, parent, false);
            holder = new GroupViewHolder();
            assert convertView != null;

            holder.text = convertView.findViewById(R.id.group);

            convertView.setTag(holder);


        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        BaseExpandableListFragment.ExpandableGroup item = mData.get(groupPosition);

        holder.text.setText(item.getTitle());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup parent) {

        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.Flag = view.findViewById(R.id.imgFlag);
            holder.TopLeft = view.findViewById(R.id.txtTopLeft);
            holder.TopRight = view.findViewById(R.id.txtTopRight);
            holder.Main = view.findViewById(R.id.txtMain);
            holder.SubMain = view.findViewById(R.id.txtSubMain);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        IListItem topic =  mData.get(groupPosition).getChildren().get(childPosition);
        holder.TopLeft.setText(topic.getTopLeft());
        holder.TopRight.setText(topic.getTopRight());
        holder.Main.setText(topic.getMain());
        holder.SubMain.setText(topic.getSubMain());

        switch (topic.getState()) {
            case IListItem.STATE_GREEN:
                holder.Flag.setBackgroundColor(App.getContext().getResources().getColor(R.color.new_flag));
                break;
            case IListItem.STATE_RED:
                holder.Flag.setBackgroundColor(App.getContext().getResources().getColor(R.color.old_flag));
                break;
            default:
                holder.Flag.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public class GroupViewHolder {
        public TextView text;
    }

    class ViewHolder {
        LinearLayout Flag;
        TextView TopLeft;
        TextView TopRight;
        TextView Main;
        TextView SubMain;
    }
}
