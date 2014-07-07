package org.softeg.slartus.forpdaplus.topicbrowser;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.topicbrowser.Notifiers.TopicNotifier;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slartus on 05.06.2014.
 */
public class TopicWebViewClient extends WebViewClient {

    private TopicNotifier topicNotifier;

    private TopicFragment topicFragment;

    public interface OnLoadChangeListener {
        void onPageStarted();

        void onPageFinished();
    }

    public interface OnMethodInvokeListener {
        void invokeMethod(String function, Class[] parameterTypes,String[] parameterValues);
    }

    public TopicWebViewClient(TopicFragment topicFragment,TopicNotifier topicNotifier) {
        this.topicFragment = topicFragment;

        this.topicNotifier = topicNotifier;
    }

    @Override
    public void onPageFinished(android.webkit.WebView view, java.lang.String url) {
        super.onPageFinished(view, url);
        topicFragment.onPageFinished();

    }

    @Override
    public void onLoadResource(android.webkit.WebView view, java.lang.String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        topicFragment.setLoading(true);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final String url) {
        if (url.contains("HTMLOUT.ru")) {
            Uri uri = Uri.parse(url);
            try {
                String function = uri.getPathSegments().get(0);
                String query = uri.getQuery();
                Class[] parameterTypes = null;
                String[] parameterValues = new String[0];
                if (!TextUtils.isEmpty(query)) {
                    Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(url);
                    ArrayList<String> objs = new ArrayList<>();

                    while (m.find()) {
                        objs.add(Uri.decode(m.group(2)));
                    }
                    parameterValues = new String[objs.size()];
                    parameterTypes = new Class[objs.size()];
                    for (int i = 0; i < objs.size(); i++) {
                        parameterTypes[i] = String.class;
                        parameterValues[i] = objs.get(i);
                    }
                }


                Method method = TopicNotifier.class.getMethod(function, parameterTypes);

                method.invoke(topicNotifier, parameterValues);
            } catch (Exception e) {
                //Log.eToast(ThemeActivity.this, e);
            }
            return true;
        }


        if (TopicApi.isTopicUrl(url)) {
            topicFragment.goToAnchorOrLoadTopic(url);
            return true;
        }
        else{

        }

//        if (tryDeletePost(url))
//            return true;
//
//        if (tryQuote(url))
//            return true;
        IntentActivity.tryShowUrl(topicFragment.getActivity(), new Handler(), url, true, false,
                Client.getInstance().getAuthKey());

        return true;
    }

}

