package org.softeg.slartus.forpdaplus.fragments.qms;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUser;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.BaseLoaderListFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.util.ArrayList;

/**
 * Created by radiationx on 12.11.15.
 */
public class QmsContactsList extends BaseLoaderListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

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
    protected BaseAdapter createAdapter() {
        return new QmsContactsAdapter(getMainActivity(), getData().getItems(), ImageLoader.getInstance());
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.list_fragment;
    }

    @Override
    protected ListData loadData(int loaderId, Bundle args) throws Throwable {
        ListData listData = new ListData();
        ArrayList<QmsUser> users = QmsApi.getQmsSubscribers(Client.getInstance());
        listData.getItems().addAll(users);
        Client.getInstance().setQmsCount(QmsUsers.unreadMessageUsersCount(users));
        Client.getInstance().doOnMailListener();

        return listData;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        try {
            id = ListViewMethodsBridge.getItemId(getMainActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final QmsUser qmsUser = (QmsUser) o;
            //QmsContactThemesActivity.showThemes(getMainActivity(), qmsUser.getId(), qmsUser.getNick().toString());
            QmsContactThemes.showThemes(qmsUser.getId(), qmsUser.getNick().toString());

        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public class QmsContactsAdapter extends BaseAdapter {

        private ArrayList<IListItem> dataList;

        final LayoutInflater inflater;
        private ImageLoader imageLoader;
        private Boolean mShowAvatars;


        public QmsContactsAdapter(Context context, ArrayList<IListItem> dataList, ImageLoader imageLoader) {
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
            boolean pauseOnScroll = false; // or true
            boolean pauseOnFling = true; // or false
            PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling);
            ((ListView)parent.findViewById(android.R.id.list)).setOnScrollListener(listener);
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;
                //holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatar);
                holder.imgAvatarSquare = (ImageView) convertView.findViewById(R.id.imgAvatarSquare);
                if (!mShowAvatars) {
                    holder.imgAvatar.setVisibility(View.GONE);
                    holder.imgAvatarSquare.setVisibility(View.GONE);
                }
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
                holder.txtCount.setText(user.getNewMessagesCount());
            holder.txtNick.setText(user.getNick());

            if (!TextUtils.isEmpty(user.getNewMessagesCount())) {
                //holder.txtIsNew.setImageResource(R.drawable.new_flag);
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsNew);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsNew);
                if (getContext() != null)
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

            } else {
                //holder.txtIsNew.setImageBitmap(null);
                holder.txtCount.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsOld);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsOld);
            }

            if (user.getAvatarUrl() != null && mShowAvatars) {
                if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isSquareAvarars",false)){
                    holder.imgAvatar.setVisibility(View.GONE);
                    imageLoader.displayImage(user.getAvatarUrl(), holder.imgAvatarSquare, new ImageLoadingListener() {

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
                }else {
                    holder.imgAvatarSquare.setVisibility(View.GONE);
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

            }
            return convertView;
        }

        private Context getContext() {
            return inflater.getContext();
        }

        public class ViewHolder {
            //ImageView txtIsNew;
            ImageView imgAvatar;
            ImageView imgAvatarSquare;
            TextView txtNick;

            TextView txtCount;
        }
    }

}
