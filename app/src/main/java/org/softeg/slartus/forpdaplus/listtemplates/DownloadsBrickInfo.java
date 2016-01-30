package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 30.01.16.
 */
public class DownloadsBrickInfo extends BrickInfo {
    public static final String NAME = "Downloads";

    @Override
    public String getTitle() {
        return "Загрузки";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_download_grey600_24dp;
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
