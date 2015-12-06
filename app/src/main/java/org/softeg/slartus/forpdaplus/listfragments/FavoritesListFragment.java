package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.prefs.FavoritesPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.ForumTopicsPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.ListPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

/*
 * Created by slartus on 20.02.14.
 */
public class FavoritesListFragment extends TopicsListFragment {

    public FavoritesListFragment() {
        super();
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());

        return org.softeg.slartus.forpdaapi.TopicsApi.getFavTopics(Client.getInstance(),
                prefs.getString(getListName() + ".sort_key", "last_post"),
                prefs.getString(getListName() + ".sort_by", "Z-A"),
                prefs.getString(getListName() + ".prune_day", "100"),
                prefs.getString(getListName() + ".topicfilter", "all"),
                prefs.getBoolean(getListName() + ".unread_in_top", false),
                Preferences.List.Favorites.isLoadFullPagesList(),
                mListInfo);
    }

    @Override
    public void saveCache() throws Exception {
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getWritableDatabase();
            BaseDao<FavTopic> baseDao = new BaseDao<>(App.getContext(), db, getListName(), FavTopic.class);
            baseDao.createTable(db);
            for (IListItem item : mData) {
                FavTopic topic = (FavTopic) item;
                baseDao.insert(topic);
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
            BaseDao<FavTopic> baseDao = new BaseDao<>(App.getContext(), db, getListName(), FavTopic.class);
            if (baseDao.isTableExists())
                mCacheList.addAll(baseDao.getAll());
        } finally {
            if (db != null)
                db.close();
        }
        sort();
    }

    @Override
    protected void sort(){

    }

    private static final int FILTER_SORT_REQUEST = 0;

    @Override
    protected void showSettings() {
        Intent settingsActivity = new Intent(
                getContext(), ListPreferencesActivity.class);
        getContext().startActivity(settingsActivity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_SORT_REQUEST && resultCode == ForumTopicsPreferencesActivity.RESULT_OK) {
            loadData(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add("Фильтр и сортировка")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        //TODO надо передалать настройки по нормальному
                        Intent settingsActivity = new Intent(
                                getContext(), ForumTopicsPreferencesActivity.class);
                        settingsActivity.putExtra("listname", getListName());
                        startActivityForResult(settingsActivity, FILTER_SORT_REQUEST);
                        return true;
                    }
                });

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

}
