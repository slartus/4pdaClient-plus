package org.softeg.slartus.forpdaplus.listfragments.news;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.News;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.fragments.NewsFragment;
import org.softeg.slartus.forpdaplus.listfragments.BaseTaskListFragment;
import org.softeg.slartus.forpdaplus.listtemplates.NewsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 20.02.14.
 */
public class NewsListFragment extends BaseTaskListFragment implements ActionBar.OnNavigationListener{
    /*
     *  Ключ подкаталога новостей:  news, articles..
     */
    public static final String TAG_EXTRA_KEY = "TAG_EXTRA_KEY";
    public static final String NEWS_LIST_URL_KEY = "NEWS_LIST_URL_KEY";

    protected ArrayList<News> mData = new ArrayList<>();
    protected ArrayList<News> mLoadResultList;
    protected ArrayList<News> mCacheList = new ArrayList<>();


    public static class NewsCategoryItem {
        String Path;
        public String Title;
        String Tag;

        NewsCategoryItem(String tag, String path, String title) {
            this.Tag = tag;
            this.Path = path;
            this.Title = title;
        }
    }

    public static class NavigationListAdapter extends BaseAdapter implements SpinnerAdapter {
        private final LayoutInflater mInflater;

        private ArrayList<NewsCategoryItem> mItems = null;

        private ArrayList<NewsCategoryItem> getItems() {
            if (mItems == null) {
                mItems = new ArrayList<>();
                mItems.add(new NewsCategoryItem("", "Новости", "Все"));
                mItems.add(new NewsCategoryItem("news", "Новости", "Новости"));
                mItems.add(new NewsCategoryItem("articles", "Новости", "Статьи"));
                mItems.add(new NewsCategoryItem("tag/how-to-android/", "Новости", "   Android"));
                mItems.add(new NewsCategoryItem("tag/how-to-ios/", "Новости", "   iOS"));
                mItems.add(new NewsCategoryItem("tag/how-to-wp/", "Новости", "   WP"));
                mItems.add(new NewsCategoryItem("articles/tag/interview/", "Новости", "   Интервью"));
                mItems.add(new NewsCategoryItem("software", "Новости", "Программы"));
                mItems.add(new NewsCategoryItem("software/tag/programs-for-android", "Новости/Программы", "   Android"));
                mItems.add(new NewsCategoryItem("software/tag/programs-for-ios", "Новости/Программы", "   iOS"));
                mItems.add(new NewsCategoryItem("software/tag/programs-for-windows-phone-7", "Новости/Программы", "   WP"));
                mItems.add(new NewsCategoryItem("software/tag/devstory/", "Новости/Программы", "   DevStory"));
                mItems.add(new NewsCategoryItem("games", "Новости", "Игры"));
                mItems.add(new NewsCategoryItem("games/tag/games-for-android", "Новости/Игры", "   Android"));
                mItems.add(new NewsCategoryItem("games/tag/games-for-ios", "Новости/Игры", "   iOS"));
                mItems.add(new NewsCategoryItem("games/tag/games-for-windows-phone-7", "Новости/Игры", "   WP"));
                mItems.add(new NewsCategoryItem("games/tag/devstory/", "Новости/Игры", "   DevStory"));
                mItems.add(new NewsCategoryItem("reviews", "Новости", "Обзоры"));
            }
            return mItems;
        }
        NavigationListAdapter(Context context) {
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
                holder.text1 = view.findViewById(R.id.text1);


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

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        selectItem(itemPosition);
        return true;
    }

    private void selectItem(int position) {
        position = Math.min(position, listAdapter.getItems().size() - 1);// на всякий случай, если изменится в будущем кол-во разделов
        String tag = listAdapter.getItems().get(position).Tag;
        Preferences.News.setLastSelectedSection(position);
        if(!tag.equals(mTag)) {
            mTag = tag;
            loadData(true);
        }

    }

    NavigationListAdapter listAdapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden&getSupportActionBar()!=null){
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
            getSupportActionBar().setSelectedNavigationItem(Preferences.News.getLastSelectedSection());
        }
    }

    @Override
    public String getListName() {
        return "news_" + mTag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Fragment newInstance(String tag) {
        NewsListFragment fragment = new NewsListFragment();
        fragment.setBrickInfo(new NewsBrickInfo());
        Bundle args = new Bundle();
        args.putString(TAG_EXTRA_KEY, tag);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onResume() {
        super.onResume();
        removeArrow();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeArrow();
        listAdapter = new NavigationListAdapter(getActivity());
        if (getArguments() != null) {
            if (getArguments().containsKey(TAG_EXTRA_KEY)) {
                mTag = getArguments().getString(TAG_EXTRA_KEY);
                mTag = listAdapter.getItems().get(Preferences.News.getLastSelectedSection()).Tag;
            }
            if (getArguments().containsKey(NEWS_LIST_URL_KEY)) {
                useCache = false;// не используем кеш для открытого по ссылке списка
                mUrl = getArguments().getString(NEWS_LIST_URL_KEY);
            }
        }

        onHiddenChanged(false);

    }

    Boolean useCache = true;

    @Override
    public void saveCache(){
        if (!useCache) return;
        super.saveCache();
    }

    @Override
    public void loadCache() {
        mCacheList = new ArrayList<>();
        if (!useCache) return;
        super.loadCache();
    }

    @Override
    protected void deliveryCache() {
        mData.clear();
        if (mCacheList != null) {
            mData.addAll(mCacheList);
            mCacheList.clear();
        }
        setCount();
        getAdapter().notifyDataSetChanged();
    }

    protected BaseAdapter createAdapter() {
        return new NewsListAdapter(getContext(), mData, ImageLoader.getInstance());
    }

    protected ListInfo mListInfo = new ListInfo();


    private String mUrl = "https://"+ HostHelper.getHost() +"/";
    private String mTag = "";

    @Override
    public boolean inBackground(boolean isRefresh) {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : mData.size());
        mLoadResultList = org.softeg.slartus.forpdaapi.NewsApi.getNews(Client.getInstance(), mUrl + mTag, mListInfo, App.getInstance().getPreferences());
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh) {
            mData.clear();
        }
        mData.addAll(mLoadResultList);
        /*for (News item : mLoadResultList) {
            mData.add(item);
        }*/

        mLoadResultList.clear();
    }

    @Override
    public void setCount() {
        int count = Math.max(mListInfo.getOutCount(), mData.size());
        getMListViewLoadMoreFooter().setCount(mData.size(), count);
        getMListViewLoadMoreFooter().setState(
                mData.size() == count ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
                        ListViewLoadMoreFooter.STATE_LOAD_MORE
        );
    }

    @Override
    public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View v, int position, long id) {
        if(!v.hasWindowFocus()) return;
        try {
            id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final News news = (News) o;
            if (TextUtils.isEmpty(news.getId())) return;
            if(!IntentActivity.tryShowSpecial(news.getUrl()))
                MainActivity.addTab(news.getTitle().toString(), news.getUrl(), NewsFragment.newInstance(news.getUrl()));
            getAdapter().notifyDataSetChanged();

        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        Object o = getAdapter().getItem((int) info.id);
        if (o == null)
            return;
        final News news = (News) o;
        if (TextUtils.isEmpty(news.getId())) return;


        List<MenuListDialog> list = new ArrayList<>();
        ExtUrl.addUrlMenu(getMHandler(), getContext(), list, news.getUrl(), news.getId(),news.getTitle().toString());
        ExtUrl.showContextDialog(getContext(), null, list);

    }

}
