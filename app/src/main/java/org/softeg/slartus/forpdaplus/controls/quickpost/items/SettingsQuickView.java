package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment;
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;
import org.softeg.slartus.forpdaplus.utils.LogUtil;


public class SettingsQuickView extends BaseQuickView {

    @Override
    public void onDestroy() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    public SettingsQuickView(Context context) {
        super(context);
    }

    @Override
    View createView() {

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setBackgroundResource(AppTheme.getThemeBackgroundColorRes());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        enableEmotics = new AppCompatCheckBox(getContext());
        enableEmotics.setText(R.string.enable_smiles);
        enableEmotics.setChecked(Preferences.Topic.Post.getEnableEmotics());
        enableEmotics.setOnCheckedChangeListener((compoundButton, b) -> Preferences.Topic.Post.setEnableEmotics(b));
        enableEmotics.setLayoutParams(params);
        linearLayout.addView(enableEmotics);

        enableSign = new AppCompatCheckBox(getContext());
        enableSign.setText(R.string.add_sign);
        enableSign.setChecked(Preferences.Topic.Post.getEnableSign());
        enableSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Preferences.Topic.Post.setEnableSign(b);
            }
        });
        enableSign.setLayoutParams(params);
        linearLayout.addView(enableSign);

        extendedFormButton = new AppCompatButton(getContext());
        extendedFormButton.setText(R.string.extended_form);
        extendedFormButton.setLayoutParams(params);
        //extendedFormButton.setTextColor(getResources().getColor(R.color.black));
        extendedFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getTopicId() == null || getAuthKey() == null || getPostBody() == null) {
                    return;
                }

                ((ThemeFragment) TabsManager.getInstance().getTabByTag(TabsManager.getInstance().getCurrentFragmentTag()).getFragment()).hideMessagePanel();
                EditPostFragment.Companion.newPost((MainActivity) getContext(), getForumId() == null ? null : getForumId().toString(),
                        getTopicId().toString(), getAuthKey().toString(),
                        getPostBody().toString(), TabsManager.getInstance().getCurrentFragmentTag());

            }
        });
        linearLayout.addView(extendedFormButton);


        /*attachesButton = new Button(getContext());
        attachesButton.setText("Прикреплённые файлы");
        attachesButton.setLayoutParams(params);
        attachesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditPostActivity.newPost((MainActivity) getContext(), getForumId().toString(), getTopicId().toString(), getAuthKey().toString(), getPostBody().toString());
                EditPostFragment.newPost((MainActivity) getContext(), getForumId().toString(), getTopicId().toString(), getAuthKey().toString(), getPostBody().toString(), App.getInstance().getCurrentFragmentTag());
            }
        });
        linearLayout.addView(attachesButton);*/

        return linearLayout;
    }


    CheckBox enableEmotics;
    CheckBox enableSign;
    Button extendedFormButton;
    Button attachesButton;


    private CharSequence getPostBody() {
        return getEditor().getText() == null ? "" : getEditor().getText().toString();
    }


}
