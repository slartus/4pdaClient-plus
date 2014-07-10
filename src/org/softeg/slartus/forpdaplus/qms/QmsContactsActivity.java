package org.softeg.slartus.forpdaplus.qms;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUser;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 10:46
 */
public class QmsContactsActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener,
        Loader.OnLoadCompleteListener<ArrayList<QmsUser>> {
    private QmsContactsAdapter mAdapter;
    private ArrayList<QmsUser> m_QmsUsers = new ArrayList<>();
    private PullToRefreshListView m_ListView;
    ImageLoader imageLoader;

    private MenuFragment mFragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_contacts_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        initImageLoader(MyApp.getContext());
        imageLoader = ImageLoader.getInstance();

        m_ListView = (PullToRefreshListView) findViewById(R.id.pulltorefresh);

        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter = new QmsContactsAdapter(this, R.layout.qms_contact_item, new QmsUsers());
        m_ListView.getRefreshableView().setAdapter(mAdapter);
        m_ListView.getRefreshableView().setOnItemClickListener(this);
    }



    public static void show(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), org.softeg.slartus.forpdaplus.qms.QmsContactsActivity.class);

        intent.putExtra("activity", activity.getClass().toString());
        activity.startActivity(intent);
    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        m_QmsUsers.clear();

        setState(true);
        QmsUsersLoader qmsUsersLoader = new QmsUsersLoader(this);
        qmsUsersLoader.registerListener(0, this);
        qmsUsersLoader.startLoading();
    }

    private void updateDataInfo() {

//        int loadMoreVisibility = (m_QmsUsers.getFullLength() > m_QmsUsers.size()) ? View.VISIBLE : View.GONE;
//        txtPullToLoadMore.setVisibility(loadMoreVisibility);
//        imgPullToLoadMore.setVisibility(loadMoreVisibility);
//        txtLoadMoreThemes.setText("Всего: " + m_QmsUsers.getFullLength());
//        setHeaderText((m_Mails == null ? 0 : m_QmsUsers.size()) + " тем");
//        m_ListFooter.setVisibility(m_Mails.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private void setState(boolean loading) {
        if (loading)
            m_ListView.setRefreshing(false);
        else
            m_ListView.onRefreshComplete();

    }

    public void onLoadComplete(Loader<ArrayList<QmsUser>> qmsUsersLoader, ArrayList<QmsUser> data) {
        if (data != null) {
            for (QmsUser item : data) {
                m_QmsUsers.add(item);
            }
            mAdapter.setData(m_QmsUsers);
        } else {
            m_QmsUsers = new QmsUsers();
            mAdapter.setData(m_QmsUsers);
        }

        updateDataInfo();
        setState(false);
        mAdapter.notifyDataSetChanged();

    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(this, i, l);
        if (l < 0 || mAdapter.getCount() <= l) return;
        QmsUser qmsUser = mAdapter.getItem((int) l);
        QmsContactThemesActivity.showThemes(this, qmsUser.getId().toString(), qmsUser.getNick().toString());

    }

    private static class QmsUsersLoader extends AsyncTaskLoader<ArrayList<QmsUser>> {

        ArrayList<QmsUser> mApps;

        Throwable ex;

        public QmsUsersLoader(Context context) {
            super(context);

        }

        @Override
        public ArrayList<QmsUser> loadInBackground() {
            try {
                ArrayList<QmsUser> users = QmsApi.getQmsSubscribers(Client.getInstance());
                Client.getInstance().setQmsCount(QmsUsers.unreadMessageUsersCount(users));
                Client.getInstance().doOnMailListener();
                return users;
            } catch (Throwable e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(ArrayList<QmsUser> apps) {
            if (ex != null)
                Log.e(getContext(), ex);
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
        public void onCanceled(ArrayList<QmsUser> apps) {
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

        protected void onReleaseResources() {
            if (mApps != null)
                mApps.clear();

            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
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
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 2 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public class QmsContactsAdapter extends ArrayAdapter<QmsUser> {
        private LayoutInflater m_Inflater;
        private Boolean mLoadImages;

        public void setData(ArrayList<QmsUser> data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (QmsUser item : data) {
                    add(item);
                }
            }
        }

        public QmsContactsAdapter(Context context, int textViewResourceId, ArrayList<QmsUser> objects) {
            super(context, textViewResourceId, objects);

            m_Inflater = LayoutInflater.from(context);
            mLoadImages = Preferences.isLoadImagesFromWeb("theme");

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatar);
                if(!mLoadImages)
                    holder.imgAvatar.setVisibility(View.GONE);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            QmsUser user = this.getItem(position);

            if(TextUtils.isEmpty(user.getNewMessagesCount()))
                holder.txtCount.setText("");
            else
                holder.txtCount.setText("(" + user.getNewMessagesCount() + ")");
            holder.txtNick.setText(user.getNick());
//            try {
//
//                holder.txtNick.setTextColor(ExtColor.parseColor(user.getHtmlColor()));
//            } catch (Exception ex) {
//                Log.e(getContext(), new Exception("Не умею цвет: " + user.getHtmlColor()));
//            }


            if (!TextUtils.isEmpty(user.getNewMessagesCount())) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageBitmap(null);
            }

            if (user.getAvatarUrl() != null && mLoadImages) {
                imageLoader.displayImage(user.getAvatarUrl(), holder.imgAvatar, new ImageLoadingListener() {

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
            return convertView;
        }

        public class ViewHolder {
            ImageView txtIsNew;
            ImageView imgAvatar;
            TextView txtNick;

            TextView txtCount;
        }
    }

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem item = menu.add("Новая тема").setIcon(R.drawable.ic_menu_send);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    QmsNewThreadActivity.showUserNewThread(getActivity(), null, null);

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(R.drawable.ic_menu_preferences);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), QmsPreferencesActivity.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }
}
