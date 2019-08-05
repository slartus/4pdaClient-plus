package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.util.ArrayList;
import java.util.List;

public class TabDrawerMenu {
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private Activity mActivity;
    private SelectItemListener mSelectItemListener;
    public static TabAdapter adapter;
    private ListView mListView;
    private Handler handler = new Handler();


    public interface SelectItemListener {
        void selectTab(TabItem tabItem);
    }

    TabDrawerMenu(Activity activity, SelectItemListener listener) {
        Resources resources = App.getInstance().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        if (dpWidth > displayMetrics.density * 400) {
            dpWidth = displayMetrics.density * 400;
        }
        dpWidth -= 80 * displayMetrics.density;
        mActivity = activity;
        mSelectItemListener = listener;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Button closeAll = (Button) findViewById(R.id.closeAll);
        closeAll.setOnClickListener(v -> {
            if (App.getInstance().getTabItems().size() > 1)
                closeAllTabs();
            else {
                closeDialog();
                toggleOpenState();
            }
        });
        closeAll.setOnLongClickListener(v -> {
            toggleOpenState();
            closeDialog();
            return false;
        });


        mDrawer = (RelativeLayout) findViewById(R.id.tab_drawer);
        mListView = (ListView) findViewById(R.id.tab_list);
        mListView.setOnItemClickListener(new TabOnClickListener());
        mListView.setStackFromBottom(App.getInstance().getPreferences().getBoolean("tabsBottom", false));

        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = Gravity.START;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        } else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }
        mDrawer.setLayoutParams(params);

        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getInstance().getTabItems());
        mListView.setAdapter(adapter);
    }

    private void closeAllTabs() {
        close();
        new Handler().postDelayed(() -> {
            String lastBrick = Preferences.Lists.getLastSelectedList();
            List<TabItem> itemsForClose = new ArrayList<>();

            for (TabItem item : App.getInstance().getTabItems())
                if (!lastBrick.equals(item.getTag()))
                    itemsForClose.add(item);
            ((MainActivity) getContext()).removeTabs(itemsForClose);
            App.getInstance().setCurrentFragmentTag(lastBrick);
            if (!App.getInstance().isContainsByTag(lastBrick)) {
                ((MainActivity) getContext()).selectItem(ListCore.getRegisteredBrick(lastBrick));
            } else {
                ((MainActivity) getContext()).selectTab(App.getInstance().getTabByTag(lastBrick));
            }
            refreshAdapter();
            notifyDataSetChanged();
        }, 300);

    }

    void toggleOpenState() {
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            mDrawerLayout.openDrawer(mDrawer);
//            ((MainActivity)getContext()).hideKeyboard();
        }
    }

    private class TabOnClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            selectTab((TabItem) adapter.getItem(position));
            close();
        }
    }

    private Runnable notifyAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };

    public void notifyDataSetChanged() {
        handler.postDelayed(notifyAdapter, 300);
    }

    void refreshAdapter() {
        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, App.getInstance().getTabItems());
        mListView.setAdapter(adapter);
    }

    void removeTab(String tag) {
        if (App.getInstance().getTabItems().size() <= 1) {
            ((MainActivity) getContext()).appExit();
            return;
        }

        for (int i = 0; i <= App.getInstance().getTabItems().size() - 1; i++) {
            if (App.getInstance().getTabItems().get(i).getTag().equals(tag)) {
                final TabItem tabItem = App.getInstance().getTabByTag(tag);
                tabItem.setFragment(null);
                App.getInstance().getTabItems().remove(tabItem);

                if (App.getInstance().getTabByTag(tabItem.getParentTag()) != null)
                    App.getInstance().setCurrentFragmentTag(tabItem.getParentTag());
                else if (tag.equals(App.getInstance().getCurrentFragmentTag()))
                    App.getInstance().setCurrentFragmentTag(App.getInstance().getTabItems().get(App.getInstance().getLastTabPosition(i)).getTag());

                ((MainActivity) getContext()).showFragment(App.getInstance().getCurrentFragmentTag(), true);
                ((MainActivity) getContext()).endActionFragment(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()).getTitle());
                ((MainActivity) getContext()).getmMainDrawerMenu().setItemCheckable(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()).getTitle());
                refreshAdapter();
                return;
            }
        }
    }

    public void close() {
        mDrawerLayout.closeDrawer(mDrawer);
    }

    Boolean isOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawer);
    }

    void selectTab(TabItem tabItem) {
        mSelectItemListener.selectTab(tabItem);
        notifyDataSetChanged();
        if (ListCore.getRegisteredBrick(tabItem.getTag()) != null) {
            Preferences.Lists.setLastSelectedList(tabItem.getTag());
            Preferences.Lists.addLastAction(tabItem.getTag());
        }
    }

    private Context getContext() {
        return mActivity;
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    public class TabAdapter extends ArrayAdapter {
        final LayoutInflater inflater;
        List<TabItem> mObjects;

        TabAdapter(Context context, int item_resource, List<TabItem> objects) {
            //noinspection unchecked
            super(context, item_resource, objects);
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
            holder.close.setOnClickListener(new CloseClickListener(item.getTag()));

            holder.text.setTextColor(ContextCompat.getColor(App.getContext(), App.getInstance().getDrawerMenuText()));
            holder.item.setBackgroundResource(android.R.color.transparent);

            if (App.getInstance().getCurrentFragmentTag().equals(item.getTag())) {
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

    public class CloseClickListener implements View.OnClickListener {
        String tag;

        CloseClickListener(String tag) {
            this.tag = tag;
        }

        public void onClick(View v) {
            if (App.getInstance().getTabItems().size() > 1) {
                ((MainActivity) getContext()).tryRemoveTab(tag);
            } else {
                closeDialog();
            }
        }
    }

    private void closeDialog() {
        new MaterialDialog.Builder(getContext())
                .content(R.string.ask_close_app)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((materialDialog, dialogAction) -> {
                    Process.killProcess(Process.myPid());
                    System.exit(1);
                })
                .onNegative((materialDialog, dialogAction) -> close())
                .show();
    }
}

