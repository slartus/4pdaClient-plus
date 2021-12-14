package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

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
import org.softeg.slartus.forpdaapi.parsers.MentionsParser;
import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdacommon.HttpHelper;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl;
import org.softeg.slartus.forpdaplus.utils.UploadUtils;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
public class Client implements IHttpClient {
    private String m_K = "";

    private Client() {
        checkLoginByCookies();
    }


    public String getAuthKey() {
        return m_K;
    }


    public static final Client INSTANCE = new Client();

    public URI getRedirectUri() {
        return HttpHelper.getRedirectUri();
    }

    public void deletePost(String postId, CharSequence authKey) throws IOException {
        PostApi.INSTANCE.delete(this, postId, authKey);
    }

    public Boolean changeReputation(String postId, String userId, String type, String message, Map<String, String> outParams) throws IOException {

        return ReputationsApi.changeReputation(this, postId, userId, type, message, outParams);

    }

    public String claim(String themeId, String postId, String message) throws IOException {
        String error = PostApi.INSTANCE.claim(this, themeId, postId, message);
        if (!TextUtils.isEmpty(error))
            return error;
        return App.getContext().getString(R.string.complaint_sent);
    }


    public Boolean hasLoginCookies() {
        boolean session = false;
        boolean pass_hash = false;
        boolean member = false;

        for (HttpCookie cookie : Http.Companion.getInstance().getCookieStore().getCookies()) {
            if (!session && cookie.getName().equals("session_id"))
                session = true;
            else if (!pass_hash && cookie.getName().equals("pass_hash"))
                pass_hash = true;
            else if (!member && cookie.getName().equals("member_id"))
                member = true;
        }

        return session && pass_hash && member;
    }

    public AppResponse performGetFullVersion(String s) throws IOException {


        //HttpHelper httpHelper = new HttpHelper();
        AppResponse res;

        // s="https://4pda.ru/2009/12/28/18506/#comment-363525";
        res = Http.Companion.getInstance().performGetFull(s);

        if (TextUtils.isEmpty(res.getResponseBody()))
            throw new NotReportException(App.getContext().getString(R.string.server_return_empty_page));
        // m_HttpHelper.close();
        return res;
    }

    public AppResponse performGet(String s) throws IOException {
        return performGet(s, true, true);
    }

    public AppResponse performGet(String s, Boolean checkEmptyResult, Boolean checkLoginAndMails) throws IOException {


        AppResponse res;

        // s="https://4pda.ru/2009/12/28/18506/#comment-363525";
        res = HttpHelper.performGet(s);

        if (checkEmptyResult && TextUtils.isEmpty(res.getResponseBody()))
            throw new NotReportException(App.getContext().getString(R.string.server_return_empty_page));
        else if (checkLoginAndMails) {
            checkLogin(res.getResponseBody());
            if (!s.contains("xhr")) {
                checkMails(res.getResponseBody());
                checkMentions(res.getResponseBody());
            }
        }
        // m_HttpHelper.close();
        return res;
    }

    public AppResponse performPost(String s, Map<String, String> additionalHeaders) throws IOException {

        // s="https://4pda.ru/2009/12/28/18506/#comment-363525";
        return HttpHelper.performPost(s, additionalHeaders);
    }


    public AppResponse uploadFile(String url, String filePath, Map<String, String> additionalHeaders,
                                  ProgressState progress) {
        return UploadUtils.okUploadFile(url, filePath, additionalHeaders, progress);
    }

    public AppResponse performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException {
        return HttpHelper.performPost(s, additionalHeaders);
    }

    @Override
    public AppResponse performPost(String s, List<NameValuePair> additionalHeaders) {
        return HttpHelper.performPost(s, additionalHeaders);
    }

    public AppResponse performPost(String url, String json) {
        return Http.Companion.getInstance().performPost(url, json);
    }

    public List<HttpCookie> getCookies() {
        return Http.Companion.getInstance().getCookieStore().getCookies();
    }

    public Users getTopicWritersUsers(String topicId) throws IOException {
        return org.softeg.slartus.forpdaapi.TopicApi.getWriters(this, topicId);
    }

    public static Client getInstance() {
        return INSTANCE;  //To change body of created methods use File | Settings | File Templates.
    }

    public void likeNews(String postId) {
        NewsApi.like(this, postId);
    }

    public void likeComment(final String id, final String comment) {
        NewsApi.likeComment(this, id, comment);
    }

    private void doOnOnProgressChanged(OnProgressChangedListener listener, String state) {
        if (listener != null) {
            listener.onProgressChanged(state);
        }
    }

