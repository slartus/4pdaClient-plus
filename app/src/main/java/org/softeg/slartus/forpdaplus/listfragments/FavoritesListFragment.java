package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.ForumTopicsPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;

import java.util.ArrayList;

/*
 * Created by slartus on 20.02.14.
 */
public class FavoritesListFragment extends TopicsListFragment {

    public FavoritesListFragment() {
        super();
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException {
        SharedPreferences prefs = App.getInstance().getPreferences();

        return org.softeg.slartus.forpdaapi.TopicsApi.getFavTopics(
                prefs.getString(getListName() + ".sort_key", "last_post"),
                prefs.getString(getListName() + ".sort_by", "Z-A"),
                prefs.getString(getListName() + ".prune_day", "100"),
                prefs.getString(getListName() + ".topicfilter", "all"),
                prefs.getBoolean(getListName() + ".unread_in_top", false),
                Preferences.List.Favorites.isLoadFullPagesList(),
                mListInfo);
    }

    @Override
    public void loadCache() {
        super.loadCache();
        sort();
    }

    @Override
    protected void sort() {

    }

    private static final int FILTER_SORT_REQUEST = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_SORT_REQUEST && resultCode == ForumTopicsPreferencesActivity.RESULT_OK) {
            loadData(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter_and_ordering_item) {
            Intent settingsActivity = new Intent(
                    getContext(), ForumTopicsPreferencesActivity.class);
            settingsActivity.putExtra("listname", getListName());
            startActivityForResult(settingsActivity, FILTER_SORT_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.list_settings_item) != null)
            menu.findItem(R.id.list_settings_item).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (inflater != null && menu.findItem(R.id.filter_and_ordering_item) == null)
            inflater.inflate(R.menu.favorites, menu);
    }

}
