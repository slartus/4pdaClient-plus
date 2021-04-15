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
import org.softeg.slartus.forpdaplus.repositories.NotesRepository;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.02.13
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class NoteDialog {

    public static void showDialog(final android.os.Handler handler, final Context context,
                                  final String title, final String body, final String url, final CharSequence topicId, final String topic,
                                  final String postId, final String userId, final String user) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.note_new, null);

        final EditText etTitle = view.findViewById(R.id.title);
        etTitle.setText(title);
        ImageButton clear_title = view.findViewById(R.id.clear_title);
        clear_title.setOnClickListener(view1 -> etTitle.setText(""));

        final EditText etMessage = view.findViewById(R.id.message);
        etMessage.setText(body);
        ImageButton clear_message = view.findViewById(R.id.clear_message);
        clear_message.setOnClickListener(view12 -> etMessage.setText(""));

        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.NewNote))
                .customView(view, true)
                .positiveText(context.getString(R.string.Save))
                .onPositive((dialog, which) -> {
                    new Thread(() -> {
                        Throwable ex = null;
                        try {
                            NotesRepository.getInstance()
                                    .insertRow(etTitle.getText().toString(), etMessage.getText().toString(),
                                            url, topicId, topic, postId, userId, user, () -> {
                                                handler.post(() -> {
                                                            Toast.makeText(context, context.getString(R.string.NoteSaved), Toast.LENGTH_LONG).show();
                                                        }
                                                );
                                                return null;
                                            });
                        } catch (Throwable e) {
                            ex = e;
                        }

                        final Throwable finalEx = ex;

                        handler.post(() -> {
                            try {
                                if (finalEx != null) {
                                    Toast.makeText(context, finalEx.getMessage(), Toast.LENGTH_SHORT).show();
                                    AppLog.e(context, finalEx);
                                }
                            } catch (Exception ex1) {
                                AppLog.e(context, ex1);
                            }

                        });
                    }).start();
                })
                .negativeText(R.string.cancel)
                .cancelable(true)
                .show();
    }

}
