package org.softeg.slartus.forpdaapi;

/**
 * Created by slinkin on 21.02.14.
 */
public interface ICatalogItem {
    CharSequence getId();

    CharSequence getTitle();

    CharSequence getSubTitle();

    ICatalogItem getParent();

    void setParent(ICatalogItem catalogItem);

    ICatalogItem clone();


}
