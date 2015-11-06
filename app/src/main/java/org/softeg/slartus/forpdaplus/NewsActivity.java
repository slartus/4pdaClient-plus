package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.fragments.NewsFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 07.12.11
 * Time: 8:07
 */
public class NewsActivity extends BaseFragmentActivity{
    private static final String URL_KEY = "Url";

    private EditText txtSearch;
    private String m_NewsUrl;
    public static String s_NewsUrl = null;
    private Uri m_Data = null;
    private ArrayList<History> m_History = new ArrayList<>();
    private boolean pencil;
    private boolean loadImages;

    public static void shownews(Context context, String url) {
        Intent intent = new Intent(context, NewsActivity.class);
        intent.putExtra(NewsActivity.URL_KEY, url);

        context.startActivity(intent);
    }

    @Override
    protected boolean isTransluent() {
        return true;
    }

    protected void afterCreate() {
        //getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simple_activity);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        assert intent != null;
        Bundle extras = intent.getExtras();

        assert extras != null;
        m_NewsUrl = extras.getString(URL_KEY);
        s_NewsUrl = m_NewsUrl;


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_fragment, new NewsFragment().newInstance(getContext(), m_NewsUrl),"News_Activity")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            //onBackPressed();
            return true;
        }

        return true;
    }
    @Override
    public void onBackPressed() {
        if(!((IBrickFragment)getSupportFragmentManager().findFragmentByTag("News_Activity")).onBackPressed()){
            super.onBackPressed();
        }
    }
}
