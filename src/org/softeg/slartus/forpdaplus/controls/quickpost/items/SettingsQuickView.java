package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.post.EditPostActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;


public class SettingsQuickView extends BaseQuickView {

    public SettingsQuickView(Context context) {
        super(context);
    }

    @Override
    View createView() {

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setBackgroundResource(MyApp.getInstance().getThemeBackgroundColorRes());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        enableEmotics = new CheckBox(getContext());
        enableEmotics.setText("Включить смайлики");
        enableEmotics.setChecked(Preferences.Topic.Post.getEnableEmotics());
        enableEmotics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Preferences.Topic.Post.setEnableEmotics(b);
            }
        });
        enableEmotics.setLayoutParams(params);
        linearLayout.addView(enableEmotics);

        enableSign = new CheckBox(getContext());
        enableSign.setText("Добавить подпись");
        enableSign.setChecked(Preferences.Topic.Post.getEnableSign());
        enableSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Preferences.Topic.Post.setEnableSign(b);
            }
        });
        enableSign.setLayoutParams(params);
        linearLayout.addView(enableSign);

        extendedFormButton = new Button(getContext());
        extendedFormButton.setText("Расширенная форма");
        extendedFormButton.setLayoutParams(params);
        extendedFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditPostActivity.newPost((Activity)getContext(), getForumId().toString(), getTopicId().toString(), getAuthKey().toString(),
                        getPostBody().toString());
            }
        });
        linearLayout.addView(extendedFormButton);

        return linearLayout;
    }



    CheckBox enableEmotics;
    CheckBox enableSign;
    Button extendedFormButton;



    private CharSequence getPostBody() {
        return getEditor().getText()==null?"":getEditor().getText().toString();
    }




}
