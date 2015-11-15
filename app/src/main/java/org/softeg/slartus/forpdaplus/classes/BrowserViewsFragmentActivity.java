package org.softeg.slartus.forpdaplus.classes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdaplus.FragmentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 16.10.12
 * Time: 8:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class BrowserViewsFragmentActivity extends FragmentActivity implements IWebViewContainer {
    public abstract String Prefix();

    public abstract WebView getWebView();


    WebViewExternals m_WebViewExternals;

    public WebViewExternals getWebViewExternals() {
        if (m_WebViewExternals == null)
            m_WebViewExternals = new WebViewExternals(this);
        return m_WebViewExternals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy()
    {
        WebView mWebView=getWebView();
        // null out before the super call
        if (mWebView != null)
        {
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.removeAllViews();
            mWebView.loadUrl("about:blank");
            mWebView = null;
        }
        super.onDestroy();
    }

    public void setHideArrows(boolean hide) {
        if (getWebView() == null || !(getWebView() instanceof AdvWebView))
            return;

        LinearLayout arrows = (LinearLayout) findViewById(R.id.arrows);
        LinearLayout arrowsShadow = (LinearLayout) findViewById(R.id.arrows_shadow);

        if (arrows == null) return;
        if(hide){
            arrows.setVisibility(View.GONE);
            arrowsShadow.setVisibility(View.GONE);
        }else {
            arrows.setVisibility(View.VISIBLE);
            arrowsShadow.setVisibility(View.VISIBLE);
        }

    }

    public void setHideActionBar() {
        if (getWebView() == null || !(getWebView() instanceof AdvWebView))
            return;
        ActionBar actionBar = getSupportActionBar();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Log.e("sethide","yes");
        if (actionBar == null) return;
        Log.e("ab","yes");
        if (fab == null) return;
        Log.e("fb","yes");
        setHideActionBar((AdvWebView)getWebView(),actionBar, fab);
    }
    public void setHideActionBar(FloatingActionButton fab) {
        if (getWebView() == null || !(getWebView() instanceof AdvWebView))
            return;
        ActionBar actionBar = getSupportActionBar();
        Log.e("sethide","yes");
        if (actionBar == null) return;
        Log.e("ab","yes");
        if (fab == null) return;
        Log.e("fb","yes");
        setHideActionBar((AdvWebView)getWebView(),actionBar, fab);
    }

    public static void setHideActionBar(AdvWebView advWebView, final ActionBar actionBar, final FloatingActionButton fab) {
        final Boolean hideAb = Preferences.isHideActionBar();
        final Boolean hideFab = Preferences.isHideFab();

        if (hideAb|hideFab) {

            advWebView.setOnScrollChangedCallback(new AdvWebView.OnScrollChangedCallback() {
                @Override
                public void onScrollDown(Boolean inTouch) {
                    if (!inTouch)
                        return;
                    if (actionBar.isShowing()&hideAb) {
                        actionBar.hide();
                    }
                    if (fab.isVisible()&hideFab){
                        fab.hide();
                    }
                }

                @Override
                public void onScrollUp(Boolean inTouch) {
                    if (!inTouch)
                        return;
                    if (!actionBar.isShowing()&hideAb) {
                        actionBar.show();
                    }
                    if (!fab.isVisible()&hideFab){
                        fab.show();
                    }

                }

                @Override
                public void onTouch() {
                    actionBar.show();
                    fab.show();
                }
            });
        } else {
            advWebView.setOnScrollChangedCallback(null);
            actionBar.show();
            fab.show();
        }
    }


    protected void setWebViewSettings(Boolean loadImagesAutomaticallyAlways) {
        getWebViewExternals().setWebViewSettings(loadImagesAutomaticallyAlways);

    }

    public void setWebViewSettings() {
        setWebViewSettings(false);
    }

    public void onPrepareOptionsMenu() {
        getWebViewExternals().onPrepareOptionsMenu();
    }

    @Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        getWebViewExternals().loadPreferences(prefs);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getWebViewExternals().dispatchKeyEvent(event);
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public void showFontSizeDialog() {
        View v = getLayoutInflater().inflate(R.layout.font_size_dialog, null);

        assert v != null;
        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
        seekBar.setProgress(Preferences.getFontSize(Prefix()) - 1);
        final TextView textView = (TextView) v.findViewById(R.id.value_textview);
        textView.setText((seekBar.getProgress() + 1) + "");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getWebView().getSettings().setDefaultFontSize(i + 1);
                textView.setText((i + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new MaterialDialog.Builder(this)
                .title("Размер шрифта")
                .customView(v,true)
                .positiveText("OK")
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Preferences.setFontSize(Prefix(), seekBar.getProgress() + 1);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        getWebView().getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
                    }
                })
                .show();

    }
}
