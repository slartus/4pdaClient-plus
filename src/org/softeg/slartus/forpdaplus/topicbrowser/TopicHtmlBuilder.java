package org.softeg.slartus.forpdaplus.topicbrowser;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.TopicBodyParser;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.common.HtmlUtils;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;

import java.util.Hashtable;

/*
 * Created by slartus on 05.06.2014.
 */
public class TopicHtmlBuilder extends HtmlBuilder {

    private Boolean m_Logined, m_IsWebviewAllowJavascriptInterface;
    private TopicBodyParser m_TopicBodyParser;

    private HtmlPreferences m_HtmlPreferences;
    private Hashtable<String, String> m_EmoticsDict;

    public TopicHtmlBuilder(TopicBodyParser topicBodyParser) {
        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(MyApp.getContext());
        m_EmoticsDict = Smiles.getSmilesDict();

        m_IsWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(MyApp.getContext());
        m_Logined = Client.getInstance().getLogined();

        m_TopicBodyParser = topicBodyParser;
    }

    @Override
    public void addScripts() {
        super.addScripts();
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/topic.js\"></script>\n");
    }

    public void beginTopic() {
        String desc = TextUtils.isEmpty(m_TopicBodyParser.getTopicDescription()) ? "" : (", " + m_TopicBodyParser.getTopicDescription());
        super.beginHtml(m_TopicBodyParser.getTopicTitle() + desc);
        super.beginBody();
        m_Body.append("<div style=\"margin-top:72pt\"/>\n");
        if (m_TopicBodyParser.getPagesCount() > 1) {
            addButtons(m_Body, m_TopicBodyParser.getCurrentPage(), m_TopicBodyParser.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, true);
        }

        m_Body.append(getTitleBlock());
    }

    public void endTopic() {
        m_Body.append("<div name=\"entryEnd\" id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (m_TopicBodyParser.getPagesCount() > 1) {
            addButtons(m_Body, m_TopicBodyParser.getCurrentPage(), m_TopicBodyParser.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, false);
        }

        m_Body.append("<br/><br/>");
//        addPostForm(m_Body);

        m_Body.append("<div id=\"viewers\"><a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showReadingUsers")).append(" class=\"href_button\">Кто читает тему..</a></div><br/>\n");
        m_Body.append("<div id=\"writers\"><a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showWriters")).append(" class=\"href_button\">Кто писал сообщения..</a></div><br/><br/>\n");
        m_Body.append(getTitleBlock());


        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");
        super.endBody();
        super.endHtml();

    }

    public void addPost(Post post, Boolean spoil) {

        m_Body.append("<div name=\"entry").append(post.getId()).append("\" id=\"entry").append(post.getId()).append("\"></div>\n");

        addPostHeader(m_Body, post, post.getId());

        m_Body.append("<div id=\"msg").append(post.getId()).append("\" name=\"msg").append(post.getId()).append("\">");

        if (spoil) {
            if (m_HtmlPreferences.isSpoilerByButton())
                m_Body.append("<div class='hidetop' style='cursor:pointer;' ><b>( &gt;&gt;&gt;ШАПКА ТЕМЫ&lt;&lt;&lt;)</b></div>" +
                        "<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility(this)\"/>" +
                        "<div class='hidemain' style=\"display:none\">");
            else
                m_Body.append("<div class='hidetop' style='cursor:pointer;' " +
                        "onclick=\"var _n=this.parentNode.getElementsByTagName('div')[1];" +
                        "if(_n.style.display=='none'){_n.style.display='';}else{_n.style.display='none';}\">" +
                        "<b>( &gt;&gt;&gt;ШАПКА ТЕМЫ&lt;&lt;&lt;)</b></div><div class='hidemain' style=\"display:none\">");
        }
        String postBody = post.getBody().trim();
        if (m_HtmlPreferences.isSpoilerByButton()) {

            postBody = HtmlPreferences.modifySpoiler(postBody);
        }
        //m_TopicAttaches.parseAttaches(post.getId(),post.getNumber(),postBody);
        m_Body.append(postBody);
        if (spoil)
            m_Body.append("</div>");
        m_Body.append("</div>\n\n");
        //m_Body.append("<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td id=\""+post.getId()+"\"></td></tr></table></div>\n\n");

        addFooter(m_Body, post);
        m_Body.append("<div class=\"between_messages\"></div>");
    }

    public String getBody() {
        String res;
        if (m_HtmlPreferences.isUseLocalEmoticons()) {
            res = HtmlPreferences.modifyStyleImagesBody(m_Body.toString());
            res = HtmlPreferences.modifyEmoticons(res, m_EmoticsDict, true);
        } else {
            res = HtmlPreferences.modifyEmoticons(m_Body.toString(), m_EmoticsDict, false);
        }
        if (!WebViewExternals.isLoadImages("theme"))
            res = HtmlPreferences.modifyAttachedImagesBody(m_IsWebviewAllowJavascriptInterface, res);
        return res;
    }

    public void addBody(String value) {
        m_Body.append(value);
    }

    public void clear() {
        m_TopicBodyParser = null;
        m_Body = null;
    }

    private String getTitleBlock() {
        String desc = TextUtils.isEmpty(m_TopicBodyParser.getTopicDescription()) ? "" : (", " + m_TopicBodyParser.getTopicDescription());
        return "<div class=\"topic_title_post\"><a href=\"" + m_TopicBodyParser.getUrl() + "\">" + m_TopicBodyParser.getTopicTitle() + desc + "</a></div>\n";
    }

    public static void addButtons(StringBuilder sb, int currentPage, int pagesCount, Boolean isUseJs,
                                  Boolean useSelectTextAsNumbers, Boolean top) {
        Boolean prevDisabled = currentPage == 1;
        Boolean nextDisabled = currentPage == pagesCount;
        sb.append("<div class=\"navi\" id=\"").append(top ? "top_navi" : "bottom_navi").append("\">\n");
        sb.append("<div class=\"first\"><a ").append(prevDisabled ? "#" : getHtmlout(isUseJs, "firstPage")).append(" class=\"href_button").append(prevDisabled ? "_disable" : "").append("\">&lt;&lt;</a></div>\n");
        sb.append("<div class=\"prev\"><a ").append(prevDisabled ? "#" : getHtmlout(isUseJs, "prevPage")).append(" class=\"href_button").append(prevDisabled ? "_disable" : "").append("\">  &lt;  </a></div>\n");
        String selectText = useSelectTextAsNumbers ? (currentPage + "/" + pagesCount) : "Выбор";
        sb.append("<div class=\"page\"><a ").append(getHtmlout(isUseJs, "jumpToPage")).append(" class=\"href_button\">").append(selectText).append("</a></div>\n");
        sb.append("<div class=\"next\"><a ").append(nextDisabled ? "#" : getHtmlout(isUseJs, "nextPage")).append(" class=\"href_button").append(nextDisabled ? "_disable" : "").append("\">  &gt;  </a></div>\n");
        sb.append("<div class=\"last\"><a ").append(nextDisabled ? "#" : getHtmlout(isUseJs, "lastPage")).append(" class=\"href_button").append(nextDisabled ? "_disable" : "").append("\">&gt;&gt;</a></div>\n");
        sb.append("</div>\n");

    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1, String val2) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1, val2});
    }

    private static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1});
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName) {
        return getHtmlout(webViewAllowJs, methodName, new String[0]);
    }


    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues) {
        return getHtmlout(webViewAllowJs, methodName, paramValues, true);
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues, Boolean modifyParams) {
        StringBuilder sb = new StringBuilder();
        if (!webViewAllowJs) {
            sb.append("href=\"http://www.HTMLOUT.ru/");
            sb.append(methodName).append("?");
            int i = 0;

            for (String paramName : paramValues) {
                sb.append("val").append(i).append("=").append(modifyParams ? Uri.encode(paramName) : paramName).append("&");
                i++;
            }

            sb = sb.delete(sb.length() - 1, sb.length());
            sb.append("\"");
        } else {

            sb.append(" onclick=\"window.HTMLOUT.").append(methodName).append("(");
            for (String paramName : paramValues) {
                sb.append("'").append(HtmlUtils.modifyHtmlQuote(paramName).replace("'", "\\'").replace("\"", "&quot;")).append("',");
            }
            if (paramValues.length > 0)
                sb.delete(sb.length() - 1, sb.length());
            sb.append(")\"");
        }
        return sb.toString();
    }

    private void addPostHeader(StringBuilder sb, Post msg, String msgId) {
        String nick = msg.getNick();
//nick="\"~!@#$%^&*()<>'/{}[]\\\\`&#377;micier2\"";
        String nickLink = nick;
        if (!TextUtils.isEmpty(msg.getUserId())) {
            nickLink = "<a " +
                    getHtmlout(m_IsWebviewAllowJavascriptInterface,
                            "showUserMenu",
                            msg.getUserId(),
                            nick)
                    + " class=\"system_link\">" + nick + "</a>";
        }


        String userState = msg.getUserState() ? "post_nick_online_cli" : "post_nick_cli";

        sb.append("<div class=\"post_header\">\n");
        sb.append("\t<table width=\"100%\">\n");
        sb.append("\t\t<tr><td><span class=\"").append(userState).append("\">").append(nickLink).append("</span></td>\n");
        sb.append("\t\t\t<td><div align=\"right\"><span class=\"post_date_cli\">").append(msg.getDate()).append("|<a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostLinkMenu", msg.getId())).append(">#").append(msg.getNumber()).append("</a></span></div></td>\n");
        sb.append("\t\t</tr>\n");
        String userGroup = msg.getUserGroup() == null ? "" : msg.getUserGroup();
        sb.append("<tr>\n" + "\t\t\t<td colspan=\"2\"><span  class=\"user_group\">").append(userGroup).append("</span></td></tr>");
        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>").append(TextUtils.isEmpty(msg.getUserId()) ? "" : getReputation(msg)).append("</td>\n");
        if (Client.getInstance().getLogined())
            sb.append("\t\t\t<td><div align=\"right\"><a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostMenu", new String[]{msgId, msg.getUserId(), msg.getCanEdit() ? "1" : "0", (msg.getCanDelete() ? "1" : "0")})).append(" class=\"system_link\">меню</a></div></td>");
        sb.append("\t\t</tr>");
        sb.append("\t</table>\n");
        sb.append("</div>\n");
    }

    private String getReputation(Post msg) {
        String[] params = new String[]{msg.getId(), msg.getUserId(), msg.getNick(), msg.getCanPlusRep() ? "1" : "0", msg.getCanMinusRep() ? "1" : "0"};

        return "<a " + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showRepMenu", params) + "  class=\"system_link\" ><span class=\"post_date_cli\">Реп(" + msg.getUserReputation() + ")</span></a>";
    }

    private void addFooter(StringBuilder sb, Post post) {
        sb.append("<div class=\"post_footer\"><table width=\"100%\"><tr>");
        if (m_Logined) {

            if (!Client.getInstance().UserId.equals(post.getUserId())) {
                sb.append("<td width=\"50\"><a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteBad", post.getId()))
                        .append(" class=\"system_link\"><span class=\"post_vote_bad\"></span></a></td>");

                sb.append("<td width=\"50\"><a ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteGood", post.getId()))
                        .append(" class=\"system_link\"><span class=\"post_vote_good\"></span></a></td>");
            }
            sb.append("<td></td>");
            String[] quoteParams = {m_TopicBodyParser.getForumId(), m_TopicBodyParser.getTopicId(), post.getId(), post.getDate(), post.getUserId(), post.getNick()};

            sb.append("<td><div style=\"text-align:right\"><a class=\"system_link\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "quote", quoteParams))
                    .append(">цитата</a></div></td>");
        }
        sb.append("</tr></table></div>\n\n");
    }
}
