package org.softeg.slartus.forpdaplus.listfragments.next;


import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;


/*
 * Created by slinkin on 25.09.2014.
 */
public abstract class BrickFragmentBase extends GeneralFragment{


    public static final String NAME_KEY = "BrickFragmentBase.NAME_KEY";
    public static final String TITLE_KEY = "BrickFragmentBase.TITLE_KEY";
    public static final String NEED_LOGIN_KEY = "BrickFragmentBase.NEED_LOGIN_KEY";

    private String m_Title;
    private String m_Name;
    private Boolean m_NeedLogin = false;

    public Fragment setBrickInfo(BrickInfo listTemplate) {
        m_Title = listTemplate.getTitle();
        m_Name = listTemplate.getName();
        m_NeedLogin = listTemplate.getNeedLogin();
        return this;
    }

    /**
     * Заголовок списка
     */
    public String getListTitle() {
        return m_Title;
    }

    /**
     * Уникальный идентификатор списка
     */
    public String getListName() {
        return m_Name;
    }

    public Boolean needLogin() {
        return m_NeedLogin;
    }

    protected Bundle Args = new Bundle();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Args = getArguments();
        }
        if (savedInstanceState != null) {
            Args = savedInstanceState;
        }

        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name);
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title);
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(NAME_KEY, m_Name);
        outState.putString(TITLE_KEY, m_Title);
        outState.putBoolean(NEED_LOGIN_KEY, m_NeedLogin);

        super.onSaveInstanceState(outState);
    }

    public void reloadData() {

    }

}
