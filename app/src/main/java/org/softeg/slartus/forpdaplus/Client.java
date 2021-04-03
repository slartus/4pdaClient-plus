package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
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
import org.softeg.slartus.forpdacommon.CollectionUtils;
import org.softeg.slartus.forpdacommon.HttpHelper;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.classes.DownloadTask;
import org.softeg.slartus.forpdaplus.classes.DownloadTasks;
import org.softeg.slartus.forpdaplus.classes.Forum;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ForumsTableOld;
import org.softeg.slartus.forpdaplus.fragments.topic.ForPdaWebInterface;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository;
import org.softeg.slartus.forpdaplus.utils.UploadUtils;

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


    static final Client INSTANCE = new Client();

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

    public List<HttpCookie> getCookies() {
        return Http.Companion.getInstance().getCookieStore().getCookies();
    }

    public Users getTopicWritersUsers(String topicId) throws IOException {
        return org.softeg.slartus.forpdaapi.TopicApi.getWriters(this, topicId);
    }

    public static Client getInstance() {
        return INSTANCE;  //To change body of created methods use File | Settings | File Templates.
    }

    public void likeNews(String postId) throws IOException {
        NewsApi.like(this, postId);
    }

    public void likeComment(final String id, final String comment) throws IOException {
        NewsApi.likeComment(this, id, comment);
    }

    public interface OnProgressPositionChangedListener {
        void onProgressChanged(Context context, DownloadTask downloadTask, Exception ex);
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
        return UserInfoRepository.Companion.getInstance().getName();
    }

    public Boolean getLogined() {
        return UserInfoRepository.Companion.getInstance().getUserInfo().getValue().getLogined();
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
        boolean logined = UserInfoRepository.Companion.getInstance().getLogined();

        m_LoginFailedReason = logined ? null : loginResult.getLoginError();

        if (logined)
            UserInfoRepository.Companion.getInstance().setName(loginResult.getUserLogin().toString());
        m_K = loginResult.getK().toString();

        Http.Companion.getInstance().getCookieStore().addCustom("4pda.User", UserInfoRepository.Companion.getInstance().getName());
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
                UserInfoRepository.Companion.getInstance().setName(cookie.getValue());
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

    int getQmsCount() {
        return UserInfoRepository.Companion.getInstance().getQmsCount();
    }

    public void setQmsCount(int count) {
        UserInfoRepository.Companion.getInstance()
                .setQmsCount(count);
    }

    public void check(String page) {
        checkMentions(page);
        checkMails(page);
    }

    private void checkMentions(String page) {
        Integer mentionsCount = MentionsParser.Companion.getInstance().parseCount(page);
        UserInfoRepository.Companion.getInstance()
                .setMentionsCount(mentionsCount);
    }

    private void checkMails(String pageBody) {
        setQmsCount(QmsApi.INSTANCE.getNewQmsCount(pageBody));
    }

    Boolean logout() throws Throwable {
        String res = ProfileApi.logout(this, m_K);

        Http.Companion.getInstance().getCookieStore().removeAll();

        checkLogin(res);
        if (UserInfoRepository.Companion.getInstance().getUserInfo().getValue().getLogined())
            m_LoginFailedReason = App.getContext().getString(R.string.bad_logout);

        return !UserInfoRepository.Companion.getInstance().getUserInfo().getValue().getLogined();
    }

    public Forum loadForums() throws Exception {
        return ForumsTableOld.loadForumsTree();
    }


    public AppResponse preformGetWithProgress(String url, OnProgressChangedListener progressChangedListener) throws IOException {
        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.receiving_data));
        AppResponse body = performGet(url);
        doOnOnProgressChanged(progressChangedListener, App.getContext().getString(R.string.processing_data));
        return body;
    }


    //loadTopic
    private final static Pattern pollFormPattern = Pattern.compile("<form[^>]*action=\"[^\"]*addpoll=1[^\"]*\"[^>]*>([\\s\\S]*?)</form>", Pattern.CASE_INSENSITIVE);
    private final static Pattern pollTitlePattern = Pattern.compile("<b>(.*?)</b>");
    private final static Pattern pollQuestionsPattern = Pattern.compile("strong>(.*?)</strong[\\s\\S]*?table[^>]*?>([\\s\\S]*?)</table>");
    private final static Pattern pollVotedPattern = Pattern.compile("(<input[^>]*?>)&nbsp;<b>([^>]*)</b>");
    @SuppressWarnings("RegExpRedundantEscape")
    private final static Pattern pollNotVotedPattern = Pattern.compile("<td[^>]*>([^<]*?)</td><td[^\\[]*\\[ <b>(.*?)</b>[^\\[]*\\[([^\\]]*)");
    private final static Pattern pollBottomPattern = Pattern.compile("<td class=\"row1\" colspan=\"3\" align=\"center\"><b>([^<]*?)</b>[\\s\\S]*?class=\"formbuttonrow\">([\\s\\S]*?)</td");


    private final static Pattern beforePostsPattern = PatternExtensions.compile("^([\\s\\S]*?)<div data-post");


    //createTopic
    private final static Pattern navStripPattern = PatternExtensions.compile("<div id=\"navstrip\">(.*?)</div>");

    private final static Pattern titlePattern = PatternExtensions.compile("<title>(.*?) - 4PDA</title>");
    private final static Pattern descriptionPattern = PatternExtensions.compile("<div class=\"topic_title_post\">([^<]*)<");
    private final static Pattern moderatorTitlePattern = PatternExtensions.compile("onclick=\"return setpidchecks\\(this.checked\\);\".*?>&nbsp;(.*?)<");
    private final static Pattern pagesCountPattern = PatternExtensions.compile("var pages = parseInt\\((\\d+)\\);");
    private final static Pattern lastPageStartPattern = PatternExtensions.compile("<a href=\"([^\"]*?4pda.ru)?\\/forum\\/index.php\\?showtopic=\\d+&amp;st=(\\d+)\"");
    private final static Pattern currentPagePattern = PatternExtensions.compile("<span class=\"pagecurrent\">(\\d+)</span>");

    public TopicBodyBuilder parseTopic(String topicPageBody,
                                       Context context, String themeUrl, Boolean spoilFirstPost) throws IOException {
        checkLogin(topicPageBody);

        Pattern pattern = PatternExtensions.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(themeUrl);
        String topicId = null;
        String urlParams = null;
        if (m.find()) {

            topicId = m.group(1);

            urlParams = m.group(3);
        }


        return loadTopic(context, topicId, topicPageBody, spoilFirstPost, UserInfoRepository.Companion.getInstance().getUserInfo().getValue().getLogined(),
                urlParams);
    }


    private static ExtTopic createTopic(String id, String page) {

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
            final Pattern forumPatter = PatternExtensions.compile("<a href=\"([^\"]*?4pda.ru)?\\/forum\\/index.php\\?.*?showforum=(\\d+).*?\">(.*?)<\\/a>");
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
            throw new IOException(context.getString(R.string.error_parsing_page) + " id=" + id);
        }
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface();

        ExtTopic topic = createTopic(id, mainMatcher.group(1));
        topicBody = topicBody.replace("^[\\s\\S]*?<div data-post", "<div data-post").replace("<div class=\"topic_foot_nav\">[\\s\\S]*", "<div class=\"topic_foot_nav\">");

        TopicBodyBuilder topicBodyBuilder = new TopicBodyBuilder(context, logined, topic, urlParams,
                isWebviewAllowJavascriptInterface);

        Document doc = Jsoup.parse(topicBody);

        //>>ОПРОС
