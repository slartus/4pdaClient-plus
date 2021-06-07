package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 10:44
 */
public class ForumUser {

    public interface InsertNickInterface {
        void insert(String text);
    }

    public static void showUserQuickAction(final FragmentActivity context, final String userId,
                                           String userNick) {
        showUserQuickAction(context, null, "", userId, userNick, null);
    }

    public static void showUserQuickAction(final FragmentActivity context, final String topicId, final String postId,
                                           final String userId,
                                           String userNick, final InsertNickInterface insertNickInterface) {
        try {
            userNick = Html.fromHtml(userNick.replace("<", "&lt;")).toString();

            List<String> items = new ArrayList<>();

            int insertNickPosition = -1;
            int sendQmsPosition = -1;
            int showProfilePosition;
            int showUserTopicsPosition;
            int showUserPostsPosition;
            int showUserPostsInTopicPosition = -1;
            int showUserReputationPosition = -1;

            if (Client.getInstance().getLogined()) {
                if (insertNickInterface != null) {
                    items.add(context.getString(R.string.InsertNick));
                    insertNickPosition = items.size() - 1;
                }
                items.add(context.getString(R.string.MessagesQms));
                sendQmsPosition = items.size() - 1;
            }
            items.add(context.getString(R.string.profile));
            showProfilePosition = items.size() - 1;
            items.add(context.getString(R.string.FindUserTopics));
            showUserTopicsPosition = items.size() - 1;
            items.add(context.getString(R.string.FindUserPosts));
            showUserPostsPosition = items.size() - 1;
            if (topicId != null) {
                items.add(context.getString(R.string.FindUserPostsInTopic));
                showUserPostsInTopicPosition = items.size() - 1;
            }

            items.add(context.getString(R.string.Reputation));
            showUserReputationPosition = items.size() - 1;

            if (items.size() == 0) return;

            final int finalInsertNickPosition = insertNickPosition;

            final int finalSendQmsPosition = sendQmsPosition;
            final int finalShowProfilePosition = showProfilePosition;
            final int finalShowUserTopicsPosition = showUserTopicsPosition;
            final int finalShowUserPostsPosition = showUserPostsPosition;
            final int finalShowUserPostsInTopicPosition = showUserPostsInTopicPosition;
            final String finalUserNick = userNick;

            int finalShowUserReputationPosition = showUserReputationPosition;
            new MaterialDialog.Builder(context)
                    .title(finalUserNick)
                    .items(items.toArray(new CharSequence[items.size()]))
                    .itemsCallback((materialDialog, view, i1, charSequence) -> {
                        if (i1 == finalInsertNickPosition) {
                            assert insertNickInterface != null;
                            insertNickInterface.insert(String.format(TopicBodyBuilder.NICK_SNAPBACK_TEMPLATE, postId, finalUserNick));
                        } else if (i1 == finalSendQmsPosition) {
                            QmsContactThemes.showThemes(userId, finalUserNick);
                        } else if (i1 == finalShowProfilePosition) {
                            ProfileFragment.showProfile(userId, finalUserNick);
                        } else if (i1 == finalShowUserTopicsPosition) {
                            MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserTopicsSearchSettings(finalUserNick));
                        } else if (i1 == finalShowUserPostsPosition) {
                            MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserPostsSearchSettings(finalUserNick));
                        } else if (i1 == finalShowUserPostsInTopicPosition) {
                            MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserPostsInTopicSearchSettings(finalUserNick, topicId));
                        }else if (i1 == finalShowUserReputationPosition) {
                            UserReputationFragment.showActivity(userId, false);
                        }
                    })
                    .show();
        } catch (Throwable ex) {
            AppLog.e(context, ex);
        }
    }

    public static void onCreateContextMenu(final Context context, List<MenuListDialog> menu, final String userId,
                                           String userNick) {
        try {
            final String finalUserNick = Html.fromHtml(userNick).toString();

            if (Client.getInstance().getLogined()) {
                menu.add(new MenuListDialog(context.getString(R.string.MessagesQms), () -> QmsContactThemes.showThemes(userId, finalUserNick)));
            }
            menu.add(new MenuListDialog(context.getString(R.string.profile), () -> ProfileFragment.showProfile(userId, finalUserNick)));
            menu.add(new MenuListDialog(context.getString(R.string.FindUserTopics), () -> MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserTopicsSearchSettings(finalUserNick))));
            menu.add(new MenuListDialog(context.getString(R.string.FindUserPosts), () -> MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserPostsSearchSettings(finalUserNick))));
        } catch (Throwable ex) {
            AppLog.e(context, ex);
        }
    }


    public static void startChangeRep(final Context context, final android.os.Handler handler, final String userId,
                                      String userNick, final String postId, final String type, String title) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation, null);

        assert layout != null;
        TextView username_view = layout.findViewById(R.id.username_view);
        TextView textUser = layout.findViewById(R.id.user);
        final EditText message_edit = layout.findViewById(R.id.message_edit);

        if (userId.equals(userNick)) {
            textUser.setVisibility(View.GONE);
            username_view.setVisibility(View.GONE);
        } else {
            username_view.setText(userNick);
        }
        new MaterialDialog.Builder(context)
                .title(title)
                .customView(layout, true)
                .positiveText(context.getString(R.string.Change))
                .onPositive((dialog, which) -> {
                    Toast.makeText(context, context.getString(R.string.ChangeReputationRequest), Toast.LENGTH_SHORT).show();

                    new Thread(() -> {
                        Exception ex = null;
                        final Map<String, String> outParams = new HashMap<>();
                        Boolean res = false;
                        try {
                            res = Client.getInstance().changeReputation(postId, userId, type, message_edit.getText().toString()
                                    , outParams);
                        } catch (IOException e) {
                            ex = e;
                        }

                        final Exception finalEx = ex;
                        final Boolean finalRes = res;
                        handler.post(() -> {
                            try {
                                if (finalEx != null) {
                                    Toast.makeText(context, context.getString(R.string.ChangeReputationError), Toast.LENGTH_SHORT).show();
                                    AppLog.e(context, finalEx);
                                } else if (!finalRes) {
                                    new MaterialDialog.Builder(context)
                                            .title(context.getString(R.string.ChangeReputationError))
                                            .content(outParams.get("Result"))
                                            .cancelable(true)
                                            .positiveText(R.string.ok)
                                            .show();
                                } else {
                                    Toast.makeText(context, outParams.get("Result"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex1) {
                                AppLog.e(context, ex1);
                            }

                        });
                    }).start();
                })
                .negativeText(R.string.cancel)
                .show();
    }
}