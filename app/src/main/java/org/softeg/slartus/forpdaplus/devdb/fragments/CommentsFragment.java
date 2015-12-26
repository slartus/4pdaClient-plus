package org.softeg.slartus.forpdaplus.devdb.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.CommentsAdapter;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;

import java.util.ArrayList;

import butterknife.ButterKnife;


/**
 * Created by isanechek on 14.12.15.
 */
public class CommentsFragment extends BaseDevDbFragment implements FLifecycleUtil {
    private static final int LAYOUT = R.layout.dev_db_list_fragment;

    private CommentsAdapter mAdapter;
    private ArrayList<CommentsModel> mModelList;

    public static CommentsFragment newInstance(Context context) {
        CommentsFragment f = new CommentsFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        f.setContext(context);
        f.setTitle("Комментарии");
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        view = inflater.inflate(LAYOUT, container, false);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onViewCreated(view, savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        if (DevDbUtils.getComments(getActivity()).size() != 0) {
            mModelList = new ArrayList<>(DevDbUtils.getComments(getActivity()));
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.devDbRecyclerView);
            recyclerView.setVisibility(View.VISIBLE);
            mAdapter = new CommentsAdapter(context, mModelList);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            /*CardView cardView = (CardView) view.findViewById(R.id.dev_db_error_message_con);
            cardView.setVisibility(View.VISIBLE);*/
            TextView textView = ButterKnife.findById(view, R.id.dev_db_error_message);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onPause();
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public void onDestroy() {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onDestroy();
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
    }

    public void setContext(Context context) {
        this.context = context;
    }

}

