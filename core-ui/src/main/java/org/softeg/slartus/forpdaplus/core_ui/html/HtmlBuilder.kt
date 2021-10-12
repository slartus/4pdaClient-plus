package org.softeg.slartus.forpdaplus.core_ui.html

import android.text.TextUtils
import org.softeg.slartus.forpdaplus.core_ui.AppTheme.Companion.themeCssFileName
import org.softeg.slartus.forpdaplus.core_ui.AppTheme.Companion.webViewFont
import javax.inject.Inject

class HtmlBuilder @Inject constructor(
    private var htmlStylePreferences: HtmlStylePreferences
) {
    private val html = StringBuilder()

    private val style = themeCssFileName

    fun beginHtml(title: String?) {
        html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")
        html.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n")
        html.append("<head>\n")
        html.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n")
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n")
        if (!htmlStylePreferences.isDevStyle) {
            addStyleSheetLink(html)
        }
        //m_Body.append("<script type=\"text/javascript\" src=\"file://").append(getStyle().replace(".css","")).append(".js\"></script>\n");
        html.append("<script type=\"text/javascript\" src=\"file://")
            .append(htmlStylePreferences.systemDir)
            .append("custom_scripts.js\"></script>\n")
        if (htmlStylePreferences.isDevGrid) html.append(
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/grid.css\"/>\n"
        )
        if (htmlStylePreferences.isDevBounds) html.append(
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/bounds.css\"/>\n"
        )
        addScripts()
        html.append("<title>").append(title).append("</title>\n")
        html.append("</head>\n")
    }

    fun addScripts() {
        if (htmlStylePreferences.onlyCustomScript) {
            html.append("<script type=\"text/javascript\" src=\"file://").append(
                style.replaceFirst(
                    "\\/[\\S\\s][^\\/]*?\\.css".toRegex(),
                    "/base_script.js"
                )
            ).append("\"></script>\n")
        } else {
            //m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/z_forum_helpers.js\"></script>\n");
            html.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n")
            html.append("<script type=\"text/javascript\" src=\"file:///android_asset/z_emoticons.js\"></script>\n")
        }
    }

    fun addStyleSheetLink(sb: StringBuilder) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://").append(style)
            .append("\" />\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n")
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n")
    }

    fun append(str: String?): HtmlBuilder {
        html.append(str)
        return this
    }

    @JvmOverloads
    fun beginBody(id: String?, bodyScript: CharSequence? = null, isImage: Boolean = true) {
        html.append("<body id=\"").append(id).append("\" class=\"modification ")
            .append(if (isImage) "" else "noimages ")
            .append(
                if (htmlStylePreferences.isAccelerateGif) "ongpuimg \" " else "\" "
            )
        if (webViewFont == "") {
            html.append(" ")
        } else {
            html.append("style=\"font-family:").append(webViewFont).append(";\" ")
        }
        html.append(if (bodyScript == null || TextUtils.isEmpty(bodyScript)) "" else bodyScript)
            .append(">\n")
    }

    fun endBody(): HtmlBuilder {
        //m_Body.append("<script>jsEmoticons.parseAll('").append("file:///android_asset/forum/style_emoticons/default/").append("');initPostBlock();</script>");
        html.append("<script>jsEmoticons.parseAll('")
            .append("file:///android_asset/forum/style_emoticons/default/").append("');</script>")
        html.append("</body>\n")
        return this
    }

    fun endHtml() {
        html.append("</html>")
    }

    fun getHtml() = html.toString()

}