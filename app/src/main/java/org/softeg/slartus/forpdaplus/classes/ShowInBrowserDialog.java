package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 15.10.12
 * Time: 9:24
 * To change this template use File | Settings | File Templates.
 */
public class ShowInBrowserDialog {
    private static final String TAG = "ShowInBrowserDialog";

    public static void showDialog(final Context context, ShowInBrowserException ex) {
        showDialog(context, context.getString(R.string.Error), ex.getMessage(), ex.Url);
    }

    public static void showDialog(final Context context, String title, String message, final String url) {
        try {
            new MaterialDialog.Builder(context)
                    .title(title)
                    .content(message + "\n" + context.getString(R.string.OpenLinkInBrowser) + "?")
                    .positiveText(context.getString(R.string.Open))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent marketIntent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url));
                            context.startActivity(Intent.createChooser(marketIntent, context.getString(R.string.choose)));
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .show();
        } catch (Throwable ex) {
            Log.e(TAG,ex.toString());
        }

    }
}
