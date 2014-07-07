package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.PostApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

/**
 * Created by slartus on 23.02.14.
 */
public class PostTask extends AsyncTask<String, Void, Boolean> {
    public class PostResult {
        public Boolean Success;
        public ExtTopic ExtTopic;
        public String ForumErrorMessage;
        public Throwable Exception;
        public String TopicBody;
        public String PostResultBody;// страница результата
    }

    private final ProgressDialog dialog;
    private String mPost;


    private String mForumId;
    private String mTopicId;
    private String mAuthKey;
    private Boolean mEnableEmotics;
    private Boolean mEnableSign;
    protected PostResult mPostResult;

    public PostTask(Context context, String post, String forumId, String topicId, String authKey,
                    Boolean enableEmotics, Boolean enableSign) {
        mPost = post;
        mForumId = forumId;
        mTopicId = topicId;
        mAuthKey = authKey;
        mEnableEmotics = enableEmotics;
        mEnableSign = enableSign;

        dialog = new AppProgressDialog(context);
    }


    @Override
    protected Boolean doInBackground(String... params) {
        try {
            mPostResult = new PostResult();
            mPostResult.PostResultBody = Client.getInstance().reply(mForumId, mTopicId, mAuthKey,
                    mPost, mEnableSign, mEnableEmotics, true, null);
            mPostResult.ForumErrorMessage = PostApi.checkPostErrors(mPostResult.PostResultBody);

            if (!TextUtils.isEmpty(mPostResult.ForumErrorMessage))
                return false;


            String lastUrl = Client.getInstance().getRedirectUri().toString();
            TopicBodyBuilder topicBodyBuilder = Client.getInstance().parseTopic(mPostResult.PostResultBody, MyApp.getInstance(), lastUrl,
                    Preferences.Topic.getSpoilFirstPost());
            mPostResult.PostResultBody = null;

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
        this.dialog.setMessage("Отправка сообщения...");
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.show();
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        mPostResult.Success = success;
    }
}
