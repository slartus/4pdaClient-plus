package org.softeg.slartus.forpdaplus.tabs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.OldUser;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.common.ExtColor;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 27.03.13
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class UsersTab extends BaseTab implements AdapterView.OnItemClickListener,
        Loader.OnLoadCompleteListener<Users> {
    public static final String TEMPLATE = "UsersTab";
    public static final String TITLE = "Пользователи";
    ListView m_ListView;

    protected Users m_Users = new Users();
    private UsersAdapter mAdapter;
    protected uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout mPullToRefreshLayout;
    public UsersTab(Context context, ITabParent tabParent) {
        super(context, tabParent);


        addView(inflate(context, R.layout.qms_contacts_list, null),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        m_ListView = (ListView) findViewById(android.R.id.list);
        mPullToRefreshLayout = App.createPullToRefreshLayout(getActivity(), findViewById(R.id.main_layout), new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });
        setState(true);


        mAdapter =createAdapter();
        m_ListView.setAdapter(mAdapter);
    }

    protected UsersAdapter createAdapter(){
        return new UsersAdapter(getContext(), new Users(),true);
    }

    protected Users loadUsers(Bundle extras) throws IOException {
        return new Users();
    }

    Bundle m_Extras;

    @Override
    public void refresh(Bundle extras) {
        m_Extras = extras;
        refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Boolean onParentBackPressed() {
        return false;
    }

    @Override
    public void refresh() {

        refreshData();
    }

    @Override
    public Boolean cachable() {
        return false;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public void loadCache() {

    }

    private void refreshData() {
        m_Users.clear();

        setState(true);
        UsersLoader qmsUsersLoader = new UsersLoader(getContext(), new GetUsersInterface() {
            @Override
            public Users loadUsers() throws IOException {
                return UsersTab.this.loadUsers(m_Extras);
            }
        });
        qmsUsersLoader.registerListener(0, this);
        qmsUsersLoader.startLoading();

    }

    private void setState(boolean loading) {
        mPullToRefreshLayout.setRefreshing(loading);

    }

    @Override
    public Boolean refreshed() {
        return true;
    }

    @Override
    public ListView getListView() {
        return m_ListView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        OldUser user = mAdapter.getItem((int) info.id);

        ForumUser.onCreateContextMenu(getContext(), menu, user.getMid(), user.getNick());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || mAdapter.getCount() <= l) return;
        OldUser qmsUser = mAdapter.getItem((int) l);

        ProfileWebViewActivity.startActivity(getContext(), qmsUser.getMid());
    }

    public void onLoadComplete(Loader<Users> qmsUsersLoader, Users data) {
        if (data != null) {
            for (OldUser item : data) {
                m_Users.add(item);
            }
            mAdapter.setData(m_Users);
        } else {
            m_Users = new Users();
            mAdapter.setData(m_Users);
        }


        setState(false);
        mAdapter.notifyDataSetChanged();
        if (getTabParent() != null)
            getTabParent().setTitle(getTitle());
    }

    public interface GetUsersInterface {
        Users loadUsers() throws IOException;
    }

    private static class UsersLoader extends AsyncTaskLoader<Users> {

        Users mApps;
        GetUsersInterface m_GetUsersInterface;
        Throwable ex;

        public UsersLoader(Context context, GetUsersInterface getUsersInterface) {
            super(context);
            m_GetUsersInterface = getUsersInterface;
        }

        @Override
        public Users loadInBackground() {
            try {
                return m_GetUsersInterface.loadUsers();
            } catch (Throwable e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(Users apps) {
            if (ex != null)
                AppLog.e(getContext(), ex);
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
        public void onCanceled(Users apps) {
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

    public static class UsersAdapter extends ArrayAdapter<OldUser> {
        protected LayoutInflater m_Inflater;
        private Boolean showCount;

        public void setData(Users data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (OldUser item : data) {
                    add(item);
                }
            }
        }

        public UsersAdapter(Context context, ArrayList<OldUser> objects, Boolean showCount) {
            super(context, android.R.layout.simple_list_item_1, objects);
            this.showCount = showCount;

            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.user_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                if(!showCount)
                    holder.txtCount.setVisibility(GONE);
                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            OldUser user = this.getItem(position);

            holder.txtCount.setText(user.MessagesCount);
            holder.txtNick.setText(user.getNick());
            try {
                holder.txtNick.setTextColor(ExtColor.parseColor(user.getHtmlColor()));
            } catch (Exception ex) {
                AppLog.e(getContext(), new Exception("Не умею цвет: " + user.getHtmlColor()));
            }
            return convertView;
        }

        public class ViewHolder {
            TextView txtNick;
            TextView txtCount;
        }
    }
}
