package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.topic.EditPostFragment;
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

public class QuickPostFragment extends Fragment {

    private String mForumId;
    private String mTopicId;
    private String mAuthKey;
    private PostSendListener mPostSendListener;


    private EditText mPostEditText;
    private String parentTag = "";
    public void setParentTag(String tag){
        parentTag = tag;
    }

    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_ALL);

    public void hidePopupWindow() {
        if(mPopupPanelView!=null) mPopupPanelView.hidePopupWindow();
    }

    public interface PostSendListener {
        void onPostExecute(PostTask.PostResult postResult);
    }

    public void setOnPostSendListener(PostSendListener postSendListener) {
        mPostSendListener = postSendListener;
    }

    public void setTopic(String forumId, String topicId, String authKey) {
        mForumId = forumId;
        mTopicId = topicId;
        mAuthKey = authKey;
        if (mPopupPanelView != null)
            mPopupPanelView.setTopic(mForumId, mTopicId, mAuthKey);
    }

    public void clearPostBody() {
        if (mPostEditText.getText() != null)
            mPostEditText.getText().clear();
    }

    public String getPostBody() {
        if (mPostEditText.getText() != null)
            return mPostEditText.getText().toString();
        else
            return "";
    }

    public void insertTextToPost(final String text) {
        int selection = mPostEditText.getSelectionStart();
        if (mPostEditText.getText() != null)
            mPostEditText.getText().insert(selection == -1 ? 0 : selection, text);
    }


    public void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPostEditText.getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState != null) {
            mPostEditText.setText(savedInstanceState.getString("QuickPostFragment.Post"));
            mForumId = savedInstanceState.getString("QuickPostFragment.ForumId");
            mTopicId = savedInstanceState.getString("QuickPostFragment.TopicId");
            mAuthKey = savedInstanceState.getString("QuickPostFragment.AuthKey");
        }

        mPopupPanelView.activityCreated(getActivity(), ((ThemeFragment)App.getInstance().getTabByTag(parentTag).getFragment()).getView());
        mPopupPanelView.setTopic(mForumId, mTopicId, mAuthKey);

    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (mPostEditText.getText() != null)
            outState.putString("QuickPostFragment.Post", mPostEditText.getText().toString());
        outState.putString("QuickPostFragment.ForumId", mForumId);
        outState.putString("QuickPostFragment.TopicId", mTopicId);
        outState.putString("QuickPostFragment.AuthKey", mAuthKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.quick_post_fragment, null);
        assert v != null;


        final ImageButton send_button = (ImageButton) v.findViewById(R.id.send_button);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPost();
            }
        });
        send_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                hideKeyboard();
                EditPostFragment.newPost(getActivity(), mForumId, mTopicId, mAuthKey,
                        getPostBody(), parentTag);
                return true;
            }
        });

        mPostEditText = (EditText) v.findViewById(R.id.post_text);
        mPostEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        ImageButton advanced_button = (ImageButton) v.findViewById(R.id.advanced_button);
        mPopupPanelView.createView(inflater, advanced_button, mPostEditText);


        return v;
    }


    private Boolean checkPostBody() {
        if (TextUtils.isEmpty(getPostBody())) {
            new MaterialDialog.Builder(getActivity())
                    .content("Введите сообщение!")
                    .positiveText("ОК").show();
            return false;
        }
        return true;
    }

    private void startPost() {
        if (!checkPostBody())
            return;
        if (Preferences.Topic.getConfirmSend()) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.send_post_confirm_dialog, null);
            assert view != null;
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.chkConfirmationSend);
            new MaterialDialog.Builder(getActivity())
                    .title("Подтвердите действие")
                    .customView(view,true)
                    .positiveText("Отправить")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            if (!checkBox.isChecked())
                                Preferences.Topic.setConfirmSend(false);
                            post();
                        }
                    })
                    .negativeText("Отмена")
                    .show();
        } else {
            post();
        }
    }

    public void post() {

        try {
            hideKeyboard();

            PostTask postTask = new InnerPostTask(getActivity(),
                    mPostEditText.getText() == null ? "" : mPostEditText.getText().toString(),
                    mForumId, mTopicId, mAuthKey,
                    Preferences.Topic.Post.getEnableEmotics(), Preferences.Topic.Post.getEnableSign());

            postTask.execute();
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    @Override
    public void onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView.destroy();
            mPopupPanelView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPopupPanelView!=null)
            mPopupPanelView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPopupPanelView!=null)
            mPopupPanelView.resume();
    }

    private class InnerPostTask extends PostTask {

        public InnerPostTask(Context context, String post, String forumId, String topicId, String authKey, Boolean enableEmotics, Boolean enableSign) {
            super(context, post, forumId, topicId, authKey, enableEmotics, enableSign);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            mPostResult.Success = success;
            if (success)
                if (mPostEditText.getText() != null)
                    mPostEditText.getText().clear();
            if (mPostSendListener != null)
                mPostSendListener.onPostExecute(mPostResult);
        }
    }


}
