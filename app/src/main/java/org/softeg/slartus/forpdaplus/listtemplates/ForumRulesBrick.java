package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by isanechek on 01.04.16.
 */
public class ForumRulesBrick extends BrickInfo {
    public static final String NAME = "forumrules";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.forum_rules);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_info__forum_rules24dp;
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
