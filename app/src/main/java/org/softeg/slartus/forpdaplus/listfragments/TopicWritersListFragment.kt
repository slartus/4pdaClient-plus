package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.OldUser;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;

import java.util.ArrayList;

/*
 * Created by slinkin on 17.06.2015.
 */
public class TopicWritersListFragment extends BaseLoaderListFragment {
    public static final java.lang.String TOPIC_ID_KEY = "TOPIC_ID_KEY";
    private String m_TopicId;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            m_TopicId = savedInstanceState.getString(TOPIC_ID_KEY);
        } else if (getArguments() != null) {
            m_TopicId = getArguments().getString(TOPIC_ID_KEY);
        }
        setArrow();
    }

    @Override
    protected Bundle getLoadArgs() {
        Bundle args = new Bundle();
        args.putString(TOPIC_ID_KEY, m_TopicId);
        return args;
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new UsersAdapter(getActivity(), getData().getItems());
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
        Users users = Client.getInstance().getTopicWritersUsers(args.getString(TOPIC_ID_KEY));
        ListData data = new ListData();
        data.getItems().addAll(users);
        return data;
    }

    public class UsersAdapter extends BaseAdapter {
        protected LayoutInflater m_Inflater;
        private final ArrayList<IListItem> mUsers;

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
                convertView = m_Inflater.inflate(R.layout.topic_writer_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;
                holder.txtCount = convertView.findViewById(R.id.txtMessagesCount);
                holder.txtNick = convertView.findViewById(R.id.txtNick);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final OldUser user = (OldUser)this.getItem(position);

            holder.txtCount.setText(user.MessagesCount);
            holder.txtNick.setText(Html.fromHtml(user.getNick()));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openProfile(user);
                }
            });
            return convertView;
        }

        public class ViewHolder {
            TextView txtNick;
            TextView txtCount;
        }
    }
    public void openProfile(OldUser user){
        ProfileFragment.showProfile(user.getMid(), user.getMid());
    }
}
