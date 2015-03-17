package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.TopicsApi;
import org.softeg.slartus.forpdaapi.classes.TopicsListData;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;

import java.util.ArrayList;

/*
 * Created by slinkin on 17.03.2015.
 */
public class FavoritesFragment extends TopicsFragment {
    @Override
    protected int getLoaderId() {
        return TopicsLoader.ID;
    }

    @Override
    protected Loader<TopicsListData> createLoader(int id, Bundle args) {
        Loader<TopicsListData> loader = null;
        if (id == TopicsLoader.ID) {
            setLoading(true);
            loader = new TopicsLoader(getActivity(), args);

        }
        return loader;
    }

    private static class TopicsLoader extends AsyncTaskLoader<TopicsListData> {
        public static final int ID = App.getInstance().getUniqueIntValue();
        TopicsListData mApps;
        private Bundle args;

        public TopicsLoader(Context context, Bundle args) {
            super(context);

            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }


        @Override
        public TopicsListData loadInBackground() {
            try {
                ArrayList<FavTopic> topics= TopicsApi.getFavTopics(Client.getInstance()
                ,new ListInfo());
                TopicsListData data=new TopicsListData();
                data.getItems().addAll(topics);
                return data;
            } catch (Throwable e) {
                TopicsListData data = new TopicsListData();
                data.setError(e);
                return data;
            }

        }

        @Override
        public void deliverResult(TopicsListData apps) {

            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

        }

        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                mApps = null;
            }
        }

        public boolean isRefresh() {
            return args.getInt(START_NUM_KEY, 0) == 0;
        }
    }
}
