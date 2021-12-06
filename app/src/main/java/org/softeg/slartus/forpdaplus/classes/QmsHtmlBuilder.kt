package org.softeg.slartus.forpdaplus.classes

import org.softeg.slartus.forpdaplus.emotic.Smiles
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences
import org.softeg.slartus.forpdaplus.prefs.Preferences

class QmsHtmlBuilder : HtmlBuilder() {
    override fun addScripts() {
        super.addScripts()
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/qms.js\"></script>\n")
    }

    fun buildBody(loadMore: Boolean, chatBody: String, htmlPreferences: HtmlPreferences?) {
        var chatBodyLocal = chatBody
        beginHtml("QMS")
        beginBody("qms${if (loadMore) "_more" else ""}", "", Preferences.Topic.isShowAvatars)
        //        htmlBuilder.beginBody("qms", "onload=\"scrollToElement('bottom_element')\"", Preferences.Topic.isShowAvatars());

        if (!Preferences.Topic.isShowAvatars)
            chatBodyLocal = chatBodyLocal.replace("<img[^>]*?class=\"avatar\"[^>]*>".toRegex(), "")
        if (htmlPreferences?.isSpoilerByButton == true)
            chatBodyLocal = HtmlPreferences.modifySpoiler(chatBodyLocal)
        chatBodyLocal = HtmlPreferences.modifyBody(chatBodyLocal, Smiles.getSmilesDict())
        chatBodyLocal = chatBodyLocal.replace("(<a[^>]*?href=\"([^\"]*?savepice[^\"]*-)[\\w]*(\\.[^\"]*)\"[^>]*?>)[^<]*?(</a>)".toRegex(), "$1<img src=\"$2prev$3\">$4")
        append(chatBodyLocal)
        append("<div id=\"bottom_element\" name=\"bottom_element\"></div>")
        //m_Body.append("<script>jsEmoticons.parseAll('").append("file:///android_asset/forum/style_emoticons/default/").append("');initPostBlock();</script>");
        append("<script>jsEmoticons.parseAll('").append("file:///android_asset/forum/style_emoticons/default/").append("');</script>")
        endBody()
        endHtml()
    }
}