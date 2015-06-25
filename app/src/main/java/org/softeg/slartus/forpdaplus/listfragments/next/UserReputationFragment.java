package org.softeg.slartus.forpdaplus.listfragments.next;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.ReputationEvent;
import org.softeg.slartus.forpdaapi.ReputationsApi;
import org.softeg.slartus.forpdaapi.classes.ListData;
import org.softeg.slartus.forpdaapi.classes.ReputationsListData;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listtemplates.UserReputationBrickInfo;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.util.ArrayList;

/*
 * Created by slinkin on 19.02.2015.
 */
public class UserReputationFragment extends BrickFragmentListBase {
    public static final String USER_ID_KEY = "USER_ID_KEY";
    public static final String USER_NICK_KEY = "USER_NICK_KEY";
    public static final String USER_FROM_KEY = "USER_FROM_KEY";

    public static void showActivity(Context context, CharSequence userId, Boolean from) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId.toString());
        if (from)
            args.putBoolean(USER_FROM_KEY, true);
        ListFragmentActivity.showListFragment(context, UserReputationBrickInfo.NAME, args);
    }


    private String getUserId() {
        return Args.getString(USER_ID_KEY);
    }

    private String getUserNick() {
        return Args.getString(USER_NICK_KEY, "");
    }

    @Override
    protected int getLoaderId() {
        return ItemsLoader.ID;
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new ListAdapter(getActivity(), getData().getItems());
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.list_fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getActivity().openContextMenu(view);

    }
    @Override
    public void onLoadFinished(Loader<ListData> loader, ListData data) {
        super.onLoadFinished(loader,data);
        if(data.getEx()==null){
            if(data instanceof ReputationsListData){
                ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(((ReputationsListData) data).getRep());
                Args.putString(USER_NICK_KEY, ((ReputationsListData) data).getUser());
            }
        }
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        addLoadMoreFooter(inflater.getContext());
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ReputationEvent item = (ReputationEvent) getAdapter().getItem((int) info.id);

        menu.setHeaderTitle(item.getUser());

        if (item.getSourceUrl()!=null&&!item.getSourceUrl().contains("forum/index.php?showuser=")) {
            menu.add("Перейти к сообщению")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            IntentActivity.tryShowUrl(getActivity(), new Handler(), item.getSourceUrl(), true, false);
                            return true;
                        }
                    });
        }

        ForumUser.onCreateContextMenu(getActivity(), menu, item.getUserId(), item.getUser());
    }


    @Override
    protected AsyncTaskLoader<ListData> createLoader(int id, Bundle args) {
        ItemsLoader loader = null;
        if (id == ItemsLoader.ID) {
            setLoading(true);
            loader = new ItemsLoader(getActivity(), args);

        }
        return loader;
    }

    private static final String START_KEY = "START_KEY";

    protected Bundle getLoadArgs() {
        Bundle args =Args;
        args.putInt(START_KEY, getData().getItems().size());

        return args;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {

            MenuItem item;

            if (Client.getInstance().getLogined() && !getUserId().equals(Client.getInstance().UserId)) {


                item = menu.add("Повысить репутацию").setIcon(R.drawable.ic_thumb_up_white_24dp);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        plusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

                item = menu.add("Понизить репутацию").setIcon(R.drawable.ic_thumb_down_white_24dp);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        minusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            item = menu.add("Профиль");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ProfileWebViewActivity.startActivity(getActivity(), getUserId(), getUserNick());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } finally {
            super.onCreateOptionsMenu(menu, inflater);
        }


    }

    private static class ItemsLoader extends AsyncTaskLoader<ListData> {
        public static final int ID = App.getInstance().getUniqueIntValue();
        ListData mApps;
        private Bundle args;

        public ItemsLoader(Context context, Bundle args) {
            super(context);

            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }


        @Override
        public ListData loadInBackground() {
            try {
                ListInfo listInfo = new ListInfo();
                listInfo.setFrom(getArgs().getBoolean(IS_REFRESH_KEY) ? 0 : getArgs().getInt(START_KEY));
                return ReputationsApi.loadReputation(Client.getInstance(),
                        args.getString(USER_ID_KEY),
                        args.getBoolean(USER_FROM_KEY), listInfo);
            } catch (Throwable e) {
                ListData forumPage = new ListData();
                forumPage.setEx(e);

                return forumPage;
            }

        }

        @Override
        public void deliverResult(ListData apps) {

            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
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
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                mApps = null;
            }
        }

    }

    private static class ListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        protected ArrayList<? extends IListItem> mData;


        public ListAdapter(Context context, ArrayList<? extends IListItem> data) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mData = data;

        }

        public void setData(ArrayList<? extends IListItem> data) {
            mData = data;

        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public android.view.View getView(int position, android.view.View view, android.view.ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_reputation, parent, false);
                holder = new ViewHolder();
                holder.Flag = (TextView) view.findViewById(R.id.imgFlag);
                holder.TopLeft = (TextView) view.findViewById(R.id.txtTopLeft);
                holder.TopRight = (TextView) view.findViewById(R.id.txtTopRight);
                holder.Main = (TextView) view.findViewById(R.id.txtMain);
                holder.SubMain = (TextView) view.findViewById(R.id.txtSubMain);
                holder.progress = view.findViewById(R.id.progressBar);
                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }
            IListItem topic = mData.get(position);
            holder.TopLeft.setText(topic.getTopLeft());
            holder.TopRight.setText(topic.getTopRight());
            holder.Main.setText(topic.getMain());
            holder.SubMain.setText(topic.getSubMain());
            setVisibility(holder.progress, topic.isInProgress() ? View.VISIBLE : View.INVISIBLE);
            switch (topic.getState()) {
                case IListItem.STATE_GREEN:
                    setVisibility(holder.Flag,View.VISIBLE);
                    holder.Flag.setText("+");
                    holder.Flag.setBackgroundResource(R.drawable.plusrep);
                    //holder.Flag.setImageResource(R.drawable.new_flag);
                    break;
                case IListItem.STATE_RED:
                    setVisibility(holder.Flag,View.VISIBLE);
                    holder.Flag.setBackgroundResource(R.drawable.minusrep);
                    holder.Flag.setText("-");
                    //holder.Flag.setImageResource(R.drawable.old_flag);
                    break;
                default:
                    setVisibility(holder.Flag,View.INVISIBLE);
                   // holder.Flag.setImageBitmap(null);
            }
            return view;
        }

        private void setVisibility(View v, int visibility) {
            if (v.getVisibility() != visibility)
                v.setVisibility(visibility);
        }

        class ViewHolder {
            TextView Flag;
            View progress;
            TextView TopLeft;
            TextView TopRight;
            TextView Main;
            TextView SubMain;
        }
    }

    public void plusRep() {
        plusRep(getUserId(), getUserNick());
    }

    public void minusRep() {
        minusRep(getUserId(), getUserNick());
    }

    public void plusRep(String userId, String userNick) {
        plusRep(getActivity(), new Handler(), "0", userId, userNick);
    }

    public void minusRep(String userId, String userNick) {
        minusRep(getActivity(), new Handler(), "0", userId, userNick);
    }

    public static void plusRep(Activity activity, Handler handler, String userId, String userNick) {
        plusRep(activity, handler, "0", userId, userNick);
    }

    public static void minusRep(Activity activity, Handler handler, String userId, String userNick) {
        minusRep(activity, handler, "0", userId, userNick);
    }

    public static void plusRep(Activity activity, Handler handler, String postId, String userId, String userNick) {
        showChangeRep(activity, handler, postId, userId, userNick, "add", "Поднять репутацию");
    }

    public static void minusRep(Activity activity, Handler handler, String postId, String userId, String userNick) {
        showChangeRep(activity, handler, postId, userId, userNick, "minus", "Опустить репутацию");
    }

    private static void showChangeRep(Activity activity, Handler handler, final String postId, String userId, String userNick, final String type, String title) {
        ForumUser.startChangeRep(activity, handler, userId, userNick, postId, type, title);
    }
}
