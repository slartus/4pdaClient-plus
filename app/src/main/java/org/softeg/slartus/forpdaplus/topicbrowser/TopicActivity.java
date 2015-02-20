package org.softeg.slartus.forpdaplus.topicbrowser;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.common.AppLog;

/*
 * User: slinkin
 * Date: 20.09.12
 * Time: 8:47
 */
public class TopicActivity extends BaseFragmentActivity {
    private String TOPIC_FRAGMENT_BUNDLE_KEY = "TOPIC_FRAGMENT_BUNDLE_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.topic_activity);

        createMenu();

        Bundle args = new Bundle();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras.containsKey("ThemeUrl")) {
                String params = null;
                if (extras.containsKey("Params"))
                    params = extras.getString("Params");
                String url = "http://4pda.ru/forum/index.php?showtopic=" + extras.getString("ThemeUrl") + (TextUtils.isEmpty(params) ? "" : ("&" + params));
                args.putString(TopicFragment.TOPIC_URL_KEY, url);
            }
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(TOPIC_FRAGMENT_BUNDLE_KEY)) {
            args.putAll(savedInstanceState.getBundle(TOPIC_FRAGMENT_BUNDLE_KEY));
        }
        TopicFragment fragment = (TopicFragment) getSupportFragmentManager().findFragmentById(R.id.topic_fragment);
        if (fragment != null) return;
        fragment = TopicFragment.newInstance(args);
        getSupportFragmentManager().beginTransaction().add(R.id.topic_fragment, fragment).commit();
    }

    private void createMenu() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ProfileMenuFragment fragment = (ProfileMenuFragment) fm.findFragmentByTag(ProfileMenuFragment.TAG);
            if (fragment == null) {
                fragment = new ProfileMenuFragment();
                ft.add(fragment, ProfileMenuFragment.TAG);
            }
            ft.commit();
        } catch (Exception ex) {
            AppLog.e(this, ex);
        }
    }

    @Override
    protected boolean isTransluent() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        TopicFragment fragment =
                (TopicFragment) getSupportFragmentManager().findFragmentById(R.id.topic_fragment);
        Bundle args = new Bundle();
        fragment.onSaveInstanceState(args);
        outState.putBundle(TOPIC_FRAGMENT_BUNDLE_KEY, args);
    }


}
