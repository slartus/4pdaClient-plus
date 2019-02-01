package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 20.03.14.
 */

import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TopicsHistoryListFragment extends TopicsListFragment {
    public TopicsHistoryListFragment() {

        super();
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return TopicsHistoryTable.getTopicsHistory(listInfo);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.id == -1) return;
            Object o = getAdapter().getItem((int) info.id);
            if (o == null)
                return;
            final IListItem topic = (IListItem) o;

            final List<MenuListDialog> list = new ArrayList<>();
            list.add(new MenuListDialog(getString(R.string.delete_from_visited), new Runnable() {
                @Override
                public void run() {
                    TopicsHistoryTable.delete(topic.getId());
                    getMData().remove(topic);
                    getAdapter().notifyDataSetChanged();
                }
            }));
            ExtUrl.showContextDialog(getContext(), null, list);
        } catch (Throwable ex) {
            AppLog.e(ex);
        }

    }
}
