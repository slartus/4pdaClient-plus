package org.softeg.slartus.forpdaplus.listfragments.next;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;

/**
 * Created by slartus on 24.02.2015.
 */
public class ForumFragment extends Fragment implements
        IBrickFragment {
    public static final String NAME_KEY = "NAME_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String NEED_LOGIN_KEY = "NEED_LOGIN_KEY";

    private String m_Title;
    private String m_Name;
    private Boolean m_NeedLogin = false;

    /**
     * Заголовок списка
     */
    public String getListTitle() {
        return m_Title;
    }

    @Override
    public void loadData(boolean isRefresh) {

    }

    @Override
    public void startLoad() {

    }

    /**
     * Уникальный идентификатор списка
     */
    public String getListName() {
        return m_Name;
    }

    public Boolean needLogin() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public Fragment setBrickInfo(BrickInfo listTemplate) {
        m_Title = listTemplate.getTitle();
        m_Name = listTemplate.getName();
        m_NeedLogin = listTemplate.getNeedLogin();
        return this;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name);
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title);
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin);
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(NAME_KEY, m_Name);
        outState.putString(TITLE_KEY, m_Title);
        outState.putBoolean(NEED_LOGIN_KEY, m_NeedLogin);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }
}
