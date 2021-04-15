package org.softeg.slartus.forpdaplus.devdb.model;

/**
 * Created by isanechek on 19.12.15.
 */
public class FirmwareModel {

    private final String firmwareTitle;
    private final String firmwareLink;
    private final String firmwareDate;
    private final String firmwareDescription;

    public FirmwareModel(String firmwareDate, String firmwareDescription, String firmwareLink, String firmwareTitle) {
        this.firmwareDate = firmwareDate;
        this.firmwareDescription = firmwareDescription;
        this.firmwareLink = firmwareLink;
        this.firmwareTitle = firmwareTitle;
    }

    public String getFirmwareDate() {
        return firmwareDate;
    }

    public String getFirmwareDescription() {
        return firmwareDescription;
    }

    public String getFirmwareLink() {
        return firmwareLink;
    }

    public String getFirmwareTitle() {
        return firmwareTitle;
    }
}
