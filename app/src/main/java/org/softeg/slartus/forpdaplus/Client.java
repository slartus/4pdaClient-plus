package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.softeg.slartus.forpdaapi.ForumsApi;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.LoginResult;
import org.softeg.slartus.forpdaapi.NewsApi;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.ReputationsApi;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaapi.TopicReadingUsers;
import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.Observer;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.SimpleCookie;
import org.softeg.slartus.forpdaplus.classes.DownloadTask;
import org.softeg.slartus.forpdaplus.classes.DownloadTasks;
import org.softeg.slartus.forpdaplus.classes.Forum;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ForumsTableOld;
import org.softeg.slartus.forpdaplus.download.DownloadReceiver;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.fragments.topic.ForPdaWebInterface;
import org.softeg.slartus.forpdaplus.utils.LogUtil;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
public class Client implements IHttpClient {

    public static final String SITE = "4pda.ru";

    public String UserId = "0";
    private String m_User = App.getContext().getString(R.string.guest);

    private String m_K = "";

    private Client() {

    }

    public String getAuthKey() {
        return m_K;
    }


    public static final Client INSTANCE = new Client();


    public URI getRedirectUri() {
        return HttpHelper.getRedirectUri();
    }

    public String getLastUrl() {
        return HttpHelper.getLastUri();
    }

    public void deletePost(String forumId, String themeId, String postId, CharSequence authKey) throws IOException {
        PostApi.delete(this, forumId, themeId, postId, authKey);
    }

    public String getEditPostPlus(String forumId, String themeId, String postId, String authKey,
                                  Map<String, String> outParams) throws Throwable {
        if (TextUtils.isEmpty(forumId) || TextUtils.isEmpty(authKey)) {
            ExtTopic topic = new ExtTopic(themeId, "");
            setThemeForumAndAuthKey(topic);

            forumId = topic.getForumId();
            authKey = topic.getAuthKey();

            outParams.put("forumId", forumId);
            outParams.put("authKey", authKey);
        }

        String res = PostApi.getEditPage(this, forumId, themeId, postId, authKey);

        String error = PostApi.checkEditPage(res);

        if (!TextUtils.isEmpty(error))
            throw new NotReportException(error);

        return res;

    }

    public String editPost(String forumId, String themeId, String authKey, String postId, Boolean enablesig,
                           Boolean enableEmo, String post, String addedFileList, String post_edit_reason) throws IOException {
        return PostApi.applyEdit(this, forumId, themeId, authKey, postId, enablesig,
                enableEmo, post, addedFileList, post_edit_reason);
    }

    public String attachFilePost(String forumId, String themeId, String authKey, String attachPostKey, String postId, Boolean enablesig, Boolean enableEmo,
                                 String post, String filePath, String addedFileList, ProgressState progress
            , String post_edit_reason) throws Exception {
        return PostApi.attachFile(this, forumId, themeId, authKey, attachPostKey, postId, enablesig, enableEmo,
                post, filePath, addedFileList, progress, post_edit_reason);
    }

    public String deleteAttachFilePost(String forumId, String themeId, String authKey, String postId,
                                       Boolean enablesig, Boolean enableemo,
                                       String post, String attachToDeleteId, String fileList
            , String post_edit_reason) throws Exception {
        return PostApi.deleteAttachedFile(this, forumId, themeId, authKey, postId,
                enablesig, enableemo,
                post, attachToDeleteId, fileList, post_edit_reason);
    }

    public Boolean changeReputation(String postId, String userId, String type, String message, Map<String, String> outParams) throws IOException {

        return ReputationsApi.changeReputation(this, postId, userId, type, message, outParams);

    }

    public String claim(String themeId, String postId, String message) throws IOException {
        String error = PostApi.claim(this, themeId, postId, message);
        if (!TextUtils.isEmpty(error))
            return error;
        return App.getContext().getString(R.string.complaint_sent);
    }


