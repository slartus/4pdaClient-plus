package org.softeg.slartus.forpdaplus.search;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaplus.App;
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
public class SearchPostsParser extends HtmlBuilder {
    private boolean m_SpoilerByButton = false;
    private Hashtable<String, String> m_EmoticsDict;

    public SearchPostsParser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        m_SpoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false);
        m_EmoticsDict = Smiles.getSmilesDict();
    }

    public SearchResult searchResult;

    public String parse(String body) throws MessageInfoException, NotReportException {


        searchResult = createSearchResult(body);


        beginHtml("Результаты поиска");

        beginTopic(searchResult);

        Document doc = Jsoup.parse(body, "http://4pda.ru");
        Elements postsElements = doc.select("div[data-post]");
        for (Element element : postsElements) {
            m_Body.append(parsePost(element));
        }

        endTopic(searchResult);
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
                    Functions.isWebviewAllowJavascriptInterface(App.getInstance()), true, true);
        }
        m_Body.append("<br/><br/>");

    }

    public void endTopic(SearchResult searchResult) {
        m_Body.append("<div id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(),
                    searchResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(App.getInstance()), true, false);
        }

        m_Body.append("<br/><br/>");

        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");

        endBody();
        endHtml();
    }

    private final Pattern postPattern = Pattern.compile("<div class=\"postcolor(?:\\s|\\w)*?\" id=\"post-\\d+\" style=\"height:300px;overflow-x:auto;\">([\\s\\S]*?)</div></td>\\s+</tr>\\s+<tr>\\s+<td class=\"row2\"></td>\\s+<td class=\"row2\">([\\s\\S]*?)</td>");
    private final Pattern topicPattern = Pattern.compile("<a href=\"/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a></div>");
    private final Pattern userPattern = Pattern.compile("<span class=\"normalname\"><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></span>[\\s\\S]*?<font color=\".*?\">\\[((offline)|(online))\\]");
    private final Pattern dateTimePattern = Pattern.compile("style=\"padding-bottom:2px\" />(.*?)</span>");

    private String parsePost(Element element) {
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(App.getInstance());

        String topic = null;
        String userId = null;
        String userName = null;
        String user = null;
        String dateTime = null;
        String userState = null;
        String post = null;
        String postFooter = null;
        Element el = element.select("div.maintitle").first();
        if (el != null)
            topic = el.html();

        el = element.select("table.ipbtable>tbody").first();
        if (el != null) {
            Elements trElements = el.children();

            Element trElement = trElements.get(0);
            Elements tdElements = trElement.children();

            Element tdElement = tdElements.get(0);
            el = tdElement.select("a[href~=showuser=\\d+]").first();
            if (el != null) {
                Uri uri = Uri.parse(el.attr("href"));
                userId = uri.getQueryParameter("showuser");
                userName = el.text();
            }
            tdElement = tdElements.get(1);
            dateTime = tdElement.text();

            trElement = trElements.get(1);
            tdElements = trElement.children();
            tdElement = tdElements.get(0);
            el = tdElement.select("span.postdetails font[color]:matches(\\[(online|offline)\\])").first();
            if (el != null)
                userState = "[online]".equals(el.text()) ? "post_nick_online_cli" : "post_nick_cli";

            user = "<span class=\"" + userState + "\"><a " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu",
                    userId, userName) + " class=\"system_link\">" + userName + "</a></span>";

            tdElement = tdElements.get(1);
            post = Post.modifyBody(tdElement.html(), m_EmoticsDict, true).replace("<br /><br />--------------------<br />", "");
            if (m_SpoilerByButton) {
                String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)";
                String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
                post = post.replaceAll(find, replace);
            }
            postFooter = Post.modifyBody(trElements.get(2).children().get(1).html(), m_EmoticsDict, true);

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
            return String.format(POST_TEMPLATE, topic, user, dateTime, post, postFooter);
        }


        return "";

        //Post.modifyBody(postMatcher.group(8));

    }


}
