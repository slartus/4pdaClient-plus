package org.softeg.slartus.forpdaplus.tabs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.QuickStartActivity;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaapi.users.Users;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 15.04.13
 * Time: 9:12
 * To change this template use File | Settings | File Templates.
 */
public class TopicWritersTab extends UsersTab {

    public static final String TEMPLATE = "TopicWritersTab";
    public static final String TITLE = "Пользователи";

    public TopicWritersTab(Context context, ITabParent tabParent) {
        super(context, tabParent);
    }

    private String m_Title;

    @Override
    public String getTitle() {
        return m_Title;
    }

    @Override
    protected Users loadUsers(Bundle extras) throws IOException {

        return Client.getInstance().getTopicWritersUsers(extras.getString(TOPIC_URL_KEY));

    }

    @Override
    public void onLoadComplete(Loader<Users> qmsUsersLoader, Users data) {
        super.onLoadComplete(qmsUsersLoader, data);
        m_Title = data == null ? null : data.getTag();
    }

    private static final String TOPIC_URL_KEY = "TopicUrlKey";

    public static void show(Context context, String topicId) {
        try {
            Intent intent = new Intent(context, QuickStartActivity.class);
            intent.putExtra("template", TEMPLATE);


            intent.putExtra(TOPIC_URL_KEY, topicId);

            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            AppLog.e(context, e);
        }
    }
}
