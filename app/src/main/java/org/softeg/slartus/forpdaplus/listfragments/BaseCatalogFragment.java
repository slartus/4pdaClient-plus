package org.softeg.slartus.forpdaplus.listfragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.adapters.CatalogAdapter;
import org.softeg.slartus.forpdaplus.listfragments.adapters.CatalogCrumbsAdapter;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public abstract class BaseCatalogFragment extends BaseTaskListFragment {
    public BaseCatalogFragment() {
        super();
    }

    protected ICatalogItem m_CurrentCatalogItem;
    protected ICatalogItem m_LoadingCatalogItem;
    private Spinner m_FooterSpinner;
    private CatalogCrumbsAdapter m_CatalogCrumbsAdapter;
    protected Crumbs m_Crumbs = new Crumbs();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        m_FooterSpinner = new Spinner(view.getContext());
        m_CatalogCrumbsAdapter = new CatalogCrumbsAdapter(view.getContext(), m_Crumbs);
        m_FooterSpinner.setAdapter(m_CatalogCrumbsAdapter);
        m_FooterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                m_LoadingCatalogItem = (ICatalogItem) m_FooterSpinner.getSelectedItem();
                assert m_LoadingCatalogItem != null;
                if (m_LoadingCatalogItem.getId().equals(m_CurrentCatalogItem.getId())) return;
                loadData(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getListView().addHeaderView(m_FooterSpinner);
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new CatalogAdapter(getActivity(), new ArrayList<ICatalogItem>());
    }

    @Override
    protected final boolean inBackground(boolean isRefresh) throws IOException, ParseException {
        return false;
    }

    @Override
    protected Task createTask(Boolean isRefresh) {
        if (isRefresh)
            m_LoadingCatalogItem = m_CurrentCatalogItem == null ? null : m_CurrentCatalogItem.clone();
        return new CatalogTask(isRefresh, m_LoadingCatalogItem);
    }

    @Override
    public boolean onBackPressed() {
        if (m_Crumbs.size() < 2)
            return false;
        m_LoadingCatalogItem = m_Crumbs.get(m_Crumbs.size() - 2).clone();

        loadData(false);
        return true;
    }

    public void onCatalogItemClick(ICatalogItem catalogItem) {
        m_LoadingCatalogItem = catalogItem == null ? null : catalogItem.clone();
        loadData(false);
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        m_CurrentCatalogItem = m_LoadingCatalogItem == null ? null : m_LoadingCatalogItem.clone();

        rebuildCrumbs(m_CurrentCatalogItem);

    }

    protected void rebuildCrumbs(ICatalogItem catalogItem) {
        assert catalogItem != null;

        m_Crumbs.rebuildCrumbs(catalogItem.clone());
        if (m_CatalogCrumbsAdapter != null)
            m_CatalogCrumbsAdapter.notifyDataSetChanged();
        if (m_FooterSpinner != null)
            m_FooterSpinner.setSelection(m_Crumbs.size() - 1);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        try {
            id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final ICatalogItem item = (ICatalogItem) o;
            if (TextUtils.isEmpty(item.getId())) return;
            onCatalogItemClick(item);
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    protected int getCatalogIndexById(ArrayList<? extends ICatalogItem> collection, ICatalogItem catalogItem) {
        for (int i = 0; i < collection.size(); i++) {
            ICatalogItem item = collection.get(i);
            if (item.getId().equals(catalogItem.getId()))
                return i;
        }
        return -1;
    }

    protected abstract boolean inBackground(boolean isRefresh, ICatalogItem catalogItem) throws Throwable;

    public class CatalogTask extends Task {

        private final ICatalogItem mCatalogItem;

        public CatalogTask(Boolean refresh, ICatalogItem catalogItem) {
            super(refresh);
            mCatalogItem = catalogItem;
        }

        @Override
        protected Boolean doInBackground(Boolean[] p1) {
            try {
                return inBackground(mRefresh, mCatalogItem);
            } catch (Throwable e) {
                mEx = e;
            }
            return false;
        }
    }

    public class Crumbs extends ArrayList<ICatalogItem> {
        public void rebuildCrumbs(ICatalogItem catalogItem) {
            clear();
            while (catalogItem != null) {
                add(0, catalogItem);
                catalogItem = catalogItem.getParent();
            }
        }
    }
}
