package org.softeg.slartus.forpdaapi;

import org.softeg.sqliteannotations.Column;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by slinkin on 17.01.14.
 */
public class AppItem implements IListItem {
    public static final int STATE_FINDED = 0;
    public static final int STATE_FINDED_AND_HAS_UPDATE = 1;
    public static final int STATE_UNFINDED = 2;

    @Column(name = "_id", isPrimaryKey = true)
    public String Uid;

    @Column(name = "topic_id")
    private CharSequence m_Id;

    @Column(name = "title")
    private CharSequence m_Title;

    @Column(name = "description")
    private CharSequence description;

    @Column(name = "packageName")
    private CharSequence packageName;

    @Column(name = "state", type = "INTEGER")
    private int state = STATE_UNFINDED;

    public ArrayList<CharSequence> Ids = new ArrayList<>();

    @Column(name = "type")
    private CharSequence type;

    public AppItem() {
        Uid = UUID.randomUUID().toString();
    }

    public AppItem(CharSequence id, CharSequence title) {
        this();
        m_Id = id;
        m_Title = title;
    }

    public CharSequence getTitle() {
        return m_Title;
    }

    @Override
    public CharSequence getId() {
        return m_Id;
    }

    public void setId(CharSequence id) {
        m_Id = id;
    }

    @Override
    public CharSequence getTopLeft() {
        return "";
    }

    @Override
    public CharSequence getTopRight() {
        return type;
    }

    @Override
    public CharSequence getMain() {
        return getTitle();
    }

    @Override
    public CharSequence getSubMain() {
        return getDescription();
    }

    @Override
    public int getState() {
        switch (state) {
            case STATE_FINDED:
                return STATE_NORMAL;
            case STATE_FINDED_AND_HAS_UPDATE:
                return STATE_GREEN;
            case STATE_UNFINDED:
                return STATE_RED;
        }
        return STATE_NORMAL;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public CharSequence getSortOrder() {
        return null;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public int getFindedState() {
        return state;
    }

    public void setFindedState(int state) {
        this.state = state;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }


    public CharSequence getType() {
        return type;
    }

    public void setType(CharSequence type) {
        this.type = type;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }
}
