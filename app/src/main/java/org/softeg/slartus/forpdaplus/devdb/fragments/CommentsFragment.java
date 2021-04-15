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

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.devdb.adapters.CommentsAdapter;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;

import java.util.ArrayList;



/**
 * Created by isanechek on 14.12.15.
 */
public class CommentsFragment extends BaseDevDbFragment implements FLifecycleUtil {
    private static final int LAYOUT = R.layout.dev_db_list_fragment;

    private CommentsAdapter mAdapter;
    private ArrayList<CommentsModel> mModelList;

    public static CommentsFragment newInstance(Context context, String list) {
        CommentsFragment f = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(LIST_ARG, list);
        f.setArguments(args);
        f.setContext(context);
        f.setTitle(context.getString(R.string.reviews));
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        view = inflater.inflate(LAYOUT, container, false);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        mModelList = new Gson().fromJson(getArguments().getString(LIST_ARG),  new TypeToken<ArrayList<CommentsModel>>() {}.getType());
        if (mModelList.size() != 0) {
            RecyclerView recyclerView = view.findViewById(R.id.devDbRecyclerView);
            recyclerView.setVisibility(View.VISIBLE);
            mAdapter = new CommentsAdapter(getActivity(), mModelList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            /*CardView cardView = (CardView) view.findViewById(R.id.dev_db_error_message_con);
            cardView.setVisibility(View.VISIBLE);*/
            TextView textView = view.findViewById(R.id.dev_db_error_message);
            textView.setVisibility(View.VISIBLE);
        }
        return view;
    }

/*    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onViewCreated(view, savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);

    }*/

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

