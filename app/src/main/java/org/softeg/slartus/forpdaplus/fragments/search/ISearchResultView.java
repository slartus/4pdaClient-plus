package org.softeg.slartus.forpdaplus.fragments.search;

import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 28.10.12
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public interface ISearchResultView {
    void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler);

    boolean dispatchKeyEvent(KeyEvent event);

    String getResultView();
    void search(String searchQuery);
}
