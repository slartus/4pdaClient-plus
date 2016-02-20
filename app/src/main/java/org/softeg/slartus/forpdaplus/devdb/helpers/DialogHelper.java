package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by isanechek on 16.12.15.
 */
public class DialogHelper {

    public static void showCommentDialog(Context context, String comment, String name) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .cancelable(true)
                .title(name)
                .content(comment)
                .negativeText("Закрыть")
                .build();

        dialog.show();
    }
}
