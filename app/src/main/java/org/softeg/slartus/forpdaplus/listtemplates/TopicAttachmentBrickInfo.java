package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 05.05.2014.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;

public class TopicAttachmentBrickInfo extends BrickInfo {
    public static final String NAME = "TopicAttachment";

    @Override
    public String getTitle() {
        return "Вложения";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return new TopicAttachmentListFragment().setBrickInfo(this);
    }
}
