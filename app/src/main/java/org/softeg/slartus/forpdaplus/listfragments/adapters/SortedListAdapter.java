package org.softeg.slartus.forpdaplus.listfragments.adapters;/*
 * Created by slinkin on 03.04.2014.
 */

import android.content.Context;

import org.softeg.slartus.forpdaapi.IListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortedListAdapter extends ListAdapter {
    public SortedListAdapter(Context context, ArrayList<? extends IListItem> data, boolean showSubMain) {
        super(context, data, showSubMain);
    }

    public void sort(Comparator<? super IListItem> comparator){
        Collections.sort(mData, comparator);
        notifyDataSetChanged();
    }
}
