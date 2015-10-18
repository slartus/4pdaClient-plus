package org.softeg.slartus.forpdaplus;/*
 * Created by slinkin on 07.04.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;
import org.softeg.slartus.forpdaplus.tabs.Tabs;

import java.util.ArrayList;

public class MainDrawerMenu {
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Activity mActivity;
    private SelectItemListener mSelectItemListener;
    private BaseExpandableListAdapter mAdapter;
    private Handler mHandler = new Handler();
    private SharedPreferences prefs;
    private Resources resources;


    public interface SelectItemListener {
        void selectItem(BrickInfo brickInfo);
    }

    public MainDrawerMenu(Activity activity, SelectItemListener listener) {
        resources = App.getInstance().getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        if (dpWidth > displayMetrics.density * 400) {
            dpWidth = displayMetrics.density * 400;
        }
        dpWidth -= 80 * displayMetrics.density;
        mActivity = activity;
        mSelectItemListener = listener;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer_list);
        mDrawer = (RelativeLayout) findViewById(R.id.left_drawer);


        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = Gravity.RIGHT;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        }
        mDrawer.setLayoutParams(params);
        mDrawerList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                Preferences.Menu.setGroupExpanded(i, true);
            }
        });

        mDrawerList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                Preferences.Menu.setGroupExpanded(i, false);
            }
        });
        mMenuGroups = new ArrayList<>();
        if (prefs.getBoolean("categoryLast", true)) {
            mMenuGroups.add(new LastActionsGroup());
        }
        mMenuGroups.add(new MainListGroup());
        mMenuGroups.add(new OthersActionsGroup());

        mAdapter = new MenuBrickAdapter(getContext());

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnChildClickListener(new BrickItemClickListener());
        for (int i = 0; i < mMenuGroups.size(); i++) {
            if (Preferences.Menu.getGroupExpanded(i))
                mDrawerList.expandGroup(i);
        }

        mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout,
                R.drawable.ic_drawer, R.string.menu, R.string.app_name) {
            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View drawerView) {
            }
        };


        BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
        if (brickInfo == null)
            brickInfo = new NewsPagerBrickInfo();
        selectItem(brickInfo);
    }

    /*
    Область вытягивания
     */
    private void setDrawerLayoutArea(Activity activity, Boolean left) {
//        try {
//            String draggerName = left ? "mLeftDragger" : "mRightDragger";
//            Field draggerField = mDrawerLayout.getClass().getDeclaredField(
//                    draggerName);//mRightDragger for right obviously
//            draggerField.setAccessible(true);
//            ViewDragHelper draggerObj = (ViewDragHelper) draggerField
//                    .get(mDrawerLayout);
//
//            Field edgeSizeField = draggerObj.getClass().getDeclaredField(
//                    "mEdgeSize");
//            edgeSizeField.setAccessible(true);
//            int edge = edgeSizeField.getInt(draggerObj);
//
//            edgeSizeField.setInt(draggerObj, edge * 5);
//        } catch (Throwable ex) {
//            Log.e(activity, ex);
//        }
    }

    public void toggleOpenState() {
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            mDrawerLayout.openDrawer(mDrawer);
        }
    }

    public void close() {
        mDrawerLayout.closeDrawer(mDrawer);
    }

    public Boolean isOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawer);
    }

    private void selectItem(BrickInfo brickIinfo) {
        mSelectItemListener.selectItem(brickIinfo);

        Preferences.Lists.setLastSelectedList(brickIinfo.getName());
        Preferences.Lists.addLastAction(brickIinfo.getName());
        mAdapter.notifyDataSetChanged();
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

    private class BrickItemClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
                                    long id) {

            Object o = mAdapter.getChild(groupPosition, childPosition);
            MenuGroup menuGroup = (MenuGroup) mAdapter.getGroup(groupPosition);
            BrickInfo brickInfo = (BrickInfo) o;
            assert menuGroup != null;
            menuGroup.itemAction(brickInfo);
            int i = 1;
            if (!prefs.getBoolean("categoryLast", true)) i = 0;
            if (groupPosition <= i) {
                prefs.edit().putInt("menuItemGroup", groupPosition).apply();
                prefs.edit().putString("menuItemChild", brickInfo.getName()).apply();
            }

            return true;
        }
    }

    public abstract class MenuGroup {
        private String mName;
        private String mTitle;

        public MenuGroup(String name, String title) {
            mName = name;
            mTitle = title;
        }

        public abstract ArrayList<BrickInfo> getChildren();

        public String getTitle() {
            return mTitle;
        }

        public abstract void itemAction(BrickInfo item);
    }

    public class MainListGroup extends MenuGroup {
        public MainListGroup() {
            super("MainListGroup", "Все");
        }

        @Override
        public ArrayList<BrickInfo> getChildren() {
            return ListCore.getMainMenuBricks();
        }

        @Override
        public void itemAction(BrickInfo item) {
            selectItem(item);
        }
    }

    public class LastActionsGroup extends MenuGroup {

        public LastActionsGroup() {
            super("LastActionsGroup", "Последние");
        }

        @Override
        public ArrayList<BrickInfo> getChildren() {
            return ListCore.createBricks(Preferences.Lists.getLastActions());
        }

        @Override
        public void itemAction(BrickInfo item) {
            selectItem(item);
        }
    }

    public class OthersActionsGroup extends MenuGroup {
        private ArrayList<BrickInfo> children = new ArrayList<>();

        public OthersActionsGroup() {
            super("OthersActionsGroup", "Разное");
            // children.add(new SearchBrickInfo());
            children.add(new PreferencesBrickInfo());
            children.add(new DownloadsBrickInfo());
            children.add(new MarkAllReadBrickInfo());
        }

        @Override
        public ArrayList<BrickInfo> getChildren() {

            return children;
        }

        @Override
        public void itemAction(BrickInfo item) {
            switch (item.getName()) {
                case PreferencesBrickInfo.NAME:
                    Intent settingsActivity = new Intent(
                            mActivity, PreferencesActivity.class);
                    mActivity.startActivity(settingsActivity);
                    break;

                case DownloadsBrickInfo.NAME:
                    try {
                        QuickStartActivity.showTab(mActivity, Tabs.TAB_DOWNLOADS);
                        //Toast.makeText(mActivity, Client.getInstance().getUser(), Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        AppLog.e(mActivity, ex);
                    }
                    break;
                case MarkAllReadBrickInfo.NAME:
                    if (!Client.getInstance().getLogined()) {
                        Toast.makeText(mActivity, "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new MaterialDialog.Builder(mActivity)
                            .title("Подтвердите действие")
                            .content("Отметить весь форум прочитанным?")
                            .positiveText("Да")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    Toast.makeText(mActivity, "Запрос отправлен", Toast.LENGTH_SHORT).show();
                                    new Thread(new Runnable() {
                                        public void run() {
                                            Throwable ex = null;
                                            try {
                                                Client.getInstance().markAllForumAsRead();
                                            } catch (Throwable e) {
                                                ex = e;
                                            }

                                            final Throwable finalEx = ex;

                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    try {
                                                        if (finalEx != null) {
                                                            Toast.makeText(mActivity, "Ошибка", Toast.LENGTH_SHORT).show();
                                                            AppLog.e(mActivity, finalEx);
                                                        } else {
                                                            Toast.makeText(mActivity, "Форум отмечен прочитанным", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception ex) {
                                                        AppLog.e(mActivity, ex);
                                                    }

                                                }
                                            });
                                        }
                                    }).start();
                                }
                            })
                            .negativeText("Отмена")
                            .show();
                    break;
            }
            close();
        }

        private class PreferencesBrickInfo extends BrickInfo {
            public static final String NAME = "Preferences";

            @Override
            public String getTitle() {
                return "Настройки программы";
            }

            @Override
            public int getIcon() {
                return R.drawable.ic_settings_grey600_24dp;
            }

            @Override
            public String getName() {
                return NAME;
            }

            @Override
            public Fragment createFragment() {
                return null;
            }
        }

        private class SearchBrickInfo extends BrickInfo {
            public static final String NAME = "Search";

            @Override
            public String getTitle() {
                return "Поиск по форуму";
            }

            @Override
            public int getIcon() {
                return R.drawable.ic_close_grey600_24dp;
            }

            @Override
            public String getName() {
                return NAME;
            }

            @Override
            public Fragment createFragment() {
                return null;
            }
        }

        private class DownloadsBrickInfo extends BrickInfo {
            public static final String NAME = "Downloads";

            @Override
            public String getTitle() {
                return "Загрузки";
            }

            @Override
            public int getIcon() {
                return R.drawable.ic_download_grey600_24dp;
            }

            @Override
            public String getName() {
                return NAME;
            }

            @Override
            public Fragment createFragment() {
                return null;
            }
        }

        private class MarkAllReadBrickInfo extends BrickInfo {
            public static final String NAME = "MarkAllRead";

            @Override
            public String getTitle() {
                return "Отметить весь форум прочитанным";
            }

            @Override
            public int getIcon() {
                return R.drawable.ic_check_all_grey600_24dp;
            }

            @Override
            public String getName() {
                return NAME;
            }

            @Override
            public Fragment createFragment() {
                return null;
            }
        }
    }

    private ArrayList<MenuGroup> mMenuGroups;

    public class MenuBrickAdapter extends BaseExpandableListAdapter {
        final LayoutInflater inflater;

        public MenuBrickAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return mMenuGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mMenuGroups.get(groupPosition).getChildren().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mMenuGroups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mMenuGroups.get(groupPosition).getChildren().get(childPosition);
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
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.main_drawer_menu_group, parent, false);
                holder = new ViewHolder();
                assert convertView != null;

                holder.text = (TextView) convertView.findViewById(R.id.group);

                convertView.setTag(holder);


            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MenuGroup item = mMenuGroups.get(groupPosition);

            holder.text.setText(item.getTitle());

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.main_drawer_menu_row, parent, false);
                holder = new ViewHolder();
                assert convertView != null;

                holder.text = (TextView) convertView.findViewById(R.id.row_title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.item = (LinearLayout) convertView.findViewById(R.id.item);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BrickInfo item = mMenuGroups.get(groupPosition).getChildren().get(childPosition);
            holder.text.setText(item.getTitle());
            holder.icon.setImageDrawable(getContext().getResources().getDrawable(item.getIcon()));
            if (groupPosition == prefs.getInt("menuItemGroup", 0)) {
                if (item.getName().equals(prefs.getString("menuItemChild", "News_pages"))) {
                    holder.text.setTextColor(resources.getColor(R.color.selectedItemText));
                    holder.item.setBackgroundResource(R.color.selectedItem);
                    holder.icon.setColorFilter(resources.getColor(R.color.selectedItemText), PorterDuff.Mode.SRC_ATOP);
                } else {
                    holder.text.setTextColor(resources.getColor(getTextColor()));
                    holder.item.setBackgroundResource(Color.TRANSPARENT);
                    holder.icon.clearColorFilter();
                }
            } else {
                holder.text.setTextColor(resources.getColor(getTextColor()));
                holder.item.setBackgroundResource(Color.TRANSPARENT);
                holder.icon.clearColorFilter();
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


        public class ViewHolder {
            public TextView text;
            public ImageView icon;
            public LinearLayout item;
        }
    }
    private int getTextColor() {
        if (App.getInstance().isWhiteTheme()) {
            return R.color.drawer_menu_text_wh;
        } else {
            return R.color.drawer_menu_text_bl;
        }
    }
}
