package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import ru.slartus.http.AppResponse;

/**
 * Created by slartus on 23.02.14.
 */
public class PostTask extends AsyncTask<String, Void, Boolean> {
    public static class PostResult {
        public Boolean Success;
        public ExtTopic ExtTopic;
        public String ForumErrorMessage;
        public Throwable Exception;
        public String TopicBody;
        public AppResponse Response;// страница результата
    }

    private final MaterialDialog dialog;
    private final String mPost;


    private final String mForumId;
    private final String mTopicId;
    private final String mAuthKey;
    private final Boolean mEnableEmotics;
    private final Boolean mEnableSign;
    PostResult mPostResult;

    PostTask(Context context, String post, String forumId, String topicId, String authKey,
             Boolean enableEmotics, Boolean enableSign) {
        mPost = post;
        mForumId = forumId;
        mTopicId = topicId;
        mAuthKey = authKey;
        mEnableEmotics = enableEmotics;
        mEnableSign = enableSign;

        dialog = new MaterialDialog.Builder(context)
                .progress(true, 0)
                .cancelable(false)
                .content(R.string.sending_message)
                .build();
    }


    @Override
    protected Boolean doInBackground(String... params) {
        try {
            mPostResult = new PostResult();

            mPostResult.Response = Client.getInstance().reply(mForumId, mTopicId, mAuthKey,
                    mPost, mEnableSign, mEnableEmotics, true, null);
            mPostResult.ForumErrorMessage = PostApi.INSTANCE.checkPostErrors(mPostResult.Response.getResponseBody());

            if (!TextUtils.isEmpty(mPostResult.ForumErrorMessage))
                return false;


            String lastUrl = mPostResult.Response.redirectUrlElseRequestUrl();
            TopicBodyBuilder topicBodyBuilder = Client.getInstance().parseTopic(mPostResult.Response.getResponseBody(), App.getInstance(), lastUrl,
                    Preferences.Topic.getSpoilFirstPost());
            mPostResult.Response.setResponseBody("");

            mPostResult.ExtTopic = topicBodyBuilder.getTopic();
            mPostResult.TopicBody = topicBodyBuilder.getBody();

            return true;
        } catch (Throwable e) {

            mPostResult.Exception = e;
            return false;
        }
    }

    // can use UI thread here
    protected void onPreExecute() {
        this.dialog.show();
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        mPostResult.Success = success;
    }
}