//        Element pollElement = doc.selectFirst("form[action*=addpoll=1]");
//        if (pollElement != null) {
//            StringBuilder pollBuilder = new StringBuilder();
//            pollBuilder.append("<form action=\"modules.php\" method=\"get\">");
//            Element el = pollElement.selectFirst("th");
//            if (el != null)
//                pollBuilder.append("<div class=\"poll_title\"><span>").append(el.text()).append("</span></div>");
//            boolean voted = false;
//        }
        //Boolean browserStyle = prefs.getBoolean("theme.BrowserStylePreRemove", false);
        topicBodyBuilder.beginTopic();

        // TODO!: переделать на jsoup
        Matcher pollMatcher = pollFormPattern.matcher(mainMatcher.group(1));
        if (pollMatcher.find()) {
            String pollSource = pollMatcher.group(1);
            StringBuilder pollBuilder = new StringBuilder();
            String percent;
            Matcher temp;

            pollBuilder.append("<form action=\"modules.php\" method=\"get\">");
            pollMatcher = pollTitlePattern.matcher(pollSource);
            if (pollMatcher.find()) {
                if (!pollMatcher.group(1).equals("-"))
                    pollBuilder.append("<div class=\"poll_title\"><span>").append(pollMatcher.group(1)).append("</span></div>");
            }
            pollBuilder.append("<div class=\"poll_body\">");
            boolean voted = false;

            pollMatcher = pollQuestionsPattern.matcher(pollSource);
            while (pollMatcher.find()) {
                if (!pollMatcher.group(2).contains("input"))
                    voted = true;
                pollBuilder.append("<div class=\"poll_theme\">");
                pollBuilder.append("<div class=\"theme_title\"><span>").append(pollMatcher.group(1)).append("</span></div>");
                pollBuilder.append("<div class=\"items").append(voted ? " voted" : "").append("\">");
                if (voted) {
                    temp = pollNotVotedPattern.matcher(pollMatcher.group(2));
                    while (temp.find()) {
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
                    while (temp.find()) {
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
            if (pollMatcher.find()) {
                pollBuilder.append("<div class=\"votes_info\"><span>").append(pollMatcher.group(1)).append("</span></div>");
                if (logined) {
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


        org.softeg.slartus.forpdaplus.classes.Post post;
        Boolean spoil = spoilFirstPost;

        for (Element postEl : doc.select("div[data-post]")) {
            // id поста-обязателен
            String postId = postEl.attr("data-post");
            String postDate = "";
            String postNumber = "";

            Element postHeaderEl = postEl.selectFirst("div.post_header");
            if (postHeaderEl == null) continue;
            // дата и номер поста не обязательны
            Element el = postHeaderEl.selectFirst("span.post_date");
            if (el != null) {
                postDate = ((TextNode) el.childNode(0)).text().replace("|", "").trim();
                postNumber = ((Element) el.childNode(1)).text().replace("#", "").trim();
            }
            // тело поста-обязательно
            el = postEl.selectFirst("div.post_body");
            if (el == null) continue;
            String postBody = "<div class=\"" + CollectionUtils.join(" ", el.classNames()) + "\">" + el.html() + "</div>";

            post = new org.softeg.slartus.forpdaplus.classes.Post(postId, postDate, postNumber);
            post.setBody(postBody);

            // всё остальное не обязательно - читать темы можно по крайней мере

            // пользователь
            el = postHeaderEl.selectFirst("span.post_nick");
            if (el != null) {
                // статус автора
                Element el1 = el.selectFirst("font");
                if (el1 != null)
                    post.setUserState(el1.attr("color"));
                // ник и аватар автора
                el1 = el.selectFirst("a[data-av]");
                if (el1 != null) {
                    post.setAvatarFileName(el1.attr("data-av"));// аватар
                    List<TextNode> textNodes = el1.textNodes();
                    if (textNodes.size() > 0)
                        post.setAuthor(el1.textNodes().get(0).toString());// ник
                    else
                        post.setAuthor(el1.html());// ник
                }
                el1 = el.selectFirst("a[href*=showuser]");
                if (el1 != null) {
                    post.setUserId(Uri.parse(el1.attr("href")).getQueryParameter("showuser"));
                    if (TextUtils.isEmpty(post.getNick()))
                        post.setAuthor(el1.html());
                    if (TextUtils.isEmpty(post.getAvatarFileName()))
                        post.setAvatarFileName(el1.attr("data-av"));
                }

            }

            // информация о пользователе
            el = postHeaderEl.selectFirst("span.post_user_info");
            if (el != null) {
                Element el1 = el.selectFirst("span>span");// группа пользователя
                if (el1 != null)
                    post.setUserGroup(el1.outerHtml());
                if (el.selectFirst("strong[class=t-cur-title]") != null)// куратор
                    post.setCurator();
            }

            // репутация
            el = postHeaderEl.selectFirst("a[href*=act=rep&view=history]");
            if (el != null)
                post.setUserReputation(el.text());
            post.setCanPlusRep(postHeaderEl.selectFirst("a[href*=act=rep&view=win_add]") != null);
            post.setCanMinusRep(postHeaderEl.selectFirst("a[href*=act=rep&view=win_minus]") != null);

            // операции над постом
            el = postHeaderEl.selectFirst("span.post_action");
            if (el != null) {
                post.setCanEdit(el.selectFirst("a[href*=do=edit_post]") != null);
                post.setCanDelete(el.selectFirst("a[href*=tact=delete]") != null);
                if (post.getCanDelete()) {
                    // если автор поста не совпадает с текущим пользователем и есть возможность удалить-значит, модератор
                    if (post.getUserId() != null && !post.getUserId().equals(UserInfoRepository.Companion.getInstance().getId())) {
                        topicBodyBuilder.setMMod(true);
                    }
                }
            }
            topicBodyBuilder.addPost(post, spoil);
            spoil = false;
        }
//        mainMatcher = postsPattern.matcher(topicBody);
//
//
//        //String today = Functions.getToday();
//        //String yesterday = Functions.getYesterToday();
//
//        while (mainMatcher.find()) {
//
//            post = new org.softeg.slartus.forpdaplus.classes.Post(mainMatcher.group(1), mainMatcher.group(2), mainMatcher.group(3));
//            post.setUserState(mainMatcher.group(4));
//            post.setAvatarFileName(mainMatcher.group(5));
//            post.setAuthor(mainMatcher.group(6));
//            post.setUserId(mainMatcher.group(7));
//            if (mainMatcher.group(8) != null) {
//                post.setCurator();
//            }
//            post.setUserGroup(mainMatcher.group(9));
//            str = mainMatcher.group(10);
//            if (str.contains("win_minus")) {
//                post.setCanMinusRep(true);
//            }
//            if (str.contains("win_add")) {
//                post.setCanPlusRep(true);
//            }
//            m = editPattern.matcher(str);
//            if (m.find()) {
//                post.setCanEdit(true);
//            }
//            m = deletePattern.matcher(str);
//            if (m.find()) {
//                post.setCanDelete(true);
//                // если автор поста не совпадает с текущим пользователем и есть возможность удалить-значит, модератор
//                if (post.getUserId() != null && !post.getUserId().equals(Client.getInstance().UserId)) {
//                    topicBodyBuilder.setMMod(true);
//                }
//            }
//            post.setUserReputation(mainMatcher.group(11));
//            post.setBody("<div class=\"post_body " + mainMatcher.group(12) + "\">" + mainMatcher.group(13) + "</div>");
//            topicBodyBuilder.addPost(post, spoil);
//            spoil = false;
//        }
        topicBodyBuilder.endTopic();
        return topicBodyBuilder;
    }

    public TopicReadingUsers getTopicReadingUsers(String topicId) throws IOException {
        return TopicApi.getReadingUsers(this, topicId);
    }


    void markAllForumAsRead() throws Throwable {
        ForumsApi.Companion.markAllAsRead(this);
    }

    private final DownloadTasks m_DownloadTasks = new DownloadTasks();

    public DownloadTasks getDownloadTasks() {
        return m_DownloadTasks;
    }


}
