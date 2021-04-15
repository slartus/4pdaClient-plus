package org.softeg.slartus.forpdaplus.devdb.model;

/**
 * Created by isanechek on 19.12.15.
 */
public class PricesModel {

    private final String pricesTitle;
    private final String pricesLink;
    private final String pricesDate;
    private final String pricesDescription;

    public PricesModel(String pricesDate, String pricesDescription, String pricesLink, String pricesTitle) {
        this.pricesDate = pricesDate;
        this.pricesDescription = pricesDescription;
        this.pricesLink = pricesLink;
        this.pricesTitle = pricesTitle;
    }

    public String getPricesDate() {
        return pricesDate;
    }

    public String getPricesDescription() {
        return pricesDescription;
    }

    public String getPricesLink() {
        return pricesLink;
    }

    public String getPricesTitle() {
        return pricesTitle;
    }
}
