package org.softeg.slartus.forpdaplus.listfragments;

import android.view.KeyEvent;

public interface IBrickFragment {
    String getListName();
    String getListTitle();

    void loadData(final boolean isRefresh);

    void startLoad();

    boolean onBackPressed();
    boolean dispatchKeyEvent(KeyEvent event);
}
