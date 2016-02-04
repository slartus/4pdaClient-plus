package org.softeg.slartus.forpdaplus.classes;

import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by slinkin on 25.12.13.
 */
public class HtmlBuilder{
    public final String ACTIONBAR_TOP_MARGIN= getMarginTop()+"px";
    protected StringBuilder m_Body;

    public void beginHtml(String title) {
        m_Body = new StringBuilder();
        m_Body.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        m_Body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n");
        if (!Preferences.System.isDevStyle())
            addStyleSheetLink(m_Body);

        addScripts();
        if (Preferences.System.isDevGrid())
            m_Body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/grid.css\"/>\n");
        if (Preferences.System.isDevBounds())
            m_Body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/bounds.css\"/>\n");
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
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/z_forum_helpers.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/blockeditor.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/z_emoticons.js\"></script>\n");
    }

    public void addStyleSheetLink(StringBuilder sb) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://").append(getStyle()).append("\" />\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
    }

    protected String getStyle() {
        return App.getInstance().getThemeCssFileName();
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
                .append(App.getInstance().isNewYear() ? "newyear " : "")
                .append(PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("isAccelerateGif", false) ? "ongpuimg \" " : "\" ");
        if(App.getInstance().getWebViewFont().equals("")) {
            m_Body.append(" ");
        }else {
            m_Body.append("style=\"font-family:").append(App.getInstance().getWebViewFont()).append(";\" ");
        }
        m_Body.append(bodyScript == null || TextUtils.isEmpty(bodyScript) ? "" : bodyScript)
                .append(">\n");
        /*if (bodyScript == null || TextUtils.isEmpty(bodyScript)) {
            if(font==0){
                m_Body.append("<body id=\""+id+"\" class=\"modification ").append(isImage ? "" : "noimages ").append(isGpuImg?"ongpuimg ":"").append("\">\n");
            }else {
                m_Body.append("<body id=\""+id+"\" class=\"modification ").append(isImage ? "" : "noimages").append(isGpuImg?"ongpuimg ":"").append("\" style=\"font-family:inherit;\">\n");
            }
        }else {
            if(font==0){
                m_Body.append("<body id=\""+id+"\" class=\"modification ").append(isImage ? "" : "noimages").append(isGpuImg?"ongpuimg ":"").append("\" " + bodyScript + ">\n");
            }else {
                m_Body.append("<body id=\""+id+"\"class=\"modification ").append(isImage ? "" : "noimages").append(isGpuImg?"ongpuimg ":"").append("\" style=\"font-family:inherit;\" " + bodyScript + ">\n");
            }
        }*/
        if(Preferences.System.isDevInterface())
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/forum/js/less-dev.js\"></script> <!-- DEVELOPER -->\n");
    }

    public HtmlBuilder endBody() {
        String emoPath = HtmlPreferences.isUseLocalEmoticons(App.getContext()) ?
                "file:///android_asset/forum/style_emoticons/default/" : "http://s.4pda.to/img/emot/";
        m_Body.append("<script>jsEmoticons.parseAll('").append(emoPath).append("');initPostBlock();</script>");
        //m_Body.append("<style>body:after {content: \"\";position: fixed;width: 100%;height: 100%;top:0;left: 0;z-index: 1000000;background-image:-webkit-linear-gradient(rgba(0, 128, 0, 0.3) 1px, transparent 1px),-webkit-linear-gradient(left, rgba(0, 128, 0, 0.3) 1px, transparent 1px);background-image:-o-linear-gradient(rgba(0, 128, 0, 0.3) 1px, transparent 1px),-o-linear-gradient(left, rgba(0, 128, 0, 0.3) 1px, transparent 1px);background-image:linear-gradient(rgba(0, 128, 0, 0.3) 1px, transparent 1px),linear-gradient(to right, rgba(0, 128, 0, 0.3) 1px, transparent 1px);background-size:0.5em 0.5em, 0.5em 0.5em;background-position:-1px -1px, -1px -1px;}</style>");
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