    public Boolean hasLoginCookies() {
        Boolean session = false;
        Boolean pass_hash = false;
        Boolean member = false;
        HttpHelper httpHelper = null;
        try {
            try {
                httpHelper = new HttpHelper();

            } catch (IOException e) {
                AppLog.e(null, e);
                return false;
            }
            List<Cookie> cookies = httpHelper.getCookies();
            for (Cookie cookie : cookies) {
                if (!session && cookie.getName().equals("session_id"))
                    session = true;
                else if (!pass_hash && cookie.getName().equals("pass_hash"))
                    pass_hash = true;
                else if (!member && cookie.getName().equals("member_id"))
                    member = true;
            }
        } finally {
            if (httpHelper != null)
                httpHelper.close();
        }
        return session && pass_hash && member;
    }


    public String performGetWithCheckLogin(String url, OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException {
        if (beforeGetPage != null)
            beforeGetPage.onProgressChanged(App.getContext().getString(R.string.receiving_data));
        String body = performGet(url);
        if (beforeGetPage != null)
            afterGetPage.onProgressChanged(App.getContext().getString(R.string.receiving_data));

        /*Matcher headerMatcher = PatternExtensions.compile("<body>([\\s\\S]*?)globalmess").matcher(body);
        if (headerMatcher.find()) {
            checkLogin(headerMatcher.group(1));
            checkMails(headerMatcher.group(1));
        } else {
            checkLogin(body);
            checkMails(body);
        }*/
        return body;
    }

    /**
     * Загрузка тестовой страницы, проверка логина, проверка писем
     */
    public void loadTestPage() throws IOException {
        String body = performGet("http://4pda.ru/forum/index.php?showforum=200");
        checkLogin(body);
        checkMails(body);
    }

    public String performGetFullVersion(String s) throws IOException {

        HttpHelper httpHelper = new HttpHelper(HttpHelper.FULL_USER_AGENT);
        //HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performGet(s);
        } finally {
            httpHelper.close();

        }
        if (TextUtils.isEmpty(res))
            throw new NotReportException(App.getContext().getString(R.string.server_return_empty_page));
        // m_HttpHelper.close();
        return res;
    }

    public String performGet(String s) throws IOException {
        return performGet(s, true, true);
    }

