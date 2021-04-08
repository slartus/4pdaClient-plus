package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 05.05.2014.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;

public class TopicAttachmentBrickInfo extends BrickInfo {
    public static final String NAME = "TopicAttachment";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.attachments);
    }

    @Override
    public int getIcon() {
        return R.drawable.close_white;
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
