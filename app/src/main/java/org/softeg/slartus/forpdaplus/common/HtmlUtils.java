package org.softeg.slartus.forpdaplus.common;

import android.text.Html;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 02.04.12
 * Time: 19:02
 * To change this template use File | Settings | File Templates.
 */
public class HtmlUtils {
    public static String modifyHtmlQuote(String body){
        return  Html.fromHtml(body.replace("\n", "<br/>")).toString();
    }
}
