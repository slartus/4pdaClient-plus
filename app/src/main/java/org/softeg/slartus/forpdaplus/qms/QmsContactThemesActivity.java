package org.softeg.slartus.forpdaplus.qms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUserTheme;
import org.softeg.slartus.forpdaapi.qms.QmsUserThemes;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 04.02.13
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class QmsContactThemesActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener,

        Loader.OnLoadCompleteListener<QmsUserThemes> {
    private QmsContactsAdapter mAdapter;
    private QmsUserThemes m_QmsUsers = new QmsUserThemes();
    private ListView m_ListView;
    private static final String MID_KEY = "mid";
    private static final String NICK_KEY = "nick";
    protected uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout mPullToRefreshLayout;
    private String m_Id;
    private String m_Nick;

    //  private MenuFragment mFragment1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_contacts_list);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        m_ListView = (ListView) findViewById(android.R.id.list);
        mPullToRefreshLayout = App.createPullToRefreshLayout(this,findViewById(R.id.main_layout),new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });

        setState(true);


        mAdapter = new QmsContactsAdapter(this, R.layout.qms_contact_item, new ArrayList<QmsUserTheme>());
        m_ListView.setAdapter(mAdapter);
        m_ListView.setOnItemClickListener(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        setTitle(m_Nick + "-QMS-Темы");
    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MenuFragment mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    public static void showThemes(Context activity, String mid, String userNick) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsContactThemesActivity.class);
        intent.putExtra(MID_KEY, mid);
        intent.putExtra(NICK_KEY, userNick);

        activity.startActivity(intent);
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
        QmsUsersLoader qmsUsersLoader = new QmsUsersLoader(this, m_Id, TextUtils.isEmpty(m_Nick));
        qmsUsersLoader.registerListener(0, this);
        qmsUsersLoader.startLoading();
    }


    private void setState(boolean loading) {
        mPullToRefreshLayout.setRefreshing(loading);

    }

    public void onLoadComplete(Loader<QmsUserThemes> qmsUsersLoader, QmsUserThemes data) {
        if (data != null) {
            if (!TextUtils.isEmpty(data.Nick)) {
                m_Nick = data.Nick;
                setTitle(m_Nick + " - QMS-Темы");
            }
            for (QmsUserTheme item : data) {
                m_QmsUsers.add(item);
            }
            mAdapter.setData(m_QmsUsers);
        } else {
            m_QmsUsers = new QmsUserThemes();
            mAdapter.setData(m_QmsUsers);
        }


        setState(false);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        l = ListViewMethodsBridge.getItemId(this, i, l);
        if (l < 0 || mAdapter.getCount() <= l) return;
        QmsUserTheme item = mAdapter.getItem((int) l);
        if (DeleteMode) {

            item.setSelected(!item.isSelected());
            mAdapter.notifyDataSetChanged();
        } else {
            org.softeg.slartus.forpdaplus.qms.QmsChatActivity.openChat(this, m_Id, m_Nick, item.Id, item.Title);
        }


    }

    ActionMode mMode;


    public String getUserId() {
        return m_Id;
    }

    public String getUserNick() {
        return m_Nick;
    }

    public Boolean DeleteMode = false;

    private void startDeleteMode() {
//        if("0".equals(m_Id))
//        {
//            new AlertDialogBuilder(this)
//                    .setTitle("Внимание!")
//                    .setMessage("В настоящее время нельзя удалить диалоги от пользователя '"+m_Nick+"'")
//                    .setCancelable(true)
//                    .setPositiveButton("OK",null)
//                    .create().show();
//            return;
//        }
        mMode = startActionMode(new AnActionModeOfEpicProportions());
        DeleteMode = true;
        m_ListView.setSelection(AbsListView.CHOICE_MODE_MULTIPLE);
        mAdapter.notifyDataSetChanged();
    }

    private void stopDeleteMode(Boolean finishActionMode) {
        if (finishActionMode && mMode != null) {
            mMode.finish();
        }
        DeleteMode = false;
        m_ListView.setSelection(AbsListView.CHOICE_MODE_NONE);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteSelectedDialogs() {

        ArrayList<String> ids = new ArrayList<>();
        for (QmsUserTheme theme : m_QmsUsers) {
            if (theme.isSelected())
                ids.add(theme.Id);
        }
        new DeleteTask(this, ids).execute();
    }

    private static class QmsUsersLoader extends AsyncTaskLoader<QmsUserThemes> {

        QmsUserThemes mApps;

        Throwable ex;
        String m_Id;
        Boolean m_ParseNick;

        public QmsUsersLoader(Context context, String mid, Boolean parseNick) {
            super(context);
            m_Id = mid;
            m_ParseNick = parseNick;
        }

        @Override
        public QmsUserThemes loadInBackground() {
            try {
                QmsUsers qmsUsers = new QmsUsers();
                QmsUserThemes mails = QmsApi.getQmsUserThemes(Client.getInstance(), m_Id, qmsUsers,
                        m_ParseNick);
                Client.getInstance().setQmsCount(qmsUsers.unreadMessageUsersCount());
                Client.getInstance().doOnMailListener();
                return mails;
            } catch (Throwable e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(QmsUserThemes apps) {
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
        public void onCanceled(QmsUserThemes apps) {
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

    public class QmsContactsAdapter extends ArrayAdapter<QmsUserTheme> {
        private LayoutInflater m_Inflater;


        public void setData(ArrayList<QmsUserTheme> data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (QmsUserTheme item : data) {
                    add(item);
                }
            }
        }

        public QmsContactsAdapter(Context context, int textViewResourceId, ArrayList<QmsUserTheme> objects) {
            super(context, textViewResourceId, objects);

            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.qms_contact_theme_item, parent, false);

                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);

                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);

                holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);

                holder.checkbox = (CheckBox) convertView.findViewById(android.R.id.text1);
                holder.checkbox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                QmsUserTheme theme = (QmsUserTheme) holder.checkbox
                                        .getTag();
                                theme.setSelected(buttonView.isChecked());


                            }
                        });
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setVisibility(DeleteMode ? View.VISIBLE : View.GONE);

            QmsUserTheme user = this.getItem(position);
            holder.checkbox.setTag(user);

            holder.txtNick.setText(Html.fromHtml(user.Title).toString());


            holder.txtDateTime.setText(user.Date);

            if (!TextUtils.isEmpty(user.NewCount)) {
                holder.txtCount.setText(user.NewCount + "/" + user.Count);
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtCount.setText(user.Count);
                holder.txtIsNew.setImageBitmap(null);
            }
            holder.checkbox.setChecked(user.isSelected());
            return convertView;
        }

        public class ViewHolder {
            ImageView txtIsNew;
            TextView txtNick;
            TextView txtDateTime;
            TextView txtCount;
            CheckBox checkbox;
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        ArrayList<String> m_Ids;

        public DeleteTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                QmsApi.deleteDialogs(Client.getInstance(), m_Id, m_Ids);

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление диалогов...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            stopDeleteMode(true);
            if (!success) {
                if (ex != null)
                    AppLog.e(QmsContactThemesActivity.this, ex);
                else
                    Toast.makeText(QmsContactThemesActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }


            refreshData();
        }
    }

    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //Used to put dark icons on light action bar


            menu.add("Удалить")

                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            if (m_QmsUsers.getSelectedCount() != 0)
                new AlertDialogBuilder(QmsContactThemesActivity.this)
                        .setTitle("Подтвердите действие")
                        .setMessage("Вы действительно хотите удалить выбранные диалоги с пользователем " + m_Nick + "?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                mode.finish();
                                deleteSelectedDialogs();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create()
                        .show();

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            stopDeleteMode(false);
        }
    }

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {

        }

        private QmsContactThemesActivity getInterface() {
            if (getActivity() == null) return null;
            return (QmsContactThemesActivity) getActivity();
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
                    QmsNewThreadActivity.showUserNewThread(getActivity(), getInterface().getUserId()
                            , getInterface().getUserNick());

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Удалить диалоги").setIcon(R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().startDeleteMode();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


            item = menu.add("Профиль");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ProfileWebViewActivity.startActivity(getActivity(), getInterface().getUserId(), getInterface().getUserNick());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }


}