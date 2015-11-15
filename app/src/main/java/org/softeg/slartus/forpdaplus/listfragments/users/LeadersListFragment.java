package org.softeg.slartus.forpdaplus.listfragments.users;/*
 * Created by slinkin on 10.04.2014.
 */

import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.MenuItem;
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
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.listfragments.BaseExpandableListFragment;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LeadersListFragment extends BaseExpandableListFragment {

    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mLoadResultList = new ArrayList<>();
        ArrayList<LeadUser> users = UsersApi.getLeaders(Client.getInstance());
        HashMap<String, ExpandableGroup> groups = new HashMap<>();
        for (LeadUser user : users) {
            if (!groups.containsKey(user.getGroup())) {
                groups.put(user.getGroup(), new ExpandableGroup(user.getGroup(), user.getGroup()));
                mLoadResultList.add(groups.get(user.getGroup()));
            }
            groups.get(user.getGroup()).getChildren().add(user);
        }
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        mData.clear();
        for (ExpandableGroup group : mLoadResultList) {
            mData.add(group);
        }
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

        // Show context menu for groups
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//            menu.setHeaderTitle("Group");
//            menu.add(0, 0, 1, "Delete");

            // Show context menu for children
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            Object o = getAdapter().getChild(groupPosition, childPosition);
            if (o == null) return;
            final LeadUser leadUser = ((LeadUser) o);


            menu.add("Список форумов")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (leadUser.isAllForumsOwner()) {
                                MainActivity.showListFragment(new ForumBrickInfo().getName(), null);
                            } else {
                                CharSequence[] forumTitles = new CharSequence[leadUser.getForums().size()];
                                int i = 0;
                                for (Forum f : leadUser.getForums()) {
                                    forumTitles[i++] = f.getTitle();
                                }
                                new MaterialDialog.Builder(getContext())
                                        .title("Форумы")
                                        .items(forumTitles)
                                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence forumTitles) {
                                                ForumTopicsListFragment.showForumTopicsList(getActivity(),
                                                        leadUser.getForums().get(i).getId(), leadUser.getForums().get(i).getTitle());
                                                return true; // allow selection
                                            }
                                        })
                                        .show();
                            }
                            return true;
                        }
                    });
            ForumUser.onCreateContextMenu(getContext(), menu, leadUser.getId().toString(), leadUser.getNick().toString());
        }

    }

    @Override
    public void saveCache() throws Exception {
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getWritableDatabase();
            BaseDao<LeadUser> baseDao = new BaseDao<>(App.getContext(), db, getListName(), LeadUser.class);
            baseDao.createTable(db);
            for (ExpandableGroup group : mData) {
                for (IListItem item : group.getChildren()) {
                    ((LeadUser) item).fillCacheFields();
                    baseDao.insert((LeadUser) item);
                }
            }
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {
        mCacheList.clear();
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getReadableDatabase();
            BaseDao<LeadUser> baseDao = new BaseDao<>(App.getContext(), db, getListName(), LeadUser.class);
            if (baseDao.isTableExists()) {
                HashMap<String, ExpandableGroup> groups = new HashMap<>();
                for (LeadUser leadUser : baseDao.getAll()) {
                    leadUser.fillFromCache();

                    if (!groups.containsKey(leadUser.getGroup())) {
                        groups.put(leadUser.getGroup(), new ExpandableGroup(leadUser.getGroup(), leadUser.getGroup()));
                        mCacheList.add(groups.get(leadUser.getGroup()));
                    }

                }
            }

        } finally {
            if (db != null)
                db.close();
        }
    }
}
