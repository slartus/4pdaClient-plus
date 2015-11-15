package org.softeg.slartus.forpdaplus.fragments.search;

import org.softeg.slartus.forpdaplus.Client;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.10.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class SearchResult {
    private int pagesCount;
    private int lastPageStartCount;
    private int currentPage;

    public void setPagesCount(String pagesCount) {
        this.pagesCount = Integer.parseInt(pagesCount) + 1;
    }

    public void setLastPageStartCount(String value) {
        this.lastPageStartCount = Math.max(Integer.parseInt(value), lastPageStartCount);
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = Integer.parseInt(currentPage);
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPostsPerPageCount(String m_LastUrl) {
        String lastUrl = m_LastUrl;
        URI redirectUri = Client.getInstance().getRedirectUri();
        if (redirectUri != null)
            lastUrl = redirectUri.toString();
        Pattern p = Pattern.compile("st=(\\d+)");
        Matcher m = p.matcher(lastUrl);
        if (m.find())
            lastPageStartCount = Math.max(Integer.parseInt(m.group(1)), lastPageStartCount);

        return lastPageStartCount / (pagesCount - 1);
    }
}
