package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 07.05.2014.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactsList;

public class QmsContactsBrickInfo extends BrickInfo {
    public static final String NAME = "QmsContacts";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.contacts);
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
        return new QmsContactsList().setBrickInfo(this);
    }
}
