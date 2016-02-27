package org.softeg.slartus.forpdaplus;/*
 * Created by slinkin on 07.04.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.DownloadFragment;
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment;
import org.softeg.slartus.forpdaplus.listtemplates.AppAndGame;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.DownloadsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.MarkAllReadBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.PreferencesBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;

import java.util.List;

public class MainDrawerMenu implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Activity mActivity;
    private SelectItemListener mSelectItemListener;
    private Handler mHandler = new Handler();
    private SharedPreferences prefs;

    private Menu menu;
    private int prevSelectedGroup;
    private int prevSelectedItem;
    private BrickInfo selectedBrick;


    public interface SelectItemListener {
        void selectItem(BrickInfo brickInfo);
    }

    public MainDrawerMenu(final Activity activity, SelectItemListener listener) {
        prefs = App.getInstance().getPreferences();
        DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        if (dpWidth > displayMetrics.density * 400) {
            dpWidth = displayMetrics.density * 400;
        }
        dpWidth -= 80 * displayMetrics.density;
        mActivity = activity;
        mSelectItemListener = listener;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mDrawer = (NavigationView) findViewById(R.id.left_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = Gravity.RIGHT;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        }
        mDrawer.setLayoutParams(params);

        setNavigationItems();

        mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, ((MainActivity)mActivity).toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ((MainActivity)mActivity).hidePopupWindows();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }


    private void setNavigationItems(){
        menu = mDrawer.getMenu();
        menu.clear();
        int itemId = 0;
        int i = 0;
        SubMenu subMenu;
        List<BrickInfo> list;
        subMenu = menu.addSubMenu(1, 0, 0,"Все");
        list = ListCore.getMainMenuBricks();
        for (i=0; i<list.size(); i++, itemId++)
            subMenu.add(1, itemId, i, list.get(i).getTitle()).setIcon(list.get(i).getIcon());
        subMenu = menu.addSubMenu(2, 0, 0, "Разное");
        list = ListCore.getOthersBricks();
        for(i = 0; i<list.size();i++, itemId++)
            subMenu.add(2, itemId, i, list.get(i).getTitle()).setIcon(list.get(i).getIcon());
    }

    public void setItemCheckable(String name){
        SubMenu subMenu;
        MenuItem item;
        for(int i = 0; i<menu.size();i++){
            subMenu = menu.getItem(i).getSubMenu();
            for(int j = 0; j<subMenu.size(); j++){
                item = subMenu.getItem(j);
                if(item.getTitle().equals(name)){
                    Log.e("keka", "true");
                    menu.getItem(prevSelectedGroup).getSubMenu().getItem(prevSelectedItem).setCheckable(false).setChecked(false);
                    item.setCheckable(true).setChecked(true);
                    prevSelectedGroup = i;
                    prevSelectedItem = j;
                    prefs.edit().putString("navItemTitle", name).apply();
                    return;
                }
            }
        }
    }

    private void selectItem(BrickInfo brickIinfo) {
        mSelectItemListener.selectItem(brickIinfo);

        Preferences.Lists.setLastSelectedList(brickIinfo.getName());
        Preferences.Lists.addLastAction(brickIinfo.getName());

        setItemCheckable(brickIinfo.getTitle());
    }

    public NavigationView getNavigationView(){
        return mDrawer;
    }
    public ActionBarDrawerToggle getDrawerToggle(){
        return mDrawerToggle;
    }
    public DrawerLayout getDrawerLayout(){
        return mDrawerLayout;
    }
    private Context getContext() {
        return mActivity;
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        close();

        switch (item.getGroupId()){
            case 0:
                selectedBrick = ListCore.createBricks(Preferences.Lists.getLastActions()).get(item.getOrder());
                break;
            case 1:
                selectedBrick =  ListCore.getMainMenuBricks().get(item.getOrder());
                break;
            case 2:
                selectedBrick = ListCore.getOthersBricks().get(item.getOrder());
                break;
        }

        switcha: switch (selectedBrick.getName()){
            case AppAndGame.NAME:
                final List<ApplicationInfo> packages = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo packageInfo : packages) {
                    if(packageInfo.packageName.equals("ru.freeman42.app4pda")){
                        getContext().startActivity(getContext().getPackageManager().getLaunchIntentForPackage(packageInfo.packageName));
                        break switcha;
                    }
                }
                ThemeFragment.showTopicById("275433");
                break;
            case PreferencesBrickInfo.NAME:
                mActivity.startActivityForResult(new Intent(mActivity, PreferencesActivity.class), 0);
                break;
            case DownloadsBrickInfo.NAME:
                try {
                    DownloadFragment.newInstance();
                } catch (Exception ex) {
                    AppLog.e(mActivity, ex);
                }
                break;
            case MarkAllReadBrickInfo.NAME:
                if (!Client.getInstance().getLogined()) {
                    Toast.makeText(mActivity, "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
                    break;
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
            default:
                if(item.getGroupId()!=2)
                    selectItem(selectedBrick);
        }
        return true;
    }
}
