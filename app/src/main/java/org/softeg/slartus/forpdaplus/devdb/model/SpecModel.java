package org.softeg.slartus.forpdaplus.devdb.model;

import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by radiationx on 05.01.16.
 */
public class SpecModel {
    private final ArrayList<String> galleryLinks = new ArrayList<>();
    private final ArrayList<String> galleryImages = new ArrayList<>();
    private String price;
    private Elements specTable;

    public void setPrice(String price) {
        this.price = price;
    }
    public void setSpecTable(Elements specTable) {
        this.specTable = specTable;
    }

    public ArrayList<String> getGalleryImages() {
        return galleryImages;
    }
    public ArrayList<String> getGalleryLinks() {
        return galleryLinks;
    }
    public Elements getSpecTable() {
        return specTable;
    }
    public String getPrice() {
        return price;
    }
}
