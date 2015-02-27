package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.TopicsApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listtemplates.ForumTopicsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.ForumTopicsPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.ListPreferencesActivity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

/*
 * Created by slinkin on 03.03.14.
 */
public class ForumTopicsListFragment extends TopicsListFragment {
    public ForumTopicsListFragment() {
        super();
    }

    @Override
    public void saveCache(){

    }

    @Override
    public void loadCache(){

    }

    @Override
    protected void sort(){

    }

    private String getForumId() {
        return getArguments().getString(ForumFragment.FORUM_ID_KEY);
    }

    private String getForumTitle() {
        return getArguments().getString(ForumFragment.FORUM_TITLE_KEY);
    }

    @Override
    public String getListName() {
        return super.getListName();
    }

    @Override
    public String getListTitle() {
        return getForumTitle();
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        return TopicsApi.getForumTopics(client, getForumId(),
                prefs.getString(getListName() + ".sort_key", "last_post"),
                prefs.getString(getListName() + ".sort_by", "Z-A"),
                prefs.getString(getListName() + ".prune_day", "100"),
                prefs.getString(getListName() + ".topicfilter", "all"),
                prefs.getBoolean(getListName() + ".unread_in_top", false),
                mListInfo);
    }

    public static void showForumTopicsList(Context context, CharSequence forumId, CharSequence forumTitle){
        Bundle args = new Bundle();
        args.putString(ForumFragment.FORUM_ID_KEY, forumId.toString());
        args.putString(ForumFragment.FORUM_TITLE_KEY, forumTitle.toString());
        ListFragmentActivity.showListFragment(context, new ForumTopicsBrickInfo().getName(), args);
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
                .setIcon(R.drawable.ic_menu_preferences)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
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
