package org.softeg.slartus.forpdaplus.devdb.fragments.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by isanechek on 14.12.15.
 */
public class BaseDevDbFragment extends Fragment {

    private String title;
    protected Context context;
    protected View view;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}