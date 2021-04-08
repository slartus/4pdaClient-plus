package org.softeg.slartus.forpdaapi;

/**
 * Created by slinkin on 17.01.14.
 */
public interface IListItem {
    int STATE_NORMAL = 0;
    int STATE_GREEN = 1;
    int STATE_RED = 2;

    CharSequence getId();

    CharSequence getTopLeft();

    CharSequence getTopRight();

    CharSequence getMain();

    CharSequence getSubMain();

    int getState();

    void setState(int state);

    CharSequence getSortOrder();

    boolean isInProgress();
}
