package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by isanechek on 16.12.15.
 */
public class DialogHelper {

    public static void showCommentDialog(Context context, String comment, String name) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .cancelable(true)
                .title(name)
                .content(comment)
                .negativeText(R.string.close)
                .build();

        dialog.show();
    }
}
