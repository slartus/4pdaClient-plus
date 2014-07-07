package org.softeg.slartus.forpdaplus.search.ui;/*
 * Created by slinkin on 29.04.2014.
 */


import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.View;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.search.SearchApi;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment;
import org.softeg.slartus.forpdaplus.search.ISearchResultView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

public class SearchTopicsResultsFragment extends TopicsListFragment
        implements ISearchResultView {
    private static final String SEARCH_URL_KEY = "SEARCH_URL_KEY";

    public static SearchTopicsResultsFragment newFragment(CharSequence searchUrl) {
        SearchTopicsResultsFragment fragment = new SearchTopicsResultsFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchUrl.toString());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException {
        return SearchApi.getSearchTopicsResult(client,args.getString(SEARCH_URL_KEY),mListInfo);
    }

    @Override
    public void saveCache(){

    }

    @Override
    public void loadCache(){

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler) {

    }

    @Override
    public String getResultView() {
        return SearchSettings.RESULT_VIEW_TOPICS;
    }

    @Override
    public void search(String searchQuery) {
        if(args==null)
            args=new Bundle();
        args.putString(SEARCH_URL_KEY,searchQuery);
        loadData(true);
    }
}
