package org.softeg.slartus.forpdaplus.classes;

import android.text.TextUtils;


import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by slinkin on 25.12.13.
 */
public class HtmlBuilder {
    public final String ACTIONBAR_TOP_MARGIN="54pt";
    protected StringBuilder m_Body;

    public void beginHtml(String title) {
        m_Body = new StringBuilder();
        m_Body.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        m_Body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n");
        if (!Preferences.System.isDeveloper())
            addStyleSheetLink(m_Body);

        addScripts();

        m_Body.append("<title>" + title + "</title>\n");
        m_Body.append("</head>\n");
    }

    public void addScripts() {
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/z_forum_helpers.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/blockeditor.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/z_emoticons.js\"></script>\n");
    }

    public void addStyleSheetLink(StringBuilder sb) {


        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://" + getStyle() + "\" />\n");
    }

    protected String getStyle() {
        String cssFile = MyApp.getInstance().getThemeCssFileName();
        return cssFile;

    }

    public void append(String str) {
        m_Body.append(str);
    }

    public void beginBody() {
        beginBody(null);
    }

    public void beginBody(CharSequence bodyScript) {
        if (bodyScript == null || TextUtils.isEmpty(bodyScript))
            m_Body.append("<body>\n");
        else
            m_Body.append("<body " + bodyScript + ">\n");
        if(Preferences.System.isDeveloper())
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/less-dev.js\"></script> <!-- DEVELOPER -->\n");
    }

    public HtmlBuilder endBody() {
        String emoPath = HtmlPreferences.isUseLocalEmoticons(MyApp.getContext()) ?
                "file:///android_asset/forum/style_emoticons/default/" : "http://s.4pda.to/img/emot/";
        m_Body.append("<script>jsEmoticons.parseAll('" + emoPath + "');initPostBlock();</script>");
        m_Body.append("</body>\n");
        return this;
    }

    public void endHtml() {
        m_Body.append("</html>");

    }

    public StringBuilder getHtml() {
        return m_Body;
    }
}
