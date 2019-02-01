package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 13.03.14.
 */

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.devdb.DevModel;
import org.softeg.slartus.forpdaapi.devdb.NewDevDbApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.devdb.ParentFragment;
import org.softeg.slartus.forpdaplus.listfragments.adapters.DevDbModelsAdapter;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.util.ArrayList;
import java.util.List;

public class DevDbModelsFragment extends BaseTaskListFragment {
    public static final String BRAND_URL_KEY = "BRAND_URL_KEY";
    public static final String BRAND_TITLE_KEY = "BRAND_TITLE_KEY";
    private String m_BrandUrl;
    private String m_BrandTitle;

    protected ArrayList<DevModel> mData = new ArrayList<>();
    protected ArrayList<DevModel> mLoadResultList;

    public DevDbModelsFragment() {

        super();
    }

    //    public static DevDbModelsFragment newInstance(String brandUrl) {
//        DevDbModelsFragment fragment = new DevDbModelsFragment();
//        Bundle args = new Bundle();
//        args.putString(BRAND_URL_KEY, brandUrl);
//        fragment.setArguments(args);
//        return fragment;
//    }
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
        if (savedInstanceState != null) {
            m_BrandUrl = savedInstanceState.getString(BRAND_URL_KEY, m_BrandUrl);
            m_BrandTitle = savedInstanceState.getString(BRAND_TITLE_KEY, m_BrandTitle);
        }
        if (getArguments() != null) {
            m_BrandUrl = getArguments().getString(BRAND_URL_KEY, m_BrandUrl);
            m_BrandTitle = getArguments().getString(BRAND_TITLE_KEY, m_BrandTitle);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
        if (!TextUtils.isEmpty(m_BrandTitle))
            setTitle(m_BrandTitle.replaceAll(" \\(\\d*\\)", ""));
        getMainActivity().notifyTabAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
    }

    @Override
    public String getListTitle() {
        if (TextUtils.isEmpty(m_BrandTitle))
            return super.getListTitle();
        return m_BrandTitle;
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(BRAND_URL_KEY, m_BrandUrl);
        outState.putString(BRAND_TITLE_KEY, m_BrandTitle);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mLoadResultList = NewDevDbApi.parseModels(Client.getInstance(), m_BrandUrl);
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh)
            mData.clear();

        mData.addAll(mLoadResultList);

        mLoadResultList.clear();
    }

    protected BaseAdapter createAdapter() {
        return new DevDbModelsAdapter(getContext(), mData);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
        if (id < 0 || getAdapter().getCount() <= id) return;

        Object o = getAdapter().getItem((int) id);
        if (o == null)
            return;
        final IListItem topic = (IListItem) o;
        if (TextUtils.isEmpty(topic.getId())) return;

        //DevDbDeviceActivity.showDevice(getContext(), topic.getId().toString());
        ParentFragment.showDevice(topic.getId().toString(), topic.getMain().toString());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final IListItem topic = (IListItem) getAdapter().getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        List<MenuListDialog> list = new ArrayList<>();
        ExtUrl.addUrlMenu(getMHandler(), getContext(), list, topic.getId().toString(), topic.getMain().toString());
        ExtUrl.showContextDialog(getContext(), null, list);
    }
}
