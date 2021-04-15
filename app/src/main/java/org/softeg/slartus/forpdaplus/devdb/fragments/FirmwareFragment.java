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
import org.softeg.slartus.forpdaplus.devdb.adapters.FirmwareAdapter;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by isanechek on 14.12.15.
 */
public class FirmwareFragment extends BaseDevDbFragment implements FLifecycleUtil {
    private static final int LAYOUT = R.layout.dev_db_list_fragment;

    private RecyclerView mRecyclerView;
    private FirmwareAdapter mAdapter;
    private List<FirmwareModel> mModelList;

    public static FirmwareFragment newInstance(Context context, String list) {
        FirmwareFragment f = new FirmwareFragment();
        Bundle args = new Bundle();
        args.putString(LIST_ARG, list);
        f.setArguments(args);
        f.setContext(context);
        f.setTitle(context.getString(R.string.firmwares));
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        view = inflater.inflate(LAYOUT, container, false);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        mModelList = new Gson().fromJson(getArguments().getString(LIST_ARG),  new TypeToken<ArrayList<FirmwareModel>>() {}.getType());
        if (mModelList.size() != 0) {
            mRecyclerView = view.findViewById(R.id.devDbRecyclerView);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new FirmwareAdapter(getActivity(), mModelList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            /*CardView cardView = (CardView) view.findViewById(R.id.dev_db_error_message_con);
            cardView.setVisibility(View.VISIBLE);*/
            TextView textView = view.findViewById( R.id.dev_db_error_message);
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

