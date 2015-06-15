package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 18.03.14.
 */

import android.os.Bundle;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.digest.DigestApi;
import org.softeg.slartus.forpdaapi.digest.DigestCatalog;
import org.softeg.slartus.forpdaplus.Client;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class DigestTopicsListFragment extends TopicsListFragment {
    public DigestTopicsListFragment() {

        super();
    }
    public static String CATALOG_KEY = "CATALOG_KEY";

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return DigestApi.loadTopics(client, m_Catalog);
    }

    private DigestCatalog m_Catalog;

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
            getActivity().setTitle(m_Catalog.getTitle());
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {

    }

    @Override
    public void saveCache() throws Exception {

    }
}
