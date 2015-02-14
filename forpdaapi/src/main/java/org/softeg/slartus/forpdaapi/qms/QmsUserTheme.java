package org.softeg.slartus.forpdaapi.qms;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 04.02.13
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public class QmsUserTheme {
    public String Id;
    public String Title;
    public String NewCount;
    public String Count;
    public String Date;
    private boolean selected;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
