package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaapi.ForumsApi;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.classes.ForumsData;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ForumsTable;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by slartus on 24.02.2015.
 */
public class ForumFragment extends GeneralFragment implements LoaderManager.LoaderCallbacks<ForumFragment.ForumBranch> {
    private static final String DATA_KEY = "BrickFragmentListBase.DATA_KEY";
    private static final String SCROLL_POSITION_KEY = "SCROLL_POSITION_KEY";
    public static final String FORUM_ID_KEY = "FORUM_ID_KEY";
    public static final String FORUM_TITLE_KEY = "FORUM_TITLE_KEY";
    private RecyclerView mListView;
    private TextView mEmptyTextView;
    private ForumFragment.ForumBranch mData = createListData();
    private SearchSettings mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings();


    private ForumsAdapter mAdapter;
    private String m_ForumId = null;

    boolean lastImageDownload = MainActivity.getPreferences().getBoolean("forum.list.show_images", true);


    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeArrow();
        if (getArguments() != null)
            m_ForumId = getArguments().getString(FORUM_ID_KEY, null);
        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name);
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title);
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin);

            if (savedInstanceState.containsKey(DATA_KEY)) {
                mData = (ForumFragment.ForumBranch) savedInstanceState.getSerializable(DATA_KEY);
            }

        }
        if (m_ForumId == null) {
            m_ForumId = Preferences.List.getStartForumId();
        }
        setTitle(m_Title);
        initAdapter();
    }
    private Menu menu;
    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Отметить этот форум прочитанным")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        markAsRead();
                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add("Задать этот форум стартовым")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Forum f = mData.getCrumbs().get(mData.getCrumbs().size() - 1);
                        Preferences.List.setStartForum(f.getId(),
                                f.getTitle());
                        Toast.makeText(getActivity(), "Форум задан стартовым", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add("Обновить структуру форума")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        new MaterialDialog.Builder(getActivity())
                                .title("Внимание!")
                                .content("Обновление структуры форума может занять продолжительное время " +
                                        "и использует большой объем интернет-траффика")
                                .positiveText("Обновить")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        new UpdateForumStructTask(getActivity()).execute();
                                    }
                                })
                                .negativeText("Отмена").show();
                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        this.menu = menu;
    }
    @Override
    public void onPause() {
        super.onPause();
        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        removeArrow();
        MainActivity.searchSettings = mSearchSetting;

        if(lastImageDownload==MainActivity.getPreferences().getBoolean("forum.list.show_images", true)){
            mAdapter.notifyDataSetChangedWithLayout();
            if(mListView!=null) mListView.refreshDrawableState();
            lastImageDownload = MainActivity.getPreferences().getBoolean("forum.list.show_images", true);
        }
    }
    private void markAsRead() {
        if (!Client.getInstance().getLogined()) {
            Toast.makeText(getActivity(), "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialDialog.Builder(getActivity())
                .title("Подтвердите действие")
                .content("Отметить этот форум прочитанным?")
                .positiveText("Да")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Toast.makeText(getActivity(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            public void run() {
                                Throwable ex = null;
                                try {
                                    Forum f = mData.getCrumbs().get(mData.getCrumbs().size() - 1);
                                    ForumsApi.markForumAsRead(Client.getInstance(), f.getId() == null ? "-1" : f.getId());

                                } catch (Throwable e) {
                                    ex = e;
                                }

                                final Throwable finalEx = ex;

                                mHandler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                                                AppLog.e(getActivity(), finalEx);
                                            } else {
                                                Toast.makeText(getActivity(), "Форум отмечен прочитанным", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            AppLog.e(getActivity(), ex);
                                        }

                                    }
                                });
                            }
                        }).start();
                    }
                })
                .negativeText("Отмена")
                .show();
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
        view = inflater.inflate(R.layout.forum_fragment, container, false);
        assert view != null;
        mListView = (RecyclerView) findViewById(android.R.id.list);

        registerForContextMenu(mListView);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);


        return view;
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
        loadForum(m_ForumId);
    }

    public void loadForum(String forumId) {
        Bundle args = new Bundle();
        args.putString(FORUM_ID_KEY, forumId);

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
            if (data.getItems().size() == 0 && data.getItems().size() > 1) {
                Forum forum = data.getItems().get(data.getItems().size() - 1);
                ForumTopicsListFragment.showForumTopicsList(getActivity(), forum.getId(), forum.getTitle());
                return;
            }
            mData.getItems().clear();
            mData.getItems().addAll(data.getItems());
            mData.getCrumbs().clear();
            mData.getCrumbs().addAll(data.getCrumbs());

            notifyDataSetChanged();
            mListView.refreshDrawableState();
            mListView.scrollToPosition(0);
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


//            if (loading) {
//                setEmptyText("Загрузка..");
//            } else {
//                setEmptyText("Нет данных");
//            }
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
        mAdapter = new ForumsAdapter(mData.getCrumbs(), mData.getItems(), new ForumsAdapter.OnClickListener() {
            @Override
            public void onItemClick(View v) {
                int itemPosition = mListView.getChildPosition(v);
                Forum forum = mData.getItems().get(itemPosition - mData.getCrumbs().size());
                if (forum.isHasForums()) {
                    loadForum(forum.getId());
                    SearchSettings searchSettings = new SearchSettings();
                    searchSettings.setSource("all");
                    searchSettings.getForumsIds().add(forum.getId() + "");
                    mSearchSetting = searchSettings;
                    MainActivity.searchSettings = mSearchSetting;
                } else {
                    ForumTopicsListFragment.showForumTopicsList(getActivity(), forum.getId(), forum.getTitle());
                }
            }

            @Override
            public void onHeaderClick(View v) {
                int itemPosition = mListView.getChildPosition(v);
                Forum forum = mData.getCrumbs().get(itemPosition);
                loadForum(forum.getId());
            }

            @Override
            public void onHeaderTopicsClick(View v) {
                int itemPosition = mListView.getChildPosition(v);
                Forum forum = mData.getCrumbs().get(itemPosition);
                ForumTopicsListFragment.showForumTopicsList(getActivity(), forum.getId(), forum.getTitle());
            }


        }, new ForumsAdapter.OnLongClickListener() {
            private void show(String id){
                ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка", "http://4pda.ru/forum/index.php?showforum="+id);
            }
            @Override
            public void onItemClick(View v) {
                show(mData.getItems().get(mListView.getChildPosition(v) - mData.getCrumbs().size()).getId());
            }

            @Override
            public void onHeaderClick(View v) {
                show(mData.getCrumbs().get(mListView.getChildPosition(v)).getId());
            }

            @Override
            public void onHeaderTopicsClick(View v) {
                show(mData.getCrumbs().get(mListView.getChildPosition(v)).getId());
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
        if (mData.getCrumbs().size() > 1) {
            loadForum(mData.getCrumbs().get(mData.getCrumbs().size() - 2).getId());
            return true;
        }
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

    public static void showActivity(Context context, String forumId, String topicId) {
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(forumId))
            args.putString(ForumFragment.FORUM_ID_KEY, forumId);
        if (!TextUtils.isEmpty(topicId))
            args.putString(TopicsListFragment.KEY_TOPIC_ID, topicId);
        Log.e("kek", forumId+" : "+topicId);
        MainActivity.showListFragment(forumId+topicId, new ForumBrickInfo().getName(), args);
    }


    public static class ForumBranch implements Serializable {
        private Throwable error;

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

            void onHeaderTopicsClick(View v);

        }
        public interface OnLongClickListener {
            void onItemClick(View v);

            void onHeaderClick(View v);

            void onHeaderTopicsClick(View v);

        }

        private List<Forum> mDataset;
        private OnClickListener mOnClickListener;
        private OnLongClickListener mOnLongClickListener;
        private List<Forum> mHeaderset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mText1;
            public TextView mText2;
            public ImageView mImageView;

            public ViewHolder(View v) {
                super(v);
                mText1 = (TextView) v.findViewById(android.R.id.text1);
                mText2 = (TextView) v.findViewById(android.R.id.text2);
                mImageView = (ImageView) v.findViewById(R.id.imageView3);

            }
        }

        public static class HeaderViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mText;


            public HeaderViewHolder(View v) {
                super(v);
                mText = (TextView) v.findViewById(R.id.textView3);
            }
        }


        private Forum getItem(int position) {
            switch (getItemViewType(position)) {
                case HEADER_CURRENT_NOTOPICS_VIEW_TYPE:
                case HEADER_CURRENT_VIEW_TYPE:
                case HEADER_VIEW_TYPE:
                    return mHeaderset.get(position);
                case DATA_VIEW_TYPE:
                    return mDataset.get(position - mHeaderset.size());
            }
            return null;
        }

        private final int HEADER_VIEW_TYPE = 0;
        private final int HEADER_CURRENT_VIEW_TYPE = 1;
        private final int HEADER_CURRENT_NOTOPICS_VIEW_TYPE = 2;
        private final int DATA_VIEW_TYPE = 3;

        @Override
        public int getItemViewType(int position) {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            if (position < mHeaderset.size()) {
                if (position == mHeaderset.size() - 1) {
                    if (!mHeaderset.get(position).isHasTopics())
                        return HEADER_CURRENT_NOTOPICS_VIEW_TYPE;
                    return HEADER_CURRENT_VIEW_TYPE;
                }
                return HEADER_VIEW_TYPE;
            }
            return DATA_VIEW_TYPE;
        }

        private Boolean mIsShowImages = true;

        // Provide a suitable constructor (depends on the kind of dataset)
        public ForumsAdapter(List<Forum> headerDataset, List<Forum> myDataset,
                             OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
            mHeaderset = headerDataset;
            mDataset = myDataset;
            mOnClickListener = onClickListener;
            mOnLongClickListener = onLongClickListener;
            mIsShowImages = Preferences.Forums.isShowImages();
        }


        public void notifyDataSetChangedWithLayout() {
            // mIsShowImages = Preferences.Forums.isShowImages();
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            switch (viewType) {
                case DATA_VIEW_TYPE:
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.forum_item, parent, false);

                    ViewHolder viewHolder = new ViewHolder(v);
                    if (!mIsShowImages)
                        viewHolder.mImageView.setVisibility(View.GONE);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onItemClick(v);
                        }
                    });
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mOnLongClickListener.onItemClick(v);
                            return true;
                        }
                    });

                    return viewHolder;
                case HEADER_VIEW_TYPE:
                    final View headerV = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.forum_header_item, parent, false);


                    HeaderViewHolder headerViewHolder = new HeaderViewHolder(headerV);
                    headerV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onHeaderClick(v);
                        }
                    });
                    headerV.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mOnLongClickListener.onHeaderClick(v);
                            return true;
                        }
                    });
                    return headerViewHolder;
                case HEADER_CURRENT_VIEW_TYPE:
                    final View headerCV = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.forum_header_current_item, parent, false);


                    HeaderViewHolder headerCViewHolder = new HeaderViewHolder(headerCV);
                    headerCV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onHeaderTopicsClick(v);
                        }
                    });
                    headerCV.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mOnLongClickListener.onHeaderTopicsClick(v);
                            return true;
                        }
                    });

                    return headerCViewHolder;
                case HEADER_CURRENT_NOTOPICS_VIEW_TYPE:
                    final View headerCNV = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.forum_header_notopics_item, parent, false);


                    HeaderViewHolder headerCNViewHolder = new HeaderViewHolder(headerCNV);
                    headerCNV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnClickListener.onHeaderClick(v);
                        }
                    });
                    headerCNV.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            mOnLongClickListener.onHeaderClick(v);
                            return false;
                        }
                    });

                    return headerCNViewHolder;
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

                    if (forum.getIconUrl() != null && mIsShowImages) {
                        ImageLoader.getInstance().displayImage(forum.getIconUrl(),
                                ((ViewHolder) holder).mImageView,
                                new ImageLoadingListener() {

                                    @Override
                                    public void onLoadingStarted(String p1, View p2) {
                                        p2.setVisibility(View.INVISIBLE);
                                        //holder.mProgressBar.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onLoadingFailed(String p1, View p2, FailReason p3) {
                                        // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onLoadingComplete(String p1, View p2, Bitmap p3) {
                                        p2.setVisibility(View.VISIBLE);
                                        // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onLoadingCancelled(String p1, View p2) {

                                    }
                                });
                    }
                    break;
                case HEADER_VIEW_TYPE:
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.mText.setText(forum.getTitle());

                    break;
                case HEADER_CURRENT_VIEW_TYPE:
                    HeaderViewHolder headerCViewHolder = (HeaderViewHolder) holder;
                    headerCViewHolder.mText.setText(forum.getTitle());

                    break;
                case HEADER_CURRENT_NOTOPICS_VIEW_TYPE:
                    HeaderViewHolder headerCNViewHolder = (HeaderViewHolder) holder;
                    headerCNViewHolder.mText.setText(forum.getTitle());

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

    private Handler mHandler = new Handler();

    private class UpdateForumStructTask extends AsyncTask<String, String, ForumsData> {

        private final MaterialDialog dialog;

        public UpdateForumStructTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(true);
                        }
                    })
                    .content("Обновление структуры форума")
                    .build();
        }

        protected void onCancelled() {
            Toast.makeText(getActivity(), "Обновление структуры форума отменено", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected ForumsData doInBackground(String... forums) {

            try {

                if (isCancelled()) return null;

                ForumsData res = ForumsApi.loadForums(Client.getInstance(), new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(String.format("%s %d", message, percents));
                    }
                });
                publishProgress("Обновление базы");
                ForumsTable.updateForums(res.getItems());
                return res;
            } catch (Throwable e) {
                ForumsData res = new ForumsData();
                res.setError(e);

                return res;
            }
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    dialog.setContent(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }


        protected void onPostExecute(final ForumsData data) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
            loadData(true);
            if (data != null) {
                if (data.getError() != null) {
                    AppLog.e(getActivity(), data.getError());
                }
            }
        }
    }
}
