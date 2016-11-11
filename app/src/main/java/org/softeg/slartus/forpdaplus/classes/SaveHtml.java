package org.softeg.slartus.forpdaplus.classes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by radiationx on 11.07.15.
 */
public class SaveHtml {
    public SaveHtml(final Activity activity, final String html, final String defaultFileName){
        final String[] fileName = {defaultFileName};
        new MaterialDialog.Builder(activity)
                .title(R.string.file_name)
                .input(defaultFileName, defaultFileName, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        fileName[0] = charSequence.toString();
                    }
                })
                .alwaysCallInputCallback()
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            String state = Environment.getExternalStorageState();
                            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                                Toast.makeText(activity, R.string.error_external_storage, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            File file = new File(App.getInstance().getExternalFilesDir(null), fileName[0]+".txt");
                            FileWriter out = new FileWriter(file);
                            out.write(html);
                            out.close();
                            Uri uri = Uri.fromFile(file);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "text/plain");
                            activity.startActivity(intent);
                        } catch (Exception e) {
                            AppLog.e(activity, e);
                        }
                    }
                })
                .show();

    }
}
