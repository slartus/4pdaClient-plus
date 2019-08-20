package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.utils.LogUtil;

public class QuickPostFragment extends Fragment {

    private boolean emptyText = true;

    private String mForumId;
    private String mTopicId;
    private String mAuthKey;
    private PostSendListener mPostSendListener;


    private EditText mPostEditText;
    private String parentTag = "";
    public void setParentTag(String tag){
        parentTag = tag;
    }

    private PopupPanelView mPopupPanelView;

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
    public void showKeyboard() {
        if (getActivity() == null) return;
        mPostEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPostEditText, InputMethodManager.SHOW_IMPLICIT);
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
    public View onCreateView(@NotNull android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.quick_post_fragment, null);
        assert v != null;


        final ImageButton send_button = v.findViewById(R.id.send_button);

        send_button.setOnClickListener(view -> startPost());
        send_button.setOnLongClickListener(view -> {
            hideKeyboard();
            EditPostFragment.Companion.newPost(getActivity(), mForumId, mTopicId, mAuthKey,
                    getPostBody(), parentTag);
            LogUtil.D("QUICK BOOM", "key " + mAuthKey);
            return true;
        });

        mPostEditText = v.findViewById(R.id.post_text);
        mPostEditText.setOnEditorActionListener((v1, actionId, event) -> false);
        mPostEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    if(!emptyText){
                        send_button.clearColorFilter();
                        emptyText = true;
                    }
                }else {
                    if(emptyText){
                        send_button.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.selectedItemText), PorterDuff.Mode.SRC_ATOP);
                        emptyText = false;
                    }
                }
            }
        });

        ImageButton advanced_button = v.findViewById(R.id.advanced_button);
        mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_ALL);
        mPopupPanelView.createView(inflater, advanced_button, mPostEditText);
        mPopupPanelView.activityCreated(getActivity(), v);


        return v;
    }

    private void startPost() {
        if (emptyText) {
            Toast toast = Toast.makeText(getContext(), R.string.enter_message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, App.getInstance().getResources().getDisplayMetrics()));
            toast.show();
            return;
        }
        if (Preferences.Topic.getConfirmSend()) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.send_post_confirm_dialog, null);
            assert view != null;
            final CheckBox checkBox = view.findViewById(R.id.chkConfirmationSend);
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.confirm_action)
                    .customView(view,true)
                    .positiveText(R.string.send)
                    .onPositive((dialog, which) -> {
                        if (!checkBox.isChecked())
                            Preferences.Topic.setConfirmSend(false);
                        post();
                    })
                    .negativeText(R.string.cancel)
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

        InnerPostTask(Context context, String post, String forumId, String topicId, String authKey, Boolean enableEmotics, Boolean enableSign) {
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
