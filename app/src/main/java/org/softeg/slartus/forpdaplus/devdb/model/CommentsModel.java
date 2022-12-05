package org.softeg.slartus.forpdaplus.devdb.model;


/**
 * Created by isanechek on 17.11.15.
 */
public class CommentsModel {

    private final String mCommentUserLink;
    private final String mCommentUserName;
    private final String mCommentDate;
    private final String mCommentText;
    private final String mCommentRatingNum;

    public CommentsModel(String commentDate, String commentRatingNum, String commentText, String commentUserLink, String commentUserName) {
        mCommentDate = commentDate;
        mCommentRatingNum = commentRatingNum;
        mCommentText = commentText;
        mCommentUserLink = commentUserLink;
        mCommentUserName = commentUserName;
    }

    public String getCommentDate() {
        return mCommentDate;
    }

    public String getCommentRatingNum() {
        return mCommentRatingNum;
    }


    public String getCommentText() {
        return mCommentText;
    }

    public String getCommentUserLink() {
        return mCommentUserLink;
    }

    public String getCommentUserName() {
        return mCommentUserName;
    }
}
