package org.softeg.slartus.forpdaapi.users;/*
 * Created by slinkin on 10.04.2014.
 */

import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.sqliteannotations.Column;

import java.util.ArrayList;

public class LeadUser extends User {
    private final ArrayList<Forum> forums = new ArrayList<>();

    @Column(name = "groupName")
    private String group;

    @Column(name = "forumIds")
    private String forumIds;

    @Column(name = "forumTitles")
    private String forumTitles;

    public void fillFromCache() {
        forums.clear();
        String[] fIds = forumIds.split("¶");
        String[] fTitles = forumTitles.split("¶");

        for (int i = 0; i < fIds.length; i++) {
            if (TextUtils.isEmpty(fIds[i]) || TextUtils.isEmpty(fTitles[i]))
                continue;

            forums.add(new Forum(fIds[i], fTitles[i]));
        }
    }

    public void fillCacheFields() {
        forumIds = "";
        forumTitles = "";
        for (Forum f : forums) {
            forumIds += f.getId() + "¶";
            forumTitles += f.getId() + "¶";
        }
    }

    public LeadUser(CharSequence id, CharSequence nick) {
        super(id, nick);
    }

    public boolean isAllForumsOwner() {
        return forums.size() == 1 && "-1".equals(forums.get(0).getId());
    }

    public ArrayList<Forum> getForums() {
        return forums;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public CharSequence getSubMain() {
        return "Форумы: " + (isAllForumsOwner() ? "Все" : Integer.toString(forums.size()));
    }


}
