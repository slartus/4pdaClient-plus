package org.softeg.slartus.forpdaplus.devdb.model;

/**
 * Created by isanechek on 18.12.15.
 */
public class DiscussionModel {
    private final String mDiscussionTitle;
    private final String mDiscussionLink;
    private final String mDiscussionDate;
    private final String mDiscussionDescription;

    public DiscussionModel(String discussionDescription, String discussionDate, String discussionLink, String discussionTitle) {
        mDiscussionDescription = discussionDescription;
        mDiscussionDate = discussionDate;
        mDiscussionLink = discussionLink;
        mDiscussionTitle = discussionTitle;
    }

    public String getDiscussionDescription() {
        return mDiscussionDescription;
    }

    public String getDiscussionDate() {
        return mDiscussionDate;
    }

    public String getDiscussionLink() {
        return mDiscussionLink;
    }

    public String getDiscussionTitle() {
        return mDiscussionTitle;
    }
}
