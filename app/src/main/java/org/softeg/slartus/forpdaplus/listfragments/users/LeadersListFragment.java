package org.softeg.slartus.forpdaplus.listfragments.users;/*
 * Created by slinkin on 10.04.2014.
 */

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ExpandableListView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.users.LeadUser;
import org.softeg.slartus.forpdaapi.users.User;
import org.softeg.slartus.forpdaapi.users.UsersApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.listfragments.BaseExpandableListFragment;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

public class LeadersListFragment extends BaseExpandableListFragment {
    @Override
    public void onResume() {
        super.onResume();
        removeArrow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeArrow();
    }

    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mLoadResultList = new ArrayList<>();
        ArrayList<LeadUser> users = UsersApi.getLeaders(Client.getInstance());
        HashMap<String, ExpandableGroup> groups = new HashMap<>();
        for (LeadUser user : users) {
            if (!groups.containsKey(user.getGroup())) {
                groups.put(user.getGroup(), new ExpandableGroup(user.getGroup()));
                mLoadResultList.add(groups.get(user.getGroup()));
            }
            groups.get(user.getGroup()).getChildren().add(user);
        }
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        mData.clear();
        mData.addAll(mLoadResultList);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
                                long id) {
        Object o = getAdapter().getChild(groupPosition, childPosition);
        if (o == null) return false;
        ProfileFragment.showProfile(((User) o).getId().toString(), ((User) o).getId().toString());
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            Object o = getAdapter().getChild(groupPosition, childPosition);
            if (o == null) return;
            final LeadUser leadUser = ((LeadUser) o);

            final List<MenuListDialog> list = new ArrayList<>();
            list.add(new MenuListDialog(App.getInstance().getString(R.string.list_forums), () -> {
                if (leadUser.isAllForumsOwner()) {
                    MainActivity.showListFragment(new ForumBrickInfo().getName(), null);
                } else {
                    CharSequence[] forumTitles = new CharSequence[leadUser.getForums().size()];
                    int i = 0;
                    for (Forum f : leadUser.getForums()) {
                        forumTitles[i++] = f.getTitle();
                    }
                    Context context = getContext();
                    if (context != null)
                        new MaterialDialog.Builder(context)
                                .title(R.string.forums)
                                .items(forumTitles)
                                .itemsCallbackSingleChoice(-1, (dialog, view, i1, forumTitles1) -> {
                                    ForumTopicsListFragment.showForumTopicsList(
                                            leadUser.getForums().get(i1).getId(), leadUser.getForums().get(i1).getTitle());
                                    return true; // allow selection
                                })
                                .show();
                }
            }));
            ForumUser.onCreateContextMenu(getContext(), list, leadUser.getId().toString(), leadUser.getNick().toString());
            ExtUrl.showContextDialog(getContext(), null, list);
        }
    }

    @Override
    public void saveCache() {
        ArrayList<LeadUser> items = new ArrayList<>();
        for (ExpandableGroup group : mData) {
            for (IListItem item : group.getChildren()) {
                ((LeadUser) item).fillCacheFields();
                items.add((LeadUser) item);
            }
        }
        Paper.book().write(getListName(), items);
    }

    @Override
    public void loadCache() {
        mCacheList.clear();
        ArrayList<LeadUser> items = Paper.book().read(getListName(), new ArrayList<>());
        HashMap<String, ExpandableGroup> groups = new HashMap<>();
        for (LeadUser leadUser : items) {
            leadUser.fillFromCache();

            if (!groups.containsKey(leadUser.getGroup())) {
                groups.put(leadUser.getGroup(), new ExpandableGroup(leadUser.getGroup()));
                mCacheList.add(groups.get(leadUser.getGroup()));
            }

        }
    }
}
