package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 30.01.16.
 */
public class MarkAllReadBrickInfo extends BrickInfo {
    public static final String NAME = "MarkAllRead";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.mark_all_forums_read);
    }

    @Override
    public int getIcon() {
        return R.drawable.check_all;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
