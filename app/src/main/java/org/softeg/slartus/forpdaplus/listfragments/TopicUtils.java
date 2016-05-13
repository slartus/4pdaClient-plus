package org.softeg.slartus.forpdaplus.listfragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ThemeOpenParams;
import org.softeg.slartus.forpdaplus.classes.TopicListItemTask;
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;

import java.io.IOException;

/**
 * Created by slinkin on 20.02.14.
 */
public class TopicUtils {
    /**
     * С какими параметрами навигации юзер решил открывать топик:  view=getlastpost и тд
     */
    public static String getOpenTopicArgs(CharSequence topicId, CharSequence template) {
        String themeActionPref = getTopicNavigateAction(template);
        return getUrlArgs(topicId, themeActionPref, null);
    }

    /**
     * Какой тип навигации юзер выбрал:  getlastpost, getfirstpost и тд
     */
    public static String getTopicNavigateAction(CharSequence template) {
        return App.getInstance().getPreferences().getString(String.format("%s.navigate_action", template), null);
    }

    public static void saveTopicNavigateAction(CharSequence template, CharSequence navigateAction) {
        App.getInstance().getPreferences()
                .edit()
                .putString(String.format("%s.navigate_action", template), navigateAction.toString())
                .commit();
    }

    public static final String BROWSER = "browser";

    public static String getUrlArgs(CharSequence topicId, String openParam, String defaultUrlParam) {
        if (openParam == null) return defaultUrlParam;
        if (openParam.equals(ThemeOpenParams.BROWSER))
            return "";
        if (openParam.equals(Topic.NAVIGATE_VIEW_FIRST_POST))
            return "";
        if (openParam.equals(Topic.NAVIGATE_VIEW_LAST_POST))
            return "view=getlastpost";
        if (openParam.equals(Topic.NAVIGATE_VIEW_NEW_POST))
            return "view=getnewpost";
        if (openParam.equals(Topic.NAVIGATE_VIEW_LAST_URL)) {
            try {
                String historyTopicUrl = TopicsHistoryTable.getTopicHistoryUrl(topicId);
                return TextUtils.isEmpty(historyTopicUrl) ?
                        "view=getlastpost"
                        :
                        Uri.parse(historyTopicUrl).getQuery().replaceAll("showtopic=\\d+&?", "");
            } catch (IOException e) {
                e.printStackTrace();
                return "view=getlastpost";
            }
        }


        return defaultUrlParam;

    }

    public static void showNavigateDialog(final Activity activity, final CharSequence templateId,
                                          final CharSequence topicId,
                                          final DialogInterface.OnClickListener onClickListener) {
        if (activity == null)
            return;

        CharSequence[] titles = new CharSequence[]{activity.getString(R.string.navigate_getfirstpost),
                activity.getString(R.string.navigate_getlastpost), activity.getString(R.string.navigate_getnewpost)};
        final CharSequence[] values = new CharSequence[]{Topic.NAVIGATE_VIEW_FIRST_POST,
                Topic.NAVIGATE_VIEW_LAST_POST, Topic.NAVIGATE_VIEW_NEW_POST};
        final int[] selected = {2};
        new MaterialDialog.Builder(activity)
                .items(titles)
                .itemsCallbackSingleChoice(selected[0], new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence titles) {
                        selected[0] = i;
                        return true; // allow selection
                    }
                })
                .title(R.string.default_action)
                .positiveText(R.string.always)
                .neutralText(R.string.only_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        new MaterialDialog.Builder(activity)
                                .title(R.string.hint)
                                .content(activity.getString(R.string.default_action_notify))
                                .cancelable(false)
                                .positiveText(R.string.ok)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        String navigateAction = values[selected[0]].toString();
                                        TopicUtils.saveTopicNavigateAction(templateId, navigateAction);
                                        ExtTopic.showActivity(topicId,
                                                ThemeOpenParams.getUrlParams(navigateAction, null));

                                        onClickListener.onClick(null, -1);
                                    }
                                })
                                .show();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        String navigateAction = values[selected[0]].toString();
                        ExtTopic.showActivity(topicId,
                                ThemeOpenParams.getUrlParams(navigateAction, null));
                        onClickListener.onClick(null, -1);
                    }
                })
                .show();

    }

    public static String getTopicUrl(String topicId, String urlArgs) {
        return "http://4pda.ru/forum/index.php?showtopic=" + topicId + (TextUtils.isEmpty(urlArgs) ? "" : ("&" + urlArgs));
    }

    public static void showSubscribeSelectTypeDialog(final Context context, final android.os.Handler handler,
                                                     final Topic topic) {
        showSubscribeSelectTypeDialog(context, handler, topic, null);
    }

    public static void showSubscribeSelectTypeDialog(final Context context, final android.os.Handler handler,
                                                     final Topic topic, final TopicListItemTask topicListItemTask) {
        CharSequence[] titles = {context.getString(R.string.no_notify), context.getString(R.string.first_time), context.getString(R.string.every_time), context.getString(R.string.every_day), context.getString(R.string.every_week)};
        final String[] values = {TopicApi.TRACK_TYPE_NONE, TopicApi.TRACK_TYPE_DELAYED,
                TopicApi.TRACK_TYPE_IMMEDIATE, TopicApi.TRACK_TYPE_DAILY, TopicApi.TRACK_TYPE_WEEKLY};
        String selectedSubscribe = null;
        if (topic instanceof FavTopic) {
            selectedSubscribe = ((FavTopic) topic).getTrackType();
        }
        final int[] selectedId = {ArrayUtils.indexOf(selectedSubscribe, values)};
        new MaterialDialog.Builder(context)
                .title(R.string.add_to_favorite)
                .items(titles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int i, CharSequence text) {
                        selectedId[0] = i;
                        if (selectedId[0] == -1)
                            return;

                        String emailtype = values[selectedId[0]];

                        Toast.makeText(context, R.string.request_sent, Toast.LENGTH_SHORT).show();
                        if (topicListItemTask != null) {
                            topicListItemTask.execute(emailtype);
                        } else {
                            final String finalEmailtype = emailtype;

                            new Thread(new Runnable() {
                                public void run() {

                                    Exception ex = null;

                                    String res = null;
                                    try {
                                        res = TopicApi.changeFavorite(Client.getInstance(), topic.getId(), finalEmailtype);
                                    } catch (Exception e) {
                                        ex = e;
                                    }

                                    final Exception finalEx = ex;
                                    final String finalRes = res;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            try {
                                                if (finalEx != null) {
                                                    Toast.makeText(context, R.string.error_request, Toast.LENGTH_SHORT).show();
                                                    AppLog.e(context, finalEx);
                                                } else {
                                                    Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception ex) {
                                                AppLog.e(context, ex);
                                            }

                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                })
                //.positiveText("Добавить")
                .negativeText(android.R.string.cancel)
                .show();
    }


}
