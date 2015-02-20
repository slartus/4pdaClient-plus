package org.softeg.slartus.forpdaplus.topicbrowser;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaapi.TopicBodyParser;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.topicbrowser.Notifiers.NotifyActivityRegistrator;
import org.softeg.slartus.forpdaplus.topicbrowser.Notifiers.TopicNotifier;

/*
 * Created by slartus on 01.06.2014.
 */
public class TopicFragment extends TopicBaseFragment {
    private static final String TOPIC_BODY_PARSER_KEY = "TOPIC_BODY_PARSER_KEY";
    public static final String TOPIC_URL_KEY = "TOPIC_URL_KEY";


    private NotifyActivityRegistrator m_NotifyActivityRegistrator;
    private int m_ScrollToY = Integer.MIN_VALUE;
    private TopicBodyParser mTopicBodyParser;

    public TopicBodyParser getTopic() {
        return mTopicBodyParser;
    }

    public static TopicFragment newInstance(Bundle args) {
        TopicFragment topicFragment = new TopicFragment();
        topicFragment.setArguments(args);
        return topicFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        m_NotifyActivityRegistrator = new NotifyActivityRegistrator();
    }

    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        TopicNotifier topicNotifier = new TopicNotifier(this);
        topicNotifier.register(m_NotifyActivityRegistrator);

        mWebView.addJavascriptInterface(topicNotifier, "HTMLOUT");
        mWebView.setWebViewClient(new TopicWebViewClient(this, topicNotifier));
        mWebView.setWebChromeClient(new TopicWebChromeClient());
        return v;
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mTopicBodyParser != null) {
            setTopicBodyParser(mTopicBodyParser);
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TOPIC_BODY_PARSER_KEY))
                setTopicBodyParser((TopicBodyParser) savedInstanceState.getParcelable(TOPIC_BODY_PARSER_KEY));
            m_ScrollToY = savedInstanceState.getInt(TopicWebView.SCROLL_Y_KEY, 0);
        } else if (getArguments() != null) {
            if (getArguments().containsKey(TOPIC_URL_KEY)) {
                if (m_Task == null)
                    loadTopic(getArguments().getString(TOPIC_URL_KEY));
            }
        }
    }

    private TopicLoadTask m_Task;

    public void goToAnchorOrLoadTopic(final String topicUrl) {
        try {
            if (mTopicBodyParser == null) {
                loadTopic(topicUrl);
                return;
            }
            Uri uri = Uri.parse(topicUrl.toLowerCase());
            String postId = null;
            if (mTopicBodyParser.getTopicId().equals(uri.getQueryParameter("showtopic")))
                postId = uri.getQueryParameter("p");
            if (TextUtils.isEmpty(postId) && "findpost".equals(uri.getQueryParameter("act")))
                postId = uri.getQueryParameter("pid");
            if (TextUtils.isEmpty(postId)) {
                loadTopic(topicUrl);
                return;
            }
            String fragment = "entry" + postId;
            if (mTopicBodyParser.getPostsBody().contains("<a name=\"" + fragment + "\">")) {
                mWebView.scrollTo(fragment);
                return;
            }
            loadTopic(topicUrl);
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }

    }

    public void loadTopic(final String topicUrl) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                m_Task = new TopicLoadTask(IntentActivity.normalizeThemeUrl(topicUrl));
                m_Task.execute();
            }
        };
        if (m_Task != null && m_Task.getStatus() != AsyncTask.Status.FINISHED)
            m_Task.cancel(runnable);
        else {
            runnable.run();
        }
    }

    @Override
    protected void reloadTopic() {
        if (mTopicBodyParser != null)
        if (mTopicBodyParser != null)
            loadTopic(mTopicBodyParser.getUrl());
    }

    public void onPreExecute() {
        setLoading(true);
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTopicBodyParser != null)
            outState.putParcelable(TOPIC_BODY_PARSER_KEY, mTopicBodyParser);
        m_ScrollToY = mWebView.getScrollY();
        outState.putInt(TopicWebView.SCROLL_Y_KEY, m_ScrollToY);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        m_ScrollToY = mWebView.getScrollY();
    }

    private int m_LoadersCount;

    public void setLoading(Boolean loading) {
        try {
            if (getActivity() == null) return;
            m_LoadersCount += loading ? 1 : -1;
            if (!loading && m_LoadersCount > 0) return;
            mPullToRefreshLayout.setRefreshing(loading);

        } catch (Throwable ignore) {
            android.util.Log.e("TAG", ignore.toString());
        }
    }

    public void onPageFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (m_ScrollToY != Integer.MIN_VALUE) {
                    mWebView.scrollBy(0, m_ScrollToY);
                } else {
                    if (mTopicBodyParser != null && !TextUtils.isEmpty(mTopicBodyParser.getFragment())) {
                        String fragment = mTopicBodyParser.getFragment();
                        if (fragment.toLowerCase().startsWith("entry")) {
                            mWebView.offActionBarOnScrollEvents();
                            mWebView.scrollTo(0, 100);
                            mWebView.scrollTo(0, 0);
                            mWebView.onActionBarOnScrollEvents();
                            mWebView.scrollTo(fragment);
                        }
                    }
                }
                setLoading(false);
            }
        }, 500);
    }


    public void setTopicBodyParser(TopicBodyParser topicBodyParser) {
        mTopicBodyParser = topicBodyParser;
        TopicHtmlBuilder htmlBuilder = new TopicHtmlBuilder(mTopicBodyParser);
        htmlBuilder.beginTopic();
        htmlBuilder.addBody(mTopicBodyParser.getPostsBody());
        htmlBuilder.endTopic();

        setData(htmlBuilder.getHtml().toString());

        if (getActivity() == null) return;
        getActivity().setTitle(mTopicBodyParser.getTopicTitle());
        getActivity().getActionBar().setSubtitle(mTopicBodyParser.getCurrentPage() + "/" + mTopicBodyParser.getPagesCount());
    }

    private void setData(String data) {
        TopicHtmlBuilder htmlBuilder = new TopicHtmlBuilder(mTopicBodyParser);
        htmlBuilder.beginHtml(getActivity().getTitle().toString());
        htmlBuilder.beginBody();
        htmlBuilder.append(data);
        htmlBuilder.endBody();
        htmlBuilder.endHtml();
        mWebView.loadDataWithBaseURL("http://4pda.ru/forum/", htmlBuilder.getHtml().toString(), "text/html", "UTF-8", null);
    }


    private class TopicLoadTask extends AsyncTask<String, String, Boolean> {
        private String url;
        private TopicBodyParser topicBodyParser;
        private Throwable ex;
        private Runnable cancelAction;

        public TopicLoadTask(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            setLoading(true);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                topicBodyParser = TopicApi.parseTopic(Client.getInstance(), url);
                return true;
            } catch (Throwable e) {
                ex = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            setLoading(false);
            if (isCancelled()) {
                cancelAction.run();

            } else {
                m_ScrollToY = Integer.MIN_VALUE;
                if (ex != null)
                    AppLog.e(getActivity(), ex);
                else
                    setTopicBodyParser(topicBodyParser);
            }
        }

        public void cancel(Runnable runnable) {

            this.cancelAction = runnable;
            cancel(false);
        }

        @Override
        protected void onCancelled(Boolean result) {
            if (cancelAction != null)
                cancelAction.run();
        }

        @Override
        protected void onCancelled() {
            if (cancelAction != null)
                cancelAction.run();
        }
    }
}
