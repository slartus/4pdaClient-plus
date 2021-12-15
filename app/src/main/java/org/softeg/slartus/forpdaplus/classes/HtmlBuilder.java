package org.softeg.slartus.forpdaplus.classes;

import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by slinkin on 25.12.13.
 */
public class HtmlBuilder{
    final String ACTIONBAR_TOP_MARGIN= getMarginTop()+"px";
    protected StringBuilder m_Body;

    public void beginHtml(String title) {
        m_Body = new StringBuilder();
        m_Body.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        m_Body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n");
        if (!Preferences.System.isDevStyle()) {
            addStyleSheetLink(m_Body);
        }
        //m_Body.append("<script type=\"text/javascript\" src=\"file://").append(getStyle().replace(".css","")).append(".js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file://").append(Preferences.System.getSystemDir()).append("custom_scripts.js\"></script>\n");
        if (Preferences.System.isDevGrid())
            m_Body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/grid.css\"/>\n");
        if (Preferences.System.isDevBounds())
            m_Body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/bounds.css\"/>\n");
        addScripts();
        m_Body.append("<title>").append(title).append("</title>\n");
        m_Body.append("</head>\n");
    }
    public static int getMarginTop(){
        /*int margin = 0;

        Context context = App.getContext();
        Resources resources = context.getResources();

        int statusBar = resources.getIdentifier("status_bar_height", "dimen", "android");
        margin += (int) Math.ceil(resources.getDimensionPixelSize(statusBar)/resources.getDisplayMetrics().density);
        TypedValue tv = new TypedValue();
        App.getContext().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true);
        margin += (int) Math.ceil(resources.getDimensionPixelSize(tv.resourceId)/resources.getDisplayMetrics().density);

        return margin;*/
        return 0;
    }

    public void addScripts() {
        if(App.getInstance().getPreferences().getBoolean("only_custom_script", false)){
            m_Body.append("<script type=\"text/javascript\" src=\"file://").append(getStyle().replaceFirst("\\/[\\S\\s][^\\/]*?\\.css","/base_script.js")).append("\"></script>\n");
        }else {
            //m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/z_forum_helpers.js\"></script>\n");
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/z_emoticons.js\"></script>\n");
        }
    }

    public void addStyleSheetLink(StringBuilder sb) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://").append(getStyle()).append("\" />\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
    }

    protected String getStyle() {
        return AppTheme.getThemeCssFileName();
    }

    public HtmlBuilder append(String str) {
        m_Body.append(str);
        return this;
    }

    public void beginBody(String id) {
        beginBody(id, null, true);
    }

    public void beginBody(String id, CharSequence bodyScript, boolean isImage) {
        m_Body.append("<body id=\"").append(id).append("\" class=\"modification ")
                .append(isImage ? "" : "noimages ")
                .append(App.getInstance().getPreferences().getBoolean("isAccelerateGif", false) ? "ongpuimg \" " : "\" ");
        if(AppTheme.getWebViewFont().equals("")) {
            m_Body.append(" ");
        }else {
            m_Body.append("style=\"font-family:").append(AppTheme.getWebViewFont()).append(";\" ");
        }
        m_Body.append(bodyScript == null || TextUtils.isEmpty(bodyScript) ? "" : bodyScript)
                .append(">\n");
    }

    public HtmlBuilder endBody() {
        //m_Body.append("<script>jsEmoticons.parseAll('").append("file:///android_asset/forum/style_emoticons/default/").append("');initPostBlock();</script>");
        m_Body.append("<script>jsEmoticons.parseAll('").append("file:///android_asset/forum/style_emoticons/default/").append("');</script>");
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
