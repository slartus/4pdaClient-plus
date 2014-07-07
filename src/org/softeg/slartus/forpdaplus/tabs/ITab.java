package org.softeg.slartus.forpdaplus.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.widget.ListView;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 16:32
 */
public interface ITab {

    Boolean onParentBackPressed();

    void refresh();

    Boolean cachable();

    String getTemplate();

    Boolean refreshed();

    ListView getListView();

    void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo, Handler handler);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    void onSaveInstanceState(Bundle outState);

    void onRestoreInstanceState(Bundle savedInstanceState);
}