    public String performGet(String s, Boolean checkEmptyResult, Boolean checkLoginAndMails) throws IOException {

        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performGet(s);
        } finally {
            httpHelper.close();

        }
        if (checkEmptyResult && TextUtils.isEmpty(res))
            throw new NotReportException(App.getContext().getString(R.string.server_return_empty_page));
        else if(checkLoginAndMails){
            checkLogin(res);
            if(!s.contains("xhr"))
                checkMails(res);
        }
        // m_HttpHelper.close();
        return res;
    }

    public String performPost(String s, Map<String, String> additionalHeaders) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performPost(s, additionalHeaders);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }


    public String uploadFile(String url, String filePath, Map<String, String> additionalHeaders
            , ProgressState progress) throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.uploadFile(url, filePath, additionalHeaders, progress);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }

    @Override
    public CookieStore getCookieStore() throws IOException {
        HttpHelper httpHelper = new HttpHelper();

        try {
            return httpHelper.getCookieStore();
        } finally {
            httpHelper.close();
        }
    }

    public String performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performPost(s, additionalHeaders, encoding);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }

    @Override
    public String performPost(String s, List<NameValuePair> additionalHeaders) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performPost(s, additionalHeaders);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }


    public List<Cookie> getCookies() throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        try {
            return httpHelper.getCookies();
        } finally {
            httpHelper.close();
        }

    }

    public Users getTopicWritersUsers(String topicId) throws IOException {

        return org.softeg.slartus.forpdaapi.TopicApi.getWriters(this, topicId);

    }

    public static Client getInstance() {
        return INSTANCE;  //To change body of created methods use File | Settings | File Templates.
    }

    public Boolean likeNews(String postId) throws IOException {
        return NewsApi.like(this, postId);
    }
    public Boolean likeComment(final String id, final String comment) throws IOException {
        return NewsApi.likeComment(this, id, comment);
    }


    public interface OnUserChangedListener {
        void onUserChanged(String user, Boolean success);
    }

    public void doOnUserChangedListener(String user, Boolean success) {
        for (OnUserChangedListener listener : m_OnUserChangeListeners.getListeners()) {
            listener.onUserChanged(user, success);
        }

    }

    private Observer<OnUserChangedListener> m_OnUserChangeListeners = new Observer<OnUserChangedListener>();

    public void addOnUserChangedListener(OnUserChangedListener p) {
        m_OnUserChangeListeners.addStrongListener(p);
    }

    public interface OnMailListener {
        void onMail(int count);
    }

    public void doOnMailListener() {
        for (OnMailListener listener : m_OnMailListeners.getListeners()) {
            listener.onMail(0);
        }

    }

    private Observer<OnMailListener> m_OnMailListeners = new Observer<OnMailListener>();

    public void addOnMailListener(OnMailListener p) {
        m_OnMailListeners.addStrongListener(p);
    }


    public interface OnProgressPositionChangedListener {
        void onProgressChanged(Context context, DownloadTask downloadTask, Exception ex);
    }

    public void doOnOnProgressChanged(OnProgressChangedListener listener, String state) {
        if (listener != null) {
            listener.onProgressChanged(state);
        }
    }

    public void showLoginForm(Context mContext, final OnUserChangedListener onUserChangedListener) {
        try {

            final LoginDialog loginDialog = new LoginDialog(mContext);

            MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.login)
                    .customView(loginDialog.getView(), true)
                    .positiveText(R.string.login)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            loginDialog.connect(onUserChangedListener);
                        }
                    })
                    .build();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();

        } catch (Exception ex) {
            AppLog.e(mContext, ex);
        }
    }

    public String getUser() {
        return m_User;
    }

    private Boolean m_Logined = false;

    public Boolean getLogined() {
        return m_Logined;
    }

    private String m_LoginFailedReason;

    public String getLoginFailedReason() {
        return m_LoginFailedReason;
    }

    public String reply(String forumId, String themeId, String authKey, String post,
                        Boolean enablesig, Boolean enableemo, Boolean quick, String addedFileList) throws IOException {
        return reply(forumId, themeId, authKey, null, post,
                enablesig, enableemo, quick, addedFileList);
    }

    public String reply(String forumId, String themeId, String authKey, String attachPostKey, String post,
                        Boolean enablesig, Boolean enableemo, Boolean quick, String addedFileList) throws IOException {
        return PostApi.reply(this, forumId, themeId, authKey, attachPostKey, post,
                enablesig, enableemo, addedFileList, quick);
    }


    public Boolean login(String login, String password, Boolean privacy,
                         String capVal, String capTime, String capSig, String session) throws Exception {

        HttpHelper httpHelper = new HttpHelper();
        try {
//            httpHelper.clearCookies();
//            httpHelper.writeExternalCookies();


            final HttpHelper finalHttpHelper = httpHelper;
            LoginResult loginResult = ProfileApi.login(new IHttpClient() {


                public String performGetWithCheckLogin(String s, OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException {
                    return null;
                }

                public String performGet(String s, Boolean b, Boolean bb) throws IOException {
                    return null;
                }


                public String performGet(String s) throws IOException {
                    return null;
                }

                @Override
                public String performGetFullVersion(String s) throws IOException {
                    return null;
                }

                public String performPost(String s, Map<String, String> additionalHeaders) throws IOException {

                    String res = null;
                    try {
                        // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
                        res = finalHttpHelper.performPost(s, additionalHeaders);
                        checkLogin(res);
                        finalHttpHelper.writeExternalCookies();
                    } catch (Exception ignored) {

                    } finally {
                        finalHttpHelper.close();
                    }
                    return res;
                }

                public String performPost(String s, List<NameValuePair> additionalHeaders) throws IOException {

                    String res = null;
                    try {
                        // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
                        res = finalHttpHelper.performPost(s, additionalHeaders);
                        checkLogin(res);
                        finalHttpHelper.writeExternalCookies();
                    } catch (Exception ignored) {

                    } finally {
                        finalHttpHelper.close();
                    }
                    return res;
                }

                public String performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException {

                    String res = null;
                    try {
                        // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
                        res = finalHttpHelper.performPost(s, additionalHeaders, encoding);
                        checkLogin(res);
                        finalHttpHelper.writeExternalCookies();
                    } catch (Exception ignored) {

                    } finally {
                        finalHttpHelper.close();
                    }
                    return res;
                }

                @Override
                public String uploadFile(String url, String filePath, Map<String, String> additionalHeaders, ProgressState progress) throws Exception {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public CookieStore getCookieStore() throws IOException {
                    try {
                        return finalHttpHelper.getCookieStore();
                    } finally {
                        finalHttpHelper.close();
                    }
                }

            }, login, password, privacy, capVal, capTime, capSig, session);
            m_Logined = loginResult.isSuccess();
            m_LoginFailedReason = m_Logined ? null : loginResult.getLoginError().toString();


            // m_SessionId = outParams.get("SessionId");
            m_User = loginResult.getUserLogin().toString();
            m_K = loginResult.getK().toString();

            httpHelper.getCookieStore().addCookie(new SimpleCookie("4pda.UserId", loginResult.getUserId().toString()));
            httpHelper.getCookieStore().addCookie(new SimpleCookie("4pda.User", m_User));
            httpHelper.getCookieStore().addCookie(new SimpleCookie("4pda.K", m_K));

            httpHelper.writeExternalCookies();

        } finally {
            httpHelper.close();
        }

        return m_Logined;
    }

    private final Pattern checkLoginPattern = PatternExtensions.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></b> \\( <a href=\"(http://4pda.ru)?/forum/index.php\\?act=Login&amp;CODE=03&amp;k=([a-z0-9]{32})\">Выход</a>");

    public void checkLoginByCookies() {
        try {
            HttpHelper httpHelper = null;
            try {
                httpHelper = new HttpHelper();

                if (checkLogin(httpHelper.getCookieStore())) {
                    m_Logined = true;
                }
            } finally {

                if (httpHelper != null)
                    httpHelper.finalize();
            }
        } catch (Throwable ignored) {

        } finally {
            doOnUserChangedListener(m_User, m_Logined);
        }

    }

    public Boolean checkLogin(CookieStore cookies) {
        if (cookies == null)
            return false;
        m_User = "";
        m_K = "";

        Cookie memberIdCookie = null;
        for (Cookie cookie : cookies.getCookies()) {
            if ("4pda.UserId".equals(cookie.getName())) {
                UserId = cookie.getValue();
            } else if ("4pda.User".equals(cookie.getName())) {
                m_User = cookie.getValue();
            } else if ("4pda.K".equals(cookie.getName())) {
                m_K = cookie.getValue();
            } else if ("member_id".equals(cookie.getName())) {
                memberIdCookie = cookie;
            }
        }

        return !TextUtils.isEmpty(m_User) && !TextUtils.isEmpty(UserId) && !TextUtils.isEmpty(m_K) && memberIdCookie != null && UserId.equals(memberIdCookie.getValue());

    }

    public void checkLogin(String pageBody) {

        try {
            HttpHelper httpHelper = null;
            try {
                httpHelper = new HttpHelper();

                if (checkLogin(httpHelper.getCookieStore())) {
                    m_Logined = true;
                    return;
                }
                if (!TextUtils.isEmpty(m_User) && !TextUtils.isEmpty(UserId)
                        && !TextUtils.isEmpty(m_K)) {

                    List<Cookie> cookies = httpHelper.getLastCookies();
                    for (Cookie cookie : cookies) {
                        if ("member_id".equals(cookie.getName())) {
                            if (UserId.equals(cookie.getValue())) {
                                m_Logined = true;
                                return;
                            }
                            break;
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpHelper != null)
                    httpHelper.close();
            }


            Matcher m = checkLoginPattern.matcher(pageBody);
            if (m.find()) {
                UserId = m.group(2);
                m_User = m.group(3);
                m_K = m.group(5);
                m_Logined = true;
            } else {
                m_Logined = false;
                m_User = "гость";
                m_K = "";
                UserId = "";
            }

        } finally {
            doOnUserChangedListener(m_User, m_Logined);
        }


    }

    private int m_QmsCount = 0;

    public int getQmsCount() {
        return m_QmsCount;
    }

    public void setQmsCount(int count) {
        m_QmsCount = count;
    }


    public void checkMails(String pageBody) {
        m_QmsCount = QmsApi.getNewQmsCount(pageBody);
        doOnMailListener();
    }

    public Boolean logout() throws Throwable {
        String res = ProfileApi.logout(this, m_K);
        HttpHelper httpHelper = new HttpHelper();
        try {
            httpHelper.clearCookies();
            httpHelper.writeExternalCookies();
        } finally {
            httpHelper.close();
        }

        checkLogin(res);
        if (m_Logined)
            m_LoginFailedReason = App.getContext().getString(R.string.bad_logout);

        return !m_Logined;
    }

    public Forum loadForums() throws Exception {
        return ForumsTableOld.loadForumsTree();
    }


    public String loadPageAndCheckLogin(String url, OnProgressChangedListener progressChangedListener) throws IOException {

        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.receiving_data));
        String body = performGet(url);
        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.processing_data));

        /*Matcher headerMatcher = PatternExtensions.compile("<body>([\\s\\S]*?)globalmess").matcher(body);
        if (headerMatcher.find()) {
            checkLogin(headerMatcher.group(1));
            checkMails(headerMatcher.group(1));
        } else {
            checkLogin(body);
            checkMails(body);
        }*/
        return body;
    }


    //loadTopic
    private final static Pattern pollFormPattern = Pattern.compile("<form[^>]*action=\"[^\"]*addpoll=1[^\"]*\"[^>]*>([\\s\\S]*?)</form>", Pattern.CASE_INSENSITIVE);
    private final static Pattern pollTitlePattern = Pattern.compile("<b>(.*?)</b>");
    private final static Pattern pollQuestionsPattern = Pattern.compile("strong>(.*?)</strong[\\s\\S]*?table[^>]*?>([\\s\\S]*?)</table>");
    private final static Pattern pollVotedPattern = Pattern.compile("(<input[^>]*?>)&nbsp;<b>([^>]*)</b>");
    private final static Pattern pollNotVotedPattern = Pattern.compile("<td[^>]*>([^<]*?)</td><td[^\\[]*\\[ <b>(.*?)</b>[^\\[]*\\[([^\\]]*)");
    private final static Pattern pollBottomPattern = Pattern.compile("<td class=\"row1\" colspan=\"3\" align=\"center\"><b>([^<]*?)</b>[\\s\\S]*?class=\"formbuttonrow\">([\\s\\S]*?)</td");


    private final static Pattern beforePostsPattern = PatternExtensions.compile("^([\\s\\S]*?)<div data-post");

    //1 - id поста
    //2 - дата
    //3 - номер поста
    //4 - ник
    //5 - урл аватарки
    //6 - куратор или нет, если нет, то null
    //7 - группа
    //8 - online|offline
    //9 - id юзера
    //10 - репа
    //11 - + - репы, действия
    //12 - класс тела
    //13 - тело
    //Да простит меня господь за это. Действие во благо не счетается грехом, ведь верно?
    private final static Pattern postsPattern = Pattern
            .compile("<div data-post=\"(\\d+)\"[^>]*>[\\s\\S]*?post_date[^>]*?>(.*?)&nbsp;[^#]*#(\\d+)[\\s\\S]*?\\[B\\](.*?),\\[/B\\]\\s*'\\)\"\\s*data-av=\"([^\"]*)\">[\\s\\S]*?<span class=\"post_user_info[^\"]*\"[^>]*>(<strong[^>]*>.*?<.strong><br .>)?Группа: (.*?)<br..><font color=\"([^\"]*)\">[\\s\\S]*?mid=(\\d+)[\\s\\S]*?<span id=\"ajaxrep-\\d+\">(.\\d+|\\d+)</span>([\\s\\S]*?)<div class=\"post_body([^>]*?)\"[^>]*?\">([\\s\\S]*?)</div></div>(?=<div data-post=\"\\d+\"[^>]*>|<!-- TABLE FOOTER -->)",
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private final static Pattern editPattern = PatternExtensions.compile("do=edit_post[^\"]*\"");
    private final static Pattern deletePattern = PatternExtensions.compile("onclick=\"[^\"]*seMODdel");


    //createTopic
    private final static Pattern navStripPattern = PatternExtensions.compile("<div id=\"navstrip\">(.*?)</div>");
    private final static Pattern userPattern =
            PatternExtensions.compile("act=login&CODE=03&k=([a-z0-9]{32})");
    private final static Pattern titlePattern = PatternExtensions.compile("<title>(.*?) - 4PDA</title>");
    private final static Pattern descriptionPattern = PatternExtensions.compile("<div class=\"topic_title_post\">([^<]*)<");
    private final static Pattern moderatorTitlePattern = PatternExtensions.compile("onclick=\"return setpidchecks\\(this.checked\\);\".*?>&nbsp;(.*?)<");
    private final static Pattern pagesCountPattern = PatternExtensions.compile("var pages = parseInt\\((\\d+)\\);");
    private final static Pattern lastPageStartPattern = PatternExtensions.compile("(http://4pda.ru)?/forum/index.php\\?showtopic=\\d+&amp;st=(\\d+)");
    private final static Pattern currentPagePattern = PatternExtensions.compile("<span class=\"pagecurrent\">(\\d+)</span>");

    public TopicBodyBuilder parseTopic(String topicPageBody,
                                       Context context, String themeUrl, Boolean spoilFirstPost) throws IOException {
        Log.d("kek", "redirected final theme url = "+themeUrl);
        checkLogin(topicPageBody);

        Pattern pattern = PatternExtensions.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(themeUrl);
        String topicId = null;
        String urlParams = null;
        if (m.find()) {

            topicId = m.group(1);

            urlParams = m.group(3);
        }


        return loadTopic(context, topicId, topicPageBody, spoilFirstPost, m_Logined,
                urlParams);
    }


    public static ExtTopic createTopic(String id, String page) {

        String title = "";
        Matcher m = titlePattern.matcher(page);
        if (m.find()) {
            title = m.group(1);
        }


        ExtTopic topic = new ExtTopic(id, title);

        m = descriptionPattern.matcher(page);
        if (m.find()) {
            topic.setDescription(m.group(1).replace(title + ", ", ""));
        } else {
            m = moderatorTitlePattern.matcher(page);
            if (m.find())
                topic.setDescription(m.group(1).replace(title + ", ", ""));
        }
        m = navStripPattern.matcher(page);
        if (m.find()) {
            final Pattern forumPatter = PatternExtensions.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?.*?showforum=(\\d+).*?\">(.*?)</a>");
            Matcher forumMatcher = forumPatter.matcher(m.group(1));
            while (forumMatcher.find()) {
                if(forumMatcher.group(2).equals("10"))
                    topic.setPostVote(false);
                topic.setForumId(forumMatcher.group(2));
                topic.setForumTitle(forumMatcher.group(3));
            }
        }

        m = userPattern.matcher(page);
        if (m.find()) {
            topic.setAuthKey(m.group(1));
        }

        m = pagesCountPattern.matcher(page);
        if (m.find()) {
            topic.setPagesCount(m.group(1));
        }

        m = lastPageStartPattern.matcher(page);

        while (m.find()) {
            topic.setLastPageStartCount(m.group(2));
        }

        m = currentPagePattern.matcher(page);
        if (m.find()) {
            topic.setCurrentPage(m.group(1));
        } else
            topic.setCurrentPage("1");
        return topic;

    }

    private TopicBodyBuilder loadTopic(Context context,
                                       String id, String topicBody, Boolean spoilFirstPost,
                                       Boolean logined, String urlParams) throws IOException {



        Matcher mainMatcher = beforePostsPattern.matcher(topicBody);

        if (!mainMatcher.find()) {
            Matcher errorMatcher = Pattern.compile("<div class=\"wr va-m text\">([\\s\\S]*?)</div>", Pattern.CASE_INSENSITIVE)
                    .matcher(topicBody);
            if (errorMatcher.find()) {

                throw new NotReportException(errorMatcher.group(1));

            }
            Pattern errorPattern = PatternExtensions.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>");
            errorMatcher = errorPattern.matcher(topicBody);
            if (errorMatcher.find()) {
                final Pattern errorReasonPattern = PatternExtensions.compile("<p>(.*?)</p>");
                Matcher errorReasonMatcher = errorReasonPattern.matcher(errorMatcher.group(1));
                if (errorReasonMatcher.find()) {
                    throw new NotReportException(errorReasonMatcher.group(1));
                }
            }


            if (TextUtils.isEmpty(topicBody))
                throw new NotReportException(context.getString(R.string.server_return_empty_page));
            if (topicBody.startsWith("<h1>"))
                throw new NotReportException(context.getString(R.string.site_response) + Html.fromHtml(topicBody).toString());
            throw new IOException(context.getString(R.string.error_parsing_page)+" id=" + id);
        }
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(context);

        ExtTopic topic = createTopic(id, mainMatcher.group(1));
        topicBody = topicBody.replace("^[\\s\\S]*?<div data-post", "<div data-post").replace("<div class=\"topic_foot_nav\">[\\s\\S]*", "<div class=\"topic_foot_nav\">");

        TopicBodyBuilder topicBodyBuilder = new TopicBodyBuilder(context, logined, topic, urlParams,
                isWebviewAllowJavascriptInterface);

        //Boolean browserStyle = prefs.getBoolean("theme.BrowserStylePreRemove", false);
        topicBodyBuilder.beginTopic();
        //>>ОПРОС
        Matcher pollMatcher = pollFormPattern.matcher(mainMatcher.group(1));
        if (pollMatcher.find()) {
            String pollSource = pollMatcher.group(1);
            StringBuilder pollBuilder = new StringBuilder();
            String percent;
            Matcher temp;

            pollBuilder.append("<form action=\"modules.php\" method=\"get\">");
            pollMatcher = pollTitlePattern.matcher(pollSource);
            if(pollMatcher.find()){
                if(!pollMatcher.group(1).equals("-"))
                    pollBuilder.append("<div class=\"poll_title\"><span>").append(pollMatcher.group(1)).append("</span></div>");
            }
            pollBuilder.append("<div class=\"poll_body\">");
            boolean voted = false;

            pollMatcher = pollQuestionsPattern.matcher(pollSource);
            while (pollMatcher.find()){
                if(!pollMatcher.group(2).contains("input"))
                    voted = true;
                pollBuilder.append("<div class=\"poll_theme\">");
                pollBuilder.append("<div class=\"theme_title\"><span>").append(pollMatcher.group(1)).append("</span></div>");
                pollBuilder.append("<div class=\"items").append(voted ? " voted" : "").append("\">");
                if (voted) {
                    temp = pollNotVotedPattern.matcher(pollMatcher.group(2));
                    while (temp.find()){
                        pollBuilder.append("<div class=\"item\">");
                        pollBuilder.append("<span class=\"name\"><span>").append(temp.group(1)).append("</span></span>");
                        pollBuilder.append("<span class=\"num_votes\"><span>").append(temp.group(2)).append("</span></span>");
                        pollBuilder.append("<div class=\"range\">");
                        percent = temp.group(3).replace(",", ".");
                        pollBuilder.append("<div class=\"range_bar\" style=\"width:").append(percent).append(";\"></div>");
                        pollBuilder.append("<span class=\"value\"><span>").append(percent).append("</span></span>");
                        pollBuilder.append("</div>");
                        pollBuilder.append("</div>");
                    }
                } else {
                    temp = pollVotedPattern.matcher(pollMatcher.group(2));
                    while (temp.find()){
                        pollBuilder.append("<label class=\"item\">");
                        pollBuilder.append(temp.group(1));
                        pollBuilder.append("<span class=\"icon\"></span>");
                        pollBuilder.append("<span class=\"item_body\"><span class=\"name\">").append(temp.group(2)).append("</span></span>");
                        pollBuilder.append("</label>");
                    }
                }
                pollBuilder.append("</div>");
                pollBuilder.append("</div>");
            }
            pollBuilder.append("</div>");


            pollMatcher = pollBottomPattern.matcher(pollSource);
            if(pollMatcher.find()){
                pollBuilder.append("<div class=\"votes_info\"><span>").append(pollMatcher.group(1)).append("</span></div>");
                if(logined) {
                    pollBuilder.append("<div class=\"buttons\">").append(pollMatcher.group(2)).append("</div>");
                }
            }

            pollBuilder.append("<input type=\"hidden\" name=\"addpoll\" value=\"1\" /></form>");
            topicBodyBuilder.addPoll(
                    pollBuilder.toString()
                            .replace("go_gadget_show()", ForPdaWebInterface.NAME + ".go_gadget_show()")
                            .replace("go_gadget_vote()", ForPdaWebInterface.NAME + ".go_gadget_vote()"),
                    urlParams != null && urlParams.contains("poll_open=true"));
        }
        //<<опрос
        topicBodyBuilder.openPostsList();


        mainMatcher = postsPattern.matcher(topicBody);



        //String today = Functions.getToday();
        //String yesterday = Functions.getYesterToday();
        org.softeg.slartus.forpdaplus.classes.Post post = null;
        Boolean spoil = spoilFirstPost;
        String str;
        Matcher m;
        while (mainMatcher.find()) {

            post = new org.softeg.slartus.forpdaplus.classes.Post(mainMatcher.group(1), mainMatcher.group(2), mainMatcher.group(3));
            post.setAuthor(mainMatcher.group(4));
            post.setAvatarFileName(mainMatcher.group(5));
            if (mainMatcher.group(6) != null)
                post.setCurator();
            post.setUserGroup(mainMatcher.group(7));
            post.setUserState(mainMatcher.group(8));
            post.setUserId(mainMatcher.group(9));
            post.setUserReputation(mainMatcher.group(10));
            str = mainMatcher.group(11);
            if(str.contains("win_minus"))
                post.setCanMinusRep(true);
            if(str.contains("win_add"))
                post.setCanPlusRep(true);
            m = editPattern.matcher(str);
            if (m.find()) {
                post.setCanEdit(true);
            }
            m = deletePattern.matcher(str);
            if (m.find()) {
                post.setCanDelete(true);
                // если автор поста не совпадает с текущим пользователем и есть возможность удалить-значит, модератор
                if (post.getUserId() != null && !post.getUserId().equals(Client.getInstance().UserId)) {
                    topicBodyBuilder.setMMod(true);
                }
            }
            post.setBody("<div class=\"post_body " + mainMatcher.group(12) + "\">" + mainMatcher.group(13)+"</div>");
            topicBodyBuilder.addPost(post, spoil);
            spoil = false;
        }
        topicBodyBuilder.endTopic();
        return topicBodyBuilder;
    }

    public TopicReadingUsers getTopicReadingUsers(String topicId) throws IOException {
        return TopicApi.getReadingUsers(this, topicId);
    }


    public void markAllForumAsRead() throws Throwable {
        ForumsApi.markAllAsRead(this);
    }

    public String getThemeForumId(CharSequence themeId) throws IOException {

        String res = performGet("http://4pda.ru/forum/lofiversion/index.php?t" + themeId + ".html");

        return parseForumId(res);

    }

    private String parseForumId(String pageBody) {
        Pattern pattern = Pattern.compile("<div class='ipbnav'>.*<a href='http://4pda.ru/forum/lofiversion/index.php\\?f(\\d+).html'>.*?</a></div>",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(pageBody);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private void setThemeForumAndAuthKey(ExtTopic topic) throws IOException {

        String res = performGet("http://4pda.ru/forum/lofiversion/index.php?t" + topic.getId() + ".html");

        if (TextUtils.isEmpty(topic.getForumId())) {
            String forumId = parseForumId(res);
            if (forumId != null) {
                topic.setForumId(forumId);
                LogUtil.E("BOOM DOOM", "forum id " + forumId);
            }
        }

        if (TextUtils.isEmpty(topic.getAuthKey())) {
            Pattern pattern = Pattern.compile("name=\"auth_key\" value=\"(.*)\"",
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(res);
            if (m.find()) {
                topic.setAuthKey(m.group(1));
                LogUtil.E("BOOM DOOM", "key " + m.group(1));
            }
        }

    }


    public DownloadTask downloadFile(Context context, String url, int notificationId, String tempFilePath) {
        DownloadTask downloadTask = m_DownloadTasks.add(url, notificationId, null);

        Intent intent = new Intent(context, DownloadsService.class);
        intent.putExtra(DownloadsService.DOWNLOAD_FILE_ID_KEY, notificationId);
        intent.putExtra(DownloadsService.DOWNLOAD_FILE_TEMP_NAME_KEY, tempFilePath);
        intent.putExtra("receiver", new DownloadReceiver(new Handler(), context));
        context.startService(intent);

        return downloadTask;
    }

    private DownloadTasks m_DownloadTasks = new DownloadTasks();

    public DownloadTasks getDownloadTasks() {
        return m_DownloadTasks;
    }


}