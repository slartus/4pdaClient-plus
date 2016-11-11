package org.softeg.slartus.forpdaplus.fragments.search;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
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

        final Pattern titlePattern = Pattern.compile("<div class=\"maintitle\">(.*)<\\/div>");
        final Pattern postPattern = Pattern.compile("<table[^>]*>([\\s\\S]*)<\\/table>");
        final Pattern userPattern = Pattern.compile("<span class=\"normalname\"><a href=\"([^\"]*)\">(.*)<\\/a>");
        final Pattern datePattern = Pattern.compile("<td[^<]*<img[^>]*>([^<]*)");
        final Pattern onlinePattern = Pattern.compile("<font color=\".*?\">([^<]*?)<\\/font>");
        final Pattern postBodyPattern = Pattern.compile("(<div class=\"postcolor[\\s\\S]*?<\\/div>)<\\/td>[^<]");
        final Pattern footerPattern = Pattern.compile("<td class=\"row2\"><\\/td>[^>]*>([\\s\\S]*?<\\/div>)[^<]");

        Matcher postMatcher = null;
        Matcher matcher = null;

        String userId = null;
        String userName = null;
        String user;
        String dateTime = null;
        String userState = null;

        Log.e("kek", "start matcher");
        Matcher postsMatcher = Pattern.compile("(<div class=\"borderwrap\" data-post=\"[^>]*>[\\s\\S]*?(<div class=\"postcolor[\\s\\S]*?<\\/div>)<\\/td>[^<][\\s\\S]*?<\\/table>[^<]*<\\/div>)<br").matcher(body);
        while (postsMatcher.find()) {
            m_Body.append("<div class=\"post_container\">");

            if(postMatcher==null)
                postMatcher = titlePattern.matcher(postsMatcher.group(1));
            else
                postMatcher.usePattern(titlePattern).reset(postsMatcher.group(1));


            if (postMatcher.find())
                m_Body.append("<div class=\"topic_title_post\">").append(postMatcher.group(1)).append("</div>\n");

            postMatcher.usePattern(postPattern).reset(postsMatcher.group(1));
            if (postMatcher.find()) {

                if(matcher==null)
                    matcher = userPattern.matcher(postMatcher.group(1));
                else
                    matcher.usePattern(userPattern).reset(postMatcher.group(1));
                if (matcher.find()) {
                    Uri uri = Uri.parse(matcher.group(1));
                    userId = uri.getQueryParameter("showuser");
                    userName = matcher.group(2);
                }

                matcher.usePattern(datePattern).reset(postMatcher.group(1));
                if (matcher.find())
                    dateTime = matcher.group(1).trim();

                matcher.usePattern(onlinePattern).reset(postMatcher.group(1));
                if (matcher.find())
                    userState = "[online]".equals(matcher.group(1)) ? "online" : "";

                user = "<a class=\"s_inf nick " + userState + "\" " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", userId, userName) + "><span>" + userName + "</span></a>";
                m_Body.append("<div class=\"post_header\">").append(user).append("<div class=\"s_inf date\"><span>").append(dateTime).append("</span></div></div>");

                if (m_SpoilerByButton) {
                    String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                            "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                            "(Спойлер \\(\\+/-\\).*?</div>)" +
                            "(\\s*<div class='hidemain' style=\"display:none\">)";
                    String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
                    m_Body.append("<div class=\"post_body emoticons\">").append(Post.modifyBody(postsMatcher.group(2), m_EmoticsDict).replaceAll(find, replace)).append("</div>");
                } else {
                    m_Body.append("<div class=\"post_body emoticons\">").append(Post.modifyBody(postsMatcher.group(2), m_EmoticsDict)).append("</div>");
                }

                matcher.usePattern(footerPattern).reset(postMatcher.group(1));
                if (matcher.find())
                    m_Body.append("<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td>").append(Post.modifyBody(matcher.group(1), m_EmoticsDict)).append("</td></tr></table></div>");
                m_Body.append("</div><div class=\"between_messages\"></div>");
            }
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
