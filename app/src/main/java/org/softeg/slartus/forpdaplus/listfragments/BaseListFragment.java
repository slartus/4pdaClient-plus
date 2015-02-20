package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 10.04.2014.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.listfragments.adapters.ListAdapter;
import org.softeg.slartus.forpdaplus.prefs.ListPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class BaseListFragment extends BaseBrickFragment implements
        AdapterView.OnItemClickListener {
    protected ArrayList<IListItem> mData=new ArrayList<>();
    private static final String TAG = "BaseListFragment";

    public static final String FIRST_VISIBLE_ROW_KEY = "FIRST_VISIBLE_ROW_KEY";
    public static final String TOP_KEY = "TOP_KEY";

    protected Handler mHandler = new Handler();
    private int m_FirstVisibleRow = 0;
    private int m_Top = 0;

    public BaseListFragment() {
super();
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            m_FirstVisibleRow = savedInstanceState.getInt(FIRST_VISIBLE_ROW_KEY, m_FirstVisibleRow);
            m_Top = savedInstanceState.getInt(TOP_KEY, m_Top);
        }
    }

    protected View getListViewHeader(){
        return null;
    }

    private ListView mListView;
    private TextView mEmptyTextView;

    protected int getViewId(){
        return R.layout.list_fragment;
    }
    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        View v = inflater.inflate(getViewId(), container, false);
        assert v != null;
        mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        View header=getListViewHeader();
        if(header!=null)
            mListView.addHeaderView(header);
        mEmptyTextView = (TextView) v.findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyTextView);
        return v;
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);

        outState.putInt(FIRST_VISIBLE_ROW_KEY, m_FirstVisibleRow);
        outState.putInt(TOP_KEY, m_Top);
        super.onSaveInstanceState(outState);
    }

    protected void showSettings(){
        Intent settingsActivity = new Intent(
                getContext(), ListPreferencesActivity.class);
        getContext().startActivity(settingsActivity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);

       MenuItem item = menu.add("Настройки списка")
                .setIcon(R.drawable.ic_menu_preferences)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showSettings();
                        return true;
                    }
                });

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    public void setCount() {
        int count = mAdapter.getCount();
        mListViewLoadMoreFooter.setCount(count, count);
        mListViewLoadMoreFooter.setState(ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED);
    }


    protected BaseAdapter mAdapter;
    protected ListViewLoadMoreFooter mListViewLoadMoreFooter;

    public Context getContext() {
        return getActivity();
    }

    protected uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout mPullToRefreshLayout;

    protected void saveListViewScrollPosition() {
        m_FirstVisibleRow = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        m_Top = (v == null) ? 0 : v.getTop();
    }

    protected void restoreListViewScrollPosition() {
        getListView().setSelectionFromTop(m_FirstVisibleRow, m_Top);
    }

    protected ListView getListView() {
        return mListView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListViewLoadMoreFooter = new ListViewLoadMoreFooter(view.getContext(), getListView());
        mListViewLoadMoreFooter.setOnLoadMoreClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListViewLoadMoreFooter.setState(ListViewLoadMoreFooter.STATE_LOADING);
                loadData(false);
            }
        });


        mPullToRefreshLayout = createPullToRefreshLayout(view);
    }

    protected PullToRefreshLayout createPullToRefreshLayout(View view) {
        // We need to create a PullToRefreshLayout manually
        PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

        // We can now setup the PullToRefreshLayout
        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create().scrollDistance(0.3f).refreshOnUp(true).build())
                        // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .allChildrenArePullable()

                        // We can now complete the setup as desired
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        loadData(true);
                    }
                })
                .setup(pullToRefreshLayout);
        return pullToRefreshLayout;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        registerForContextMenu(getListView());
        setListShown(false);
        mAdapter = createAdapter();


        setListAdapter(mAdapter);
    }

    private void setListAdapter(BaseAdapter mAdapter) {
        getListView().setAdapter(mAdapter);
    }

    protected void setListShown(boolean b) {
        //mListView.setVisibility(b?);
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    protected BaseAdapter createAdapter() {
        return new ListAdapter(getActivity(), mData);
    }

    protected void setLoading(Boolean loading) {
        try {
            if (getActivity() == null) return;

            mPullToRefreshLayout.setRefreshing(loading);
            if (loading) {
                setEmptyText("Загрузка..");
            } else {
                setEmptyText("Нет данных");
            }
        } catch (Throwable ignore) {
            android.util.Log.e("TAG", ignore.toString());
        }
    }

    protected void setEmptyText(String s) {
        mEmptyTextView.setText(s);
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!Preferences.Lists.getScrollByButtons())
            return false;

        int action = event.getAction();

        ListView scrollView = getListView();
        int visibleItemsCount = scrollView.getLastVisiblePosition() - scrollView.getFirstVisiblePosition();

        int keyCode = event.getKeyCode();
        if(Preferences.System.isScrollUpButton(keyCode)){
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(Math.max(scrollView.getFirstVisiblePosition() - visibleItemsCount, 0));
            return true;// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        }
        if(Preferences.System.isScrollDownButton(keyCode)){
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(Math.min(scrollView.getLastVisiblePosition(), scrollView.getCount() - 1));
            return true;// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        }

        return false;
    }

}
