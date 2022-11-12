package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Process;
import android.util.DisplayMetrics;
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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TabDrawerMenu {
    private final DrawerLayout mDrawerLayout;
    private final RelativeLayout mDrawer;
    private final WeakReference<Context> mActivity;
    private final SelectItemListener mSelectItemListener;
    public static TabAdapter adapter;
    private final ListView mListView;
    private final Handler handler = new Handler();


    public interface SelectItemListener {
        void selectTab(TabItem tabItem);
    }

    public TabDrawerMenu(Activity activity, SelectItemListener listener) {
        Resources resources = App.getInstance().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        if (dpWidth > displayMetrics.density * 400) {
            dpWidth = displayMetrics.density * 400;
        }
        dpWidth -= 80 * displayMetrics.density;
        mActivity = new WeakReference<>(activity);
        mSelectItemListener = listener;
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        Button closeAll = (Button) activity.findViewById(R.id.closeAll);
        closeAll.setOnClickListener(v -> {
            if (TabsManager.getInstance().getTabItems().size() > 1)
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


        mDrawer = (RelativeLayout) activity.findViewById(R.id.tab_drawer);
        mListView = (ListView) activity.findViewById(R.id.tab_list);
        mListView.setOnItemClickListener(new TabOnClickListener());
        mListView.setStackFromBottom(App.getInstance().getPreferences().getBoolean("tabsBottom", false));

        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = GravityCompat.START;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        } else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }
        mDrawer.setLayoutParams(params);

        adapter = new TabAdapter(activity, R.layout.tab_drawer_item, TabsManager.getInstance().getTabItems());
        mListView.setAdapter(adapter);
    }

    private void closeAllTabs() {
        close();
        new Handler().postDelayed(() -> {
            String lastBrick = Preferences.Lists.getLastSelectedList();
            List<TabItem> itemsForClose = new ArrayList<>();

            for (TabItem item : TabsManager.getInstance().getTabItems())
                if (!lastBrick.equals(item.getTag()))
                    itemsForClose.add(item);
            MainActivity activity = (MainActivity) getContext();
            activity.removeTabs(itemsForClose);
            TabsManager.getInstance().setCurrentFragmentTag(lastBrick);
            if (!TabsManager.getInstance().isContainsByTag(lastBrick)) {
                activity.selectItem(ListCore.getRegisteredBrick(lastBrick));
            } else {
                activity.selectTab(TabsManager.getInstance().getTabByTag(lastBrick));
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

    private final Runnable notifyAdapter = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };

    public void notifyDataSetChanged() {
        handler.postDelayed(notifyAdapter, 300);
    }

    void refreshAdapter() {
        adapter = new TabAdapter(getContext(), R.layout.tab_drawer_item, TabsManager.getInstance().getTabItems());
        mListView.setAdapter(adapter);
    }

    void removeTab(String tag) {
        MainActivity mainActivity = (MainActivity) getContext();
        if (TabsManager.getInstance().getTabItems().size() <= 1) {
            mainActivity.appExit();
            return;
        }

        for (int i = 0; i <= TabsManager.getInstance().getTabItems().size() - 1; i++) {
            if (TabsManager.getInstance().getTabItems().get(i).getTag().equals(tag)) {
                final TabItem tabItem = TabsManager.getInstance().getTabByTag(tag);
                tabItem.setFragment(null);
                TabsManager.getInstance().getTabItems().remove(tabItem);

                if (TabsManager.getInstance().getTabByTag(tabItem.getParentTag()) != null)
                    TabsManager.getInstance().setCurrentFragmentTag(tabItem.getParentTag());
                else if (tag.equals(TabsManager.getInstance().getCurrentFragmentTag()))
                    TabsManager.getInstance().setCurrentFragmentTag(TabsManager.getInstance().getTabItems().get(TabsManager.getInstance().getLastTabPosition(i)).getTag());

                mainActivity.showFragment(TabsManager.getInstance().getCurrentFragmentTag(), true);
                mainActivity.endActionFragment(TabsManager.getInstance().getTabByTag(TabsManager.getInstance().getCurrentFragmentTag()).getTitle());
                mainActivity.getmMainDrawerMenu().setItemCheckable(TabsManager.getInstance().getTabByTag(TabsManager.getInstance().getCurrentFragmentTag()).getTitle());
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
        return mActivity.get();
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

            holder.text.setTextColor(ContextCompat.getColor(App.getContext(), AppTheme.getDrawerMenuText()));
            holder.item.setBackgroundResource(android.R.color.transparent);

            if (TabsManager.getInstance().getCurrentFragmentTag().equals(item.getTag())) {
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
            if (TabsManager.getInstance().getTabItems().size() > 1) {
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

