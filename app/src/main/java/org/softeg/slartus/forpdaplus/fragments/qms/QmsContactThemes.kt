package org.softeg.slartus.forpdaplus.fragments.qms

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import ru.slartus.feature_qms_contact_threads.QmsContactThreadsFragment

class QmsContactThemes : BaseBrickContainerFragment() {
    private var contactId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId =
            savedInstanceState?.getString(MID_KEY) ?: arguments?.getString(MID_KEY) ?: contactId
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    override fun setBrickInfo(listTemplate: BrickInfo): Fragment {
        return super.setBrickInfo(listTemplate)
    }

    override fun getListName(): String {
        return "QmsContactThemes_$contactId"
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return QmsContactThreadsFragment().apply {
            this.arguments = args
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MID_KEY, contactId)
    }

    companion object {
        const val MID_KEY = QmsContactThreadsFragment.ARG_CONTACT_ID
        const val NICK_KEY = "nick"

        @JvmStatic
        fun showThemes(userId: String, userNick: String?) {
            val bundle = bundleOf(MID_KEY to userId, NICK_KEY to userNick)
            MainActivity.addTab(userNick, userId, newInstance(bundle))
        }

        @JvmStatic
        fun newInstance(args: Bundle?): QmsContactThemes = QmsContactThemes().apply {
            arguments = args
        }
    }

//
    //    

    //    ActionMode mMode;
    //    private String m_Id;
    //    private String m_Nick = "";
    //
    //    private Boolean DeleteMode = false;
    //
    //   
    //

