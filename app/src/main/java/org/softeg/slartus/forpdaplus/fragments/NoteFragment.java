package org.softeg.slartus.forpdaplus.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.notes.Note;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.repositories.NotesRepository;
import org.softeg.slartus.hosthelper.HostHelper;

import java.lang.ref.WeakReference;

/**
 * Created by radiationx on 17.11.15.
 */
public class NoteFragment extends GeneralFragment {
    @Override
    public boolean closeTab() {
        return false;
    }

    private final Handler mHandler = new Handler();
    private static final String NOTE_ID_KEY = "NoteId";

    private String m_Id;
    private AdvWebView webView;
    private TableLayout infoTable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_view, container, false);
        //createActionMenu();

        infoTable = (TableLayout) findViewById(R.id.infoTable);
        webView = (AdvWebView) findViewById(R.id.webView);
        Bundle extras = getArguments();

        m_Id = extras.getString(NOTE_ID_KEY);
        loadData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(NOTE_ID_KEY, m_Id);
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
    }

    public static NoteFragment newInstance(Bundle args) {
        NoteFragment fragment = new NoteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showNote(String id) {
        Bundle args = new Bundle();
        args.putString(NOTE_ID_KEY, id);
        MainActivity.addTab(App.getContext().getString(R.string.note), NOTE_ID_KEY + id, newInstance(args));
    }


    private void loadData() {
        new LoadPageTask(new WeakReference<>(this), m_Id).execute();
    }

    private void fillData(final Note note) {
        try {
            setTitle(DbHelper.getDateString(note.Date));
            infoTable.removeAllViews();
            TableLayout.LayoutParams rowparams = new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            TableRow.LayoutParams textviewparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            if (!TextUtils.isEmpty(note.Title)) {
                addRow(getString(R.string.theme), note.Title, null, rowparams, textviewparams);
            }


            if (!TextUtils.isEmpty(note.Topic)) {
                addRow(getString(R.string.topic), note.getTopicLink(), note.getTopicUrl(), rowparams, textviewparams);
            }

            if (!TextUtils.isEmpty(note.User)) {
                addRow(getString(R.string.user), note.getUserLink(), note.getUserUrl(), rowparams, textviewparams);
            }

            if (!TextUtils.isEmpty(note.Url)) {
                addRow(getString(R.string.link), note.getUrlLink(), note.Url, rowparams, textviewparams);
            }

            webView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", transformChatBody(note.Body), "text/html", "UTF-8", null);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }


    }

    private void addRow(String title, String text, final String url,
                        TableLayout.LayoutParams rowparams, TableRow.LayoutParams textviewparams) {
        TableRow row = new TableRow(getContext());

        TextView textView = createFirtsTextView();
        textView.setText(title);
        row.addView(textView, textviewparams);
        infoTable.addView(row, rowparams);

        row = new TableRow(getContext());

        TextView textView2 = createSecondTextView();
        textView2.setText(Html.fromHtml(text));
        textView2.setEllipsize(null);
        textView2.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(url))

                IntentActivity.tryShowUrl(getMainActivity(), mHandler, url, true, false);
        });
        textView2.setOnLongClickListener(view -> {
            if (!TextUtils.isEmpty(url)) {
                ExtUrl.showSelectActionDialog(mHandler, getMainActivity(), url);
            }
            return true;
        });

        row.addView(textView2, textviewparams);


        infoTable.addView(row, rowparams);
    }

    private String transformChatBody(String chatBody) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.beginHtml(getString(R.string.note));
        htmlBuilder.append("<div class=\"emoticons\">");

        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict());
        htmlBuilder.append(chatBody);
        htmlBuilder.append("</div>");

        htmlBuilder.endBody();
        htmlBuilder.endHtml();

        return htmlBuilder.getHtml().toString();
    }

    private TextView createFirtsTextView() {
        return (TextView) getMainActivity().getLayoutInflater().inflate(R.layout.note_first_textview, null);
    }

    private TextView createSecondTextView() {
        return (TextView) getMainActivity().getLayoutInflater().inflate(R.layout.note_second_textview, null);
    }

    private static class LoadPageTask extends AsyncTask<String, String, Note> {

        private final MaterialDialog dialog;

        private final String id;

        private final WeakReference<NoteFragment> fragment;

        public LoadPageTask(WeakReference<NoteFragment> fragment, String id) {
            this.id = id;
            this.fragment = fragment;
            Context context = fragment.get().getContext();
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .cancelable(false)
                    .content(context.getString(R.string.loading))
                    .build();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setContent(progress[0]);
        }

        private Throwable ex;

        @Override
        protected Note doInBackground(String... params) {
            try {
                return NotesRepository.getInstance().getNote(id);
            } catch (Throwable e) {

                ex = e;
                return null;
            }
        }

        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
                this.cancel(true);
            }
        }

        protected void onCancelled() {
            super.onCancelled();

        }


        // can use UI thread here
        protected void onPostExecute(final Note note) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            NoteFragment f = fragment.get();
            if (note != null) {
                if (f != null)
                    f.fillData(note);
            } else {
                if (ex != null)
                    AppLog.e(f == null ? null : f.getContext(), ex, f != null ? f::loadData : null);
            }
        }

    }


}
