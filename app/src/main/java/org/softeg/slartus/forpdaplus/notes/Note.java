package org.softeg.slartus.forpdaplus.notes;

import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.annotations.SerializedName;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 21.02.13
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class Note implements IListItem {
    @SerializedName("_id")
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
        return "<a href='https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + TopicId + "'>" + Topic + "</a>";
    }

    public String getTopicUrl() {
        return "https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + TopicId;
    }

    public String getUserLink() {
        return "<a href='https://"+ HostHelper.getHost() +"/forum/index.php?showuser=" + UserId + "'>" + User + "</a>";
    }

    public String getUserUrl() {
        return "https://"+ HostHelper.getHost() +"/forum/index.php?showuser=" + UserId;
    }

    public String getUrlLink() {
        return "<a href='" + Url + "'>"+App.getContext().getString(R.string.link)+"</a>";
    }

    @Override
    public CharSequence getId() {
        return Id;
    }


    @Override
    public CharSequence getTopLeft() {
        return User;
    }

    @Override
    public CharSequence getTopRight() {
        return Functions.getForumDateTime(Date);
    }

    @Override
    public CharSequence getMain() {
        return Title;
    }

    @Override
    public CharSequence getSubMain() {
        return Body;
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
        return Functions.getForumDateTime(Date);
    }

    @Override
    public boolean isInProgress() {
        return false;
    }
}