    //
    //    @Override
    //    public void onCreate(Bundle savedInstanceState) {
    //        super.onCreate(savedInstanceState);
    //        setArrow();
    //
    //        if (savedInstanceState != null) {
    //            m_Id = savedInstanceState.getString(MID_KEY);
    //            m_Nick = savedInstanceState.getString(NICK_KEY);
    //        } else if (getArguments() != null) {
    //            m_Id = getArguments().getString(MID_KEY);
    //            m_Nick = getArguments().getString(NICK_KEY);
    //        }
    //        if (m_Nick != null)
    //            if (m_Nick.equals(""))
    //                new GetUserTask(m_Id).execute();
    //    }
    //
    //    @Override
    //    public void onSaveInstanceState(Bundle outState) {
    //        super.onSaveInstanceState(outState);
    //        outState.putString(MID_KEY, m_Id);
    //        outState.putString(NICK_KEY, m_Nick);
    //    }
    //
    //    private class GetUserTask extends AsyncTask<String, Void, Boolean> {
    //        private final String userId;
    //        private String userNick;
    //
    //        GetUserTask(String userId) {
    //            this.userId = userId;
    //        }
    //
    //        @Override
    //        protected Boolean doInBackground(String... params) {
    //            try {
    //                userNick = ProfileApi.getUserNick(Client.getInstance(), userId);
    //                return true;
    //            } catch (Exception e) {
    //                ex = e;
    //                return false;
    //            }
    //        }
    //
    //        protected void onPreExecute() {
    //            Toast.makeText(getContext(), R.string.getting_user_nick, Toast.LENGTH_SHORT).show();
    //        }
    //
    //        private Exception ex;
    //
    //        protected void onPostExecute(final Boolean success) {
    //            if (success && !TextUtils.isEmpty(userNick)) {
    //                m_Nick = userNick;
    //                Toast.makeText(App.getContext(), App.getContext().getString(R.string.nick_received) + ": " + m_Nick, Toast.LENGTH_SHORT).show();
    //                setTitle(m_Nick);
    //                TabsManager.getInstance().getTabByTag(getTag()).setTitle(m_Nick);
    //                getMainActivity().notifyTabAdapter();
    //            } else {
    //                if (ex != null)
    //                    AppLog.e(getMainActivity(), ex, () -> new GetUserTask(userId).execute());
    //                else if (TextUtils.isEmpty(userNick))
    //                    Toast.makeText(getMainActivity(), R.string.error_getting_nick,
    //                            Toast.LENGTH_SHORT).show();
    //                else
    //                    Toast.makeText(getMainActivity(), R.string.unknown_error,
    //                            Toast.LENGTH_SHORT).show();
    //            }
    //        }
    //    }
    //
    //    private boolean dialogShowed = false;
    //
    //    @Override
    //    public void onLoadFinished(Loader<ListData> loader, ListData data) {
    //        super.onLoadFinished(loader, data);
    //        if (data.getItems().size() <= 0 & !dialogShowed) {
    //            new MaterialDialog.Builder(getContext())
    //                    .content(String.format(App.getContext().getString(R.string.ask_create_dialog), m_Nick))
    //                    .positiveText(R.string.yes)
    //                    .negativeText(R.string.no)
    //                    .onPositive((dialog, which) -> QmsNewThreadFragment.showUserNewThread(getMainActivity(), m_Id, m_Nick))
    //                    .show();
    //            dialogShowed = true;
    //        }
    //    }
    //
    //    @Override
    //    protected Boolean useCache() {
    //        return false;
    //    }
    //
    //    @Override
    //    protected BaseAdapter createAdapter() {
    //        return new QmsContactsAdapter(getMainActivity(), getData().getItems());
    //    }
    //
    //    @Override
    //    protected int getViewResourceId() {
    //        return R.layout.list_fragment;
    //    }
    //
    //    @Override
    //    protected ListData loadData(int loaderId, Bundle args) throws Throwable {
    //        ListData listData = new ListData();
    //
    //
    //        QmsUsers qmsUsers = new QmsUsers();
    //        QmsUserThemes mails = QmsApi.INSTANCE.getQmsUserThemes(m_Id, qmsUsers,
    //                TextUtils.isEmpty(m_Nick));
    //        listData.getItems().addAll(mails);
    //        Client.getInstance().setQmsCount(qmsUsers.unreadMessageUsersCount());
    //
    //        return listData;
    //    }
    //
    //    @Override
    //    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    //
    //        l = ListViewMethodsBridge.getItemId(getMainActivity(), i, l);
    //        if (l < 0 || getAdapter().getCount() <= l) return;
    //        QmsUserTheme item = (QmsUserTheme) getAdapter().getItem((int) l);
    //        if (DeleteMode) {
    //
    //            item.setSelected(!item.isSelected());
    //            getAdapter().notifyDataSetChanged();
    //        } else {
    //            QmsChatFragment.Companion.openChat(m_Id, m_Nick, item.Id, item.Title);
    //            //org.softeg.slartus.forpdaplus.qms.QmsChatActivity.openChat(getMainActivity(), m_Id, m_Nick, item.Id, item.Title);
    //        }
    //
    //
    //    }
    //
    //    @Override
    //    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    //        if (!DeleteMode)
    //            startDeleteMode();
    //        return true;
    //    }
    //
    //
    //    @Override
    //    public boolean onOptionsItemSelected(MenuItem item) {
    //        switch (item.getItemId()){
    //            case R.id.new_thread_item:
    //                QmsNewThreadFragment.showUserNewThread(getMainActivity(), m_Id, m_Nick);
    //                return true;
    //            case R.id.profile_interlocutor_item:
    //                ProfileFragment.showProfile(m_Id, m_Nick);
    //                return true;
    //        }
    //        return super.onOptionsItemSelected(item);
    //    }
    //
    //    @Override
    //    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    //        super.onCreateOptionsMenu(menu, inflater);
    //        if(inflater!=null)
    //            inflater .inflate(R.menu.qms_contact_themes, menu);
    //    }
    //
    //    private void startDeleteMode() {
    //        mMode = getMainActivity().startActionMode(new AnActionModeOfEpicProportions());
    //        DeleteMode = true;
    //        getListView().setSelection(AbsListView.CHOICE_MODE_MULTIPLE);
    //        getAdapter().notifyDataSetChanged();
    //    }
    //
    //    private void stopDeleteMode(Boolean finishActionMode) {
    //        if (finishActionMode && mMode != null) {
    //            mMode.finish();
    //        }
    //        DeleteMode = false;
    //        getListView().setSelection(AbsListView.CHOICE_MODE_NONE);
    //        getAdapter().notifyDataSetChanged();
    //    }
    //
    //    private void deleteSelectedDialogs() {
    //
    //        ArrayList<String> ids = new ArrayList<>();
    //        for (IListItem item : getData().getItems()) {
    //            QmsUserTheme theme = (QmsUserTheme) item;
    //            if (theme.isSelected())
    //                ids.add(theme.Id);
    //        }
    //        new DeleteTask(getMainActivity(), ids).execute();
    //    }
    //
    //    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
    //        @Override
    //        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    //            //Used to put dark icons on light action bar
    //
    //
    //            menu.add(R.string.delete)
    //                    .setIcon(R.drawable.delete)
    //                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    //
    //
    //            return true;
    //        }
    //
    //        @Override
    //        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    //            return false;
    //        }
    //
    //        @Override
    //        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
    //            boolean anySelected = false;
    //            for (IListItem listItem : getData().getItems()) {
    //                QmsUserTheme theme = (QmsUserTheme) listItem;
    //                if (theme.isSelected()) {
    //                    anySelected = true;
    //                    break;
    //                }
    //            }
    //            if (anySelected)
    //                new MaterialDialog.Builder(getMainActivity())
    //                        .title(R.string.confirm_action)
    //                        .content(String.format(getString(R.string.ask_deleting_dialogs), m_Nick))
    //                        .positiveText(R.string.ok)
    //                        .onPositive((materialDialog, dialogAction) -> {
    //                            mode.finish();
    //                            deleteSelectedDialogs();
    //                        })
    //                        .negativeText(R.string.cancel)
    //                        .show();
    //
    //            return true;
    //        }
    //
    //        @Override
    //        public void onDestroyActionMode(ActionMode mode) {
    //            stopDeleteMode(false);
    //        }
    //    }
    //
    //    private class DeleteTask extends AsyncTask<String, Void, Boolean> {
    //
    //
    //        private final MaterialDialog dialog;
    //        ArrayList<String> m_Ids;
    //        private Throwable ex;
    //
    //        DeleteTask(Context context, ArrayList<String> ids) {
    //            m_Ids = ids;
    //            dialog = new MaterialDialog.Builder(context)
    //                    .progress(true, 0)
    //                    .content(App.getContext().getString(R.string.deleting_dialogs))
    //                    .build();
    //        }
    //
    //        @Override
    //        protected Boolean doInBackground(String... params) {
    //            try {
    //
    //                QmsApi.INSTANCE.deleteDialogs(Client.getInstance(), m_Id, m_Ids);
    //
    //                return true;
    //            } catch (Throwable e) {
    //                ex = e;
    //                return false;
    //            }
    //        }
    //
    //        // can use UI thread here
    //        protected void onPreExecute() {
    //            this.dialog.show();
    //        }
    //
    //        // can use UI thread here
    //        protected void onPostExecute(final Boolean success) {
    //            if (this.dialog.isShowing()) {
    //                this.dialog.dismiss();
    //            }
    //            stopDeleteMode(true);
    //            if (!success) {
    //                if (ex != null)
    //                    AppLog.e(getMainActivity(), ex);
    //                else
    //                    Toast.makeText(getMainActivity(), R.string.unknown_error,
    //                            Toast.LENGTH_SHORT).show();
    //            }
    //
    //
    //            reloadData();
    //        }
    //    }
    //
    //    public class QmsContactsAdapter extends BaseAdapter {
    //        private final LayoutInflater m_Inflater;
    //        private final ArrayList<IListItem> dataList;
    //
    //        QmsContactsAdapter(Context context, ArrayList<IListItem> objects) {
    //
    //
    //            m_Inflater = LayoutInflater.from(context);
    //            dataList = objects;
    //        }
    //
    //        @Override
    //        public int getCount() {
    //            return dataList.size();
    //        }
    //
    //        @Override
    //        public Object getItem(int p1) {
    //            return dataList.get(p1);
    //        }
    //
    //        @Override
    //        public long getItemId(int p1) {
    //            return p1;
    //        }
    //
    //
    //        @Override
    //        public View getView(final int position, View convertView, ViewGroup parent) {
    //
    //            final ViewHolder holder;
    //
    //            if (convertView == null) {
    //                convertView = m_Inflater.inflate(R.layout.qms_contact_theme_item, parent, false);
    //
    //                holder = new ViewHolder();
    //                //holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
    //                holder.txtCount = convertView.findViewById(R.id.txtMessagesCount);
    //                holder.txtAllCount = convertView.findViewById(R.id.txtAllMessagesCount);
    //
    //                holder.txtNick = convertView.findViewById(R.id.txtNick);
    //
    //                holder.txtDateTime = convertView.findViewById(R.id.txtDateTime);
    //
    //                holder.checkbox = convertView.findViewById(R.id.text1);
    //                holder.checkbox
    //                        .setOnCheckedChangeListener((buttonView, isChecked) -> {
    //                            QmsUserTheme theme = (QmsUserTheme) holder.checkbox
    //                                    .getTag();
    //                            theme.setSelected(buttonView.isChecked());
    //
    //
    //                        });
    //                convertView.setTag(holder);
    //
    //            } else {
    //                holder = (ViewHolder) convertView.getTag();
    //            }
    //            holder.checkbox.setVisibility(DeleteMode ? View.VISIBLE : View.GONE);
    //
    //            QmsUserTheme user = (QmsUserTheme) this.getItem(position);
    //            holder.checkbox.setTag(user);
    //
    //            holder.txtNick.setText(Html.fromHtml(user.Title).toString());
    //
    //
    //            holder.txtDateTime.setText(user.Date);
    //
    //            if (!TextUtils.isEmpty(user.NewCount)) {
    //                holder.txtCount.setText(user.NewCount);
    //                holder.txtAllCount.setText(user.Count);
    //                switch (App.getInstance().getPreferences().getString("mainAccentColor", "pink")) {
    //                    case "pink":
    //                        holder.txtCount.setBackgroundResource(R.drawable.qmsnew);
    //                        break;
    //                    case "blue":
    //                        holder.txtCount.setBackgroundResource(R.drawable.qmsnewblue);
    //                        break;
    //                    case "gray":
    //                        holder.txtCount.setBackgroundResource(R.drawable.qmsnewgray);
    //                        break;
    //                }
    //                //holder.txtIsNew.setImageResource(R.drawable.new_flag);
    //                holder.txtNick.setTextAppearance(getContext(), R.style.QmsNew);
    //                holder.txtCount.setTextAppearance(getContext(), R.style.QmsNew);
    //            } else {
    //                holder.txtAllCount.setText(user.Count);
    //                holder.txtCount.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    //                //holder.txtIsNew.setImageBitmap(null);
    //                holder.txtNick.setTextAppearance(getContext(), R.style.QmsOld);
    //                holder.txtCount.setTextAppearance(getContext(), R.style.QmsOld);
    //            }
    //            holder.checkbox.setChecked(user.isSelected());
    //            return convertView;
    //        }
    //
    //        private Context getContext() {
    //            return m_Inflater.getContext();
    //        }
    //
    //        public class ViewHolder {
    //            //ImageView txtIsNew;
    //            TextView txtNick;
    //            TextView txtDateTime;
    //            TextView txtCount;
    //            TextView txtAllCount;
    //            CheckBox checkbox;
    //        }
    //    }
}