package org.softeg.slartus.forpdaplus.prefs;

import android.content.Context;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.Devices;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.02.13
 * Time: 8:18
 * To change this template use File | Settings | File Templates.
 */
public class HtmlPreferences {

    private Boolean m_SpoilerByButton = false;

    public Boolean isSpoilerByButton() {
        return m_SpoilerByButton;
    }

    public static Boolean isFullThemeTitle() {
        return App.getInstance().getPreferences().getBoolean("fullThemeTitle", false);
    }

    private static boolean getDefaultSpoilByButton() {
        return Devices.isAsus_EePad_TF300TG();
    }

    public void load(Context context) {
        m_SpoilerByButton = App.getInstance().getPreferences().getBoolean("theme.SpoilerByButton", getDefaultSpoilByButton());
    }

    public static String modifySpoiler(String postBody) {
        String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                "(Спойлер \\(\\+/-\\).*?</div>)" +
                "(\\s*<div class='hidemain' style=\"display:none\">)";
        String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
        return postBody.replaceAll(find, replace);
    }


    public static String modifyBody(String value, Hashtable<String, String> emoticsDict) {
        value = modifyStyleImagesBody(value);
        value = modifyLinksBody(value);
        value = modifyEmoticons(value, emoticsDict);
        // value=value.replaceAll("<TABLE(.*?)><TD","<TABLE$1><TR><TD").replace("</TD></TABLE>","</TD></TR></TABLE>");
        return value;
    }

    public static String modifyStyleImagesBody(String value) {
        value = value
                .replaceAll("(['\"])[^\"'<>]*style_images/([^\"'<>]*)", "$1file:///android_asset/forum/style_images/$2")
                .replaceAll("\u0000", "");

        return value;
    }

    private static String modifyEmoticons(String value, Hashtable<String, String> emoticsDict, String path) {
        return value;
//        SmilesComparator bvc = new SmilesComparator(emoticsDict);
//        TreeMap<String, String> sorted_map = new TreeMap<String, String>(bvc);
//        sorted_map.putAll(emoticsDict);
//        for (Map.Entry<String, String> entry : sorted_map.entrySet()) {
//            String emo=entry.getKey();
//            if(!emo.startsWith(":")||!emo.endsWith(":")){
//                value = value.replaceAll("(^|\\s+)"+Pattern.quote(emo)+"($|\\s+)", String.format("$1<img src=\"%s%s\"/>$2", path, entry.getValue()));
//            }
//            else
//                value = value.replaceAll("(^|.)"+Pattern.quote(emo)+"($|.)", String.format("$1<img src=\"%s%s\"/>$2", path, entry.getValue()));
//        }
//        return value;
    }

    public static String modifyEmoticons(String value, Hashtable<String, String> emoticsDict) {
        return modifyEmoticonsToLocal(value, emoticsDict);
    }

    private static String modifyEmoticonsToLocal(String value, Hashtable<String, String> emoticsDict) {
        return modifyEmoticons(value, emoticsDict, "file:///android_asset/forum/style_emoticons/default/");
    }

    public static String modifyAttachedImagesBody(Boolean webViewAllowJs, String value) {

        return value
                .replaceAll("<a [^>]*? id=\"ipb-attach-url-\\d+-bb\"[^>]*?href=\"([^\"]*)\"[^>]*?title=\"([^\"]*)\"[^>]*?><img src=\"([^\"]*)\"[^>]*id=\"ipb-attach-img-\\d+-bb\".*?</a>",
                        "<img src=\"file:///android_asset/forum/style_images/1/folder_mime_types/gif.gif\"><a class=\"sp_img\" "
                                + TopicBodyBuilder.getHtmlout(webViewAllowJs, "showImgPreview",
                                new String[]{"Прикрепленное изображение", "$3", "$1"}, false) + ">$2</a>")
                .replaceAll("<img src=\"([^\"]*)\" class=\"linked-image\"[^>]*?>",
                        "<img src=\"file:///android_asset/forum/style_images/1/folder_mime_types/gif.gif\"><a class=\"sp_img\" "
                                + TopicBodyBuilder.getHtmlout(webViewAllowJs, "showImgPreview",
                                new String[]{"Прикрепленное изображение", "$1", "$1"}, false) + ">Прикрепленное изображение</a>")
                /*.replaceAll("<img attach_id=\"\\d+\"[^>]*?src=\"([^\"]*)\"[^>]*?>",
                "<img src=\"file:///android_asset/forum/style_images/1/folder_mime_types/gif.gif\"><a class=\"sp_img\" "
                        + TopicBodyBuilder.getHtmlout(webViewAllowJs, "showImgPreview",
                        new String[]{"Прикрепленное изображение", "$1", "$1"}, false) + ">Прикрепленное изображение</a>")*/;

    }

    private static String modifyLinksBody(String value) {
        return value
                .replaceAll("(src|href)=(['\"])index.php", "$1=$2https://"+ HostHelper.getHost() +"/forum/index.php")
                .replaceAll("(src|href)=(['\"])/forum", "$1=$2https://"+ HostHelper.getHost() +"/forum");
    }
}
