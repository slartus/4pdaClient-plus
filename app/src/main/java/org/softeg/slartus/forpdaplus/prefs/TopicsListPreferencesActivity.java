package org.softeg.slartus.forpdaplus.prefs;/*
 * Created by slinkin on 16.04.2014.
 */

import org.softeg.slartus.forpdaplus.R;

import java.util.List;

public class TopicsListPreferencesActivity extends BasePreferencesActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.topics_list_headers, target);
    }

}
