package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.os.Bundle;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.util.ArrayList;

import io.paperdb.Paper;

/*
 * Created by slinkin on 17.06.2015.
 */
public abstract class BaseLoaderListFragment extends BaseBrickFragment
        implements LoaderManager.LoaderCallbacks<ListData>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String FIRST_VISIBLE_POSITION_KEY = "BrickFragmentListBase.FIRST_VISIBLE_POSITION_KEY";
    private static final String FIRST_VISIBLE_VIEW_KEY = "BrickFragmentListBase.FIRST_VISIBLE_VIEW_KEY";
    private ListView mListView;
    private TextView mEmptyTextView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private final ListData mData = createListData();

    protected int getLoaderId() {
        return ItemsLoader.ID;
    }

    /**
     * Использовать ли кеш списка
     */
    protected Boolean useCache() {
        return true;
    }

    private BaseAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            loadCache();
        }
        initAdapter();
    }


    protected ListData createListData() {
        return new ListData();
    }

    protected ListView getListView() {
        return mListView;
    }

    protected ListData getData() {
        return mData;
    }

    protected abstract BaseAdapter createAdapter();

    protected BaseAdapter getAdapter() {
        return mAdapter;
    }

    protected void onBrickFragmentListBaseActivityCreated() {
        if (mData.getItems().size() == 0) {
            loadData(1, true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        setListViewAdapter();

        if (mData.getItems().size() == 0 && useCache()) {
            startLoadCache();
        } else {
            onBrickFragmentListBaseActivityCreated();
        }
    }

    private void startLoadCache() {
        setLoading(true);
        if (getLoaderManager().getLoader(CacheLoader.ID) != null)
            getLoaderManager().restartLoader(CacheLoader.ID, null, this);
        else
            getLoaderManager().initLoader(CacheLoader.ID, null, this);
    }


    protected abstract int getViewResourceId();

    @Override
    public android.view.View onCreateView(@NotNull android.view.LayoutInflater inflater, android.view.ViewGroup container,
                                          android.os.Bundle savedInstanceState) {
        view = inflater.inflate(getViewResourceId(), container, false);
        assert view != null;
        mListView = view.findViewById(android.R.id.list);
        mEmptyTextView = view.findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(FIRST_VISIBLE_POSITION_KEY)) {
            mListView.setSelectionFromTop(savedInstanceState.getInt(FIRST_VISIBLE_POSITION_KEY), savedInstanceState.getInt(FIRST_VISIBLE_VIEW_KEY));
        }


        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = createSwipeRefreshLayout(view);
    }

    private SwipeRefreshLayout createSwipeRefreshLayout(View view) {
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.ptr_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> loadData(true));
        swipeRefreshLayout.setColorSchemeResources(AppTheme.getMainAccentColor());
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppTheme.getSwipeRefreshBackground());
        return swipeRefreshLayout;
    }

    public void reloadData() {
        loadData(1, true);
    }

    protected Bundle getLoadArgs() {
        return null;
    }

    public static final String PAGE_KEY = "BrickFragmentListBase.URL_KEY";
    private static final String IS_REFRESH_KEY = "BrickFragmentListBase.IS_REFRESH_KEY";

    public void loadData(int page, final boolean isRefresh) {
        Bundle args = new Bundle();
        args.putInt(PAGE_KEY, page);
        args.putBoolean(IS_REFRESH_KEY, isRefresh);
        Bundle loadArgs = getLoadArgs();
        if (loadArgs != null)
            args.putAll(loadArgs);
        setLoading(true);
        if (getLoaderManager().getLoader(getLoaderId()) != null)
            getLoaderManager().restartLoader(getLoaderId(), args, this);
        else
            getLoaderManager().initLoader(getLoaderId(), args, this);
    }

    protected AsyncTaskLoader<ListData> createLoader(int loaderId, Bundle args) {
        if (loaderId == ItemsLoader.ID)
            return new ItemsLoader(getActivity(), args, BaseLoaderListFragment.this::loadData);
        else if (loaderId == CacheLoader.ID)
            return new CacheLoader(getActivity(), (loaderId12, args12) -> {
                ListData data = new ListData();
                data.getItems().addAll(BaseLoaderListFragment.this.loadCache());
                return data;
            });
        return null;
    }

    protected abstract ListData loadData(int loaderId, Bundle args) throws Throwable;

    @Override
    public Loader<ListData> onCreateLoader(int id, Bundle args) {
        Loader<ListData> loader;

        setLoading(true);
        loader = createLoader(id, args);
        return loader;
    }


    @Override
    public void onLoadFinished(Loader<ListData> loader, ListData data) {
        if (data != null && data.getEx() != null) {
            AppLog.e(getActivity(), data.getEx());
        } else if (data != null) {
            if (data.getCurrentPage() == 1)
                mData.getItems().clear();
            mData.getItems().addAll(data.getItems());
            mData.setCurrentPage(data.getCurrentPage());
            mData.setPagesCount(data.getPagesCount());
            notifyDataSetChanged();
            mListView.refreshDrawableState();
        }

        if (loader.getId() == CacheLoader.ID) {
            loadData(true);
        } else {
            setLoading(false);
            if (data != null && useCache())
                new Thread(this::saveCache).start();
        }

    }

    @Override
    public void onLoaderReset(Loader<ListData> loader) {
        setLoading(false);
    }

    protected void setEmptyText(String s) {
        mEmptyTextView.setText(s);
    }

    protected void setLoading(final Boolean loading) {
        try {
            if (getActivity() == null) return;
            //mSwipeRefreshLayout.setRefreshing(loading);
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
            if (loading) {
                setEmptyText(App.getContext().getString(R.string.loading));
            } else {
                setEmptyText(App.getContext().getString(R.string.no_data));
            }
        } catch (Throwable ex) {
            android.util.Log.e("TAG", ex.toString());
        }
    }


    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mListView != null) {
            outState.putInt(FIRST_VISIBLE_POSITION_KEY, mListView.getFirstVisiblePosition());
            View v = mListView.getChildAt(0);
            outState.putInt(FIRST_VISIBLE_VIEW_KEY, (v == null) ? 0 : v.getTop());
        }

        saveCache();
    }

    protected void notifyDataSetChanged() {
        getAdapter().notifyDataSetChanged();
    }

    protected void setListViewAdapter() {
        mListView.setAdapter(mAdapter);
    }

    protected void initAdapter() {
        mAdapter = createAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    @Override
    public void loadData(boolean isRefresh) {
        loadData(1, isRefresh);
    }

    @Override
    public void startLoad() {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    public interface LoadDataListener {
        ListData loadData(int loaderId, Bundle args) throws Throwable;

    }

    private void saveCache() {
        try {
            if (!TextUtils.isEmpty(getListName()))
                Paper.book().write(getListName(), mData.getItems());
        } catch (Throwable ex) {
            AppLog.e(ex);
        }
    }

    private ArrayList<IListItem> loadCache() {
        if (!TextUtils.isEmpty(getListName()))
            return Paper.book().read(getListName(), new ArrayList<>());
        else
            return new ArrayList<>();
    }

    private static class CacheLoader extends AsyncTaskLoader<ListData> {
        static final int ID = App.getInstance().getUniqueIntValue();
        ListData mApps;

        private final LoadDataListener mLoadDataListener;

        CacheLoader(Context context, LoadDataListener loadDataListener) {
            super(context);
            mLoadDataListener = loadDataListener;
        }

        @Override
        public ListData loadInBackground() {
            try {
                ListData listData = new ListData();
                listData.getItems().addAll(mLoadDataListener.loadData(ID, null).getItems());
                return listData;
            } catch (Throwable e) {
                ListData listData = new ListData();
                listData.setEx(e);
                return listData;
            }
        }

        @Override
        public void deliverResult(ListData apps) {
            if (isReset()) {
                if (apps != null) {
                    onReleaseResources();
                }
            }
            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

            if (apps != null) {
                onReleaseResources();
            }
        }

        @Override
        protected void onStartLoading() {

            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ListData apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources();
                mApps = null;
            }


        }

        void onReleaseResources() {

        }
    }

    private static class ItemsLoader extends AsyncTaskLoader<ListData> {
        static final int ID = App.getInstance().getUniqueIntValue();
        ListData mApps;

        private final Bundle args;
        private final LoadDataListener mLoadDataListener;

        ItemsLoader(Context context, Bundle args, LoadDataListener loadDataListener) {
            super(context);
            this.args = args;
            mLoadDataListener = loadDataListener;
        }

        @Override
        public ListData loadInBackground() {
            try {
                return mLoadDataListener.loadData(ID, args);
            } catch (Throwable e) {
                ListData listData = new ListData();
                listData.setEx(e);
                return listData;
            }
        }

        @Override
        public void deliverResult(ListData apps) {
            if (isReset()) {
                if (apps != null) {
                    onReleaseResources();
                }
            }
            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

            if (apps != null) {
                onReleaseResources();
            }
        }

        @Override
        protected void onStartLoading() {

            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ListData apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources();
                mApps = null;
            }


        }

        void onReleaseResources() {

        }
    }
}
