package org.softeg.slartus.forpdaplus.devdb.model;

import java.util.ArrayList;

/**
 * Created by isanechek on 17.11.15.
 */
public class CommentsModel {

    private final String mCommentUserLink;
    private final String mCommentUserName;
    private final String mCommentDate;
    private final String mCommentText;
    private final String mCommentRatingText;
    private final String mCommentRatingNum;
    private final ArrayList<String> mRatingList;

    public CommentsModel(String commentDate, String commentRatingNum, String commentRatingText, String commentText, String commentUserLink, String commentUserName, ArrayList<String> listRating) {
        mCommentDate = commentDate;
        mCommentRatingNum = commentRatingNum;
        mCommentRatingText = commentRatingText;
        mCommentText = commentText;
        mCommentUserLink = commentUserLink;
        mCommentUserName = commentUserName;
        mRatingList = listRating;
    }

    public String getCommentDate() {
        return mCommentDate;
    }

    public String getCommentRatingNum() {
        return mCommentRatingNum;
    }

    public String getCommentRatingText() {
        return mCommentRatingText;
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

    public ArrayList<String> getRatingList() {
        return mRatingList;
    }
}
