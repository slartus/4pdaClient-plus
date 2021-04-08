package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 21.03.14.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment;

public class NotesBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.notes);
    }

    @Override
    public int getIcon() {
        return R.drawable.bookmark;
    }

    @Override
    public String getName() {
        return "Notes";
    }

    @Override
    public Fragment createFragment() {
        return new NotesListFragment().setBrickInfo(this);
    }
}
