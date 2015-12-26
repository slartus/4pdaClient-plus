package org.softeg.slartus.forpdaplus.devdb.model;

import java.util.ArrayList;

/**
 * Created by isanechek on 17.11.15.
 */
public class CommentsModel {

    private String mCommentUserLink;
    private String mCommentUserName;
    private String mCommentDate;
    private String mCommentText;
    private String mCommentRatingText;
    private String mCommentRatingNum;
    private ArrayList<String> mRatingList;

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
