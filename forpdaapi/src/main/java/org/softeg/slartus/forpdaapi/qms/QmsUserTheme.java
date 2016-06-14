package org.softeg.slartus.forpdaapi.qms;

import org.softeg.slartus.forpdaapi.IListItem;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 04.02.13
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public class QmsUserTheme implements IListItem, Serializable {
    public String Id = "";
    public String Title = "";
    public String NewCount = "";
    public String Count = "";
    public String Date = "";
    private boolean selected;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public CharSequence getId() {
        return Id;
    }

    @Override
    public CharSequence getTopLeft() {
        return null;
    }

    @Override
    public CharSequence getTopRight() {
        return null;
    }

    @Override
    public CharSequence getMain() {
        return null;
    }

    @Override
    public CharSequence getSubMain() {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public CharSequence getSortOrder() {
        return null;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }
}
