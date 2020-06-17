package org.softeg.slartus.forpdaplus.notes;

import android.text.TextUtils;
import android.util.Pair;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 21.02.13
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class Note {
    public String Id;
    public String Title;
    public String Body;
    public String Url;
    public String TopicId;
    public String PostId;
    public String UserId;
    public String User;
    public String Topic;
    public java.util.Date Date;

    public ArrayList<Pair> getLinks(){
        ArrayList<Pair> links=new ArrayList<Pair>();

        if (!TextUtils.isEmpty(Topic)) {
            links.add(new Pair(Topic, getTopicUrl()));
        }

        if (!TextUtils.isEmpty(User)) {
            links.add(new Pair(User,getUserUrl()));
        }

        if (!TextUtils.isEmpty(Url)) {
            links.add(new Pair(App.getContext().getString(R.string.link_to_post),Url));
        }

        return links;
    }

    public String getTopicLink() {
        return "<a href='https://4pda.ru/forum/index.php?showtopic=" + TopicId + "'>" + Topic + "</a>";
    }

    public String getTopicUrl() {
        return "https://4pda.ru/forum/index.php?showtopic=" + TopicId;
    }

    public String getUserLink() {
        return "<a href='https://4pda.ru/forum/index.php?showuser=" + UserId + "'>" + User + "</a>";
    }

    public String getUserUrl() {
        return "https://4pda.ru/forum/index.php?showuser=" + UserId;
    }

    public String getUrlLink() {
        return "<a href='" + Url + "'>"+App.getContext().getString(R.string.link)+"</a>";
    }
}
