package org.softeg.slartus.forpdaplus.search;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.emotic.Smiles;

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

        m_Body.append("<div class=\"posts_list search-results\">");
        Document doc = Jsoup.parse(body, "http://4pda.ru");
        Elements postsElements = doc.select("div[data-post]");
        if(postsElements.size()==0){
            m_Body.append("<div class=\"bad-search-result\">\n" +
                    "\t<h3>Поиск не дал результатов</h3>\n" +
                    "\t<span>Попробуйте сформулировать свой запрос иначе</span>\n" +
                    "</div>");
        }else {
            for (Element element : postsElements) {
                m_Body.append(parsePost(element));
            }
        }
        m_Body.append("</div>");
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
        beginBody("search");
        m_Body.append("<div id=\"topMargin\"></div>\n<div class=\"panel top\">");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(), searchResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(App.getInstance()), true, true);
        }
        m_Body.append("</div>");
        //m_Body.append("<br/><br/>");

    }

    public void endTopic(SearchResult searchResult) {
        m_Body.append("<div id=\"entryEnd\"></div>\n");
        m_Body.append("<div class=\"panel bottom\">");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(),
                    searchResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(App.getInstance()), true, false);
        }
        m_Body.append("</div><div id=\"bottomMargin\"></div>");
        endBody();
        endHtml();
    }



    private String parsePost(Element element) {
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(App.getInstance());

        String topic = null;
        String userId = null;
        String userName = null;
        String user;
        String dateTime;
        String userState = null;
        String post;
        String postFooter;
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
                userState = "[online]".equals(el.text()) ? "online" : "";

            user = "<a class=\"s_inf nick " + userState + "\" "+TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", userId, userName)+"><span>"+userName+"</span></a>";

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

            String POST_TEMPLATE = "<div class=\"post_container\">\n" +
                    "<div class=\"topic_title_post\">%1s</div>\n" +
                    "<div class=\"post_header\">\n" +
                    "\t\t%2s\n" +
                    "\t<div class=\"s_inf date\"><span>%3s</span></div>\n" +
                    "\n" +
                    "</div>" +
                    "<div class=\"post_body emoticons\">%4s</div>" +
                    "<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td>%5s</td></tr></table></div></div><div class=\"between_messages\"></div>";
            return String.format(POST_TEMPLATE, topic, user, dateTime, post, postFooter);
        }


        return "";

        //Post.modifyBody(postMatcher.group(8));

    }


}
