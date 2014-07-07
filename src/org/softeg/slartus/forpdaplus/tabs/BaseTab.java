package org.softeg.slartus.forpdaplus.tabs;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.LinearLayout;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 21.10.12
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseTab extends LinearLayout implements ITab {
    private ITabParent m_TabParent;

    public BaseTab(Context context, ITabParent tabParent) {
        super(context);
        m_TabParent = tabParent;
    }

    @Override
    public Boolean cachable() {
        return false;
    }

    public ITabParent getTabParent() {
        return m_TabParent;
    }

    private OnTabTitleChangedListener m_OnTabTitleChangedListener;

    public abstract String getTemplate();

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public abstract void loadCache();


    public interface OnTabTitleChangedListener {
        void onTabTitleChanged(String title);
    }

    public void setTitle(String title) {
        if (m_OnTabTitleChangedListener != null) {
            m_OnTabTitleChangedListener.onTabTitleChanged(title);
        }
    }

    public void setOnTabTitleChangedListener(OnTabTitleChangedListener p) {
        m_OnTabTitleChangedListener = p;
    }

    public Boolean refreshable() {
        return true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


    }

    public abstract void refresh(Bundle extras);

    public abstract String getTitle();

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    protected Activity getActivity() {
        if (!(getContext() instanceof Activity))
            return null;
        return (Activity) getContext();
    }
}
