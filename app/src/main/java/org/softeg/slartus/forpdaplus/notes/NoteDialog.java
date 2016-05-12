package org.softeg.slartus.forpdaplus.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.NotesTable;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.02.13
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class NoteDialog {
    public static void showDialogForUrl(final android.os.Handler handler, Context context,
                                        String url) {
        showDialog(handler, context, "", "", url, "", "", "", "", "");
    }

    public static void showDialog(final android.os.Handler handler, final Context context,
                                  final String title, final String body, final String url, final CharSequence topicId, final String topic,
                                  final String postId, final String userId, final String user) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.note_new, null);

        final EditText etTitle = (EditText) view.findViewById(R.id.title);
        etTitle.setText(title);
        ImageButton clear_title = (ImageButton) view.findViewById(R.id.clear_title);
        clear_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTitle.setText("");
            }
        });

        final EditText etMessage = (EditText) view.findViewById(R.id.message);
        etMessage.setText(body);
        ImageButton clear_message = (ImageButton) view.findViewById(R.id.clear_message);
        clear_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMessage.setText("");
            }
        });

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getString(R.string.NewNote))
                .customView(view,true)
                .positiveText(context.getString(R.string.Save))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        new Thread(new Runnable() {
                            public void run() {
                                Throwable ex = null;


                                try {
                                    NotesTable.insertRow(etTitle.getText().toString(), etMessage.getText().toString(),
                                            url, topicId, topic, postId, userId, user);
                                } catch (Throwable e) {
                                    ex = e;
                                }

                                final Throwable finalEx = ex;

                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, finalEx.getMessage(), Toast.LENGTH_SHORT).show();
                                                AppLog.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.NoteSaved), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception ex) {
                                            AppLog.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();
                    }
                })
                .negativeText(R.string.cancel)
                .cancelable(true)
                .show();
    }

}
