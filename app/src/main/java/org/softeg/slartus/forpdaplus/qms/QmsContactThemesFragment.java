package org.softeg.slartus.forpdaplus.qms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUserTheme;
import org.softeg.slartus.forpdaapi.qms.QmsUserThemes;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.BaseLoaderListFragment;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.util.ArrayList;

/*
 * Created by slinkin on 17.06.2015.
 */
public class QmsContactThemesFragment extends BaseLoaderListFragment {

    public static QmsContactThemesFragment newInstance(Bundle args){
        QmsContactThemesFragment fragment=new QmsContactThemesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String m_Id ;
    private String m_Nick;

    private Boolean DeleteMode = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        if(savedInstanceState!=null){
            m_Id = savedInstanceState.getString(QmsContactThemesActivity.MID_KEY);
            m_Nick = savedInstanceState.getString(QmsContactThemesActivity.NICK_KEY);
        }else if(getArguments()!=null){
            m_Id = getArguments().getString(QmsContactThemesActivity.MID_KEY);
            m_Nick = getArguments().getString(QmsContactThemesActivity.NICK_KEY);
        }
    }

    @Override
    protected Boolean useCache() {
        return false;
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new QmsContactsAdapter(getActivity(), getData().getItems());
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.list_fragment;
    }

    @Override
    protected ListData loadData(int loaderId, Bundle args) throws Throwable {
        ListData listData = new ListData();


        QmsUsers qmsUsers = new QmsUsers();
        QmsUserThemes mails = QmsApi.getQmsUserThemes(Client.getInstance(), m_Id, qmsUsers,
                TextUtils.isEmpty(m_Nick));
        listData.getItems().addAll(mails);
        Client.getInstance().setQmsCount(qmsUsers.unreadMessageUsersCount());
        Client.getInstance().doOnMailListener();
        return listData;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        l = ListViewMethodsBridge.getItemId(getActivity(), i, l);
        if (l < 0 || getAdapter().getCount() <= l) return;
        QmsUserTheme item = (QmsUserTheme) getAdapter().getItem((int) l);
        if (DeleteMode) {

            item.setSelected(!item.isSelected());
            getAdapter().notifyDataSetChanged();
        } else {
            org.softeg.slartus.forpdaplus.qms.QmsChatActivity.openChat(getActivity(), m_Id, m_Nick, item.Id, item.Title);
        }


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(!DeleteMode)
            startDeleteMode();
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("Новая тема").setIcon(R.drawable.ic_pencil_white_24dp);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                QmsNewThreadActivity.showUserNewThread(getActivity(), m_Id
                        , m_Nick);

                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        item = menu.add("Профиль");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                ProfileWebViewActivity.startActivity(getActivity(), m_Id
                        , m_Nick);
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    ActionMode mMode;
    private void startDeleteMode() {
        mMode = getActivity().startActionMode(new AnActionModeOfEpicProportions());
        DeleteMode = true;
        getListView().setSelection(AbsListView.CHOICE_MODE_MULTIPLE);
        getAdapter().notifyDataSetChanged();
    }

    private void stopDeleteMode(Boolean finishActionMode) {
        if (finishActionMode && mMode != null) {
            mMode.finish();
        }
        DeleteMode = false;
        getListView().setSelection(AbsListView.CHOICE_MODE_NONE);
        getAdapter().notifyDataSetChanged();
    }

    private void deleteSelectedDialogs() {

        ArrayList<String> ids = new ArrayList<>();
        for (IListItem item : getData().getItems()) {
            QmsUserTheme theme=(QmsUserTheme)item;
            if (theme.isSelected())
                ids.add(theme.Id);
        }
        new DeleteTask(getActivity(), ids).execute();
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
            Boolean anySelected=false;
            for (IListItem listItem : getData().getItems()) {
                QmsUserTheme theme=(QmsUserTheme)listItem;
                if (theme.isSelected()) {
                    anySelected = true;
                    break;
                }
            }
            if (anySelected)
                new MaterialDialog.Builder(getActivity())
                        .title("Подтвердите действие")
                        .content("Вы действительно хотите удалить выбранные диалоги с пользователем " + m_Nick + "?")
                        .positiveText("OK")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mode.finish();
                                deleteSelectedDialogs();
                            }
                        })
                        .negativeText("Отмена")
                        .show();

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            stopDeleteMode(false);
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, Boolean> {


        private final MaterialDialog dialog;
        ArrayList<String> m_Ids;

        public DeleteTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Удаление диалогов")
                    .build();
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
                    AppLog.e(getActivity(), ex);
                else
                    Toast.makeText(getActivity(), "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }


            reloadData();
        }
    }

    public class QmsContactsAdapter extends BaseAdapter {
        private LayoutInflater m_Inflater;
        private ArrayList<IListItem> dataList;

        public QmsContactsAdapter(Context context, ArrayList<IListItem> objects) {


            m_Inflater = LayoutInflater.from(context);
            dataList = objects;
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
                convertView = m_Inflater.inflate(R.layout.qms_contact_theme_item, parent, false);

                holder = new ViewHolder();
                //holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                holder.txtAllCount = (TextView) convertView.findViewById(R.id.txtAllMessagesCount);

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

            QmsUserTheme user = (QmsUserTheme) this.getItem(position);
            holder.checkbox.setTag(user);

            holder.txtNick.setText(Html.fromHtml(user.Title).toString());


            holder.txtDateTime.setText(user.Date);

            if (!TextUtils.isEmpty(user.NewCount)) {
                holder.txtCount.setText(user.NewCount);
                holder.txtAllCount.setText(user.Count);
                switch (PreferenceManager.getDefaultSharedPreferences(getContext()).getString("mainAccentColor", "pink")) {
                    case "pink":
                        holder.txtCount.setBackgroundResource(R.drawable.qmsnew);
                        break;
                    case "blue":
                        holder.txtCount.setBackgroundResource(R.drawable.qmsnewblue);
                        break;
                    case "gray":
                        holder.txtCount.setBackgroundResource(R.drawable.qmsnewgray);
                        break;
                }
                //holder.txtIsNew.setImageResource(R.drawable.new_flag);
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsNew);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsNew);
            } else {
                holder.txtAllCount.setText(user.Count);
                holder.txtCount.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                //holder.txtIsNew.setImageBitmap(null);
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsOld);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsOld);
            }
            holder.checkbox.setChecked(user.isSelected());
            return convertView;
        }

        private Context getContext() {
            return m_Inflater.getContext();
        }

        public class ViewHolder {
            //ImageView txtIsNew;
            TextView txtNick;
            TextView txtDateTime;
            TextView txtCount;
            TextView txtAllCount;
            CheckBox checkbox;
        }
    }


}
