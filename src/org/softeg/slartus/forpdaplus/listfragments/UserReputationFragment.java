package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 23.04.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.ReputationEvent;
import org.softeg.slartus.forpdaapi.ReputationsApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.listtemplates.UserReputationBrickInfo;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.util.ArrayList;

public class UserReputationFragment extends BaseTaskListFragment {
    public static final String USER_ID_KEY = "USER_ID_KEY";
    public static final String USER_NICK_KEY = "USER_NICK_KEY";
    public static final String USER_FROM_KEY = "USER_FROM_KEY";
    protected ListInfo mListInfo = new ListInfo();

    public UserReputationFragment() {

        super();
    }

    protected ArrayList<ReputationEvent> mLoadResultList;

    public static void showActivity(Context context, CharSequence userId, Boolean from) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId.toString());
        if (from)
            args.putBoolean(USER_FROM_KEY, true);
        ListFragmentActivity.showListFragment(context, UserReputationBrickInfo.NAME, args);
    }

    public static void showActivity(Context context, CharSequence userId) {
        showActivity(context, userId, false);
    }

    private String getUserId() {
        return args.getString(USER_ID_KEY);
    }

    private String getUserNick() {
        return args.getString(USER_NICK_KEY, "");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        getActivity().openContextMenu(v);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ReputationEvent item = (ReputationEvent) getAdapter().getItem((int) info.id);

        menu.setHeaderTitle(item.getUser());

        if (!item.getSourceUrl().contains("forum/index.php?showuser=")) {
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
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : mData.size());
        mLoadResultList = ReputationsApi.loadReputation(Client.getInstance(),
                getUserId(),
                args.getBoolean(USER_FROM_KEY), mListInfo);
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        getActivity().setTitle(mListInfo.getTitle());
        if (mListInfo.getParams().containsKey("USER_REP") && getActivity().getActionBar() != null)
            getActivity().getActionBar().setSubtitle(mListInfo.getParams().get("USER_REP"));
        if (mListInfo.getParams().containsKey("USER_NICK"))
            args.putString(USER_NICK_KEY, mListInfo.getParams().get("USER_NICK"));
        if (isRefresh)
            mData.clear();
        for (ReputationEvent item : mLoadResultList) {
            mData.add(item);
        }

        mLoadResultList.clear();
    }

    @Override
    public void setCount() {
        int count = Math.max(mListInfo.getOutCount(), mData.size());
        mListViewLoadMoreFooter.setCount(mData.size(), count);
        mListViewLoadMoreFooter.setState(
                mData.size() == count ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
                        ListViewLoadMoreFooter.STATE_LOAD_MORE
        );
    }

    public void plusRep() {
        plusRep(getUserId(), getUserNick());
    }

    public void minusRep() {
        minusRep(getUserId(), getUserNick());
    }

    public void plusRep(String userId, String userNick) {
        plusRep(getActivity(), mHandler, "0", userId, userNick);
    }

    public void minusRep(String userId, String userNick) {
        minusRep(getActivity(), mHandler, "0", userId, userNick);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {

            MenuItem item;

            if (Client.getInstance().getLogined() && !getUserId().equals(Client.getInstance().UserId)) {


                item = menu.add("Повысить репутацию").setIcon(R.drawable.ic_menu_rating_good);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        plusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

                item = menu.add("Понизить репутацию").setIcon(R.drawable.ic_menu_rating_bad);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        minusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            item = menu.add("Профиль").setIcon(R.drawable.ic_menu_rating_good);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ProfileWebViewActivity.startActivity(getContext(), getUserId(), getUserNick());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } finally {
            super.onCreateOptionsMenu(menu, inflater);
        }


    }
}
