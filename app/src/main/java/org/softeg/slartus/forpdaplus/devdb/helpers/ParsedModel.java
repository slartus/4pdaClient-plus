package org.softeg.slartus.forpdaplus.devdb.helpers;

import org.softeg.slartus.forpdaplus.devdb.model.SpecModel;

/**
 * Created by radiationx on 05.01.16.
 */
public class ParsedModel {
    private String title;
    private String commentsModels;
    private String discussionModels;
    private String reviewsModels;
    private String firmwareModels;
    private String pricesModels;
    private SpecModel specModel;

    public void setTitle(String title) {
        this.title = title;
    }
    public void setCommentsModels(String commentsModels) {
        this.commentsModels = commentsModels;
    }
    public void setDiscussionModels(String discussionModels) {
        this.discussionModels = discussionModels;
    }
    public void setFirmwareModels(String firmwareModels) {
        this.firmwareModels = firmwareModels;
    }
    public void setPricesModels(String pricesModels) {
        this.pricesModels = pricesModels;
    }
    public void setReviewsModels(String reviewsModels) {
        this.reviewsModels = reviewsModels;
    }
    public void setSpecModel(SpecModel specModel) {
        this.specModel = specModel;
    }

    public String getTitle() {
        return title;
    }
    public String getCommentsModels() {
        return commentsModels;
    }
    public String getDiscussionModels() {
        return discussionModels;
    }
    public String getFirmwareModels() {
        return firmwareModels;
    }
    public String getPricesModels() {
        return pricesModels;
    }
    public String getReviewsModels() {
        return reviewsModels;
    }
    public SpecModel getSpecModel() {
        return specModel;
    }
}
