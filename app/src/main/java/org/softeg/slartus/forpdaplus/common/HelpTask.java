package org.softeg.slartus.forpdaplus.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * User: slinkin
 * Date: 10.10.11
 * Time: 9:09
 */
public class HelpTask extends AsyncTask<HelpTask.OnMethodListener, String, Boolean> {

    public interface OnMethodListener {
        Object onMethod(Object param) throws IOException, ParseException, URISyntaxException;
    }


    private final ProgressDialog dialog;
    private String m_ProcessMessage = MyApp.getInstance().getString(R.string.Loading_);
    private Context mContext;

    public HelpTask(Context context, String processMessage) {
        mContext = context;

        m_ProcessMessage = processMessage;
        dialog = new AppProgressDialog(context);
        dialog.setCancelable(false);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        this.dialog.setMessage(progress[0]);
    }

    public void progressUpdate(String... progress) {
        publishProgress(progress);
    }

    private OnMethodListener m_OnPostMethod;

    public void setOnPostMethod(OnMethodListener onPostMethod) {
        m_OnPostMethod = onPostMethod;
    }

    public Boolean Success;
    public Object Result;

    @Override
    protected Boolean doInBackground(HelpTask.OnMethodListener... params) {
        try {
            Result = params[0].onMethod(null);
            return true;
        } catch (Exception e) {
            Log.e(MyApp.getInstance(), e);
            ex = e;
            return false;
        }
    }


    protected void onPreExecute() {
        this.dialog.setCancelable(false);
        this.dialog.setMessage(m_ProcessMessage);
        this.dialog.show();
    }

    protected void onCancelled() {
        Toast.makeText(mContext, mContext.getString(R.string.Canceled),
                Toast.LENGTH_SHORT).show();
        mContext = null;
    }

    public Exception ex;


    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        Success = success;
        try {
            m_OnPostMethod.onMethod(Result);
        } catch (Exception ex) {
            Log.e(mContext, ex);
        }
        mContext = null;
    }

}