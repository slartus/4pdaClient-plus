package org.softeg.slartus.forpdaplus.prefs;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.util.List;

/*
 * Created by slinkin on 04.03.2015.
 */
public class ForumPreferencesActivity extends BasePreferencesActivity  {

    public static final int REQUEST_CODE= App.getInstance().getUniqueIntValue();
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.forum_headers, target);
    }



}