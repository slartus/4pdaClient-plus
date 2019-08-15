package org.softeg.slartus.forpdaplus.fragments.qms.tasks;

import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment;

import java.lang.ref.WeakReference;

public class DeleteAttachTask extends AsyncTask<Void, Void, Boolean> {

    private final WeakReference<QmsChatFragment> fragment;
    private final String attachId;
    private MaterialDialog dialog = null;
    private Throwable ex;

    public DeleteAttachTask(QmsChatFragment fragment, String attachId) {

        if (fragment.getActivity() != null)
            dialog = new MaterialDialog.Builder(fragment.getActivity())
                    .progress(true, 0)
                    .content(R.string.deleting_messages)
                    .build();

        this.fragment = new WeakReference<>(fragment);
        this.attachId = attachId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            return QmsApi.INSTANCE.deleteAttach(attachId);
        } catch (Throwable ex) {
            this.ex = ex;
            return false;
        }
    }

    protected void onPreExecute() {
        this.dialog.show();
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        QmsChatFragment listener=fragment.get();
        if (!success) {
            if (ex != null)
                AppLog.e(listener==null?null:listener.getActivity(), ex);
            else
                Toast.makeText(listener==null?null:listener.getActivity(), R.string.unknown_error,
                        Toast.LENGTH_SHORT).show();
        }else {
            if(listener!=null)
                listener.onAttachDeleted(attachId);
        }
    }
}
