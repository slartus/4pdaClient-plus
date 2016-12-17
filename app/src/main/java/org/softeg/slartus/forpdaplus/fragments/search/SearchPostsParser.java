package org.softeg.slartus.forpdaplus.fragments.search;

import android.content.SharedPreferences;
import android.util.Log;

import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
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
        SharedPreferences prefs = App.getInstance().getPreferences();
        m_SpoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false);
        m_EmoticsDict = Smiles.getSmilesDict();
    }

    public SearchResult searchResult;

    public String parse(String body) throws MessageInfoException, NotReportException {
        int posts = 0;
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(App.getInstance());
        searchResult = createSearchResult(body);
        beginHtml(App.getContext().getString(R.string.search_result));
        beginTopic(searchResult);

        m_Body.append("<div class=\"posts_list search-results\">");

        String userId, userName, user, dateTime, userState;

        Log.e("kek", "start matcher");
        Matcher matcher = Pattern.compile("<div class=\"cat_name\" style=\"margin-bottom:0\">([\\s\\S]*?)<\\/div>[\\s\\S]*?post_date\">([^\\|&]*)[\\s\\S]*?<font color=\"([^\"]*?)\"[\\s\\S]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div class=\"cat_name\"|<div><div class=\"pagination\">)").matcher(body);
        while (matcher.find()) {
            m_Body.append("<div class=\"post_container\">");
            m_Body.append("<div class=\"topic_title_post\">").append(matcher.group(1)).append("</div>\n");
            dateTime = matcher.group(2);
            userState = matcher.group(3).equals("red") ? "" : "online";
            userId = matcher.group(4);
            userName = matcher.group(5);
            user = "<a class=\"s_inf nick " + userState + "\" " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", userId, userName) + "><span>" + userName + "</span></a>";
            m_Body.append("<div class=\"post_header\">").append(user).append("<div class=\"s_inf date\"><span>").append(dateTime).append("</span></div></div>");
            m_Body.append("<div class=\"post_body emoticons\">");
            if (m_SpoilerByButton) {
                String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)";
                String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
                m_Body.append(Post.modifyBody(matcher.group(6), m_EmoticsDict).replaceAll(find, replace));
            } else {
                m_Body.append(Post.modifyBody(matcher.group(6), m_EmoticsDict));
            }
            m_Body.append("</div>").append("</div><div class=\"between_messages\"></div>");
            posts++;
        }
        Log.e("kek", "start matcher");
        if (posts == 0) {
            m_Body.append("<div class=\"bad-search-result\">\n" +
                    "\t<h3>Поиск не дал результатов</h3>\n" +
                    "\t<span>Попробуйте сформулировать свой запрос иначе</span>\n" +
                    "</div>");
        }
        m_Body.append("</div>");
        endTopic(searchResult);
        return m_Body.toString();
    }

    private SearchResult createSearchResult(String page) {


        final Pattern pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);");
        // http://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        //final Pattern paginationPattern = Pattern.compile("<div class=\"pagination\">([\\s\\S]*?)<\\/div><\\/div><br");

        final Pattern lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?act=search.*?st=(\\d+)");
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
}
