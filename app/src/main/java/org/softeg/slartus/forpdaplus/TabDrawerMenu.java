package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;
import java.util.List;

public class TabDrawerMenu {
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Activity mActivity;
    private SelectItemListener mSelectItemListener;
    private Handler mHandler = new Handler();
    private Resources resources;

    private TabAdapter adapter;
    private ListView mListView;
    List mTabItems = new ArrayList();


    public interface SelectItemListener {
        void selectTab(TabItem tabItem);
    }

    public TabDrawerMenu(Activity activity, SelectItemListener listener) {
        resources = App.getInstance().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        if (dpWidth > displayMetrics.density * 400) {
            dpWidth = displayMetrics.density * 400;
        }
        dpWidth -= 80 * displayMetrics.density;
        mActivity = activity;
        mSelectItemListener = listener;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mDrawer = (RelativeLayout) findViewById(R.id.tab_drawer);
        mListView = (ListView) findViewById(R.id.tab_list);
        mListView.setOnItemClickListener(new TabOnClickListener());



        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.END);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = Gravity.LEFT;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        }else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }
        mDrawer.setLayoutParams(params);

        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getTabItems());
        mListView.setAdapter(adapter);
    }
    private class TabOnClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object o = adapter.getItem(position);
            selectTab((TabItem)o);
            close();
        }
    }
    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }
    public void addTab(String name, String url, Fragment fragment){
        if(!findTabByTag(url)) {
            TabItem item = new TabItem(name, url, fragment);
            App.getTabItems().add(item);
            adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getTabItems());
            mListView.setAdapter(adapter);
        }

    }
    public void addTabSelect(String name, String url, Fragment fragment){
        //findTabByTag(url);
        TabItem item = new TabItem(name,url,fragment);
        App.getTabItems().add(item);
        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getTabItems());
        mListView.setAdapter(adapter);
        selectTab(item);
    }
    public boolean findTabByTag(String tag){
        boolean result=false;
        for(Object item:App.getTabItems()){
            if(((TabItem)item).getUrl().equals(tag)) result=true;
        }
        return result;
    }
    public void removeTab(String tag){
        if(App.getTabItems().size()<=1) return;

        for(int i = 0; i <= App.getTabItems().size()-1; i++){
            if(App.getTabItems().get(i).getUrl().equals(tag)) {
                //((MainActivity)getContext()).removeTab(tag);
                App.getTabItems().remove(i);
                if(tag.equals(App.getCurrentFragmentTag())){
                    App.setCurrentFragmentTag(App.getTabItems().get(App.getLastTabPosition(i)).getUrl());
                }

                ((MainActivity)getContext()).showFragmentByTag(App.getCurrentFragmentTag());
                adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getTabItems());
                mListView.setAdapter(adapter);
                return;
            }
        }

    }




    public void close() {
        mDrawerLayout.closeDrawer(mDrawer);
    }

    public Boolean isOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawer);
    }

    private void selectTab(TabItem tabItem) {
        mSelectItemListener.selectTab(tabItem);
        adapter.notifyDataSetChanged();
    }

    private Context getContext() {
        return mActivity;
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    public void syncState() {
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }


    public class TabItem{
        private String title;
        private String url;
        private Fragment fragment;
        public TabItem(String title, String url, Fragment fragment){
            this.title = title;
            this.url = url;
            this.fragment = fragment;
        }
        public String getTitle() {
            return title;
        }
        public String getUrl() {
            return url;
        }
        public Fragment createFragment(){return fragment;}
    }
    public class TabAdapter extends ArrayAdapter{
        final LayoutInflater inflater;
        List<TabItem> mObjects = null;
        public TabAdapter(Context context, int item_resource, List<TabItem> objects) {
            super(context, item_resource, objects);
            mObjects = new ArrayList<TabItem>(objects);
            inflater = LayoutInflater.from(context);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.tab_drawer_item, parent, false);
                holder = new ViewHolder();
                assert convertView != null;

                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.close = (ImageView) convertView.findViewById(R.id.close);
                holder.item = (RelativeLayout) convertView.findViewById(R.id.item);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TabItem item = mObjects.get(position);
            holder.text.setText(item.getTitle());
            holder.close.setOnClickListener(new CloseClickListener(item.getUrl()));

            holder.text.setTextColor(resources.getColor(App.getInstance().isWhiteTheme() ? R.color.drawer_menu_text_wh : R.color.drawer_menu_text_bl));
            holder.item.setBackgroundResource(Color.TRANSPARENT);

            if(App.getCurrentFragmentTag().equals(item.getUrl())){
                holder.text.setTextColor(resources.getColor(R.color.selectedItemText));
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
    public class CloseClickListener implements View.OnClickListener {
        String tag;
        public CloseClickListener(String url){
            tag = url;
        }
        public void onClick(View v) {
            if(App.getTabItems().size()>1) {
                ((MainActivity) getContext()).removeTab(tag);
            }else {
                close();
            }
        }
    }
}

