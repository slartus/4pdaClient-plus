package org.softeg.slartus.forpdaplus.fragments.search;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AbsListView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.search.SearchApi;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by radiationx on 15.11.15.
 */
public class SearchTopicsFragment extends TopicsListFragment
        implements ISearchResultView, AbsListView.OnScrollListener {
    private static final String SEARCH_URL_KEY = "SEARCH_URL_KEY";


    public static SearchTopicsFragment newFragment(CharSequence searchUrl) {
        SearchTopicsFragment fragment = new SearchTopicsFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchUrl.toString());
        fragment.setArguments(args);
        return fragment;
    }

    protected int getViewId(){
        return R.layout.list_translucent_fragment;
    }

    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        getListView().setOnScrollListener(this);
        return view;
    }

//    @Override
//    protected View getListViewHeader(){
//        LayoutInflater inflater=getLayoutInflater(null);
//        inflater.inflate()
//        return ;
//    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException {
        return SearchApi.INSTANCE.getSearchTopicsResult(client, args.getString(SEARCH_URL_KEY), mListInfo);
    }

    @Override
    public void saveCache() {

    }

    @Override
    public void loadCache() {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public String getResultView() {
        return SearchSettings.RESULT_VIEW_TOPICS;
    }

    @Override
    public void search(String searchQuery) {
        if (args == null)
            args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchQuery);


        loadData(true);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {

    }

    private int m_PrevVisible = 0;

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        if (firstVisibleItem < m_PrevVisible || firstVisibleItem == 0)
//            getMainActivity().getActionBar().show();
//        else if (firstVisibleItem > m_PrevVisible) {
//            getMainActivity().getActionBar().hide();
//        }
        m_PrevVisible = firstVisibleItem;
    }
    @Override
    public void onPause() {
        super.onPause();
        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.searchSettings = SearchSettings.parse(args.getString(SEARCH_URL_KEY));
        setArrow();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }
}
