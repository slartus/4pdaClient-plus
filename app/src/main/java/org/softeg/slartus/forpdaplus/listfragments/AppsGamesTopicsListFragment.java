package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 17.03.14.
 */

import android.os.Bundle;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppGameCatalog;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppsGamesCatalogApi;
import org.softeg.slartus.forpdaplus.Client;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class AppsGamesTopicsListFragment extends TopicsListFragment {

    public static String CATALOG_KEY = "CATALOG_KEY";

    public AppsGamesTopicsListFragment(){
        super();
    }
    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return AppsGamesCatalogApi.loadTopics(client, m_Catalog);
    }

    private AppGameCatalog m_Catalog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Catalog = null;
        if (getArguments() != null) {
            m_Catalog = getArguments().getParcelable(CATALOG_KEY);
        }
        if (savedInstanceState != null) {
            m_Catalog = savedInstanceState.getParcelable(CATALOG_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (m_Catalog != null)
            outState.putParcelable(CATALOG_KEY, m_Catalog);


        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (m_Catalog != null)
            setTitle(m_Catalog.getTitle());
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {

    }

    @Override
    public void saveCache() throws Exception {

    }
}