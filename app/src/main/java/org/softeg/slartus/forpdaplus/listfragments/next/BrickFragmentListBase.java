package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Context;
import android.os.Bundle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;

import io.paperdb.Paper;


/*
 * Created by slartus on 18.10.2014.
 */
public abstract class BrickFragmentListBase extends BrickFragmentBase
        implements LoaderManager.LoaderCallbacks<ListData>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String FIRST_VISIBLE_POSITION_KEY = "BrickFragmentListBase.FIRST_VISIBLE_POSITION_KEY";
    private static final String FIRST_VISIBLE_VIEW_KEY = "BrickFragmentListBase.FIRST_VISIBLE_VIEW_KEY";
    private ListView mListView;
    private TextView mEmptyTextView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private final ListData mData = createListData();
    private ListViewLoadMoreFooter mListViewLoadMoreFooter;

    protected abstract int getLoaderId();

    private BaseAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            loadCache();
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
            loadCache();
            loadData(1, true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        setListViewAdapter();

        onBrickFragmentListBaseActivityCreated();
    }

    protected abstract int getViewResourceId();

    @Override
    public View onCreateView(@NotNull android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(getViewResourceId(), container, false);
        assert view != null;
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(FIRST_VISIBLE_POSITION_KEY)) {
            mListView.setSelectionFromTop(savedInstanceState.getInt(FIRST_VISIBLE_POSITION_KEY), savedInstanceState.getInt(FIRST_VISIBLE_VIEW_KEY));
        }

        registerForContextMenu(mListView);
        return view;
    }

    protected void addLoadMoreFooter(Context context) {
        mListViewLoadMoreFooter = new ListViewLoadMoreFooter(context, getListView());
        mListViewLoadMoreFooter.setOnLoadMoreClickListener(view -> {
            mListViewLoadMoreFooter.setState(ListViewLoadMoreFooter.STATE_LOADING);
            loadData(getData().getCurrentPage() + 1, false);
        });
        refreshLoadMoreFooter();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = App.createSwipeRefreshLayout(view, this::reloadData);
    }

    @Override
    public void reloadData() {
        loadData(1, true);
    }

    @Override
    public void loadData(final boolean isRefresh) {
        loadData(1, isRefresh);
    }

    @Override
    public void startLoad() {
        reloadData();
    }


    protected Bundle getLoadArgs() {
        return null;
    }

    public static final String PAGE_KEY = "BrickFragmentListBase.URL_KEY";
    public static final String IS_REFRESH_KEY = "BrickFragmentListBase.IS_REFRESH_KEY";

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

    protected abstract AsyncTaskLoader<ListData> createLoader(int id, Bundle args);

    @Override
    public Loader<ListData> onCreateLoader(int id, Bundle args) {
        Loader<ListData> loader = null;
        if (id == getLoaderId()) {
            setLoading(true);
            loader = createLoader(id, args);

        }
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

        refreshLoadMoreFooter();

        setLoading(false);
        if (data != null)
            new Thread(this::saveCache).start();
    }

    private void refreshLoadMoreFooter() {
        if (getData() != null && mListViewLoadMoreFooter != null) {
            mListViewLoadMoreFooter.setCount(mData.getItems().size(), mData.getItems().size());
            mListViewLoadMoreFooter.setState(
                    getData().getPagesCount() <= getData().getCurrentPage() ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
                            ListViewLoadMoreFooter.STATE_LOAD_MORE
            );
        }
    }

    @Override
    public void onLoaderReset(Loader<ListData> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            if (mListView != null) {
                outState.putInt(FIRST_VISIBLE_POSITION_KEY, mListView.getFirstVisiblePosition());
                View v = mListView.getChildAt(0);
                outState.putInt(FIRST_VISIBLE_VIEW_KEY, (v == null) ? 0 : v.getTop());
            }

            saveCache();

        } catch (Throwable ex) {
            AppLog.e(ex);
        }

    }

    private void saveCache() {
        if (mData != null)
            Paper.book().write(getListName(), mData);
    }

    protected void loadCache() {
        mData.getItems().clear();
        ListData data = Paper.book().read(getListName(), new ListData());
        mData.setCurrentPage(data.getCurrentPage());
        mData.setPagesCount(data.getPagesCount());
        mData.getItems().addAll(data.getItems());
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


}
