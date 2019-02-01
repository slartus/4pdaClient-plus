package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 10.04.2014.
 */

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.users.User;
import org.softeg.slartus.forpdaplus.Client;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class UsersListFragment extends BaseTaskListFragment {
    protected ListInfo mListInfo = new ListInfo();
    public UsersListFragment() {

        super();
    }
    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : getMData().size());
        mLoadResultList = loadUsers(Client.getInstance(), mListInfo);
        return true;
    }

    protected abstract ArrayList<? extends User> loadUsers(Client client, ListInfo listInfo) throws IOException, ParseException;

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh)
            getMData().clear();
        for (IListItem item : mLoadResultList) {
            getMData().add(item);
        }

        mLoadResultList.clear();

        Collections.sort(getMData(), getComparator());
    }

    private Comparator<? super IListItem> getComparator(){
        return new Comparator<IListItem>() {
            @Override
            public int compare(IListItem listItem1, IListItem listItem2) {
                return 0;
            }
        };
    }
}
