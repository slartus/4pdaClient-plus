package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.qms.QmsContactThemesActivity;
import org.softeg.slartus.forpdaplus.qms.QmsNewThreadActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 10:44
 */
public class ForumUser {
    private String m_Nick;
    private String m_Group;
    private String m_Id;
    private String m_Reputation;

    public interface InsertNickInterface {
        void insert(String text);
    }

    public static void showUserQuickAction(final FragmentActivity context, View webView, final String userId,
                                           String userNick) {
        showUserQuickAction(context, webView, "", userId, userNick, null);
    }

    public static void showUserQuickAction(final FragmentActivity context, View webView, final String postId,
                                           final String userId,
                                           String userNick,final InsertNickInterface insertNickInterface) {
        try {
            userNick = Html.fromHtml(userNick.replace("<", "&lt;")).toString();


            // не забыть менять в ForumUser
            net.londatiga.android3d.QuickAction mQuickAction = new net.londatiga.android3d.QuickAction(context);
            int id = 0;
            Resources resourses = context.getResources();

//            if(!TextUtils.isEmpty(avatar)){
//                mQuickAction.getImageView().setMinimumHeight(200);
//                mQuickAction.getImageView().setMinimumWidth(200);
//                ImageLoader imageLoader = ImageLoader.getInstance();
//                imageLoader.displayImage(avatar, mQuickAction.getImageView());
//
//            }
            int insertNickPosition = id++;
            int sendQmsPosition = id++;
            if (Client.getInstance().getLogined()) {
                if (insertNickInterface != null)
                    mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(insertNickPosition,
                            context.getString(R.string.InsertNick)));
                mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(sendQmsPosition,
                        context.getString(R.string.MessagesQms)));
            }
            int showProfilePosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showProfilePosition,
                    context.getString(R.string.Profile)));
            int showUserTopicsPosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showUserTopicsPosition,
                    context.getString(R.string.FindUserTopics)));
            int showUserPostsPosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showUserPostsPosition,
                    context.getString(R.string.FindUserPosts)));

            if (mQuickAction.getItemsCount() == 0) return;

            final int finalInsertNickPosition = insertNickPosition;

            final int finalSendQmsPosition = sendQmsPosition;
            final int finalShowProfilePosition = showProfilePosition;
            final int finalShowUserTopicsPosition = showUserTopicsPosition;
            final int finalShowUserPostsPosition = showUserPostsPosition;
            final String finalUserNick = userNick;
            final String finalUserNick1 = userNick;
            mQuickAction.setOnActionItemClickListener(new net.londatiga.android3d.QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(net.londatiga.android3d.QuickAction source, int pos, int actionId) {
                    try {
                        if (actionId == finalInsertNickPosition) {
                            assert insertNickInterface != null;
                            insertNickInterface.insert(String.format(TopicBodyBuilder.NICK_SNAPBACK_TEMPLATE,postId, finalUserNick ));
                        } else if (actionId == finalSendQmsPosition) {
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.SelectAnAction))
                                    .content(context.getString(R.string.OpenWith) + " " + finalUserNick + "...")
                                    .cancelable(true)
                                    .positiveText(context.getString(R.string.NewDialog))
                                    .neutralText(context.getString(R.string.AllDialogs))
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            QmsNewThreadActivity.showUserNewThread(context, userId, finalUserNick);
                                        }
                                        @Override
                                        public void onNeutral(MaterialDialog dialog) {
                                            QmsContactThemesActivity.showThemes(context, userId, finalUserNick);
                                        }
                                    })
                                    .show();

                        } else if (actionId == finalShowProfilePosition) {
                            //ProfileWebViewFragment.showDialog((FragmentActivity)context,userId, finalUserNick);
                            ProfileFragment.showProfile(userId, finalUserNick);
                        } else if (actionId == finalShowUserTopicsPosition) {
                            SearchActivity.startForumSearch(context,SearchSettingsDialogFragment.createUserTopicsSearchSettings(finalUserNick1));

                        } else if (actionId == finalShowUserPostsPosition) {
                            SearchActivity.startForumSearch(context,SearchSettingsDialogFragment.createUserPostsSearchSettings(finalUserNick1));

                        }
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }
                }
            });

            if (webView.getClass() == AdvWebView.class)
                mQuickAction.show(webView, ((AdvWebView) webView).getLastMotionEvent());
            else
                mQuickAction.show(webView);
        } catch (Throwable ex) {
            AppLog.e(context, ex);
        }
    }

    public static void onCreateContextMenu(final Context context, ContextMenu menu, final String userId,
                                           String userNick) {
        try {
            userNick = Html.fromHtml(userNick).toString();
            //if(!TextUtils.isEmpty(userNick)&&menu.hea)
            final String finalUserNick = userNick;

            if (Client.getInstance().getLogined()) {
                menu.add(context.getString(R.string.MessagesQms))
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                new MaterialDialog.Builder(context)
                                        .title(context.getString(R.string.SelectAnAction))
                                        .content(context.getString(R.string.OpenWith) + " " + finalUserNick + "...")
                                        .cancelable(true)
                                        .positiveText(context.getString(R.string.NewDialog))
                                        .neutralText(context.getString(R.string.AllDialogs))
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                QmsNewThreadActivity.showUserNewThread(context, userId, finalUserNick);
                                            }
                                            @Override
                                            public void onNeutral(MaterialDialog dialog) {
                                                QmsContactThemesActivity.showThemes(context, userId, finalUserNick);
                                            }
                                        })
                                        .show();
                                return true;
                            }
                        });
            }

            menu.add(context.getString(R.string.Profile))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            ProfileFragment.showProfile(userId, finalUserNick);
                            return true;
                        }
                    });

            menu.add(context.getString(R.string.FindUserTopics))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            SearchActivity.startForumSearch(context,SearchSettingsDialogFragment.createUserTopicsSearchSettings(finalUserNick));
                            return true;
                        }
                    });
            menu.add(context.getString(R.string.FindUserPosts))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            SearchActivity.startForumSearch(context,SearchSettingsDialogFragment.createUserPostsSearchSettings(finalUserNick));
                            return true;
                        }
                    });


        } catch (Throwable ex) {
            AppLog.e(context, ex);
        }
    }


    public static void startChangeRep(final Context context, final android.os.Handler handler, final String userId,
                                      String userNick, final String postId, final String type, String title) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation, null);

        assert layout != null;
        TextView username_view = (TextView) layout.findViewById(R.id.username_view);
        TextView textUser = (TextView) layout.findViewById(R.id.user);
        final EditText message_edit = (EditText) layout.findViewById(R.id.message_edit);

        if(userId.equals(userNick)){
            textUser.setVisibility(View.GONE);
            username_view.setVisibility(View.GONE);
        }else {
            username_view.setText(userNick);
        }
        new MaterialDialog.Builder(context)
                .title(title)
                .customView(layout,true)
                .positiveText(context.getString(R.string.Change))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Toast.makeText(context, context.getString(R.string.ChangeReputationRequest), Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            public void run() {
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
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, context.getString(R.string.ChangeReputationError), Toast.LENGTH_SHORT).show();
                                                AppLog.e(context, finalEx);
                                            } else if (!finalRes) {
                                                new MaterialDialog.Builder(context)
                                                        .title(context.getString(R.string.ChangeReputationError))
                                                        .content(outParams.get("Result"))
                                                        .cancelable(true)
                                                        .positiveText("OK")
                                                        .show();
                                            } else {
                                                Toast.makeText(context, outParams.get("Result"), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            AppLog.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();
                    }
                })
                .negativeText("Отмена")
                .show();
    }
}