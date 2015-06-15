package org.softeg.slartus.forpdaplus.tabs;

import android.content.Context;

import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 29.11.11
 * Time: 16:01
 */
public class QuickStartTab extends ThemesTab {

    public static final String TEMPLATE = Tabs.TAB_QUICK_START;
    public static final String TITLE = "Быстрый доступ";
    private String template;

    public QuickStartTab(Context context, String tabTag, ITabParent tabParent, String template) {
        super(context, tabTag, tabParent);
        this.template = template;
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }


    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {

    }

}
