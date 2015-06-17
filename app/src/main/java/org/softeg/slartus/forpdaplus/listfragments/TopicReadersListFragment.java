package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.OldUser;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.common.ExtColor;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.util.ArrayList;

/*
 * Created by slinkin on 17.06.2015.
 */
public class TopicReadersListFragment extends BaseLoaderListFragment {
    public static final String TOPIC_ID_KEY = "TOPIC_ID_KEY"    ;
    private String m_TopicId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            m_TopicId=savedInstanceState.getString(TOPIC_ID_KEY);
        }
        else if(getArguments()!=null){
            m_TopicId=getArguments().getString(TOPIC_ID_KEY);
        }
    }

    @Override
    protected Bundle getLoadArgs() {
        Bundle args=new Bundle();
        args.putString(TOPIC_ID_KEY,m_TopicId);
        return args;
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new UsersAdapter(getActivity(),getData().getItems());
    }

    @Override
    protected Boolean useCache() {
        return false;
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.list_fragment;
    }

    @Override
    protected ListData loadData(int loaderId, Bundle args) throws Throwable {
        Users users= Client.getInstance().getTopicReadingUsers(args.getString(TOPIC_ID_KEY));
        ListData data=new ListData();
        data.getItems().addAll(users);
        return data;
    }

    public static class UsersAdapter extends BaseAdapter {
        protected LayoutInflater m_Inflater;
        private ArrayList<IListItem> mUsers;

        public UsersAdapter(Context context, ArrayList<IListItem> users) {
            mUsers = users;
            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.topic_reader_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;

                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            OldUser user = (OldUser) this.getItem(position);


            holder.txtNick.setText(user.getNick());
            try {
                holder.txtNick.setTextColor(ExtColor.parseColor(user.getHtmlColor()));
            } catch (Exception ex) {
                AppLog.e(App.getContext(), new Exception("Не умею цвет: " + user.getHtmlColor()));
            }
            return convertView;

        }

        public class ViewHolder {
            TextView txtNick;
        }
    }
}
