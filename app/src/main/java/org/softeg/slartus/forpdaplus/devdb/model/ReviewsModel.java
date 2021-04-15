package org.softeg.slartus.forpdaplus.devdb.model;

/**
 * Created by isanechek on 23.11.15.
 */
public class ReviewsModel {

    private final String mReviewLink;
    private final String mReviewImgLink;
    private final String mReviewTitle;
    private final String mReviewDate;
    private final String mReviewsDescription;

    public ReviewsModel(String reviewDate, String reviewImgLink, String reviewLink, String reviewsDescription, String reviewTitle) {
        mReviewDate = reviewDate;
        mReviewImgLink = reviewImgLink;
        mReviewLink = reviewLink;
        mReviewsDescription = reviewsDescription;
        mReviewTitle = reviewTitle;
    }

    public String getReviewDate() {
        return mReviewDate;
    }

    public String getReviewImgLink() {
        return mReviewImgLink;
    }

    public String getReviewLink() {
        return mReviewLink;
    }

    public String getReviewsDescription() {
        return mReviewsDescription;
    }

    public String getReviewTitle() {
        return mReviewTitle;
    }
}
