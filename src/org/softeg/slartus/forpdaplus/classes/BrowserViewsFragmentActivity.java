package org.softeg.slartus.forpdaplus.classes;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 16.10.12
 * Time: 8:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class BrowserViewsFragmentActivity extends BaseFragmentActivity implements IWebViewContainer {
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

    public void setHideActionBar() {
        if (getWebView() == null || !(getWebView() instanceof AdvWebView))
            return;
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;
        setHideActionBar((AdvWebView)getWebView(),actionBar);
    }

    public static void setHideActionBar(AdvWebView advWebView, final ActionBar actionBar) {
        Boolean hide = Preferences.isHideActionBar();

        if (hide) {

            advWebView.setOnScrollChangedCallback(new AdvWebView.OnScrollChangedCallback() {
                @Override
                public void onScrollDown(Boolean inTouch) {
                    if (!inTouch)
                        return;
                    if (actionBar.isShowing()) {

                        actionBar.hide();
                    }
                }

                @Override
                public void onScrollUp(Boolean inTouch) {
                    if (!inTouch)
                        return;
                    if (!actionBar.isShowing()) {

                        actionBar.show();
                    }
                }

                @Override
                public void onTouch() {
                    actionBar.show();
                }
            });
        } else {
            advWebView.setOnScrollChangedCallback(null);
            actionBar.show();
        }
    }


    protected void setWebViewSettings(Boolean loadImagesAutomaticallyAlways) {
        getWebViewExternals().setWebViewSettings(loadImagesAutomaticallyAlways);

    }

    protected void setWebViewSettings() {
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
    public boolean dispatchKeyEvent(@NotNull KeyEvent event) {
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
        new AlertDialogBuilder(this)
                .setTitle("Размер шрифта")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Preferences.setFontSize(Prefix(), seekBar.getProgress() + 1);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getWebView().getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
                    }
                })
                .create().show();

    }
}
