package org.softeg.slartus.forpdaplus.listfragments.news;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.News;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.NewsActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.listfragments.BaseTaskListFragment;
import org.softeg.slartus.forpdaplus.listfragments.adapters.NewsListAdapter;
import org.softeg.slartus.forpdaplus.listtemplates.NewsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.NewsListPreferencesActivity;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.util.ArrayList;

/*
 * Created by slinkin on 20.02.14.
 */
public class NewsListFragment extends BaseTaskListFragment {
    /*
     *  Ключ подкаталога новостей:  news, articles..
     */
    public static final String TAG_EXTRA_KEY = "TAG_EXTRA_KEY";
    public static final String NEWS_LIST_URL_KEY = "NEWS_LIST_URL_KEY";

    protected ArrayList<News> mData = new ArrayList<>();
    protected ArrayList<News> mLoadResultList;
    protected ArrayList<News> mCacheList = new ArrayList<>();
    ImageLoader imageLoader;

    public NewsListFragment() {
        super();
        initImageLoader(App.getContext());
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public String getListName() {
        return "news_" + mTag;
    }

    @Override
    public void onDestroy() {
        setHasOptionsMenu(false);
        super.onDestroy();
    }

    public static Fragment newInstance(String tag) {
        NewsListFragment fragment = new NewsListFragment();
        fragment.setBrickInfo(new NewsBrickInfo(tag));
        Bundle args = new Bundle();
        args.putString(TAG_EXTRA_KEY, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(TAG_EXTRA_KEY))
                mTag = getArguments().getString(TAG_EXTRA_KEY);
            if (getArguments().containsKey(NEWS_LIST_URL_KEY)) {
                useCache = false;// не используем кеш для открытого по ссылке списка
                mUrl = getArguments().getString(NEWS_LIST_URL_KEY);
            }
        }
    }

    Boolean useCache = true;

    @Override
    public void saveCache() throws Exception {
        if (!useCache) return;
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getWritableDatabase();
            BaseDao<News> baseDao = new BaseDao<>(App.getContext(), db, getListName(), News.class);
            baseDao.createTable(db);
            for (IListItem item : mData) {
                News news = (News) item;
                baseDao.insert(news);
            }

        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {
        mCacheList = new ArrayList<>();
        if (!useCache) return;
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getReadableDatabase();
            BaseDao<News> baseDao = new BaseDao<>(App.getContext(), db, getListName(), News.class);
            if (baseDao.isTableExists())
                mCacheList.addAll(baseDao.getAll());
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    protected void deliveryCache() {
        mData.clear();
        if (mCacheList != null) {
            mData.addAll(mCacheList);
            mCacheList.clear();
        }
        setCount();
        mAdapter.notifyDataSetChanged();
    }

    private static void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.no_image)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .handler(new Handler())
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 2 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }

    protected BaseAdapter createAdapter() {
        return new NewsListAdapter(getContext(), mData, imageLoader);
    }

    protected ListInfo mListInfo = new ListInfo();


    private String mUrl = "http://4pda.ru/";
    private String mTag = "";

    @Override
    public boolean inBackground(boolean isRefresh) throws Exception {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : mData.size());
        mLoadResultList = org.softeg.slartus.forpdaapi.NewsApi.getNews(Client.getInstance(), mUrl + mTag, mListInfo);
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh) {
            mData.clear();
        }
        for (News item : mLoadResultList) {
            mData.add(item);
        }

        mLoadResultList.clear();
    }

    @Override
    public void setCount() {
        int count = Math.max(mListInfo.getOutCount(), mData.size());
        mListViewLoadMoreFooter.setCount(mData.size(), count);
        mListViewLoadMoreFooter.setState(
                mData.size() == count ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
                        ListViewLoadMoreFooter.STATE_LOAD_MORE
        );
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        try {
            id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final News news = (News) o;
            if (TextUtils.isEmpty(news.getId())) return;


            NewsActivity.shownews(getContext(), news.getUrl());
            mAdapter.notifyDataSetChanged();

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

        ExtUrl.addUrlMenu(mHandler, getContext(), menu, news.getUrl(), news.getId(),
                news.getTitle().toString());

    }

    private static final int SETTINGS_REQUEST = 0;

    @Override
    protected void showSettings() {
        Intent settingsActivity = new Intent(
                getContext(), NewsListPreferencesActivity.class);
        startActivityForResult(settingsActivity, SETTINGS_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST) {

            getAdapter().notifyDataSetChanged();

        }
    }
}
