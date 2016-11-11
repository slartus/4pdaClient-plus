package org.softeg.slartus.forpdaplus.listfragments;

import android.view.KeyEvent;

/*
 * Created by slinkin on 21.02.14.
 */
public interface IBrickFragment {
    String getListName();
    String getListTitle();

    void loadData(final boolean isRefresh);

    void startLoad();

    boolean onBackPressed();
    boolean dispatchKeyEvent(KeyEvent event);
}
