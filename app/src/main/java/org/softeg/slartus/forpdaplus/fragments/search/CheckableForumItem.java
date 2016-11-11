package org.softeg.slartus.forpdaplus.fragments.search;/*
 * Created by slinkin on 24.04.2014.
 */

public class CheckableForumItem {
    public CheckableForumItem(String id, String title) {
        Id = id;
        Title = title;
    }

    public int level=0;
    public String Id;
    public String Title;
    public Boolean IsChecked=false;
}
