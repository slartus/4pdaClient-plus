package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicsApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listtemplates.ForumTopicsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.ForumTopicsPreferencesActivity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
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
        if (savedInstanceState != null)
            mUrl = savedInstanceState.getString(URL_KEY, mUrl);
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(URL_KEY, mUrl);

        super.onSaveInstanceState(outState);
    }

    private final String URL_KEY = "URL_KEY";
    private String mUrl = null;

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException {
        SharedPreferences prefs = App.getInstance().getPreferences();
        if (mUrl == null) {
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("showforum", getForumId()));
            qparams.add(new BasicNameValuePair("sort_key", prefs.getString(getListName() + ".sort_key", "last_post")));
            qparams.add(new BasicNameValuePair("sort_by", prefs.getString(getListName() + ".sort_by", "Z-A")));
            qparams.add(new BasicNameValuePair("prune_day", prefs.getString(getListName() + ".prune_day", "100")));
            qparams.add(new BasicNameValuePair("topicfilter", prefs.getString(getListName() + ".topicfilter", "all")));
            qparams.add(new BasicNameValuePair("st", Integer.toString(listInfo.getFrom())));


            URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                    URLEncodedUtils.format(qparams, "UTF-8"), null);
            mUrl = uri.toString();
        } else {
            mUrl = mUrl.replaceAll("&st=\\d+", "").concat("&st=" + mListInfo.getFrom());
        }
        ArrayList<Topic> res = TopicsApi.getForumTopics(client, mUrl,getForumId(),
                prefs.getBoolean(getListName() + ".unread_in_top", false),
                mListInfo);
        mUrl = Client.getInstance().getRedirectUri() != null ? Client.getInstance().getRedirectUri().toString() : mUrl;
        return res;
    }

    public static void showForumTopicsList(Context context, CharSequence forumId, CharSequence forumTitle) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(settingItemId);

        menu.add("Фильтр и сортировка")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent settingsActivity = new Intent(
                                getContext(), ForumTopicsPreferencesActivity.class);
                        settingsActivity.putExtra("listname", getListName());
                        startActivityForResult(settingsActivity, FILTER_SORT_REQUEST);
                        return true;
                    }
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
}
