package org.softeg.slartus.forpdaplus.notes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.db.NotesTable;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 21.02.13
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class NoteActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    private static final String NOTE_ID_KEY = "NoteId";

    private String m_Id;
    private String m_TopicId;
    private AdvWebView webView;
    private TableLayout infoTable;
    private MenuFragment mFragment1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.note_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //createActionMenu();

        infoTable = (TableLayout) findViewById(R.id.infoTable);
        webView = (AdvWebView) findViewById(R.id.webView);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(NOTE_ID_KEY);


    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(NOTE_ID_KEY, m_Id);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);

        m_Id = outState.getString(NOTE_ID_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    public static void showNote(Context context, String id) {
        Intent intent = new Intent(context.getApplicationContext(), NoteActivity.class);
        intent.putExtra(NOTE_ID_KEY, id);
        context.startActivity(intent);
    }


    private void loadData() {
        new LoadPageTask(this).execute();
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
                addRow("Тема", note.Title, null, rowparams, textviewparams);
            }


            if (!TextUtils.isEmpty(note.Topic)) {
                addRow("Топик", note.getTopicLink(), note.getTopicUrl(), rowparams, textviewparams);
            }

            if (!TextUtils.isEmpty(note.User)) {
                addRow("Пользователь", note.getUserLink(), note.getUserUrl(), rowparams, textviewparams);
            }

            if (!TextUtils.isEmpty(note.Url)) {
                addRow("Ссылка", note.getUrlLink(), note.Url, rowparams, textviewparams);
            }

            webView.loadDataWithBaseURL("http://4pda.ru/forum/", transformChatBody(note.Body), "text/html", "UTF-8", null);
        } catch (Throwable ex) {
            AppLog.e(this, ex);
        }


    }

    private void addRow(String title, String text, final String url,
                        TableLayout.LayoutParams rowparams, TableRow.LayoutParams textviewparams) {
        TableRow row = new TableRow(this);

        TextView textView = createFirtsTextView();
        textView.setText(title);
        row.addView(textView, textviewparams);
        infoTable.addView(row, rowparams);

        row = new TableRow(this);

        TextView textView2 = createSecondTextView();
        textView2.setText(Html.fromHtml(text));
        textView2.setEllipsize(null);
        textView2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!TextUtils.isEmpty(url))
                    IntentActivity.tryShowUrl(NoteActivity.this, mHandler, url, true, false);
            }
        });
        textView2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!TextUtils.isEmpty(url)) {
                    ExtUrl.showSelectActionDialog(mHandler, NoteActivity.this, url);
                }
                return true;
            }
        });

        row.addView(textView2, textviewparams);


        infoTable.addView(row, rowparams);
    }

    private String transformChatBody(String chatBody) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.beginHtml("Заметка");
        htmlBuilder.append("<div class=\"emoticons\">");

        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict(), true);
        htmlBuilder.append(chatBody);
        htmlBuilder.append("</div>");

        htmlBuilder.endBody();
        htmlBuilder.endHtml();

        return htmlBuilder.getHtml().toString();
    }

    private TextView createFirtsTextView() {
        return (TextView) getLayoutInflater().inflate(R.layout.note_first_textview, null);
    }
    private TextView createSecondTextView() {
        return (TextView) getLayoutInflater().inflate(R.layout.note_second_textview, null);
    }

    public class LoadPageTask extends AsyncTask<String, String, Note> {


        private final MaterialDialog dialog;

        public LoadPageTask(Context context) {

            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .cancelable(false)
                    .content("Загрузка")
                    .build();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }

        private Throwable ex;

        @Override
        protected Note doInBackground(String... params) {
            try {

                return NotesTable.getNote(m_Id);
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

            if (note != null) {

                fillData(note);

            } else {
                if (ex != null)
                    AppLog.e(NoteActivity.this, ex, new Runnable() {
                        @Override
                        public void run() {
                            loadData();
                        }
                    });
            }
        }

    }

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {
            super();
        }

        private NoteActivity getInterface() {
            if (getActivity() == null) return null;
            return (NoteActivity) getActivity();
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item;

            item = menu.add("Удалить").setIcon(R.drawable.ic_delete_white_24dp);
            //item.setVisible(Client.getInstance().getLogined());
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }


    }


}
