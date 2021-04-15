package org.softeg.slartus.forpdaapi.devdb;

import org.softeg.slartus.forpdaapi.IListItem;

/**
 * User: slinkin
 * Date: 25.09.12
 * Time: 9:05
 */
public class DevModel implements IListItem {
    public String name;
    private final String id;
    private CharSequence description;
    private String imgUrl;
    private String rate;

    public DevModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public CharSequence getId() {
        return id;
    }

    @Override
    public CharSequence getTopLeft() {
        return null;
    }

    @Override
    public CharSequence getTopRight() {
        return null;
    }

    @Override
    public CharSequence getMain() {
        return name;
    }

    @Override
    public CharSequence getSubMain() {
        return description;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public CharSequence getSortOrder() {
        return null;
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
