package org.softeg.slartus.forpdaplus.search;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.10.12
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchPostsParser extends HtmlBuilder  {
    private boolean m_SpoilerByButton = false;
    private Hashtable<String,String> m_EmoticsDict;
    public SearchPostsParser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        m_SpoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false);
        m_EmoticsDict=Smiles.getSmilesDict();
    }

    public SearchResult searchResult;

    public String parse(String body) throws MessageInfoException, NotReportException {

        Matcher m = Pattern.compile("<div class=\"borderwrap-header\">([\\s\\S]*?)<br /><div class=\"borderwrap\">([\\s\\S]*?)").matcher(body);
        if (!m.find()) {
            if (body.indexOf("К сожалению, Ваш поиск не дал никаких результатов") > 0)
                throw new MessageInfoException("Поиск", "К сожалению, Ваш поиск не дал никаких результатов.\n" +
                        "Попробуйте расширить параметры поиска, используя другое ключевое слово или изменением формата поиска.");
            else
                throw new NotReportException("Ошибка разбора результатов поиска");
        }
        searchResult = createSearchResult(m.group(1));


        beginHtml("Результаты поиска");

        beginTopic(searchResult);

        parsePosts(body);

        endTopic( searchResult);
        return m_Body.toString();
    }

    private SearchResult createSearchResult(String page) {


        final Pattern pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);");
        // http://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        final Pattern lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?act=Search.*?st=(\\d+)");
        final Pattern currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>");

        SearchResult searchResult = new SearchResult();

        Matcher m = pagesCountPattern.matcher(page);
        if (m.find()) {
            searchResult.setPagesCount(m.group(1));
        }

        m = lastPageStartPattern.matcher(page);
        while (m.find()) {
            searchResult.setLastPageStartCount(m.group(2));
        }

        m = currentPagePattern.matcher(page);
        if (m.find()) {
            searchResult.setCurrentPage(m.group(1));
        } else
            searchResult.setCurrentPage("1");
        return searchResult;
    }

    public void beginTopic(SearchResult searchResult) {
        beginBody();
        m_Body.append("<div style=\"margin-top:").append(ACTIONBAR_TOP_MARGIN).append("\"/>\n");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(), searchResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(MyApp.getInstance()), true, true);
        }
        m_Body.append("<br/><br/>");

    }

    public void endTopic(SearchResult searchResult) {
        m_Body.append("<div id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(),
                    searchResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(MyApp.getInstance()), true, false);
        }

        m_Body.append("<br/><br/>");

        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");

        endBody();
        endHtml();
    }


    private StringBuilder parsePosts(String page) {
        Matcher postMatcher = Pattern.compile("<div class=\"maintitle\">([\\s\\S]*?)(<div class=\"borderwrap\">|<div id=\"gfooter\">)").matcher("<div class=\"borderwrap\">" + page);

        while (postMatcher.find()) {
            m_Body.append(parsePost(postMatcher.group(1)));
        }

        return m_Body;
    }

    private final Pattern postPattern = Pattern.compile("<div class=\"postcolor(?:\\s|\\w)*?\" id=\"post-\\d+\" style=\"height:300px;overflow-x:auto;\">([\\s\\S]*?)</div></td>\\s+</tr>\\s+<tr>\\s+<td class=\"row2\"></td>\\s+<td class=\"row2\">([\\s\\S]*?)</td>");
    private final Pattern topicPattern = Pattern.compile("<a href=\"/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a></div>");
    private final Pattern userPattern = Pattern.compile("<span class=\"normalname\"><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></span>[\\s\\S]*?<font color=\".*?\">\\[((offline)|(online))\\]");
    private final Pattern dateTimePattern = Pattern.compile("style=\"padding-bottom:2px\" />(.*?)</span>");

    private String parsePost(String page) {
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(MyApp.getInstance());
        String s1 = null;
        Matcher m = topicPattern.matcher(page);
        if (m.find())
            s1 = "<a href=\"http://4pda.ru/forum/index.php?showtopic=" + m.group(1) + "\">" + m.group(2) + "</a>";

        m = userPattern.matcher(page);
        String s2 = null;
        if (m.find()) {
            String userState = "online".equals(m.group(3)) ? "post_nick_online_cli" : "post_nick_cli";
            s2 = "<span class=\"" + userState + "\"><a " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu",
                    m.group(1), m.group(2)) + " class=\"system_link\">" + m.group(2) + "</a></span>";
        }

        String s3 = null;
        m = dateTimePattern.matcher(page);
        if (m.find()) {
            s3 = m.group(1);
        }

        String s4 = null;
        String s5 = null;
        m = postPattern.matcher(page);
        if (m.find()) {
            s4 = Post.modifyBody(m.group(1),m_EmoticsDict,true).replace("<br /><br />--------------------<br />", "");
            if (m_SpoilerByButton) {
                String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)";
                String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
                s4 = s4.replaceAll(find, replace);
            }
            s5 = Post.modifyBody(m.group(2),m_EmoticsDict,true);
        }


        //Post.modifyBody(postMatcher.group(8));
        String POST_TEMPLATE = "<div class=\"between_messages\"></div>\n" +
                "<div class=\"topic_title_post\">%1s</div>\n" +
                "<div class=\"post_header\">\n" +
                "\t<table width=\"100%%\">\n" +
                "\t\t<tr><td>%2s</td>\n" +
                "\t<td><div align=\"right\"><span class=\"post_date_cli\">%3s</span></div></td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div>" +
                "<div class=\"post_body emoticons\">%4s</div>" +
                "<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td>%5s</td></tr></table></div>";
        return String.format(POST_TEMPLATE, s1, s2, s3, s4, s5);
    }


}
