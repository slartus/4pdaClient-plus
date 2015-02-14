package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 20.03.14.
 */

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class TopicsHistoryListFragment extends TopicsListFragment {
    public TopicsHistoryListFragment() {

        super();
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return TopicsHistoryTable.getTopicsHistory(listInfo);
    }


}
