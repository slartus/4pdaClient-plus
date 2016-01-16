package org.softeg.slartus.forpdaplus.fragments.qms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUserTheme;
import org.softeg.slartus.forpdaapi.qms.QmsUserThemes;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.listfragments.BaseLoaderListFragment;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.util.ArrayList;

/**
 * Created by radiationx on 12.11.15.
 */
public class QmsContactThemes extends BaseLoaderListFragment {
    public static final String MID_KEY = "mid";
    public static final String NICK_KEY = "nick";
    ActionMode mMode;
    private String m_Id ;
    private String m_Nick = "";

    private Boolean DeleteMode = false;
    public Menu menu;
    
    @Override
    public void onResume() {
        super.onResume();
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public Menu getMenu(){
        return menu;
    }

    public static void showThemes(String userId, String userNick){
        Bundle bundle = new Bundle();
        bundle.putString("mid", userId);
        bundle.putString("nick", userNick);
        MainActivity.addTab(userNick, userId, newInstance(bundle));
    }

    public static QmsContactThemes newInstance(Bundle args){
        QmsContactThemes fragment=new QmsContactThemes();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setArrow();

        if(savedInstanceState!=null){
            m_Id = savedInstanceState.getString(MID_KEY);
            m_Nick = savedInstanceState.getString(NICK_KEY);
        }else if(getArguments()!=null){
            m_Id = getArguments().getString(MID_KEY);
            m_Nick = getArguments().getString(NICK_KEY);
        }
        if(m_Nick!=null)
            if(m_Nick.equals(""))
                new GetUserTask(m_Id).execute();
    }

    private class GetUserTask extends AsyncTask<String, Void, Boolean> {
        private String userId;
        private String userNick;

        public GetUserTask(String userId) {
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                userNick = ProfileApi.getUserNick(Client.getInstance(), userId);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            Toast.makeText(getContext(), "Получение ника пользователя...", Toast.LENGTH_SHORT).show();
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            if (success && !TextUtils.isEmpty(userNick)) {
                m_Nick = userNick;
                Toast.makeText(getContext(), "Ник получен: " + m_Nick, Toast.LENGTH_SHORT).show();
                setTitle(m_Nick);
                App.getInstance().getTabByTag(getTag()).setTitle(m_Nick);
                TabDrawerMenu.notifyDataSetChanged();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex, new Runnable() {
                        @Override
                        public void run() {
                            new GetUserTask(userId).execute();
                        }
                    });
                else if (TextUtils.isEmpty(userNick))
                    Toast.makeText(getMainActivity(), "Не удалось получить ник пользователя",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean dialogShowed = false;

    @Override
    public void onLoadFinished(Loader<ListData> loader, ListData data) {
        super.onLoadFinished(loader, data);
        if(data.getItems().size()<=0&!dialogShowed) {
            new MaterialDialog.Builder(getContext())
                    .content("С пользователем " + m_Nick + " нет диалогов. Создать?")
                    .positiveText("Да")
                    .negativeText("Нет")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            QmsNewThreadFragment.showUserNewThread(getMainActivity(), m_Id, m_Nick);
                        }
                    })
                    .show();
            dialogShowed = true;
        }
    }

    @Override
    protected Boolean useCache() {
        return false;
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new QmsContactsAdapter(getMainActivity(), getData().getItems());
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

        l = ListViewMethodsBridge.getItemId(getMainActivity(), i, l);
        if (l < 0 || getAdapter().getCount() <= l) return;
        QmsUserTheme item = (QmsUserTheme) getAdapter().getItem((int) l);
        if (DeleteMode) {

            item.setSelected(!item.isSelected());
            getAdapter().notifyDataSetChanged();
        } else {
            QmsChatFragment.openChat(m_Id, m_Nick, item.Id, item.Title);
            //org.softeg.slartus.forpdaplus.qms.QmsChatActivity.openChat(getMainActivity(), m_Id, m_Nick, item.Id, item.Title);
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
        menu.add("Новая тема")
                .setIcon(R.drawable.ic_pencil_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                QmsNewThreadFragment.showUserNewThread(getMainActivity(), m_Id
                        , m_Nick);

                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("Профиль собеседника")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                ProfileFragment.showProfile(m_Id, m_Nick);
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        this.menu = menu;
    }

    private void startDeleteMode() {
        mMode = getMainActivity().startActionMode(new AnActionModeOfEpicProportions());
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
        new DeleteTask(getMainActivity(), ids).execute();
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
                new MaterialDialog.Builder(getMainActivity())
                        .title("Подтвердите действие")
                        .content("Вы действительно хотите удалить выбранные диалоги с пользователем " + m_Nick + "?")
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
        private Throwable ex;

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

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            stopDeleteMode(true);
            if (!success) {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка",
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
