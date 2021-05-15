package org.softeg.slartus.forpdaplus.classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.HtmlUtils;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.hosthelper.HostHelper;

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
    private Boolean m_IsCurator = false;
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
    private String avatarFileName = "";

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

    public static String modifyBody(String value, Hashtable<String, String> emoticsDict) {
        return HtmlPreferences.modifyBody(value, emoticsDict);
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
        return "https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + topicId + "&view=findpost&p=" + postId;
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

    private String m_NickParam = null;

    public String getNickParam() {
        if (m_NickParam == null && m_UserNick != null) {
            m_NickParam = HtmlUtils.modifyHtmlQuote(m_UserNick)
                    .replace("'", "\\'").replace("\"", "&quot;");
        }
        return m_NickParam;
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

    public static void delete(String postId, String authKey) throws IOException {
        Client.getInstance().deletePost(postId, authKey);
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

        final SharedPreferences prefs = App.getInstance().getPreferences();
        if(prefs.getBoolean("showClaimWarn",true)){
            new MaterialDialog.Builder(context)
                    .title(R.string.attention)
                    .content(R.string.ClaimDescription)
                    .positiveText(R.string.understand)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            prefs.edit().putBoolean("showClaimWarn",false).apply();
                            showClaimDialog(context, handler,themeId,postId);
                        }
                    })
                    .show();
        }else {
            showClaimDialog(context, handler,themeId,postId);
        }
    }

    public static void showClaimDialog(
            final Context context,
            final android.os.Handler handler,
            final String themeId,
            final String postId){
        final String[] text = {""};

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.claim, null);


        assert layout != null;
        final EditText message_edit = layout.findViewById(R.id.message_edit);


        new MaterialDialog.Builder(context)
                .title(R.string.claim_title)
                .customView(layout,true)
                .positiveText(R.string.send)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Toast.makeText(context, R.string.claim_sent, Toast.LENGTH_SHORT).show();

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
                                                Toast.makeText(context, R.string.error_request, Toast.LENGTH_LONG).show();
                                                AppLog.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, finalRes, Toast.LENGTH_LONG).show();
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
                .negativeText(R.string.cancel)
                .show();
    }

    public static void plusOne(Activity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity, handler, postId, "1");
    }

    public static void minusOne(Activity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity, handler, postId, "-1");
    }

    private static void changePostReputation(final Activity themeActivity, final Handler handler, final String postId, final String direction) {
        Toast.makeText(themeActivity, R.string.vote_request_sent, Toast.LENGTH_SHORT).show();
        // https://s.4pda.ru/forum/jscripts/karma3.js
        new Thread(new Runnable() {
            public void run() {
                Throwable ex = null;

                String message = null;
                try {
                    String res = Client.getInstance().performGet("https://"+ HostHelper.getHost() +"/forum/zka.php?i=" + postId + "&v=" + direction, true, false).getResponseBody();

                    Matcher m = Pattern.compile("ok:\\s*?((?:\\+|\\-)?\\d+)").matcher(res);
                    if (m.find()) {
                        int code = Integer.parseInt(m.group(1));
                        switch (code) {
                            case 0:
                                message = themeActivity.getString(R.string.vote_error_already_voted);
                                break;
                            case 1:
                                message = themeActivity.getString(R.string.vote_post_increased);
                                break;
                            case -1:
                                message = themeActivity.getString(R.string.vote_post_decreased);
                                break;
                            default:
                                message = themeActivity.getString(R.string.vote_change_error)+": " + res;
                        }
                    } else
                        message = themeActivity.getString(R.string.vote_change_error)+": " + res;

                } catch (Throwable e) {
                    ex = e;
                }

                final Throwable finalEx = ex;

                final String finalMessage = message;
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(themeActivity, themeActivity.getString(R.string.vote_change_error) , Toast.LENGTH_LONG).show();
                                AppLog.e(themeActivity, finalEx);
                            } else {
                                Toast.makeText(themeActivity, finalMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            AppLog.e(themeActivity, ex);
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
        if (TextUtils.isEmpty(avatarFileName))
            return;
        String path = "https://s.4pda.to/forum/uploads/";
        if (avatarFileName.contains("/"))
            if(!Pattern.compile("\\d+\\/").matcher(avatarFileName).find())
                path = "https://s.4pda.to/forum/style_avatars/";
        this.avatarFileName = path + avatarFileName;
    }

    public void setCurator() {
        m_IsCurator=true;
    }

    public Boolean isCurator(){
        return m_IsCurator;
    }
}

