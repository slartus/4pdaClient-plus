package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 15.10.12
 * Time: 9:24
 * To change this template use File | Settings | File Templates.
 */
public class ShowInBrowserDialog {
    public static void showDialog(final Context context, ShowInBrowserException ex) {
        showDialog(context, context.getString(R.string.Error), ex.getMessage(), ex.Url);
    }

    public static void showDialog(final Context context, String title, String message, final String url) {
        new AlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message + "\n" + context.getString(R.string.OpenLinkInBrowser) + "?")
                .setPositiveButton(context.getString(R.string.Open), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url));
                        context.startActivity(Intent.createChooser(marketIntent, "Выберите"));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }
}
