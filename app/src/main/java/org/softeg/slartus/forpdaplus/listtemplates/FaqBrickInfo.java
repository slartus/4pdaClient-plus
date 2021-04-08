package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by isanechek on 01.04.16.
 */
public class FaqBrickInfo extends BrickInfo {
    public static final String NAME = "faqinfo";

    @Override
    public String getTitle() {
        return "FAQ";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_school_24dp;
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
