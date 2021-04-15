package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.fragments.search.CheckableForumItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 15.11.11
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class ForumsAdapter extends ArrayAdapter<CheckableForumItem> {
    private final LayoutInflater m_Inflater;

    public ForumsAdapter(Context context,
                         ArrayList<CheckableForumItem> objects) {
        super(context, R.layout.search_forum_item, objects);

        m_Inflater = LayoutInflater.from(context);
    }

    public Set<String> getCheckedIds() {
        Set<String> res = new HashSet<>();
        for (int i = 0; i < getCount(); i++) {
            CheckableForumItem item = this.getItem(i);
            if (item.IsChecked)
                res.add(item.Id);
        }
        return res;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            rowView = m_Inflater.inflate(R.layout.search_forum_item, null);
            holder = new ViewHolder();
            assert rowView != null;
            rowView.setClickable(false);
            rowView.setFocusable(false);
            holder.text = rowView
                    .findViewById(R.id.text);
            holder.checkBox = rowView
                    .findViewById(R.id.checkBox);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    CheckableForumItem item = getItem((int) holder.checkBox.getTag());
                    item.IsChecked = b;
                    notifyDataSetChanged();
                }
            });

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        CheckableForumItem item = this.getItem(position);
        holder.checkBox.setText(item.Title);
        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(item.IsChecked);

        holder.text.setPadding(30 * item.level, 0, 0, 0);

        return rowView;
    }

    public void toggleChecked(int position) {
        CheckableForumItem item = this.getItem(position);
        item.IsChecked = !item.IsChecked;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        CheckBox checkBox;
        TextView text;
    }


}
