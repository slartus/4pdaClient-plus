package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.softeg.slartus.forpdacommon.HtmlOutUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository;

import java.util.Hashtable;

/**
 * User: slinkin
 * Date: 26.03.12
 * Time: 16:50
 */
public class TopicBodyBuilder extends HtmlBuilder {

    static final String NICK_SNAPBACK_TEMPLATE = "[SNAPBACK]%s[/SNAPBACK] [B]%s,[/B] \n";
    private Boolean m_Logined, m_IsWebviewAllowJavascriptInterface;
    private ExtTopic m_Topic;
    private String m_UrlParams;
    private HtmlPreferences m_HtmlPreferences;
    private Hashtable<String, String> m_EmoticsDict;
    private boolean m_MMod = false;
    private Boolean m_IsLoadImages;
    private Boolean m_IsShowAvatars;

    public TopicBodyBuilder(Context context, Boolean logined, ExtTopic topic, String urlParams,
                            Boolean isWebviewAllowJavascriptInterface) {

        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(context);
        m_EmoticsDict = Smiles.getSmilesDict();

        m_IsWebviewAllowJavascriptInterface = isWebviewAllowJavascriptInterface;
        m_Logined = logined;
        m_UrlParams = urlParams;
        m_Topic = topic;
        m_IsLoadImages = WebViewExternals.isLoadImages("theme");
        m_IsShowAvatars = Preferences.Topic.isShowAvatars();
    }

    @Override
    public void addScripts() {
        if (m_UrlParams != null)
            m_Body.append("<script type=\"text/javascript\">window.FORPDA_POST = \"").append(m_UrlParams.replaceFirst("(?:^|\\n)[\\s\\S]*?(#.*|anchor=.*)", "$1")).append("\";</script>\n");
        super.addScripts();

    }

