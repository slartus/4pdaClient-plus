package org.softeg.slartus.forpdaplus.controls.quickpost.items;/*
 * Created by slinkin on 18.04.2014.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaplus.R;

public class BBCodesAndSmilesQuickView extends  BaseQuickView{

    @Override
    public void onDestroy() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    public BBCodesAndSmilesQuickView(Context context) {
        super(context);
    }

    private BaseQuickView mBbCodesQuickView;
    private BaseQuickView mEmoticsQuickView;
    @Override
    View createView() {
        View v= LayoutInflater.from(getContext()).inflate(R.layout.codes_smiles_view_layout,null);
        assert v != null;
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mBbCodesQuickView=new BbCodesQuickView(getContext());
        mBbCodesQuickView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((LinearLayout)v.findViewById(R.id.first_frame_layout)).addView(mBbCodesQuickView);

        mEmoticsQuickView=new EmoticsQuickView(getContext());
        mEmoticsQuickView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((LinearLayout)v.findViewById(R.id.second_frame_layout)).addView(mEmoticsQuickView);
        return v;
    }


    public void setEditor(EditText editor){
        super.setEditor(editor);
        mEmoticsQuickView.setEditor(editor);
        mBbCodesQuickView.setEditor(editor);
    }

    public void setTopic(String forumId, String topicId, String authKey) {
       super.setTopic(forumId,topicId,authKey);
        mEmoticsQuickView.setTopic(forumId,topicId,authKey);
        mBbCodesQuickView.setTopic(forumId,topicId,authKey);
    }
}