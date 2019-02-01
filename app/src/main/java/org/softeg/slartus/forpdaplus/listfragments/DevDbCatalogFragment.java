package org.softeg.slartus.forpdaplus.listfragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaapi.devdb.DevCatalog;
import org.softeg.slartus.forpdaapi.devdb.NewDevDbApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.listfragments.adapters.DevDbAdapter;
import org.softeg.slartus.forpdaplus.listtemplates.DevDbModelsBrickInfo;

import java.util.ArrayList;
import java.util.List;

public class DevDbCatalogFragment extends BaseCatalogFragment {

    protected ArrayList<DevCatalog> mData = new ArrayList<>();
    protected ArrayList<DevCatalog> mLoadResultList;
    public static final String URL_KEY = "URL_KEY";

    public DevDbCatalogFragment() {
        super();
        m_CurrentCatalogItem = new DevCatalog("-1", App.getContext().getString(R.string.catalog)).setType(DevCatalog.ROOT);
    }

    private String m_Url;
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
        m_Url = null;
        if (getArguments() != null) {
            m_Url = getArguments().getString(URL_KEY, null);
        }
        if (savedInstanceState != null) {
            m_CurrentCatalogItem = (DevCatalog) savedInstanceState.getParcelable("CurrentCatalogItem");
            m_Url = savedInstanceState.getString(URL_KEY, m_Url);
            mData = savedInstanceState.getParcelableArrayList("Data");
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
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (m_CurrentCatalogItem != null)
            outState.putString(URL_KEY, m_CurrentCatalogItem.getId().toString());
        outState.putParcelable("CurrentCatalogItem", (DevCatalog) m_CurrentCatalogItem);
        outState.putParcelableArrayList("Data", mData);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean inBackground(boolean isRefresh, ICatalogItem catalogItem) throws Throwable {
        if (!TextUtils.isEmpty(m_Url)) {
            catalogItem = NewDevDbApi.getCatalog(m_Url);
            m_Url = null;
            m_LoadingCatalogItem = catalogItem;
        }
        if (catalogItem == null)
            return false;
        switch (((DevCatalog) catalogItem).getType()) {
            case DevCatalog.ROOT:
                mLoadResultList = NewDevDbApi.getStandartDevicesTypes();
                for (DevCatalog c : mLoadResultList) {
                    c.setParent(catalogItem);
                }
                break;
            case DevCatalog.DEVICE_TYPE:
                mLoadResultList = NewDevDbApi.parseBrands(Client.getInstance(), catalogItem.getId().toString());
                for (DevCatalog c : mLoadResultList) {
                    c.setParent(catalogItem);
                }
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onFailureResult() {
        Bundle args = new Bundle();
        args.putString(DevDbModelsFragment.BRAND_URL_KEY, m_LoadingCatalogItem.getId().toString());
        args.putString(DevDbModelsFragment.BRAND_TITLE_KEY, m_LoadingCatalogItem.getTitle().toString());
        MainActivity.showListFragment(m_LoadingCatalogItem.getId().toString(), new DevDbModelsBrickInfo().getName(), args);
    }

    protected BaseAdapter createAdapter() {
        return new DevDbAdapter(getContext(), mData);
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {

        super.deliveryResult(isRefresh);
        mData.clear();
        for (DevCatalog item : mLoadResultList) {
            mData.add(item);
        }
        mLoadResultList.clear();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ICatalogItem topic = (ICatalogItem) getAdapter().getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        List<MenuListDialog> list = new ArrayList<>();
        ExtUrl.addUrlMenu(getMHandler(), getContext(), list, topic.getId().toString(), topic.getTitle().toString());
        ExtUrl.showContextDialog(getContext(), null, list);
    }
}
