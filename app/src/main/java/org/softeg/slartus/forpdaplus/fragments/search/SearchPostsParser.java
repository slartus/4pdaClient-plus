package org.softeg.slartus.forpdaplus.fragments.search;

import android.content.SharedPreferences;

import org.softeg.slartus.forpdaapi.common.ParseFunctions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.slartus.http.AppResponse;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.10.12
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchPostsParser extends HtmlBuilder {
    private boolean m_SpoilerByButton = false;
    private final Hashtable<String, String> m_EmoticsDict;

    public SearchPostsParser() {
        SharedPreferences prefs = App.getInstance().getPreferences();
        m_SpoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false);
        m_EmoticsDict = Smiles.getSmilesDict();
    }

    public SearchResult searchResult;

    public String parse(AppResponse response) {

        boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface();
        searchResult = createSearchResult(response);
        beginHtml(App.getContext().getString(R.string.search_result));
        beginTopic(searchResult);

        m_Body.append("<div class=\"posts_list search-results\">");

        List<SearchResultPost> posts = parsePosts(ParseFunctions.decodeEmails(response.getResponseBody()));
        for (SearchResultPost post : posts) {
            m_Body.append("<div class=\"post_container\">");
            m_Body.append("<div class=\"topic_title_post\">").append(post.titleHtml).append("</div>\n");
            String user = "<a class=\"s_inf nick " + post.userState + "\" " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", post.userId, post.userName) + "><span>" + post.userName + "</span></a>";
            m_Body.append("<div class=\"post_header\">").append(user).append("<div class=\"s_inf date\"><span>").append(post.dateTimeHtml).append("</span></div></div>");
            m_Body.append("<div class=\"post_body emoticons\">");
            if (m_SpoilerByButton) {
                String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)";
                String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
                m_Body.append(Post.modifyBody(post.postBodyHtml, m_EmoticsDict).replaceAll(find, replace));
            } else {
                m_Body.append(Post.modifyBody(post.postBodyHtml, m_EmoticsDict));
            }
            m_Body.append("</div>").append("</div><div class=\"between_messages\"></div>");
        }
        if (posts.isEmpty()) {
            m_Body.append("<div class=\"bad-search-result\">\n" +
                    "\t<h3>Поиск не дал результатов</h3>\n" +
                    "\t<span>Попробуйте сформулировать свой запрос иначе</span>\n" +
                    "</div>");
        }
        m_Body.append("</div>");
        endTopic(searchResult);
        return m_Body.toString();
    }

    public static List<SearchResultPost> parsePosts(String pageBody) {
        Matcher matcher = Pattern
                .compile("<div[^>]*?class=\"cat_name\"[^>]*?>([\\s\\S]*?)<\\/div>[\\s\\S]*?post_date\">([^\\|&]*)[\\s\\S]*?<font color=\"([^\"]*?)\"[\\s\\S]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)(?:<i[\\s\\S]*?\\/i>)?<\\/a>[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div class=\"cat_name\"|<div><div class=\"pagination\">|<div><\\/div><br \\/><\\/form>|<\\/body>)",
                        Pattern.CASE_INSENSITIVE)
                .matcher(pageBody);
        ArrayList<SearchResultPost> result = new ArrayList<>();
        while (matcher.find()) {
            String titleHtml = matcher.group(1);
            String dateTime = matcher.group(2);
            String userState = matcher.group(3).equals("red") ? "" : "online";
            String userId = matcher.group(4);
            String userName = matcher.group(5);
            String postBody = matcher.group(6);
            result.add(new SearchResultPost(titleHtml, dateTime, userState, userId, userName, postBody));
        }
        return result;
    }

    private SearchResult createSearchResult(AppResponse response) {
        final Pattern pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);");
        // https://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        //final Pattern paginationPattern = Pattern.compile("<div class=\"pagination\">([\\s\\S]*?)<\\/div><\\/div><br");

        final Pattern lastPageStartPattern = Pattern.compile("(https?://" + HostHelper.getHost() + ")?/forum/index.php\\?act=search.*?st=(\\d+)");
        final Pattern currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>");

        SearchResult searchResult = new SearchResult(response.redirectUrlElseRequestUrl());

        String page = response.getResponseBody();
        if (page == null)
            page = "";
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
                    Functions.isWebviewAllowJavascriptInterface(), true, true);
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
                    Functions.isWebviewAllowJavascriptInterface(), true, false);
        }
        m_Body.append("</div><div id=\"bottomMargin\"></div>");
        endBody();
        endHtml();
    }
}
