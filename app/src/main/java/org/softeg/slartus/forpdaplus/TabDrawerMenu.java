package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.util.ArrayList;
import java.util.List;

public class TabDrawerMenu {
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private Activity mActivity;
    private SelectItemListener mSelectItemListener;
    private Resources resources;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private static TabAdapter adapter;
    private ListView mListView;


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
        mListView.setStackFromBottom(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("tabsBottom", false));



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

        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getInstance().getTabItems());
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
    public static void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }

    public void addTab(String name, String url, String tag, Fragment fragment, boolean select){
        TabItem item = null;
        if(App.getInstance().isContainsByUrl(url)){
            if(select) item = App.getInstance().getTabByUrl(url);
        }else if(!App.getInstance().isContainsByTag(tag)) {

            item = new TabItem(name, url, tag, fragment);
            App.getInstance().getTabItems().add(item);
            App.getInstance().plusTabIterator();
            adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getInstance().getTabItems());
            mListView.setAdapter(adapter);
        }else {
            if(select) item = App.getInstance().getTabByTag(tag);
        }
        if(select) selectTab(item);
    }

    public void removeTab(String tag){
        if(App.getInstance().getTabItems().size()<=1) return;

        for(int i = 0; i <= App.getInstance().getTabItems().size()-1; i++){
            if(App.getInstance().getTabItems().get(i).getTag().equals(tag)) {
                App.getInstance().getTabItems().remove(i);
                if(tag.equals(App.getInstance().getCurrentFragmentTag()))
                    App.getInstance().setCurrentFragmentTag(App.getInstance().getTabItems().get(App.getInstance().getLastTabPosition(i)).getTag());

                ((MainActivity)getContext()).showFragmentByTag(App.getInstance().getCurrentFragmentTag(), true);
                ((MainActivity)getContext()).endActionFragment(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()).getTitle());
                adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getInstance().getTabItems());
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

    public void selectTab(TabItem tabItem) {
        mSelectItemListener.selectTab(tabItem);
        adapter.notifyDataSetChanged();
    }

    private Context getContext() {
        return mActivity;
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
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
            holder.close.setOnClickListener(new CloseClickListener(item.getTag()));

            holder.text.setTextColor(resources.getColor(App.getInstance().isWhiteTheme() ? R.color.drawer_menu_text_wh : R.color.drawer_menu_text_bl));
            holder.item.setBackgroundResource(Color.TRANSPARENT);

            if(App.getInstance().getCurrentFragmentTag().equals(item.getTag())){
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
        public CloseClickListener(String tag){
            this.tag = tag;
        }
        public void onClick(View v) {
            if(App.getInstance().getTabItems().size()>1) {
                ((MainActivity) getContext()).tryRemoveTab(tag);
            }else {
                new MaterialDialog.Builder(getContext())
                        .content("Закрыть приложение?")
                        .positiveText("Да")
                        .negativeText("Нет")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                App.getInstance().exit();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                close();
                            }
                        })
                        .show();
            }
        }
    }
}

