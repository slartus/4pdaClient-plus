package org.softeg.slartus.forpdaplus.emotic;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:26
 * To change this template use File | Settings | File Templates.
 */
public class Smile {
    public String HtmlText;
    public String FileName;
    public int Weight = 0;

    public Smile(String htmlText, String fileName) {
        HtmlText = htmlText;
        FileName = fileName;
    }
}
