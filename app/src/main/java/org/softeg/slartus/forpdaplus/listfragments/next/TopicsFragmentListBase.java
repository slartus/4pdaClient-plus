package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.classes.TopicsListData;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


/*
 * Created by slartus on 18.10.2014.
 */
public abstract class TopicsFragmentListBase extends Fragment
        implements IBrickFragment, LoaderManager.LoaderCallbacks<TopicsListData> {
    private static final String DATA_KEY = "DATA_KEY";
    private static final String SCROLL_POSITION_KEY = "SCROLL_POSITION_KEY";
    protected static final String START_NUM_KEY = "START_NUM_KEY";
    private RecyclerView mListView;

    private TopicsListData mData = createListData();
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private TopicsAdapter mAdapter;
    private String m_ForumId = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name);
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title);
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin);

            if (savedInstanceState.containsKey(DATA_KEY)) {
                mData = (TopicsListData) savedInstanceState.getSerializable(DATA_KEY);
            }

        }
        if (m_ForumId == null) {
            m_ForumId = Preferences.List.getStartForumId();
        }
        initAdapter();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.topics_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                loadData(true);
                return true;
        }
        return false;
    }

    protected TopicsListData createListData() {
        return new TopicsListData();
    }

    protected RecyclerView getListView() {
        return mListView;
    }

    protected TopicsListData getData() {
        return mData;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(mLayoutManager);
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION_KEY)) {
            mListView.scrollToPosition(savedInstanceState.getInt(SCROLL_POSITION_KEY));
        }
        setListViewAdapter();

        if (mData.getItems().size() == 0)
            reloadData();
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.topics_fragment, container, false);
        assert v != null;
        mListView = (RecyclerView) v.findViewById(android.R.id.list);

        registerForContextMenu(mListView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);

        mSwipeRefreshLayout.setColorSchemeResources(App.getInstance().getMainAccentColor());

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData();
            }
        });

    }

    public void reloadData() {
        loadData(true);
    }

    @Override
    public void startLoad() {
        reloadData();
    }

    @Override
    public void loadData(final boolean isRefresh) {
        Bundle args = new Bundle();
        args.putInt(START_NUM_KEY, isRefresh ? 0 : mData.getItems().size());

        setLoading(true);
        if (getLoaderManager().getLoader(getLoaderId()) != null)
            getLoaderManager().restartLoader(getLoaderId(), args, this);
        else
            getLoaderManager().initLoader(getLoaderId(), args, this);
    }

    protected abstract int getLoaderId();

    @Override
    public Loader<TopicsListData> onCreateLoader(int id, Bundle args) {
        Loader<TopicsListData> loader = null;
        if (id == getLoaderId()) {
            setLoading(true);
            loader = createLoader(id, args);

        }
        return loader;
    }

    protected abstract Loader<TopicsListData> createLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<TopicsListData> loader, TopicsListData data) {
        if (data != null && data.getError() != null) {
            AppLog.e(getActivity(), data.getError());
        } else if (data != null) {

            mData.getItems().addAll(data.getItems());

            notifyDataSetChanged();
            mListView.refreshDrawableState();
            mListView.scrollToPosition(0);
        }

        setLoading(false);
    }

    @Override
    public void onLoaderReset(Loader<TopicsListData> loader) {

    }


    protected void setLoading(final Boolean loading) {
        try {
            if (getActivity() == null) return;

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(loading);
                }
            });
        } catch (Throwable ignore) {

            android.util.Log.e("TAG", ignore.toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME_KEY, m_Name);
        outState.putString(TITLE_KEY, m_Title);
        outState.putBoolean(NEED_LOGIN_KEY, m_NeedLogin);
        outState.putSerializable(DATA_KEY, mData);
        try {
            if (mListView != null) {
                outState.putInt(SCROLL_POSITION_KEY,
                        ((LinearLayoutManager) mListView.getLayoutManager()).findFirstCompletelyVisibleItemPosition());
            }
        } catch (Throwable ex) {
            AppLog.e(ex);
        }

    }

    protected void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    protected void setListViewAdapter() {
        mListView.setAdapter(mAdapter);
    }

    protected void initAdapter() {
        mAdapter = new TopicsAdapter(mData.getItems(), new TopicsAdapter.OnClickListener() {
            @Override
            public void onItemClick(View v) {
                try {
                    int itemPosition = mListView.getChildPosition(v);
                    Topic topic = mData.getItems().get(itemPosition);
                    onTopicClick(topic);
                } catch (Throwable ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
    }

    protected abstract void onTopicClick(Topic topic);


    public static final String NAME_KEY = "NAME_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String NEED_LOGIN_KEY = "NEED_LOGIN_KEY";

    private String m_Title;
    private String m_Name;
    private Boolean m_NeedLogin = false;

    /**
     * Заголовок списка
     */
    public String getListTitle() {
        return m_Title;
    }

    /**
     * Уникальный идентификатор списка
     */
    public String getListName() {
        return m_Name;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public Fragment setBrickInfo(BrickInfo listTemplate) {
        m_Title = listTemplate.getTitle();
        m_Name = listTemplate.getName();
        m_NeedLogin = listTemplate.getNeedLogin();
        return this;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    private static class TopicsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public interface OnClickListener {
            void onItemClick(View v);
        }

        private List<Topic> mDataset;
        private OnClickListener mOnClickListener;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView author_textview;
            public TextView title_textview;
            public TextView desc_textview;
            public TextView date_textview;

            public ViewHolder(View v) {
                super(v);
                author_textview = (TextView) v.findViewById(R.id.author_textview);
                title_textview = (TextView) v.findViewById(R.id.title_textview);
                desc_textview = (TextView) v.findViewById(R.id.desc_textview);
                date_textview = (TextView) v.findViewById(R.id.date_textview);
            }
        }

        private Topic getItem(int position) {

            return mDataset.get(position);

        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public TopicsAdapter(List<Topic> myDataset,
                             OnClickListener onClickListener) {
            mDataset = myDataset;
            mOnClickListener = onClickListener;
        }


        public void notifyDataSetChangedWithLayout() {
            // mIsShowImages = Preferences.Forums.isShowImages();
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.topic_list_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onItemClick(v);
                }
            });

            return viewHolder;

        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            Topic topic = getItem(position);

            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.author_textview.setText(topic.getLastMessageAuthor());
            viewHolder.title_textview.setText(topic.getTitle());
            viewHolder.desc_textview.setText(topic.getDescription());
            viewHolder.date_textview.setText(topic.getLastMessageDateStr());

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }


    private void loadCache(){
        try {
            FileInputStream fis = getActivity().openFileInput(getListName());
            ObjectInputStream is = new ObjectInputStream(fis);
            TopicsListData cache = (TopicsListData) is.readObject();
            is.close();
            mData.getItems().addAll(cache.getItems());
            mData.setPagesCount(cache.getPagesCount());
            mData.setCurrentPage(cache.getCurrentPage());
            notifyDataSetChanged();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void updateCache() {
        FileOutputStream fos;
        try {
            fos = getActivity().openFileOutput(getListName(), Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mData);
            os.close();
        } catch (Throwable e) {

            e.printStackTrace();
        }
    }
}
