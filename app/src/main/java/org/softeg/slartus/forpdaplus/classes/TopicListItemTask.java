package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaplus.common.AppLog;

/*
 * Created by slartus on 04.06.2014.
 */
public abstract class TopicListItemTask extends AsyncTask<String, String, String> {
    private Context context;
    private Topic topic;
    private BaseAdapter listAdapter;

    public TopicListItemTask(Context context, Topic topic, BaseAdapter listAdapter) {
        this.context = context;
        this.topic = topic;
        this.listAdapter = listAdapter;
    }

    protected void onPreExecute(Topic topic) {
        topic.inProgress(true);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPreExecute() {
        onPreExecute(topic);
    }

    public abstract String doInBackground(Topic topic, String... pars) throws Throwable;

    private Throwable ex = null;

    @Override
    protected String doInBackground(String... pars) {
        try {
            return doInBackground(topic, pars);
        } catch (Throwable e) {
            ex = e;
            return null;
        }
    }

    public abstract void onPostExecute(Topic topic);

    @Override
    protected void onPostExecute(String result) {
        try {
            topic.inProgress(false);

            if (ex == null) {
                Toast.makeText(context, "\"" + topic.getTitle().substring(0, Math.min(10, topic.getTitle().length() - 1)) + "..\": " + result, Toast.LENGTH_SHORT).show();
                onPostExecute(topic);
            } else
                AppLog.e(context, ex);
        } catch (Throwable ex1) {
            AppLog.e(context, ex1);
        }
        listAdapter.notifyDataSetChanged();
    }
}
