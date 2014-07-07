package org.softeg.slartus.forpdaplus.classes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 21.09.11
 * Time: 0:10
 * To change this template use File | Settings | File Templates.
 */
public class Post {
    private String m_Id = "0";
    private String m_Date = "09.03.1983";
    private String m_UserNick = "unknown";
    private String m_UserGroup = "unknown";
    private String m_UserId = "0";
    private String m_UserState;
    private String m_UserReputation = "0";
    private String m_Body = "";
    private Boolean m_CanEdit = false;
    private Boolean m_CanDelete = false;
    private String m_Number = "0";
    private Boolean m_CanPlusRep = false;
    private Boolean m_CanMinusRep = false;
    private String avatarFileName;

    public Post(String id, String date, String author, String body) {
        m_Id = id;
        m_Date = date;
        m_UserNick = author;
        m_Body = body;
    }

    public Post(String id, String date, String number) {
        m_Id = id;
        m_Date = date;
        m_Number = number;
    }

    public String getDate() {
        return m_Date;
    }

    public void setAuthor(String author) {
        m_UserNick = author;
    }

    public static String modifyBody(String value, Hashtable<String, String> emoticsDict, Boolean useLocalEmoticons) {
        return HtmlPreferences.modifyBody(value, emoticsDict, useLocalEmoticons);
    }

    public void setBody(String value) {
        m_Body = value;
    }

    public String getNumber() {
        return m_Number;
    }

    public String getBody() {
        return m_Body;
    }

    public static String getQuote(String postId, String date, String userNick, String text) {
        return "[quote name='" + userNick + "' date='" + date + "' post=" + postId + "]" + text + "[/quote]";
    }

    public String getId() {
        return m_Id;
    }

    public static String getLink(String topicId, String postId) {
        return "http://4pda.ru/forum/index.php?showtopic=" + topicId + "&view=findpost&p=" + postId;
    }

    public void setUserId(String value) {
        m_UserId = value;
    }

    public String getUserId() {
        return m_UserId;
    }

    public void setUserState(String value) {
        m_UserState = value;
    }

    public Boolean getUserState() {
        return m_UserState != null && m_UserState.equals("green");
    }

    public void setUserGroup(String value) {
        m_UserGroup = value;
    }

    public String getUserGroup() {
        return m_UserGroup;
    }

    public void setUserReputation(String value) {
        m_UserReputation = value;
    }

    @Override
    public String toString() {
        return m_Body;
    }

    public String getNick() {
        return m_UserNick;
    }

    public String getUserReputation() {
        return m_UserReputation;
    }

    public void setCanEdit(boolean value) {
        m_CanEdit = value;
    }

    public Boolean getCanEdit() {
        return m_CanEdit;
    }

    public void setCanDelete(boolean value) {
        m_CanDelete = value;
    }

    public Boolean getCanDelete() {
        return m_CanDelete;
    }

    public static void delete(String postId, String forumId, String themeId, String authKey) throws IOException {
        Client.getInstance().deletePost(forumId, themeId, postId, authKey);
    }

    public void setCanPlusRep(boolean value) {
        m_CanPlusRep = value;
    }

    public Boolean getCanPlusRep() {
        return m_CanPlusRep;
    }

    public void setCanMinusRep(boolean value) {
        m_CanMinusRep = value;
    }

    public Boolean getCanMinusRep() {
        return m_CanMinusRep;
    }

    public static void claim(
            final Context context,
            final android.os.Handler handler,
            final String themeId,
            final String postId) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.claim, null);


        assert layout != null;
        final EditText message_edit = (EditText) layout.findViewById(R.id.message_edit);

        new AlertDialogBuilder(context)
                .setTitle("Отправить жалобу модератору на сообщение")
                .setView(layout)
                .setPositiveButton("Отправить жалобу", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Toast.makeText(context, "Жалоба отправлена", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            public void run() {
                                Exception ex = null;

                                String res = null;
                                try {
                                    res = Client.getInstance().claim(themeId, postId, message_edit.getText().toString());
                                } catch (IOException e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final String finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, "Ошибка отправки жалобы", Toast.LENGTH_LONG).show();
                                                Log.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, finalRes, Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception ex) {
                                            Log.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    public static void plusOne(Activity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity, handler, postId, "1");
    }

    public static void minusOne(Activity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity, handler, postId, "-1");
    }

    private static void changePostReputation(final Activity themeActivity, final Handler handler, final String postId, final String direction) {
        Toast.makeText(themeActivity, "Запрос на изменение репутации сообщения отправлен", Toast.LENGTH_SHORT).show();
        // http://s.4pda.ru/forum/jscripts/karma3.js
        new Thread(new Runnable() {
            public void run() {
                Throwable ex = null;

                String message = null;
                try {
                    String res = Client.getInstance().performGet("http://4pda.ru/forum/zka.php?i=" + postId + "&v=" + direction);

                    Matcher m = Pattern.compile("ok:\\s*?((?:\\+|\\-)?\\d+)").matcher(res);
                    if (m.find()) {
                        int code = Integer.parseInt(m.group(1));
                        switch (code) {
                            case 0:
                                message = "Ошибка изменения репутации: Вы уже голосовали за этот пост";
                                break;
                            case 1:
                                message = "Репутация поста увеличена";
                                break;
                            case -1:
                                message = "Репутация поста снижена";
                                break;
                            default:
                                message = "Ошибка изменения репутации: " + res;
                        }
                    } else
                        message = "Ошибка изменения репутации: " + res;

                } catch (Throwable e) {
                    ex = e;
                }

                final Throwable finalEx = ex;

                final String finalMessage = message;
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(themeActivity, "Ошибка изменения репутации поста", Toast.LENGTH_LONG).show();
                                Log.e(themeActivity, finalEx);
                            } else {
                                Toast.makeText(themeActivity, finalMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.e(themeActivity, ex);
                        }

                    }
                });
            }
        }).start();
    }

    public String getAvatarFileName() {
        return avatarFileName;
    }

    public void setAvatarFileName(String avatarFileName) {
        String path = "http://s.4pda.to/forum/uploads/";
        if (avatarFileName != null && avatarFileName.contains("/"))
            path = "http://s.4pda.to/forum/style_avatars/";
        this.avatarFileName = path + avatarFileName;
    }
}

