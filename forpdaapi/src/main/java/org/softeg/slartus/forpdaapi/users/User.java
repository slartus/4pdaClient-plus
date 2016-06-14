package org.softeg.slartus.forpdaapi.users;/*
 * Created by slinkin on 10.04.2014.
 */

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.sqliteannotations.Column;

public class User implements IListItem {
    @Column(name = "_id", isPrimaryKey = true)
    protected CharSequence id;
    @Column(name = "nick")
    protected CharSequence nick;

    public User(CharSequence id, CharSequence nick) {

        this.id = id;
        this.nick = nick;
    }

    public CharSequence getId() {
        return id;
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
        return getNick();
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

    public CharSequence getNick() {
        return nick;
    }
}