    public void showLoginForm(Context mContext) {
        try {

            final LoginDialog loginDialog = new LoginDialog(mContext);

            MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.login)
                    .customView(loginDialog.getView(), true)
                    .positiveText(R.string.login)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog1, which) -> loginDialog.connect())
                    .build();
            Window window = dialog.getWindow();
            assert window != null;
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();

        } catch (Exception ex) {
            AppLog.e(mContext, ex);
        }
    }

    public String getUser() {
        return UserInfoRepositoryImpl.getInstance().getName();
    }

    public Boolean getLogined() {
        return UserInfoRepositoryImpl.getInstance().getLogined();
    }

    private CharSequence m_LoginFailedReason;

    String getLoginFailedReason() {
        return m_LoginFailedReason == null ? null : m_LoginFailedReason.toString();
    }

    public AppResponse reply(String forumId, String themeId, String authKey, String post,
                             Boolean enablesig, Boolean enableemo, Boolean quick, String addedFileList) throws IOException {
        return PostApi.INSTANCE.reply(forumId, themeId, authKey, null, post,
                enablesig, enableemo, addedFileList, quick);
    }

    Boolean login(String login, String password, Boolean privacy,
                  String capVal, String capTime, String capSig) throws Exception {
        Http.Companion.getInstance().getCookieStore().removeAll();

        LoginResult loginResult = ProfileApi.login(login, password, privacy, capVal, capTime, capSig);
        boolean logined = UserInfoRepositoryImpl.Companion.getInstance().getLogined();

        m_LoginFailedReason = logined ? null : loginResult.getLoginError();

        if (logined)
            UserInfoRepositoryImpl.Companion.getInstance().setName(loginResult.getUserLogin().toString());
        m_K = loginResult.getK().toString();

        Http.Companion.getInstance().getCookieStore().addCustom("4pda.User", UserInfoRepositoryImpl.Companion.getInstance().getName());
        Http.Companion.getInstance().getCookieStore().addCustom("4pda.K", m_K);


        return logined;
    }

    private final Pattern checkLoginPattern = PatternExtensions.compile("\\Wk=([a-z0-9]{32})");

    public void checkLoginByCookies() {
        try {
            checkLogin();
        } catch (Throwable ignored) {

        }
    }

    private void checkLogin() {
        for (HttpCookie cookie : Http.Companion.getInstance().getCookieStore().getCookies()) {
            if ("4pda.User".equals(cookie.getName())) {
                UserInfoRepositoryImpl.Companion.getInstance().setName(cookie.getValue());
            } else if ("4pda.K".equals(cookie.getName())) {
                m_K = cookie.getValue();
            }
        }
    }

    private void checkLogin(String pageBody) {
        checkLogin();

        Matcher m = checkLoginPattern.matcher(pageBody);
        if (m.find()) {
            m_K = m.group(1);
        } else {
            m_K = "";
        }
    }

    public void setQmsCount(int count) {
        UserInfoRepositoryImpl.Companion.getInstance()
                .setQmsCount(count);
    }

    public void check(String page) {
        checkMentions(page);
        checkMails(page);
    }

    private void checkMentions(String page) {
        Integer mentionsCount = MentionsParser.Companion.getInstance().parseCount(page);
        UserInfoRepositoryImpl.Companion.getInstance()
                .setMentionsCount(mentionsCount);
    }

    private void checkMails(String pageBody) {
        setQmsCount(QmsApi.INSTANCE.getNewQmsCount(pageBody));
    }

    Boolean logout() throws Throwable {
        String res = ProfileApi.logout(this, m_K);

        Http.Companion.getInstance().getCookieStore().removeAll();

        checkLogin(res);
        if (UserInfoRepositoryImpl.Companion.getInstance().getLogined())
            m_LoginFailedReason = App.getContext().getString(R.string.bad_logout);

        return !UserInfoRepositoryImpl.Companion.getInstance().getLogined();
    }

    public AppResponse preformGetWithProgress(String url, OnProgressChangedListener progressChangedListener) throws IOException {
        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.receiving_data));
        AppResponse body = performGet(url);
        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.processing_data));
        return body;
    }

    //createTopic
    private final static Pattern navStripPattern = PatternExtensions.compile("<div id=\"navstrip\">(.*?)</div>");

    private final static Pattern titlePattern = PatternExtensions.compile("<title>(.*?) - 4PDA</title>");
    private final static Pattern descriptionPattern = PatternExtensions.compile("<div class=\"topic_title_post\">([^<]*)<");
    private final static Pattern moderatorTitlePattern = PatternExtensions.compile("onclick=\"return setpidchecks\\(this.checked\\);\".*?>&nbsp;(.*?)<");
    private final static Pattern pagesCountPattern = PatternExtensions.compile("var pages = parseInt\\((\\d+)\\);");
    private final static Pattern lastPageStartPattern = PatternExtensions.compile("<a href=\"([^\"]*?" + HostHelper.getHost() + ")?\\/forum\\/index.php\\?showtopic=\\d+&amp;st=(\\d+)\"");
    private final static Pattern currentPagePattern = PatternExtensions.compile("<span class=\"pagecurrent\">(\\d+)</span>");

    public TopicBodyBuilder parseTopic(String topicPageBody,
                                       Context context, String themeUrl, Boolean spoilFirstPost) throws IOException {
        checkLogin(topicPageBody);

        Pattern pattern = PatternExtensions.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(themeUrl);
        String topicId;
        String urlParams;
        if (m.find()) {
            topicId = m.group(1);

            urlParams = m.group(3);
        } else {
            throw new NotReportException("topic id not found in " + themeUrl);
        }

        return TopicParser.loadTopic(context, topicId, topicPageBody, spoilFirstPost,
                UserInfoRepositoryImpl.Companion.getInstance().getLogined(),
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
            final Pattern forumPatter = PatternExtensions.compile("<a href=\"([^\"]*?" + HostHelper.getHost() + ")?\\/forum\\/index.php\\?.*?showforum=(\\d+).*?\">(.*?)<\\/a>");
            Matcher forumMatcher = forumPatter.matcher(m.group(1));
            while (forumMatcher.find()) {
                if (forumMatcher.group(2).equals("10"))
                    topic.setPostVote(false);
                topic.setForumId(forumMatcher.group(2));
                topic.setForumTitle(forumMatcher.group(3));
            }
        }

        if (Client.getInstance().getAuthKey() != null && !Client.getInstance().getAuthKey().isEmpty())
            topic.setAuthKey(Client.getInstance().getAuthKey());


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

    public TopicReadingUsers getTopicReadingUsers(String topicId) throws IOException {
        return TopicApi.getReadingUsers(this, topicId);
    }

    void markAllForumAsRead() throws Throwable {
        ForumsApi.Companion.markAllAsRead(this);
    }


}
