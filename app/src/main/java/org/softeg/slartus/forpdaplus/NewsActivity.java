package org.softeg.slartus.forpdaplus;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.NewsFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.video.PlayerActivity;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
