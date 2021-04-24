package org.softeg.slartus.forpdaplus.controls.TabDrawerMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.repositories.TabsRepository;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.util.List;

public class TabAdapter extends ArrayAdapter {
    final LayoutInflater inflater;
    private final OnTabItemListener onTabItemListener;
    List<TabItem> mObjects;

    TabAdapter(Context context,
               OnTabItemListener onTabItemListener,
               List<TabItem> objects) {
        //noinspection unchecked
        super(context, R.layout.tab_drawer_item, objects);
        this.onTabItemListener = onTabItemListener;
        mObjects = objects;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tab_drawer_item, parent, false);
            holder = new ViewHolder();
            assert convertView != null;

            holder.text = convertView.findViewById(R.id.text);
            holder.close = convertView.findViewById(R.id.close);
            holder.item = convertView.findViewById(R.id.item);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TabItem item = mObjects.get(position);
        holder.text.setText(item.getTitle());
        holder.close.setOnClickListener(v -> onTabItemListener.onClose(item));

        holder.text.setTextColor(ContextCompat.getColor(App.getContext(), AppTheme.getDrawerMenuText()));
        holder.item.setBackgroundResource(android.R.color.transparent);

        if (TabsRepository.getInstance().getCurrentFragmentTag().equals(item.getTag())) {
            holder.text.setTextColor(ContextCompat.getColor(App.getContext(), R.color.selectedItemText));
            holder.item.setBackgroundResource(R.color.selectedItem);
        }

        return convertView;
    }

    public class ViewHolder {
        public TextView text;
        public ImageView close;
        public RelativeLayout item;
    }
}
