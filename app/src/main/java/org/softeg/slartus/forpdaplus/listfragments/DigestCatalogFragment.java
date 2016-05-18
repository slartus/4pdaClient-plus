package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 18.03.14.
 */

import android.os.Bundle;
import android.widget.BaseAdapter;

import com.android.internal.util.Predicate;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaapi.digest.DigestApi;
import org.softeg.slartus.forpdaapi.digest.DigestCatalog;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.adapters.CatalogAdapter;
import org.softeg.slartus.forpdaplus.listtemplates.DigestTopicsListBrickInfo;

import java.util.ArrayList;

public class DigestCatalogFragment extends BaseCatalogFragment {

    protected ArrayList<DigestCatalog> mCatalogData = new ArrayList<>();
    protected ArrayList<DigestCatalog> mData = new ArrayList<>();
    protected ArrayList<DigestCatalog> mLoadResultList;

    protected Boolean isSavedInstanceStateEnabled() {
        return m_CurrentCatalogItem != null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            m_CurrentCatalogItem = (DigestCatalog) savedInstanceState.getParcelable("CurrentCatalogItem");
            m_LoadingCatalogItem = (DigestCatalog) savedInstanceState.getParcelable("LoadingCatalogItem");
            mData = savedInstanceState.getParcelableArrayList("Data");
            mCatalogData = savedInstanceState.getParcelableArrayList("CatalogData");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        afterDeliveryResult();

        if (m_CurrentCatalogItem != null)
            rebuildCrumbs(m_CurrentCatalogItem);
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putParcelable("CurrentCatalogItem", (DigestCatalog) m_CurrentCatalogItem);
        outState.putParcelable("LoadingCatalogItem", (DigestCatalog) m_LoadingCatalogItem);
        outState.putParcelableArrayList("Data", mData);
        outState.putParcelableArrayList("CatalogData", mCatalogData);

        super.onSaveInstanceState(outState);
    }

    protected BaseAdapter createAdapter() {
        return new CatalogAdapter(getContext(), mData);
    }

    public DigestCatalogFragment() {
        super();
        m_CurrentCatalogItem = new DigestCatalog("-1", App.getContext().getString(R.string.apps_and_games));
        m_LoadingCatalogItem = m_CurrentCatalogItem;
    }

    private ArrayList<DigestCatalog> getFilteredList(Predicate<DigestCatalog> predicate) {
        ArrayList<DigestCatalog> res = new ArrayList<>();
        for (DigestCatalog item : mCatalogData) {
            if (!predicate.apply(item)) continue;
            res.add(item);
        }
        return res;
    }

    @Override
    protected boolean inBackground(boolean isRefresh, final ICatalogItem catalogItem) throws Throwable {
        if (mCatalogData.size() == 0)
            mCatalogData = DigestApi.getCatalog(Client.getInstance(), (DigestCatalog) m_CurrentCatalogItem);

        if (((DigestCatalog) catalogItem).getLevel() == DigestCatalog.LEVEL_TOPICS_NEXT)
            return false;

        mLoadResultList = getFilteredList(new Predicate<DigestCatalog>() {
            @Override
            public boolean apply(DigestCatalog catalog) {
                return catalog.getParent() != null && catalog.getParent().getId().equals(catalogItem.getId());
            }
        });
        if (mCatalogData.size() > 0 && mLoadResultList.size() == 0)
            return false;
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        super.deliveryResult(isRefresh);
        mData.clear();
        for (DigestCatalog item : mLoadResultList) {
            mData.add(item);
        }

        mLoadResultList.clear();
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onFailureResult() {
        Bundle args = new Bundle();
        args.putParcelable(DigestTopicsListFragment.CATALOG_KEY, (DigestCatalog) m_LoadingCatalogItem);
        MainActivity.showListFragment(new DigestTopicsListBrickInfo().getName(), args);
    }
}
