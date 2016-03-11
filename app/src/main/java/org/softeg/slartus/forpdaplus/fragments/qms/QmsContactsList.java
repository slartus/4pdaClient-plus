package org.softeg.slartus.forpdaplus.fragments.qms;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
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
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.qms.QmsUser;
import org.softeg.slartus.forpdaapi.qms.QmsUsers;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.MaterialImageLoading;
import org.softeg.slartus.forpdaplus.listfragments.BaseLoaderListFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (Preferences.Notifications.Qms.isReadDone())
            reloadData();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        registerForContextMenu(getListView());
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        Object o = getAdapter().getItem((int) info.id);
        if (o == null)
            return;
        final QmsUser qmsUser = (QmsUser) o;
        if (TextUtils.isEmpty(qmsUser.getId())) return;

        final List<MenuListDialog> list = new ArrayList<>();
        list.add(new MenuListDialog("Удалить", new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, String> additionalHeaders = new HashMap<>();
                            additionalHeaders.put("act", "qms-xhr");
                            additionalHeaders.put("action", "del-member");
                            additionalHeaders.put("del-mid", qmsUser.getId());
                            Client.getInstance().performPost("http://4pda.ru/forum/index.php", additionalHeaders);
                            reloadData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }));

        ExtUrl.showContextDialog(getContext(), null, list);
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
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                assert convertView != null;
                if(App.getInstance().getPreferences().getBoolean("isSquareAvarars",false)){
                    holder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatarSquare);
                }else {
                    holder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatar);
                }

                if (!mShowAvatars)
                    holder.imgAvatar.setVisibility(View.GONE);
                else
                    holder.imgAvatar.setVisibility(View.VISIBLE);

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
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsNew);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsNew);
                if (getContext() != null)
                    switch (App.getInstance().getPreferences().getString("mainAccentColor", "pink")) {
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
                holder.txtCount.setBackgroundColor(ContextCompat.getColor(App.getContext(), android.R.color.transparent));
                holder.txtNick.setTextAppearance(getContext(), R.style.QmsOld);
                holder.txtCount.setTextAppearance(getContext(), R.style.QmsOld);
            }

            if (user.getAvatarUrl() != null && mShowAvatars) {
                imageLoader.displayImage(user.getAvatarUrl(), new ImageViewAware(holder.imgAvatar, false));
            }
            return convertView;
        }

        private Context getContext() {
            return inflater.getContext();
        }

        public class ViewHolder {
            ImageView imgAvatar;
            TextView txtNick;
            TextView txtCount;
        }
    }

}
