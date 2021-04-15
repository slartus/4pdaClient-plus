package org.softeg.slartus.forpdaplus.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by slinkin on 20.02.14.
 */
public class ListViewLoadMoreFooter {
    private final Context mContext;
    private final Boolean mCanLoadMore = false;
    private View mFooterView = null;
    private TextView count_textview;
    private View progressPanel;
    private View loadMorePanel;
    private int mState = 0;
    public static final int STATE_FULL_DOWNLOADED = 0;
    public static final int STATE_LOAD_MORE = 1;
    public static final int STATE_LOADING = 2;
    private View.OnClickListener mClickListener;

    public ListViewLoadMoreFooter(Context context, ListView listView) {
        mContext = context;
        createView();
        listView.addFooterView(mFooterView);
    }


    public void setState(int state) {
        mState = state;
        switch (mState) {
            case STATE_FULL_DOWNLOADED:
                loadMorePanel.setVisibility(View.GONE);
                progressPanel.setVisibility(View.GONE);
                break;
            case STATE_LOAD_MORE:
                loadMorePanel.setVisibility(View.VISIBLE);
                progressPanel.setVisibility(View.GONE);
                break;
            case STATE_LOADING:
                loadMorePanel.setVisibility(View.GONE);
                progressPanel.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setCount(int loaded, int count) {
        if (loaded == count) {
            count_textview.setText(Integer.toString(count));
        } else {
            count_textview.setText(String.format("%s / %s ", loaded, count));
        }

    }

    public void setOnLoadMoreClickListener(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void createView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFooterView = inflater.inflate(R.layout.listfragment_footer, null);
        mFooterView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mClickListener == null)
                    return;

                if (mState != STATE_LOAD_MORE)
                    return;
                mClickListener.onClick(view);
            }
        });
        count_textview = mFooterView.findViewById(R.id.count_textview);
        progressPanel = mFooterView.findViewById(R.id.progressPanel);
        loadMorePanel = mFooterView.findViewById(R.id.loadMorePanel);
    }
}
