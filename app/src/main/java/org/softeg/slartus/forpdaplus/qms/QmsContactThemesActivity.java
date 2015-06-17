package org.softeg.slartus.forpdaplus.qms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 04.02.13
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class QmsContactThemesActivity extends BaseFragmentActivity {

    public static final String MID_KEY = "mid";
    public static final String NICK_KEY = "nick";

    private String m_Id;
    private String m_Nick;

    //  private MenuFragment mFragment1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_contacts_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (savedInstanceState == null ||
                getSupportFragmentManager()
                        .findFragmentById(R.id.container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, QmsContactThemesFragment.newInstance(getIntent().getExtras()))
                    .commitAllowingStateLoss();
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        setTitle("QMS-Темы");
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(m_Nick);
    }


    public static void showThemes(Context activity, String mid, String userNick) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsContactThemesActivity.class);
        intent.putExtra(MID_KEY, mid);
        intent.putExtra(NICK_KEY, userNick);

        activity.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    public String getUserId() {
        return m_Id;
    }

    public String getUserNick() {
        return m_Nick;
    }



}