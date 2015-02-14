package org.softeg.slartus.forpdaplus.qms;/*
 * Created by slinkin on 07.05.2014.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUser;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.listfragments.BaseTaskListFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.util.ArrayList;

public class QmsContactsListFragment extends BaseTaskListFragment {

    protected ArrayList<QmsUser> mData = new ArrayList<>();
    protected ArrayList<QmsUser> mLoadResultList;
    protected ArrayList<QmsUser> mCacheList = new ArrayList<>();


    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    protected BaseAdapter createAdapter() {

        ImageLoader imageLoader = ImageLoader.getInstance();
        return new QmsContactsAdapter(getContext(), mData, imageLoader);
    }


    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        ArrayList<QmsUser> users = QmsApi.getQmsSubscribers(Client.getInstance());
        Client.getInstance().setQmsCount(QmsUsers.unreadMessageUsersCount(users));
        Client.getInstance().doOnMailListener();
        mLoadResultList = users;
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh) {
            mData.clear();
        }
        for (QmsUser item : mLoadResultList) {
            mData.add(item);
        }

        mLoadResultList.clear();
    }

    @Override
    public void saveCache() throws Exception {
        CacheDbHelper cacheDbHelper = new CacheDbHelper(MyApp.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getWritableDatabase();
            BaseDao<QmsUser> baseDao = new BaseDao<>(MyApp.getContext(), db, getListName(), QmsUser.class);
            baseDao.createTable(db);
            for (IListItem item : mData) {
                QmsUser user = (QmsUser) item;
                baseDao.insert(user);
            }

        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {
        mCacheList = new ArrayList<>();

        CacheDbHelper cacheDbHelper = new CacheDbHelper(MyApp.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getReadableDatabase();
            BaseDao<QmsUser> baseDao = new BaseDao<>(MyApp.getContext(), db, getListName(), QmsUser.class);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        try {
            id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final QmsUser qmsUser = (QmsUser) o;
            QmsContactThemesActivity.showThemes(getActivity(), qmsUser.getId(), qmsUser.getNick().toString());

        } catch (Throwable ex) {
            Log.e(getActivity(), ex);
        }
    }


    public class QmsContactsAdapter extends BaseAdapter {

        private ArrayList<QmsUser> dataList;

        final LayoutInflater inflater;
        private ImageLoader imageLoader;
        private Boolean mShowAvatars;


        public QmsContactsAdapter(Context context, ArrayList<QmsUser> dataList, ImageLoader imageLoader) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            this.imageLoader = imageLoader;
            this.dataList = dataList;
            mShowAvatars = Preferences.Topic.isShowAvatars();
        }

        @Override
        public void notifyDataSetChanged() {

            mShowAvatars = Preferences.Topic.isShowAvatars();

            super.notifyDataSetChanged();
        }

        public void setData(ArrayList<QmsUser> data) {
            this.dataList = data;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int p1) {
            return dataList.get(p1);
        }

        @Override
        public long getItemId(int p1) {
            return p1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;
                holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatar);
                if (!mShowAvatars)
                    holder.imgAvatar.setVisibility(View.GONE);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            QmsUser user = (QmsUser) this.getItem(position);

            if (TextUtils.isEmpty(user.getNewMessagesCount()))
                holder.txtCount.setText("");
            else
                holder.txtCount.setText("(" + user.getNewMessagesCount() + ")");
            holder.txtNick.setText(user.getNick());

            if (!TextUtils.isEmpty(user.getNewMessagesCount())) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageBitmap(null);
            }

            if (user.getAvatarUrl() != null && mShowAvatars) {
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
}
