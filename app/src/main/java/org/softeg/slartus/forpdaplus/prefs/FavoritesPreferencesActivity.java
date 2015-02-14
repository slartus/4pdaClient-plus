package org.softeg.slartus.forpdaplus.prefs;

import android.os.Bundle;

import org.softeg.slartus.forpdaplus.R;

import java.util.List;

/**
 * Created by Артём on 01.05.14.
 */
public class FavoritesPreferencesActivity extends BasePreferencesActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(ForumTopicsPreferencesActivity.RESULT_NONE);

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.favorites_list_headers, target);
    }

    @Override
    public android.content.Intent onBuildStartFragmentIntent(java.lang.String fragmentName, android.os.Bundle args, int titleRes, int shortTitleRes) {
        args=getIntent().getExtras();
        return super.onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
    }

    @Override
    protected boolean isValidFragment (String fragmentName) {
        if(ForumTopicsPreferencesFragment.class.getName().equals(fragmentName)) return true;
        if(FavoritesPreferencesFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

}
