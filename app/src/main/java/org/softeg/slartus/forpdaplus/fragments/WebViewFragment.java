package org.softeg.slartus.forpdaplus.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.IWebViewContainer;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;

import java.util.ArrayList;

/**
 * Created by radiationx on 17.10.15.
 */
public abstract class WebViewFragment extends GeneralFragment implements IWebViewContainer {


    public abstract AdvWebView getWebView();

    public abstract WebViewClient getWebViewClient();

    public abstract String getTitle();

    public abstract String getUrl();

    public abstract void reload();

    public abstract AsyncTask getAsyncTask();

    private final Handler mHandler = new Handler();

    public void showLinkMenu(String url) {
        if (TextUtils.isEmpty(url) || url.contains("HTMLOUT.ru")
                || url.equals("#")
                || url.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getContext(), url);
    }

    @Override
    public void onCreateContextMenu(@NotNull ContextMenu menu, @NotNull View view, ContextMenu.ContextMenuInfo menuInfo) {
        try {
            switch (getWebView().getHitTestResult().getType()) {
                case WebView.HitTestResult.UNKNOWN_TYPE:
                case WebView.HitTestResult.EDIT_TEXT_TYPE:
                    break;
                case WebView.HitTestResult.IMAGE_TYPE:
                    ExtUrl.showImageSelectActionDialog(mHandler, getContext(), getWebView().getHitTestResult().getExtra());
                    break;
                default: {
                    Handler handler = new Handler();
                    Message message = handler.obtainMessage();
                    getWebView().requestFocusNodeHref(message);
                    String url = message.getData().get("url").toString();
                    showLinkMenu(url);
                }
            }
        } catch (Throwable ex) {
            AppLog.e(ex);
        }
    }

    WebViewExternals m_WebViewExternals;

    public WebViewExternals getWebViewExternals() {
        if (m_WebViewExternals == null)
            m_WebViewExternals = new WebViewExternals(this);
        return m_WebViewExternals;
    }

