package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 17.03.14.
 */

import android.os.Bundle;
import android.widget.BaseAdapter;


import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppGameCatalog;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppsGamesCatalogApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.adapters.CatalogAdapter;
import org.softeg.slartus.forpdaplus.listtemplates.AppsGamesTopicsBrickInfo;

import java.util.ArrayList;

public class AppsGamesCatalogFragment extends BaseCatalogFragment {

    protected ArrayList<AppGameCatalog> mCatalogData = new ArrayList<>();
    protected ArrayList<AppGameCatalog> mData = new ArrayList<>();

    protected ArrayList<AppGameCatalog> mLoadResultList;

    protected BaseAdapter createAdapter() {
        return new CatalogAdapter(getContext(), mData);
    }

    public AppsGamesCatalogFragment() {
        super();
        m_CurrentCatalogItem = new AppGameCatalog("-1", App.getContext().getString(R.string.apps_and_games));
        m_LoadingCatalogItem = m_CurrentCatalogItem;
    }

    private ArrayList<AppGameCatalog> getFilteredList(Predicate<AppGameCatalog> predicate) {
        ArrayList<AppGameCatalog> res = new ArrayList<>();
        for (AppGameCatalog item : mCatalogData) {
            if (!predicate.apply(item)) continue;
            res.add(item);
        }
        return res;
    }

    /*
   Поддерживает ли загрузку состояния
    */
    protected Boolean isSavedInstanceStateEnabled() {
        return m_CurrentCatalogItem != null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeArrow();
        if (savedInstanceState != null) {
            m_CurrentCatalogItem = (AppGameCatalog) savedInstanceState.getParcelable("CurrentCatalogItem");
            m_LoadingCatalogItem = (AppGameCatalog) savedInstanceState.getParcelable("LoadingCatalogItem");
            mData = savedInstanceState.getParcelableArrayList("Data");
            mCatalogData = savedInstanceState.getParcelableArrayList("CatalogData");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        removeArrow();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        afterDeliveryResult();

        if (m_CurrentCatalogItem != null)
            rebuildCrumbs(m_CurrentCatalogItem);
    }

    @Override
    public void onSaveInstanceState(@NotNull android.os.Bundle outState) {
        outState.putParcelable("CurrentCatalogItem", (AppGameCatalog) m_CurrentCatalogItem);
        outState.putParcelable("LoadingCatalogItem", (AppGameCatalog) m_LoadingCatalogItem);
        outState.putParcelableArrayList("Data", mData);
        outState.putParcelableArrayList("CatalogData", mCatalogData);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean inBackground(boolean isRefresh, final ICatalogItem catalogItem) throws Throwable {
        if (mCatalogData.size() == 0)
            mCatalogData = AppsGamesCatalogApi.getCatalog(Client.getInstance(), (AppGameCatalog) m_CurrentCatalogItem);


        if (catalogItem.getParent() != null && catalogItem.getId().equals(catalogItem.getParent().getId()))
            return false;
        mLoadResultList = getFilteredList(catalog -> catalog.getParent() != null && catalog.getParent().getId().equals(catalogItem.getId()));
        return !(mCatalogData.size() > 0 && mLoadResultList.size() == 0);
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        super.deliveryResult(isRefresh);
        mData.clear();
        mData.addAll(mLoadResultList);

        mLoadResultList.clear();
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onFailureResult() {
        Bundle args = new Bundle();
        args.putParcelable(AppsGamesTopicsListFragment.CATALOG_KEY, (AppGameCatalog) m_LoadingCatalogItem);
        MainActivity.showListFragment(new AppsGamesTopicsBrickInfo().getName(), args);
    }


    @Override
    public void loadCache()  {

    }

    @Override
    public void saveCache(){

    }

    @Override
    protected void deliveryCache() {

    }
}
