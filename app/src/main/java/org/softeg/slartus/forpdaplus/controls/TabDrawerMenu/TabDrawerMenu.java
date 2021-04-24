package org.softeg.slartus.forpdaplus.controls.TabDrawerMenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.repositories.TabsRepository;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TabDrawerMenu {
    private final DrawerLayout mDrawerLayout;
    private final RelativeLayout mDrawer;
    private final WeakReference<Activity> mActivity;
    private final SelectItemListener mSelectItemListener;
    public static TabAdapter adapter;

    private final ArrayList<TabItem> tabs = new ArrayList<>();

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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Button closeAll = (Button) findViewById(R.id.closeAll);
        closeAll.setOnClickListener(v -> {
            if (TabsRepository.getInstance().size() > 1)
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
        ListView listView = (ListView) findViewById(R.id.tab_list);
        listView.setOnItemClickListener(new TabOnClickListener());
        listView.setStackFromBottom(App.getInstance().getPreferences().getBoolean("tabsBottom", false));

        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
        params.width = (int) dpWidth;
        if ("right".equals(Preferences.System.getDrawerMenuPosition())) {
            params.gravity = Gravity.START;
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        } else {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);
        }
        mDrawer.setLayoutParams(params);

        OnTabItemListener onTabItemListener = tabItem -> {
            if (TabsRepository.getInstance().size() > 1) {
                ((MainActivity) getContext()).tryRemoveTab(tabItem.getTag());
            } else {
                closeDialog();
            }
        };
        adapter = new TabAdapter(getContext(), onTabItemListener,tabs);
        listView.setAdapter(adapter);
        subscribeTabs();
    }

    private void subscribeTabs() {
        App.getInstance().addToDisposable(
                TabsRepository
                        .getInstance()
                        .getTabsSubject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                tabItems -> {
                                    tabs.clear();
                                    tabs.addAll(tabItems);
                                    adapter.notifyDataSetChanged();
                                },
                                throwable -> AppLog.e(mActivity.get(), throwable)
                        ));
    }

    private void closeAllTabs() {
        close();
        new Handler().postDelayed(() -> {
            String lastBrick = Preferences.Lists.getLastSelectedList();
            List<TabItem> itemsForClose = new ArrayList<>();

            for (TabItem item : TabsRepository.getInstance().getTabItems())
                if (!lastBrick.equals(item.getTag()))
                    itemsForClose.add(item);
            ((MainActivity) getContext()).removeTabs(itemsForClose);
            TabsRepository.getInstance().setCurrentFragmentTag(lastBrick);
            if (!TabsRepository.getInstance().isContainsByTag(lastBrick)) {
                ((MainActivity) getContext()).selectItem(ListCore.getRegisteredBrick(lastBrick));
            } else {
                ((MainActivity) getContext()).selectTab(TabsRepository.getInstance().getTabByTag(lastBrick));
            }
        }, 300);

    }

    public void toggleOpenState() {
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


    public void removeTab(String tag) {
        if (TabsRepository.getInstance().size() <= 1) {
            ((MainActivity) getContext()).appExit();
            return;
        }

        for (int i = 0; i <= TabsRepository.getInstance().size() - 1; i++) {
            if (TabsRepository.getInstance().getTabItems().get(i).getTag().equals(tag)) {
                final TabItem tabItem = TabsRepository.getInstance().getTabByTag(tag);
                tabItem.setFragment(null);
                TabsRepository.getInstance().remove(tabItem);

                if (TabsRepository.getInstance().getTabByTag(tabItem.getParentTag()) != null)
                    TabsRepository.getInstance().setCurrentFragmentTag(tabItem.getParentTag());
                else if (tag.equals(TabsRepository.getInstance().getCurrentFragmentTag()))
                    TabsRepository.getInstance().setCurrentFragmentTag(TabsRepository.getInstance().getTabItems().get(TabsRepository.getInstance().getLastTabPosition(i)).getTag());

                ((MainActivity) getContext()).showFragment(TabsRepository.getInstance().getCurrentFragmentTag(), true);
                ((MainActivity) getContext()).endActionFragment(TabsRepository.getInstance().getTabByTag(TabsRepository.getInstance().getCurrentFragmentTag()).getTitle());
                ((MainActivity) getContext()).getmMainDrawerMenu().setItemCheckable(TabsRepository.getInstance().getTabByTag(TabsRepository.getInstance().getCurrentFragmentTag()).getTitle());

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
        if (ListCore.getRegisteredBrick(tabItem.getTag()) != null) {
            Preferences.Lists.setLastSelectedList(tabItem.getTag());
            Preferences.Lists.addLastAction(tabItem.getTag());
        }
    }

    private Context getContext() {
        return mActivity.get();
    }

    private View findViewById(int id) {
        return mActivity.get().findViewById(id);
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

