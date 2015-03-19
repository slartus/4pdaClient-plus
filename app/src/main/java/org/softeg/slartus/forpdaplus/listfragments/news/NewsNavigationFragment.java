package org.softeg.slartus.forpdaplus.listfragments.news;


import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.BaseBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;

/*
 * Created by slinkin on 21.02.14.
 */
public class NewsNavigationFragment extends BaseBrickFragment implements ActionBar.OnNavigationListener {

    private ArrayList<NewsCategoryItem> mItems = null;

    private ArrayList<NewsCategoryItem> getItems() {
        if (mItems == null) {
            mItems = new ArrayList<>();
            mItems.add(new NewsCategoryItem("", "Новости", "Все"));
            mItems.add(new NewsCategoryItem("news", "Новости", "Новости"));
            mItems.add(new NewsCategoryItem("articles", "Новости", "Статьи"));
            mItems.add(new NewsCategoryItem("software", "Новости", "Программы"));
            mItems.add(new NewsCategoryItem("software/tag/programs-for-android", "Новости/Программы", "   Android"));
            mItems.add(new NewsCategoryItem("software/tag/programs-for-ios", "Новости/Программы", "   iOS"));
            mItems.add(new NewsCategoryItem("software/tag/programs-for-windows-phone-7", "Новости/Программы", "   WP"));
            mItems.add(new NewsCategoryItem("games", "Новости", "Игры"));
            mItems.add(new NewsCategoryItem("games/tag/programs-for-android", "Новости/Игры", "   Android"));
            mItems.add(new NewsCategoryItem("games/tag/programs-for-ios", "Новости/Игры", "   iOS"));
            mItems.add(new NewsCategoryItem("games/tag/programs-for-windows-phone-7", "Новости/Игры", "   WP"));
            mItems.add(new NewsCategoryItem("reviews", "Новости", "Обзоры"));
        }
        return mItems;
    }

    private NewsPagerBrickInfo newsBrickInfo;

    public NewsNavigationFragment() {
        super();
    }

    public static NewsNavigationFragment createInstance(NewsPagerBrickInfo newsBrickInfo){
        NewsNavigationFragment fragment=new NewsNavigationFragment();
        Bundle args=new Bundle();
        args.putSerializable("NewsPagerBrickInfo",newsBrickInfo);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        selectItem(i);
        return true;
    }

    private void selectItem(int position) {
        position = Math.min(position, getItems().size() - 1);// на всякий случай, если изменится в будущем кол-во разделов
        String tag = getItems().get(position).Tag;

        Preferences.News.setLastSelectedSection(position);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.news_content_frame);
        if (currentFragment != null) {
            if (((IBrickFragment) currentFragment)
                    .getListName().equals(tag)) {

                return;
            }
            currentFragment.onDestroy();
        }

        Fragment fragment = NewsListFragment.newInstance(tag);
        fragmentManager.beginTransaction()
                .replace(R.id.news_content_frame, fragment)
                .commit();
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        if(getArguments()!=null&&getArguments().containsKey("NewsPagerBrickInfo"))
            newsBrickInfo=(NewsPagerBrickInfo)getArguments().getSerializable("NewsPagerBrickInfo");
        if (savedInstanceState != null) {
            newsBrickInfo = new NewsPagerBrickInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.news_navigation_fragment, container, false);
        assert v != null;

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity().getActionBar() != null;
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);

        NavigationListAdapter listAdapter = new NavigationListAdapter(getActivity());
        getActivity().getActionBar().setListNavigationCallbacks(listAdapter, this);

        getActivity().getActionBar().setSelectedNavigationItem(Preferences.News.getLastSelectedSection());

    }

    @Override
    public String getListName() {
        return newsBrickInfo == null ? null : newsBrickInfo.getName();
    }

    @Override
    public String getListTitle() {
        return newsBrickInfo == null ? null : newsBrickInfo.getTitle();
    }

    @Override
    public void loadData(boolean isRefresh) {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.news_content_frame);
        if (currentFragment != null) {
            ((IBrickFragment) currentFragment).loadData(isRefresh);
        }
    }

    @Override
    public void startLoad() {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.news_content_frame);
        if (currentFragment != null) {
            ((IBrickFragment) currentFragment).startLoad();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.news_content_frame);
        if (currentFragment != null) {
            return ((IBrickFragment) currentFragment).dispatchKeyEvent(event);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.news_content_frame);
        if (currentFragment != null) {
//            getFragmentManager().beginTransaction().remove(currentFragment).commit();
            currentFragment.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public class NewsCategoryItem {
        public String Path;
        public String Title;
        public String Tag;

        public NewsCategoryItem(String tag, String path, String title) {
            this.Tag = tag;
            this.Path = path;
            this.Title = title;
        }
    }

    public class NavigationListAdapter extends BaseAdapter implements SpinnerAdapter {
        private final LayoutInflater mInflater;


        public NavigationListAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return getItems().size();
        }

        @Override
        public Object getItem(int i) {
            return getItems().get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.news_navigation_item, viewGroup, false);
                holder = new ViewHolder();

                assert view != null;
                holder.text1 = (TextView) view.findViewById(R.id.text1);


                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }
            NewsCategoryItem item = getItems().get(i);


            holder.text1.setText(item.Title);

            return view;
        }

        class ViewHolder {
            TextView text1;


        }
    }
}
