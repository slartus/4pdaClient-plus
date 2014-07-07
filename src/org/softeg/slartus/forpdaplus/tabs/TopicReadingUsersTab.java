package org.softeg.slartus.forpdaplus.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.QuickStartActivity;
import org.softeg.slartus.forpdaapi.TopicReadingUsers;
import org.softeg.slartus.forpdaapi.users.Users;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 27.03.13
 * Time: 15:36
 * To change this template use File | Settings | File Templates.
 */
public class TopicReadingUsersTab extends UsersTab {

    public static final String TEMPLATE = "TopicReadingUsersTab";
    public static final String TITLE = "Пользователи";

    public TopicReadingUsersTab(Context context, ITabParent tabParent) {
        super(context, tabParent);
    }

    @Override
    protected UsersAdapter createAdapter() {
        return new UsersAdapter(getContext(), new Users(),false);
    }

    @Override
    protected TopicReadingUsers loadUsers(Bundle extras) throws IOException {

        return Client.getInstance().getTopicReadingUsers(extras.getString(TOPIC_URL_KEY));

    }

    @Override
    public void onLoadComplete(Loader<Users> qmsUsersLoader, Users data) {
        super.onLoadComplete(qmsUsersLoader, data);

        if (data == null) {
            setTitle(String.format("Гостей: %d, Скрытых: %d", 0, 0));
            return;
        }
        TopicReadingUsers topicReadingUsers = (TopicReadingUsers) data;
        setTitle(String.format("Гостей: %s, Скрытых: %s",
                topicReadingUsers.getGuestsCount(), topicReadingUsers.getHideCount()));
    }

    private static final String TOPIC_URL_KEY = "TopicUrlKey";

    public static void show(Context context, String topicId) {

        Intent intent = new Intent(context, QuickStartActivity.class);
        intent.putExtra("template", TEMPLATE);


        intent.putExtra(TOPIC_URL_KEY, topicId);

        context.startActivity(intent);
    }

}