    public void beginTopic() {
        String desc = TextUtils.isEmpty(m_Topic.getDescription()) ? "" : (", " + m_Topic.getDescription());
        super.beginHtml(m_Topic.getTitle() + desc);
        super.beginBody("topic", null, m_IsLoadImages);

        m_Body.append("<div id=\"topMargin\" style=\"height:").append(ACTIONBAR_TOP_MARGIN).append(";\"></div>");

        m_Body.append("<div class=\"panel top\">");
        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body, m_Topic.getCurrentPage(), m_Topic.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, true);
        }
        m_Body.append(getTitleBlock()).append("</div>");
    }

    public void openPostsList() {
        m_Body.append("<div class=\"posts_list\">");
    }

    public void endTopic() {
        m_Body.append("</div>");
        m_Body.append("<div name=\"entryEnd\" id=\"entryEnd\"></div>\n");
        m_Body.append("<div class=\"panel bottom\">");
        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body, m_Topic.getCurrentPage(), m_Topic.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, false);
        }
        if (Preferences.Topic.getReadersAndWriters()) {
            m_Body.append("<div class=\"who\"><a id=\"viewers\" ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showReadingUsers"))
                    .append("><span>Кто читает тему</span></a>\n");
            m_Body.append("<a id=\"writers\" ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showWriters"))
                    .append("><span>Кто писал сообщения</span></a></div>\n");
        }

        m_Body.append(getTitleBlock()).append("</div><div id=\"bottomMargin\"></div>");


        //m_Body.append("<div style=\"padding-top:"+ACTIONBAR_TOP_MARGIN+"\"></div>\n");
        super.endBody();
        super.endHtml();
    }

    private String getSpoiler(String title, String body, Boolean opened) {
        return
                (
                        m_HtmlPreferences.isSpoilerByButton() ?
                                "<div class=\"hat\"><div class='hidetop ".concat(opened ? "open " : "close").concat("' style='cursor:pointer;' ><input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility(this)\"/>")
                                        .concat(title)
                                :
                                "<div class=\"hat\"><div class='hidetop ".concat(opened ? "open " : "close").concat("' style='cursor:pointer;' onclick=\"openHat(this);\">")
                                        .concat(title)
                ).concat("</div><div class='hidemain'").concat(opened ? " " : " style=\"display:none\"").concat(">").concat(body).concat("</div></div>");
    }

    public void addPost(Post post, Boolean spoil) {
        m_Body.append("<div name=\"entry")
                .append(post.getId())
                .append("\"class=\"jump\" style=\"position: absolute; width: 100%; margin-top:-")
                .append(ACTIONBAR_TOP_MARGIN)
                .append("; left: 0;\" id=\"entry")
                .append(post.getId())
                .append("\"></div>\n");
        m_Body
                .append("<div class=\"post_container\" name=\"del")
                .append(post.getId())
                .append("\">");

        addPostHeader(m_Body, post);

        //m_Body.append("<div id=\"msg").append(post.getId()).append("\" name=\"msg").append(post.getId()).append("\">");

        String postBody = post.getBody().trim();
        if (m_HtmlPreferences.isSpoilerByButton())
            postBody = HtmlPreferences.modifySpoiler(postBody);

        if (spoil)
            m_Body.append(getSpoiler("<b><span>Показать шапку</span></b>", postBody, false));
        else
            m_Body.append(postBody);
        //m_Body.append("</div>\n\n");

        addFooter(m_Body, post);
        m_Body.append("<div class=\"between_messages\"></div>");
        m_Body.append("</div>");
    }

    public String getBody() {
        String res;
        res = HtmlPreferences.modifyStyleImagesBody(m_Body.toString());
        res = HtmlPreferences.modifyEmoticons(res, m_EmoticsDict);
        if (!m_IsLoadImages)
            res = HtmlPreferences.modifyAttachedImagesBody(m_IsWebviewAllowJavascriptInterface, res);
        return res;
    }

    public void addPoll(String value, boolean openSpoil) {
        m_Body.append("<div class=\"poll\">").append(getSpoiler("<b><span>Опрос</span></b>", value, openSpoil)).append("</div>");
    }

    public void clear() {
        m_Topic = null;
        m_Body = null;
    }

    private String getTitleBlock() {
        String desc = TextUtils.isEmpty(m_Topic.getDescription()) ? "" : ("<span class=\"comma\">, </span>" + m_Topic.getDescription());
        return "<div class=\"topic_title_post\"><a href=\"http://4pda.ru/forum/index.php?showtopic="
                + m_Topic.getId()
                + (TextUtils.isEmpty(m_UrlParams) ? "" : ("&" + m_UrlParams)) + "\">"
                + "<span class=\"name\">" + m_Topic.getTitle() + "</span>"
                + (HtmlPreferences.isFullThemeTitle() ? "<span class=\"description\">" + desc + "</span>" : "") + "</a></div>\n";
    }

    public static void addButtons(StringBuilder sb, int currentPage, int pagesCount, Boolean isUseJs,
                                  Boolean useSelectTextAsNumbers, Boolean top) {
        boolean prevDisabled = currentPage == 1;
        boolean nextDisabled = currentPage == pagesCount;
        sb.append("\n<div class=\"navi ").append(top ? "top" : "bottom").append("\">\n");
        sb.append("<a class=\"button first").append(prevDisabled ? " disable\"" : "\"" + getHtmlout(isUseJs, "firstPage")).append("><span>&lt;&lt;</span></a>\n");
        sb.append("<a class=\"button prev").append(prevDisabled ? " disable\"" : "\"" + getHtmlout(isUseJs, "prevPage")).append("><span>&lt;</span></a>\n");
        sb.append("<a class=\"button page\" ").append(getHtmlout(isUseJs, "jumpToPage")).append("><span>").append(useSelectTextAsNumbers ? (currentPage + "/" + pagesCount) : "Выбор").append("</span></a>\n");
        sb.append("<a class=\"button next").append(nextDisabled ? " disable\"" : "\"" + getHtmlout(isUseJs, "nextPage")).append("><span>&gt;</span></a>\n");
        sb.append("<a class=\"button last").append(nextDisabled ? " disable\"" : "\"" + getHtmlout(isUseJs, "lastPage")).append("><span>&gt;&gt;</span></a>\n");
        sb.append("</div>\n");
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1, String val2) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1, val2});
    }

    private static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1});
    }

    private static String getHtmlout(Boolean webViewAllowJs, String methodName) {
        return getHtmlout(webViewAllowJs, methodName, new String[0]);
    }


    private static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues) {
        return getHtmlout(webViewAllowJs, methodName, paramValues, true);
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues, Boolean modifyParams) {
        return HtmlOutUtils.getHtmlout(methodName,paramValues,modifyParams);
    }

    private void addPostHeader(StringBuilder sb, Post msg) {
        String nick = msg.getNick();
        String nickParam = msg.getNickParam();

        sb.append("<div class=\"post_header\"><div class=\"header_wrapper\">\n");

        //Аватарка
        sb.append("<div class=\"avatar ").append(App.getInstance().getPreferences().getBoolean("isSquareAvarars", false) ? "" : "circle ").append(m_IsShowAvatars ? "\"" : "disable\"")
                .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showUserMenu", new String[]{msg.getId(), msg.getUserId(), nickParam}));
        String avatar = msg.getAvatarFileName();
        if (TextUtils.isEmpty(avatar)) {
            avatar = "file:///android_asset/profile/av.png";
        }
        sb.append("><div class=\"img\" style=\"background-image:url(").append((m_IsShowAvatars ? avatar : "file:///android_asset/profile/av.png")).append(");\"></div></div>");

        //Ник
        sb.append("<a class=\"inf nick ")
                .append(msg.getUserState() ? "online " : "")
                .append(msg.isCurator() ? "curator\"" : "\" ")
                .append(!TextUtils.isEmpty(msg.getUserId()) ? getHtmlout(m_IsWebviewAllowJavascriptInterface, "showUserMenu", new String[]{msg.getId(), msg.getUserId(), nickParam}) : "")
                .append("><span><b>").append(nick).append("</b></span></a>");

        //Группа
        sb.append("<div class=\"inf group\">").append(msg.getUserGroup() == null ? "" : msg.getUserGroup()).append("</div>");

        //Репутация
        if (!TextUtils.isEmpty(msg.getUserId())) {
            sb.append("<a class=\"inf reputation\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showRepMenu", new String[]{msg.getId(), msg.getUserId(), msg.getNickParam(), msg.getCanPlusRep() ? "1" : "0", msg.getCanMinusRep() ? "1" : "0"}))
                    .append("><span>").append(msg.getUserReputation()).append("</span></a>");
        }

        //Дата
        sb.append("<div class=\"date-link\"><span class=\"inf date\"><span>").append(msg.getDate()).append("</span></span>");

        //Ссылка на пост
        sb.append("<a class=\"inf link\" ")
                .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostLinkMenu", msg.getId()))
                .append("><span><span class=\"sharp\">#</span>").append(msg.getNumber()).append("</span></a></div>");

        //Меню
        if (Client.getInstance().getLogined()) {
            sb.append("<a class=\"inf menu\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostMenu", new String[]{msg.getId(), msg.getDate(), msg.getUserId(), nickParam, msg.getCanEdit() ? "1" : "0", msg.getCanDelete() ? "1" : "0"}))
                    .append("><span>Меню</span></a>");
        }
        sb.append("</div></div>\n");
    }

    private void addFooter(StringBuilder sb, Post post) {
        sb.append("<div class=\"post_footer")
                .append(post.getCanDelete() ? " delete" : "")
                .append(post.getCanEdit() ? " edit" : "")
                .append(getTopic().isPostVote() ? "" : " nopostvote")
                .append("\">");
        if (m_Logined) {
            String nickParam = post.getNickParam();
            String postNumber = post.getNumber();

            try {
                postNumber = Integer.toString(Integer.parseInt(post.getNumber()) - 1);
            } catch (Throwable ignored) {
            }

            sb.append(String.format("<a class=\"link button claim\" href=\"/forum/index.php?act=report&t=%s&p=%s&st=%s\"><span>Жалоба</span></a>",
                    m_Topic.getId(), post.getId(), postNumber));

            sb.append("<a class=\"button nick\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "insertTextToPost", String.format("[SNAPBACK]%s[/SNAPBACK] [B]%s,[/B] \\n", post.getId(), nickParam)))
                    .append("><span>Ник</span></a>");

            sb.append("<a class=\"button quote\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "quote", new String[]{m_Topic.getForumId(), m_Topic.getId(), post.getId(), post.getDate(), post.getUserId(), nickParam}))
                    .append("><span>Цитата</span></a>");

            if (!UserInfoRepository.Companion.getInstance().getId().equals(post.getUserId()) & getTopic().isPostVote()) {
                sb.append("<a class=\"button vote bad\" ")
                        .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteBad", post.getId()))
                        .append("><span>Плохо</span></a>");

                sb.append("<a class=\"button vote good\" ")
                        .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteGood", post.getId()))
                        .append("><span>Хорошо</span></a>");
            }

            if (post.getCanEdit())
                sb.append(String.format("<a class=\"button edit\" id=\"edit-but-%s\" href=\"/forum/index.php?act=post&do=edit_post&f=%s&t=%s&p=%s&st=%s\"><span>Изменить</span></a>",
                        post.getId(), m_Topic.getForumId(), m_Topic.getId(), post.getId(), postNumber));
            if (post.getCanDelete())
                sb.append(String.format("<a class=\"button delete\" href=\"/forum/index.php?act=Mod&CODE=04&f=%s&t=%s&p=%s&st=%s&auth_key=%s\"><span>Удалить</span></a>",
                        m_Topic.getForumId(), m_Topic.getId(), post.getId(), postNumber, m_Topic.getAuthKey()));
        }
        sb.append("</div>\n\n");
    }

    public ExtTopic getTopic() {
        return m_Topic;
    }

    public void setMMod(boolean MMod) {
        this.m_MMod = MMod;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isMMod() {
        return m_MMod;
    }
}
