package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public abstract class BaseQuickView extends LinearLayout {
    public abstract void onDestroy();
    public abstract void onResume();
    public abstract void onPause();
    private EditText mEditor;

    public BaseQuickView(Context context) {
        super(context);
        addView(createView());
    }

    public void setEditor(EditText editor){
        mEditor = editor;
    }

    public EditText getEditor(){
        return mEditor;
    }

    abstract View createView();

    private CharSequence mForumId;
    private CharSequence mTopicId;
    private CharSequence mAuthKey;
    protected CharSequence getAuthKey() {
        return mAuthKey;
    }

    protected CharSequence getTopicId() {
        return mTopicId;
    }

    protected CharSequence getForumId() {
        return mForumId;
    }

    public void setTopic(String forumId, String topicId, String authKey) {
        mForumId=forumId;
        mTopicId=topicId;
        mAuthKey=authKey;
    }
}
