package uk.co.senab.actionbarpulltorefresh.library;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by slinkin on 24.02.14.
 */
public class CustomisedHeaderTransformer extends HeaderTransformer {

    private View mHeaderView;
    private TextView mMainTextView;
    private TextView mProgressTextView;

    @Override
    public void onViewCreated(Activity activity, View headerView) {
        mHeaderView = headerView;
        mMainTextView = (TextView) headerView.findViewById(R.id.ptr_text);
        mProgressTextView = (TextView) headerView.findViewById(R.id.ptr_text_secondary);
    }

    @Override
    public void onReset() {
        mMainTextView.setVisibility(View.VISIBLE);
        mMainTextView.setText(R.string.pull_to_refresh_pull_label);

        mProgressTextView.setVisibility(View.GONE);
        mProgressTextView.setText("");
    }

    @Override
    public void onPulled(float percentagePulled) {
        mProgressTextView.setVisibility(View.VISIBLE);
        mProgressTextView.setText(Math.round(100f * percentagePulled) + "%");
    }

    @Override
    public void onRefreshStarted() {
        mMainTextView.setText(R.string.pull_to_refresh_refreshing_label);
        mProgressTextView.setVisibility(View.GONE);
    }

    @Override
    public void onReleaseToRefresh() {
        mMainTextView.setText(R.string.pull_to_refresh_release_label);
    }

    @Override
    public void onRefreshMinimized() {
        // In this header transformer, we will ignore this call
    }

    @Override
    public boolean showHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() != View.VISIBLE;
        if (changeVis) {
            mHeaderView.setVisibility(View.VISIBLE);
        }
        return changeVis;
    }

    @Override
    public boolean hideHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() == View.VISIBLE;
        if (changeVis) {
            mHeaderView.setVisibility(View.GONE);
        }
        return changeVis;
    }
}