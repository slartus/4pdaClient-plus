package org.softeg.slartus.forpdaplus.devdb.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.ReviewsAdapter;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.model.ReviewsModel;

import java.util.ArrayList;


/**
 * Created by isanechek on 14.12.15.
 */
public class ReviewsFragment extends BaseDevDbFragment implements FLifecycleUtil {
    private static final int LAYOUT = R.layout.dev_db_list_fragment;

    private RecyclerView mRecyclerView;
    private ReviewsAdapter mAdapter;
    private ArrayList<ReviewsModel> mModelList;

    public static ReviewsFragment newInstance(Context context, String list) {
        ReviewsFragment f = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString(LIST_ARG, list);
        f.setArguments(args);
        f.setContext(context);
        f.setTitle(App.getContext().getString(R.string.review));
        f.setData();
        return f;
    }

    public void setData(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        view = inflater.inflate(LAYOUT, container, false);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        mModelList = new Gson().fromJson(getArguments().getString(LIST_ARG),  new TypeToken<ArrayList<ReviewsModel>>() {}.getType());
        if (mModelList.size() != 0) {
            mRecyclerView = view.findViewById(R.id.devDbRecyclerView);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new ReviewsAdapter(getActivity(), mModelList, ImageLoader.getInstance());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            /*CardView cardView = (CardView) view.findViewById(R.id.dev_db_error_message_con);
            cardView.setVisibility(View.VISIBLE);*/
            TextView textView = view.findViewById(R.id.dev_db_error_message);
            textView.setVisibility(View.VISIBLE);
        }
        return view;
    }

/*
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onViewCreated(view, savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
    }
*/


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

