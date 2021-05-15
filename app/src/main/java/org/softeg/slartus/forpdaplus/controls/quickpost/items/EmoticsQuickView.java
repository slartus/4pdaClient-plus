package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.Hashtable;
import java.util.Set;

public class EmoticsQuickView extends BaseQuickView {
    private WebView webView;
    @Override
    public void onDestroy(){
        if (webView != null) {
            webView.setWebViewClient(null);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }

    @Override
    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }

    public EmoticsQuickView(Context context) {
        super(context);
    }

    @Override
    View createView() {
        if (getContext() == null)
            return new View(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        webView = new WebView(inflater.getContext());
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setBackgroundColor(AppTheme.getThemeStyleWebViewBackground());
        loadWebView();
        return webView;
    }

    private void loadWebView() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body bgcolor=\"").append(AppTheme.getCurrentBackgroundColorHtml()).append("\">");

        Hashtable<String, String> smiles = Smiles.getSmilesDict();
        Set<String> favorites = Preferences.Topic.Post.getEmoticFavorites();
        int favoritesCount = 0;
        for (String key : favorites) {
            if (TextUtils.isEmpty(key)) continue;
            favoritesCount++;

            sb.append("<a style=\"text-decoration: none;\" href=\"")
                    .append(key)
                    .append("\"><img style=\"padding:5px;\" src=\"file:///android_asset/forum/style_emoticons/default/")
                    .append(smiles.get(key.trim()))
                    .append("\" /></a> ");
        }
        if (favoritesCount > 0)
            sb.append("<hr/>");
        for (String key : smiles.keySet()) {
            sb.append("<a style=\"text-decoration: none;\" href=\"")
                    .append(key)
                    .append("\"><img style=\"padding:5px;\" src=\"file:///android_asset/forum/style_emoticons/default/")
                    .append(smiles.get(key.trim()))
                    .append("\" /></a> ");
        }

        sb.append("</body></html>");
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", sb.toString(), "text/html", "UTF-8", null);
    }

    public class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, java.lang.String url) {
            Uri uri = Uri.parse(url);

            if (uri.getPathSegments() == null || uri.getPathSegments().size() != 2)
                return true;

            String emoticText = uri.getPathSegments().get(1);

            if (getEditor().getText() != null) {
                String text = getEditor().getText().toString();
                if (getEditor().getSelectionEnd() > 0
                        && !" ".equals(text.substring(getEditor().getSelectionEnd() - 1, getEditor().getSelectionEnd())) )
                    emoticText = " " + emoticText;

                getEditor().getText().insert(getEditor().getSelectionEnd(), emoticText);
            } else
                getEditor().setText(emoticText);
            Preferences.Topic.Post.addEmoticToFavorites(emoticText.trim());
            return true;
        }
    }
}

