package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.core.util.Pair;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicsApi;
import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.URIUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.fragments.ForumFragment;
import org.softeg.slartus.forpdaplus.listtemplates.ForumTopicsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.ForumTopicsPreferencesActivity;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 03.03.14.
 */
public class ForumTopicsListFragment extends TopicsListFragment {
    public ForumTopicsListFragment() {
        super();
    }

    @Override
    public void saveCache() {

    }

    @Override
    public void loadCache() {

    }

    @Override
    protected void sort() {

    }

    private String getForumId() {
        return getArguments() == null ? null : getArguments().getString(ForumFragment.FORUM_ID_KEY);
    }

    private String getForumTitle() {
        return getArguments() == null ? null : getArguments().getString(ForumFragment.FORUM_TITLE_KEY);
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(URL_KEY, mUrl);
        } else {
            if (getArguments() != null) {
                if (getArguments().containsKey(ForumFragment.FORUM_TITLE_KEY)) {

                    setTitle(getArguments().getString(ForumFragment.FORUM_TITLE_KEY));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull android.os.Bundle outState) {
        outState.putString(URL_KEY, mUrl);

        super.onSaveInstanceState(outState);
    }

    private final String URL_KEY = "URL_KEY";
    private String mUrl = null;

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException {
        SharedPreferences prefs = App.getInstance().getPreferences();
        if (mUrl == null) {
            List<NameValuePair> qparams = new ArrayList<>();
            if (getForumId() != null)
                qparams.add(new BasicNameValuePair("showforum", getForumId()));
            qparams.add(new BasicNameValuePair("sort_key", prefs.getString(getListName() + ".sort_key", "last_post")));
            qparams.add(new BasicNameValuePair("sort_by", prefs.getString(getListName() + ".sort_by", "Z-A")));
            qparams.add(new BasicNameValuePair("prune_day", prefs.getString(getListName() + ".prune_day", "100")));
            qparams.add(new BasicNameValuePair("topicfilter", prefs.getString(getListName() + ".topicfilter", "all")));
            qparams.add(new BasicNameValuePair("st", Integer.toString(listInfo.getFrom())));


            mUrl = URIUtils.createURI("http", HostHelper.getHost(), "/forum/index.php",
                    qparams, "UTF-8");
        } else {
            mUrl = mUrl.replaceAll("&st=\\d+", "").concat("&st=" + mListInfo.getFrom());
        }
        Pair<String, ArrayList<Topic>> response = TopicsApi.getForumTopics(mUrl, getForumId(),
                prefs.getBoolean(getListName() + ".unread_in_top", false),
                mListInfo);
        ArrayList<Topic> res = response.second;
        mUrl = response.first;
        return res;
    }

    public static void showForumTopicsList(CharSequence forumId, CharSequence forumTitle) {
        Bundle args = new Bundle();
        args.putString(ForumFragment.FORUM_ID_KEY, forumId.toString());
        args.putString(ForumFragment.FORUM_TITLE_KEY, forumTitle.toString());
        MainActivity.showListFragment(forumId.toString(), new ForumTopicsBrickInfo().getName(), args);
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
            inflater.inflate(R.menu.forum_topics, menu);

    }
}