    public void showBody() throws Exception {
        getThisTab().setTitle(getTitle()).setUrl(getUrl());
        getMainActivity().notifyTabAdapter();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (Preferences.System.isDevSavePage()) {
            menu.add(R.string.save_page).setOnMenuItemClickListener(menuItem -> {
                try {
                    saveHtml();
                } catch (Exception ex) {
                    return false;
                }
                return true;
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
        loadPreferences(App.getInstance().getPreferences());
        if (Preferences.System.isDevSavePage() |
                Preferences.System.isDevStyle())
            Toast.makeText(getMainActivity(), R.string.dev_mode, Toast.LENGTH_SHORT).show();
    }

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected void initSwipeRefreshLayout() {
        mSwipeRefreshLayout = getView().findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout = App.createSwipeRefreshLayout(getView(), this::reload);
    }

    public void setLoading(final Boolean loading) {
        try {
            if (getMainActivity() == null) return;
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        } catch (Throwable ex) {
            android.util.Log.e("TAG", ex.toString());
        }
    }

    public void setFabColors(final FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(AppTheme.getColorAccent("Accent")));
        fab.setRippleColor(AppTheme.getColorAccent("Pressed"));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getWebView() != null) {
            getWebView().onResume();
            getWebView().setWebViewClient(getWebViewClient());
        }
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getWebView() != null) {
            new Handler().postDelayed(() -> {
                if (getWebView() != null && isFragmentPaused())
                    getWebView().onPause();
            }, 1500);
            getWebView().setWebViewClient(null);
            getWebView().setPictureListener(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getWebView() != null) {
            getWebView().setWebViewClient(null);
            getWebView().setPictureListener(null);
        }
    }

    @Override
    public void onDestroy() {
        if (getWebView() != null) {
            getWebView().setWebViewClient(null);
            getWebView().removeAllViews();
            getWebView().loadUrl("about:blank");
        }
        if (getAsyncTask() != null) getAsyncTask().cancel(false);
        super.onDestroy();
    }

    public void setHideArrows(boolean hide) {
        if (getWebView() == null)
            return;

        LinearLayout arrows = getView().findViewById(R.id.arrows);
        LinearLayout arrowsShadow = getView().findViewById(R.id.arrows_shadow);

        if (arrows == null) return;
        if (hide) {
            arrows.setVisibility(View.GONE);
            arrowsShadow.setVisibility(View.GONE);
        } else {
            arrows.setVisibility(View.VISIBLE);
            arrowsShadow.setVisibility(View.VISIBLE);
        }

    }

    /*private boolean fabIsVisible = false;
    private FloatingActionButton.OnVisibilityChangedListener fabListener = new FloatingActionButton.OnVisibilityChangedListener(){
        @Override
        public void onShown(FloatingActionButton fab) {
            super.onShown(fab);
            fabIsVisible = true;
        }

        @Override
        public void onHidden(FloatingActionButton fab) {
            super.onHidden(fab);
            fabIsVisible = false;
        }
    };*/
    public void setHideFab(final FloatingActionButton fab) {
        if (getWebView() == null) return;
        if (fab == null) return;
        if (Preferences.isHideFab()) {
            getWebView().setOnScrollChangedCallback(new AdvWebView.OnScrollChangedCallback() {
                @Override
                public void onScrollDown(Boolean inTouch) {
                    if (!inTouch) return;
                    fab.hide();
                }

                @Override
                public void onScrollUp(Boolean inTouch) {
                    if (!inTouch) return;
                    fab.show();
                }

                @Override
                public void onTouch() {
                    fab.show();
                }
            });
        } else {
            getWebView().setOnScrollChangedCallback(null);
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

    protected void loadPreferences(SharedPreferences prefs) {
        getWebViewExternals().loadPreferences(prefs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getWebViewExternals().dispatchKeyEvent(event);
    }

    public void showFontSizeDialog() {
        View v = getMainActivity().getLayoutInflater().inflate(R.layout.font_size_dialog, null);

        assert v != null;
        final SeekBar seekBar = v.findViewById(R.id.value_seekbar);
        seekBar.setProgress(Preferences.getFontSize(Prefix()) - 1);
        final TextView textView = v.findViewById(R.id.value_textview);
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
        MaterialDialog dialog = new MaterialDialog.Builder(getMainActivity())
                .title(R.string.font_size)
                .customView(v, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive((dialog1, which) -> Preferences.setFontSize(Prefix(), seekBar.getProgress() + 1))
                .onNegative((dialog12, which) -> getWebView().getSettings().setDefaultFontSize(Preferences.Topic.getFontSize()))
                .show();
        dialog.setActionButton(DialogAction.NEUTRAL, R.string.reset);
        View neutral = dialog.getActionButton(DialogAction.NEUTRAL);
        neutral.setOnClickListener(v1 -> {
            seekBar.setProgress(15);
            Preferences.setFontSize(Prefix(), 16);
            getWebView().getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        });
        dialog.show();
    }

    public void showStylesDialog(final SharedPreferences prefs) {
        try {
            final String currentValue = AppTheme.getCurrentTheme();

            ArrayList<CharSequence> newStyleNames = new ArrayList<>();
            final ArrayList<CharSequence> newStyleValues = new ArrayList<>();

            PreferencesActivity.getStylesList(getMainActivity(), newStyleNames, newStyleValues);
            final int[] selected = {newStyleValues.indexOf(currentValue)};
            CharSequence[] styleNames = newStyleNames.toArray(new CharSequence[newStyleNames.size()]);

            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.app_theme)
                    .cancelable(true)
                    .positiveText(R.string.accept)
                    .items(styleNames)
                    .itemsCallbackSingleChoice(selected[0], (dialog, view, which, text) -> {
                        if (which == -1) {
                            Toast.makeText(getMainActivity(), R.string.ChooseStyle, Toast.LENGTH_LONG).show();
                            return false;
                        }
                        selected[0] = which;
                        return true;
                    })
                    .alwaysCallSingleChoiceCallback()
                    .onPositive((dialog, which) -> {
                        int lastTheme = AppTheme.getThemeStyleResID();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("appstyle", newStyleValues.get(selected[0]).toString());
                        //editor.putBoolean("theme.BrowserStyle", checkBox.isChecked());
                        editor.apply();
                        if (AppTheme.getThemeStyleResID() != lastTheme)
                            getMainActivity().recreate();
                        else
                            setStyleSheet();
                    })
                    .negativeText(R.string.cancel)
                    .show();
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void setStyleSheet() {
        try {
            getWebView().loadUrl("javascript:changeStyle('file://" + AppTheme.getThemeCssFileName() + "');");
            Log.e("Test", "setStyleSheet: " + "file://" + AppTheme.getThemeCssFileName());
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void saveHtml() {
        try {
            getWebView().evalJs("window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(() -> new SaveHtml(getMainActivity(), html, Prefix() + ""));
    }

    @Override
    public String Prefix() {
        return null;
    }

    @Override
    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public Window getWindow() {
        return getMainActivity().getWindow();
    }

    @Override
    public void nextPage() {
    }

    @Override
    public void prevPage() {
    }

}
