package org.softeg.slartus.forpdaplus.listfragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.ThemeOpenParams;
import org.softeg.slartus.forpdaplus.classes.TopicListItemTask;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;

import java.io.IOException;

/**
 * Created by slinkin on 20.02.14.
 */
public class TopicUtils {
    /**
     * С какими параметрами навигации юзер решил открывать топик:  view=getlastpost и тд
     */
    public static String getOpenTopicArgs(CharSequence topicId,CharSequence template) {
        String themeActionPref = getTopicNavigateAction(template);
        return getUrlArgs(topicId,themeActionPref, null);
    }

    /**
     * Какой тип навигации юзер выбрал:  getlastpost, getfirstpost и тд
     */
    public static String getTopicNavigateAction(CharSequence template) {
        return PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()).getString(String.format("%s.navigate_action", template), null);
    }

    public static void saveTopicNavigateAction(CharSequence template, CharSequence navigateAction) {
        PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
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
        if (openParam.equals(Topic.NAVIGATE_VIEW_LAST_URL)){
            try {
                String historyTopicUrl= TopicsHistoryTable.getTopicHistoryUrl(topicId);
                return TextUtils.isEmpty(historyTopicUrl)?
                        "view=getlastpost"
                        :
                        Uri.parse(historyTopicUrl).getQuery().replaceAll("showtopic=\\d+&?","");
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
        new AlertDialogBuilder(activity)
                .setSingleChoiceItems(titles, selected[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected[0] = i;
                    }
                })
                .setTitle("Действие по умолчанию")
                .setPositiveButton("Всегда",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                new AlertDialogBuilder(activity)
                                        .setTitle("Подсказка")
                                        .setMessage("Вы можете изменить действие по умолчанию долгим тапом по теме")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();

                                                String navigateAction = values[selected[0]].toString();
                                                TopicUtils.saveTopicNavigateAction(templateId, navigateAction);
                                                ExtTopic.showActivity(activity, topicId,
                                                        ThemeOpenParams.getUrlParams(navigateAction, null));

                                                onClickListener.onClick(null, -1);
                                            }
                                        })
                                        .create().show();
                            }
                        }
                )
                .setNeutralButton("Только сейчас",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                String navigateAction = values[selected[0]].toString();
                                ExtTopic.showActivity(activity, topicId,
                                        ThemeOpenParams.getUrlParams(navigateAction, null));

                                onClickListener.onClick(null, -1);
                            }
                        }
                )
                .create()
                .show();

    }

    public static String getTopicUrl(String topicId, String urlArgs) {
        return "http://4pda.ru/forum/index.php?showtopic=" + topicId + (TextUtils.isEmpty(urlArgs) ? "" : ("&" + urlArgs));
    }

    public static void showSubscribeSelectTypeDialog(final Context context, final android.os.Handler handler,
                                                     final CharSequence topicId) {
        showSubscribeSelectTypeDialog(context, handler, topicId,null);
    }
    public static void showSubscribeSelectTypeDialog(final Context context, final android.os.Handler handler,
                                                     final CharSequence topicId, final TopicListItemTask topicListItemTask) {
        CharSequence[] titles = {"Не уведомлять", "Первый раз", "Каждый раз", "Каждый день", "Каждую неделю"};
        final String[] values = {TopicApi.TRACK_TYPE_NONE, TopicApi.TRACK_TYPE_DELAYED,
                TopicApi.TRACK_TYPE_IMMEDIATE, TopicApi.TRACK_TYPE_DAILY, TopicApi.TRACK_TYPE_WEEKLY};
        final int[] selectedId = {1};
        new AlertDialogBuilder(context)
                .setTitle("Добавление в избранное/подписки")
                .setSingleChoiceItems(titles, 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedId[0] = i;
                    }
                })

                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String emailtype = values[selectedId[0]];

                        Toast.makeText(context, "Запрос на добавление отправлен", Toast.LENGTH_SHORT).show();
                        if (topicListItemTask != null) {
                            topicListItemTask.execute(emailtype);
                        } else {
                            final String finalEmailtype = emailtype;

                            new Thread(new Runnable() {
                                public void run() {

                                    Exception ex = null;

                                    String res = null;
                                    try {
                                        res = TopicApi.changeFavorite(Client.getInstance(), topicId, finalEmailtype);
                                    } catch (Exception e) {
                                        ex = e;
                                    }

                                    final Exception finalEx = ex;
                                    final String finalRes = res;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            try {
                                                if (finalEx != null) {
                                                    Toast.makeText(context, "Ошибка добавления в избранное/подписки", Toast.LENGTH_SHORT).show();
                                                    Log.e(context, finalEx);
                                                } else {
                                                    Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception ex) {
                                                Log.e(context, ex);
                                            }

                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }


}
