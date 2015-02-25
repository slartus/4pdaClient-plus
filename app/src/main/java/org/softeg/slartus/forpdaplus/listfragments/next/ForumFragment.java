package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ForumsTable;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by slartus on 24.02.2015.
 */
public class ForumFragment extends Fragment implements
        IBrickFragment, LoaderManager.LoaderCallbacks<ForumFragment.ForumBranch> {
    private static final String DATA_KEY = "BrickFragmentListBase.DATA_KEY";
    private static final String SCROLL_POSITION_KEY = "SCROLL_POSITION_KEY";
    private static final String FORUM_ID_KEY = "FORUM_ID_KEY";
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private TextView mEmptyTextView;
    private ForumFragment.ForumBranch mData = createListData();


    private RecyclerView.Adapter mAdapter;
    private String m_ForumId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name);
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title);
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin);

            if (savedInstanceState.containsKey(DATA_KEY)) {
                mData = (ForumFragment.ForumBranch) savedInstanceState.getSerializable(DATA_KEY);
            }

        }
        initAdapter();
    }


    protected ForumFragment.ForumBranch createListData() {
        return new ForumFragment.ForumBranch();
    }

    protected RecyclerView getListView() {
        return mListView;
    }

    protected ForumFragment.ForumBranch getData() {
        return mData;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mListView.setLayoutManager(mLayoutManager);
        setListViewAdapter();
        reloadData();
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.forum_fragment, container, false);
        assert v != null;
        mListView = (RecyclerView) v.findViewById(android.R.id.list);


        mEmptyTextView = (TextView) v.findViewById(android.R.id.empty);

        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION_KEY)) {
            mListView.scrollToPosition(savedInstanceState.getInt(SCROLL_POSITION_KEY));
        }


        return v;
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
        args.putString(FORUM_ID_KEY, m_ForumId);
        setLoading(true);
        if (getLoaderManager().getLoader(getLoaderId()) != null)
            getLoaderManager().restartLoader(getLoaderId(), args, this);
        else
            getLoaderManager().initLoader(getLoaderId(), args, this);
    }

    public void loadForum(String forumId) {
        Bundle args = new Bundle();
        args.putString(FORUM_ID_KEY, forumId);
        m_ForumId = forumId;
        setLoading(true);
        if (getLoaderManager().getLoader(getLoaderId()) != null)
            getLoaderManager().restartLoader(getLoaderId(), args, this);
        else
            getLoaderManager().initLoader(getLoaderId(), args, this);
    }

    private int getLoaderId() {
        return ForumLoader.ID;
    }


    @Override
    public Loader<ForumFragment.ForumBranch> onCreateLoader(int id, Bundle args) {
        Loader<ForumBranch> loader = null;
        if (id == getLoaderId()) {
            setLoading(true);
            loader = createLoader(id, args);

        }
        return loader;
    }

    private Loader<ForumBranch> createLoader(int id, Bundle args) {
        ForumLoader loader = null;
        if (id == ForumLoader.ID) {
            setLoading(true);
            loader = new ForumLoader(getActivity(), args);

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ForumFragment.ForumBranch> loader, ForumFragment.ForumBranch data) {
        if (data != null && data.getError() != null) {
            AppLog.e(getActivity(), data.getError());
        } else if (data != null) {
            if (data.getItems().size() == 0)
                ForumTopicsListFragment.showForumTopicsList(getActivity(), loader.);
            mData.getItems().clear();
            mData.getItems().addAll(data.getItems());
            mData.getCrumbs().clear();
            mData.getCrumbs().addAll(data.getCrumbs());

            notifyDataSetChanged();
            mListView.refreshDrawableState();
        }

        setLoading(false);
    }

    @Override
    public void onLoaderReset(Loader<ForumFragment.ForumBranch> loader) {

    }

    protected void setEmptyText(String s) {
        mEmptyTextView.setText(s);
    }

    protected void setLoading(Boolean loading) {
        try {
            if (getActivity() == null) return;


            if (loading) {
                setEmptyText("Загрузка..");
            } else {
                setEmptyText("Нет данных");
            }
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
        outState.putString(FORUM_ID_KEY, m_ForumId);
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
        mAdapter = new ForumsAdapter(mData.getCrumbs(), mData.getItems(), new ForumsAdapter.OnClickListener() {
            @Override
            public void onItemClick(View v) {
                int itemPosition = mListView.getChildPosition(v);
                Forum forum = mData.getItems().get(itemPosition - mData.getCrumbs().size());
                loadForum(forum.getId());
            }

            @Override
            public void onHeaderClick(View v) {
                int itemPosition = mListView.getChildPosition(v);
                Forum forum = mData.getCrumbs().get(itemPosition);
                loadForum(forum.getId());
            }
        });
    }


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

    public static class ForumBranch extends Forum {
        private Throwable error;

        public ForumBranch() {
            super(null, null);
        }

        public ForumBranch(String id, String title) {
            super(id, title);
        }

        private List<Forum> mCrumbs = null;

        public List<Forum> getCrumbs() {
            if (mCrumbs == null)
                mCrumbs = new ArrayList<Forum>();
            return mCrumbs;
        }

        private List<Forum> mItems = null;

        public List<Forum> getItems() {
            if (mItems == null)
                mItems = new ArrayList<Forum>();
            return mItems;
        }

        public Throwable getError() {
            return error;
        }

        public void setError(Throwable error) {
            this.error = error;
        }
    }

    private static class ForumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public interface OnClickListener {
            void onItemClick(View v);

            void onHeaderClick(View v);
        }

        private List<Forum> mDataset;
        private OnClickListener mOnClickListener;
        private List<Forum> mHeaderset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mText1;
            public TextView mText2;

            public ViewHolder(View v) {
                super(v);
                mText1 = (TextView) v.findViewById(android.R.id.text1);
                mText2 = (TextView) v.findViewById(android.R.id.text2);
            }
        }

        public static class HeaderViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public Button mButton;


            public HeaderViewHolder(View v) {
                super(v);
                mButton = (Button) v.findViewById(R.id.header_button);
            }
        }


        private Forum getItem(int position) {
            switch (getItemViewType(position)) {
                case HEADER_VIEW_TYPE:
                    return mHeaderset.get(position);
                case DATA_VIEW_TYPE:
                    return mDataset.get(position - mHeaderset.size());
            }
            return null;
        }

        private final int HEADER_VIEW_TYPE = 0;
        private final int DATA_VIEW_TYPE = 1;

        @Override
        public int getItemViewType(int position) {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            return position < mHeaderset.size() ? HEADER_VIEW_TYPE : DATA_VIEW_TYPE;
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ForumsAdapter(List<Forum> headerDataset, List<Forum> myDataset,
                             OnClickListener onClickListener) {
            mHeaderset = headerDataset;
            mDataset = myDataset;
            mOnClickListener = onClickListener;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            switch (viewType) {
                case DATA_VIEW_TYPE:
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(android.R.layout.simple_list_item_2, parent, false);

                    ViewHolder viewHolder = new ViewHolder(v);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onItemClick(v);
                        }
                    });
                    return viewHolder;
                case HEADER_VIEW_TYPE:
                    final View headerV = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.forum_header_item, parent, false);


                    HeaderViewHolder headerViewHolder = new HeaderViewHolder(headerV);
                    headerViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onHeaderClick(headerV);
                        }
                    });
                    return headerViewHolder;
            }
            // create a new view

            return null;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            Forum forum = getItem(position);
            switch (viewType) {
                case DATA_VIEW_TYPE:
                    ViewHolder viewHolder = (ViewHolder) holder;
                    viewHolder.mText1.setText(forum.getTitle());
                    viewHolder.mText2.setText(forum.getDescription());
                    break;
                case HEADER_VIEW_TYPE:
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.mButton.setText(forum.getTitle());

                    break;
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mHeaderset.size() + mDataset.size();
        }
    }

    private static class ForumLoader extends AsyncTaskLoader<ForumFragment.ForumBranch> {
        public static final int ID = App.getInstance().getUniqueIntValue();
        ForumFragment.ForumBranch mApps;
        private Bundle args;

        public ForumLoader(Context context, Bundle args) {
            super(context);

            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }


        @Override
        public ForumFragment.ForumBranch loadInBackground() {
            try {
                return ForumsTable.getForums(args.getString(FORUM_ID_KEY));
            } catch (Throwable e) {
                ForumFragment.ForumBranch forumPage = new ForumFragment.ForumBranch();
                forumPage.setError(e);
                return forumPage;
            }

        }

        @Override
        public void deliverResult(ForumFragment.ForumBranch apps) {

            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
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
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                mApps = null;
            }
        }

    }
}
