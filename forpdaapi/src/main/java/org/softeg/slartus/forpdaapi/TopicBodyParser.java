package org.softeg.slartus.forpdaapi;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.softeg.slartus.hosthelper.HostHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by slartus on 05.06.2014.
 */
public class TopicBodyParser implements Parcelable {
    private String forumId;
    private String topicTitle;
    private String topicDescription;
    private String postsBody;
    private final Uri uri;

    private int pagesCount = 1, postsPerPage = 1, currentPage = 1;

    public TopicBodyParser(String url) {
        this.uri = Uri.parse(url);
    }

    public void parse(String pageBody) {
        Matcher m = Pattern.compile("^([\\s\\S]*?)<a[^>]*name=\"entry([\\s\\S]*?)<div[^>]*class=\"[^\"]*topic_foot_nav[^\"]*\"[^>]*>",
                Pattern.CASE_INSENSITIVE).matcher(pageBody);

        String headerBody;
        String postsBody;
        if (m.find()) {
            headerBody = m.group(1);
            postsBody = "<a name=\"entry" + m.group(2);
        } else {
            m = Pattern.compile("^([\\s\\S]*?)<body[^>]*>([\\s\\S]*?)</body>", Pattern.CASE_INSENSITIVE)
                    .matcher(pageBody);
            if (m.find()) {
                headerBody = m.group(1);
                postsBody = m.group(2);
            } else {
                headerBody = pageBody;
                postsBody = pageBody;
            }

        }


        parseHeader(headerBody);
        parseBody(postsBody);
    }

    private void parseBody(String postsBody) {
        this.postsBody = postsBody;
    }

    private void parseHeader(String headerBody) {
        Matcher m;
        m = Pattern.compile("<title>(.*?)\\s*-\\s*4PDA\\s*</title>", Pattern.CASE_INSENSITIVE).matcher(headerBody);
        if (m.find())
            topicTitle = m.group(1);
        m = Pattern.compile("<div[^>]*class=\"topic_title_post\"[^>]*>([^<]*)<", Pattern.CASE_INSENSITIVE).matcher(headerBody);
        if (m.find())
            topicDescription = m.group(1);

        m = Pattern.compile("<div[^>]*class=\"pagination\">[^>]*>(\\d+) страниц.?</a>.*?<span[^>]*class=\"pagecurrent\"[^>]*>(\\d+)</span>.*?\"/forum/index.php\\?showtopic=\\d+&amp;st=(\\d+)\">&raquo;</a>",
                Pattern.CASE_INSENSITIVE).matcher(headerBody);
        if (m.find()) {
            currentPage = Integer.parseInt(m.group(2));
            pagesCount = Integer.parseInt(m.group(1));
            postsPerPage = Integer.parseInt(m.group(3)) / (pagesCount - 1);
        }

        m = Pattern.compile("<div[^>]*id=\"navstrip\"[^>]*>.*?showforum=(\\d+).*?</div>", Pattern.CASE_INSENSITIVE).matcher(headerBody);
        if (m.find())
            forumId = m.group(1);
    }

    public static final Parcelable.Creator<TopicBodyParser> CREATOR
            = new Parcelable.Creator<TopicBodyParser>() {
        public TopicBodyParser createFromParcel(Parcel in) {
            return new TopicBodyParser(in);
        }

        public TopicBodyParser[] newArray(int size) {
            return new TopicBodyParser[size];
        }
    };

    public String getTopicId() {
        return uri.getQueryParameter("showtopic");
    }

    public String getFragment() {
        return uri.getFragment();
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public String getTopicDescription() {
        return topicDescription;
    }

    public String getPostsBody() {
        return postsBody;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public int getPostsPerPage() {
        return postsPerPage;
    }

    private TopicBodyParser(Parcel parcel) {
        topicTitle = parcel.readString();
        topicDescription = parcel.readString();
        postsBody = parcel.readString();
        uri = Uri.parse(parcel.readString());
        pagesCount = parcel.readInt();
        postsPerPage = parcel.readInt();
        currentPage = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(topicTitle);
        parcel.writeString(topicDescription);
        parcel.writeString(postsBody);
        parcel.writeString(uri.toString());
        parcel.writeInt(pagesCount);
        parcel.writeInt(postsPerPage);
        parcel.writeInt(currentPage);

    }

    public String getForumId() {
        return forumId;
    }

    public String getUrl() {
        return uri.toString();
    }

    public String getNextPageUrl() {
        return String.format("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=%s&st=%d", getTopicId(), currentPage * postsPerPage);
    }

    public String getPrevPageUrl() {
        return String.format("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=%s&st=%d", getTopicId(), (currentPage - 2) * postsPerPage);
    }

    public String getLastPageUrl() {
        return String.format("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=%s&st=%d", getTopicId(), (pagesCount - 1) * postsPerPage);
    }

    public String getFirstPageUrl() {
        return String.format("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=%s", getTopicId());
    }

    public String getPageUrl(int pageNum) {
        return String.format("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=%s&st=%d", getTopicId(), (pageNum - 1) * postsPerPage);
    }
}
