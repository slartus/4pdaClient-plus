package org.softeg.slartus.forpdaapi.search;/*
 * Created by slinkin on 14.04.2014.
 */

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.URIUtils;
import org.softeg.slartus.forpdacommon.UrlExtensions;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchSettings implements Parcelable {
    /*
    сортировать результаты по релевантности
     */
    @SuppressWarnings("unused")
    public static final String RESULT_SORT_RELEVANT = "rel";
    /*
    сортировать результаты по дате(от новых к старым)
     */
    public static final String RESULT_SORT_DATE_DESC = "dd";
    /*
   сортировать результаты по дате(от старых к новым)
    */
    @SuppressWarnings("unused")
    public static final String RESULT_SORT_DATE = "da";

    /*
    результаты в виде сообщений
     */
    public static final String RESULT_VIEW_POSTS = "posts";
    /*
    результаты в виде тем
    */
    public static final String RESULT_VIEW_TOPICS = "topics";

    /*
   Искать везде
    */
    public static final String SOURCE_ALL = "all";
    /*
   Искать Только в заголовках тем
    */
    public static final String SOURCE_TOPICS = "top";
    /*
    Искать Только в сообщениях
   */
    public static final String SOURCE_POSTS = "pst";

    //общий поиск
    public static final String SEARCH_TYPE_FORUM = "SEARCH_TYPE_FORUM";
    // поиск в теме
    public static final String SEARCH_TYPE_TOPIC = "SEARCH_TYPE_TOPIC";
    // поиск тем пользователя
    public static final String SEARCH_TYPE_USER_TOPICS = "SEARCH_TYPE_USER_TOPICS";
    // поиск сообщений пользователя
    public static final String SEARCH_TYPE_USER_POSTS = "SEARCH_TYPE_USER_POSTS";

    private String m_SearchType = SEARCH_TYPE_FORUM;
    private Set<String> m_TopicsIds = new HashSet<>();

    private String m_Query;
    private String m_ResultView = RESULT_VIEW_POSTS;
    private Boolean m_SearchInSubForums = true;
    private String m_Sort = RESULT_SORT_DATE_DESC;
    private String m_UserName;
    private String m_Source = SOURCE_ALL;
    private Set<String> m_ForumsIds = new HashSet<>();

    public SearchSettings() {

    }

    public SearchSettings(String searchType) {
        m_SearchType = searchType;
    }

    public String getQuery() {
        return m_Query;
    }

    public String getUserName() {
        return m_UserName;
    }

    public String getSource() {
        return m_Source;
    }

    public String getSort() {
        return m_Sort;
    }


    public void setSearchInSubForums(Boolean value) {
        m_SearchInSubForums = value;
    }

    public Set<String> getForumsIds() {
        return m_ForumsIds;
    }

    public String getSearchQuery() {

        List<NameValuePair> qualms = new ArrayList<>();
        qualms.add(new BasicNameValuePair("act", "search"));
        qualms.add(new BasicNameValuePair("query", m_Query));
        qualms.add(new BasicNameValuePair("username", m_UserName));
        for (String key : m_ForumsIds)
            qualms.add(new BasicNameValuePair("forums[]", key));
        for (String key : m_TopicsIds)
            qualms.add(new BasicNameValuePair("topics[]", key));
        qualms.add(new BasicNameValuePair("subforums", m_SearchInSubForums ? "1" : "0"));
        qualms.add(new BasicNameValuePair("source", m_Source));
        qualms.add(new BasicNameValuePair("sort", m_Sort));
        qualms.add(new BasicNameValuePair("result", m_ResultView));
        qualms.add(new BasicNameValuePair("noform", "1"));

        String uri = URIUtils.createURI("http", HostHelper.getHost(), "/forum/index.php",
                qualms, "windows-1251");
        return uri;
    }

    public String getResultView() {
        return m_ResultView;
    }

    private void tryParse(String url) {
        url = tryUrlDecode(url);
        url = Html.fromHtml(url).toString();
        Matcher m = Pattern.compile("(?:([\\w\\[\\]]+)=(.*?))(?:\\&|$)").matcher(url);
        m_ForumsIds = new HashSet<>();
        m_TopicsIds = new HashSet<>();
        while (m.find()) {
            String key = m.group(1).toLowerCase();
            String val = m.group(2);
            switch (key) {
                case "query":
                    m_Query = val;
                    break;
                case "username":
                    m_UserName = val;
                    break;
                case "forums[]":
                    m_ForumsIds.add(val);
                    break;
                case "subforums":
                    m_SearchInSubForums = "1".equals(val);
                    break;
                case "source":
                    m_Source = val;
                    break;
                case "sort":
                    m_Sort = val;
                    break;
                case "result":
                    m_ResultView = val;
                    break;
                case "topics[]":
                    m_TopicsIds.add(val);
                    break;
            }
        }
    }


    private String tryUrlDecode(String url) {
        try {
            if (UrlExtensions.isUrlUtf8Encoded(url))
                return URLDecoder.decode(url, "UTF-8");
            return URLDecoder.decode(url, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(m_SearchType);
        parcel.writeString(m_Query);
        parcel.writeString(m_UserName);
        parcel.writeInt(m_TopicsIds.size());
        for (String forumId : m_TopicsIds) {
            parcel.writeString(forumId);
        }

        parcel.writeString(m_ResultView);
        parcel.writeByte((byte) (m_SearchInSubForums ? 1 : 0));
        parcel.writeString(m_Sort);
        parcel.writeString(m_Source);
        parcel.writeInt(m_ForumsIds.size());
        for (String forumId : m_ForumsIds) {
            parcel.writeString(forumId);
        }
    }

    public static final Parcelable.Creator<SearchSettings> CREATOR = new Parcelable.Creator<SearchSettings>() {
        // распаковываем объект из Parcel
        public SearchSettings createFromParcel(Parcel in) {

            return new SearchSettings(in);
        }

        public SearchSettings[] newArray(int size) {
            return new SearchSettings[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private SearchSettings(Parcel parcel) {
        m_SearchType = parcel.readString();
        m_Query = parcel.readString();
        m_UserName = parcel.readString();
        int count = parcel.readInt();
        for (int i = 0; i < count; i++) {
            m_TopicsIds.add(parcel.readString());
        }

        m_ResultView = parcel.readString();
        m_SearchInSubForums = parcel.readByte() == 1;
        m_Sort = parcel.readString();
        m_Source = parcel.readString();

        count = parcel.readInt();
        for (int i = 0; i < count; i++) {
            m_ForumsIds.add(parcel.readString());
        }
    }

    public SharedPreferences.Editor save(SharedPreferences.Editor editor) {
        editor.putString("search.query", m_Query);
        editor.putString("search.username", m_UserName);

        saveForums(editor);
        saveSearchInSubForums(editor);

        editor.putString("search.source", m_Source);
        editor.putString("search.sort", m_Sort);
        editor.putString("search.result", m_ResultView);


        return editor.putStringSet("search.topics", m_TopicsIds);
    }

    private void saveForums(SharedPreferences.Editor editor) {
        editor.putStringSet("search.forums", m_ForumsIds);
    }

    private void saveSearchInSubForums(SharedPreferences.Editor editor) {
        editor.putBoolean("search.subforums", m_SearchInSubForums);
    }

    public void load(SharedPreferences preferences) {
        m_Query = preferences.getString("search.query", m_Query);
        m_UserName = preferences.getString("search.username", m_UserName);

        loadForums(preferences);
        m_SearchInSubForums = preferences.getBoolean("search.subforums", m_SearchInSubForums);

        m_Source = preferences.getString("search.source", m_Source);
        m_Sort = preferences.getString("search.sort", m_Sort);
        m_ResultView = preferences.getString("search.result", m_ResultView);


        m_TopicsIds = preferences.getStringSet("search.topics", m_TopicsIds);
    }


    private void loadForums(SharedPreferences preferences) {
        m_ForumsIds = new HashSet<>();
        Set<String> defaultForumIds = new HashSet<>();
        defaultForumIds.add("all");
        m_ForumsIds.addAll(preferences.getStringSet("search.forums", defaultForumIds));
    }

    public boolean getIsSubForums() {
        return m_SearchInSubForums;
    }

    public void setQuery(String query) {
        this.m_Query = query;
    }

    public void setUserName(String userName) {
        this.m_UserName = userName;
    }

    public Set<String> getTopicIds() {
        return m_TopicsIds;
    }

    public void setSource(String source) {
        this.m_Source = source;
    }

    public void setSort(String sort) {
        this.m_Sort = sort;
    }

    public void setResultView(String resultView) {
        this.m_ResultView = resultView;
    }

    public static SearchSettings parse(String url) {
        SearchSettings res = new SearchSettings(SEARCH_TYPE_FORUM);
        res.tryParse(url);
        return res;
    }

    public String getSearchType() {
        return m_SearchType;
    }
}
