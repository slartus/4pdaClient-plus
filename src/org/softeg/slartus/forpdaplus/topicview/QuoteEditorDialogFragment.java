package org.softeg.slartus.forpdaplus.topicview;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.HtmlUtils;
import org.softeg.slartus.forpdaplus.common.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Артём on 11.05.2014.
 */
public class QuoteEditorDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String QUOTE_URL_KEY = "QUOTE_TEXT_KEY";
    private static final String QUOTE_TEXT_KEY = "QUOTE_TEXT_KEY";
    private static final String EDITOR_TEXT_KEY = "EDITOR_TEXT_KEY";


    public static DialogFragment newInstance(CharSequence quoteUrl) {
        QuoteEditorDialogFragment fragment = new QuoteEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(QUOTE_URL_KEY, quoteUrl.toString());
        fragment.setArguments(args);
        return fragment;
    }

    EditText txtBody;

    protected Bundle args = new Bundle();
    private String m_Author, m_Text;
    private View progressBar;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            args = getArguments();
        }
        if (savedInstanceState != null) {
            args = savedInstanceState;
            parseQuote();
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);
        outState.putString(EDITOR_TEXT_KEY, txtBody.getText().toString());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.quote_editor, null);
        assert v != null;
        txtBody = (EditText) v.findViewById(R.id.txtBody);
        progressBar = v.findViewById(R.id.progressBar);
        v.findViewById(R.id.btnAll).setOnClickListener(this);
        v.findViewById(R.id.btnAuthor).setOnClickListener(this);
        v.findViewById(R.id.btnText).setOnClickListener(this);
        v.findViewById(R.id.btnClear).setOnClickListener(this);


        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Редактирование цитаты")
                .setView(v)
                .setPositiveButton("Вставить",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                ((ThemeActivity) getActivity()).insertTextToPost(txtBody.getText().toString());
                            }
                        }
                )
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new QuoteLoader(args.getString(QUOTE_URL_KEY)).execute();
    }


    private void parseQuote() {
        m_Author = "";
        m_Text = "";
        Matcher m = Pattern.compile("(\\[quote name=\"[^\"]*\" date=\"[^\"]*\" post=\"\\d+\"\\])([\\s\\S]*?)\\[/quote\\]", Pattern.CASE_INSENSITIVE)
                .matcher(args.getString(QUOTE_TEXT_KEY));
        if (m.find()) {
            m_Author = m.group(1);
            m_Text = m.group(2);
            if (m_Text != null)
                m_Text = m_Text.trim();
        } else {
            Toast.makeText(getActivity(), "Ошибка разбора цитаты", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAll:
                txtBody.setText(args.getString(QUOTE_TEXT_KEY));
                break;
            case R.id.btnAuthor:
                insertText(m_Author+"\n", "\n[/quote]");
                break;
            case R.id.btnText:
                insertText(m_Text, "");
                break;
            case R.id.btnClear:
                txtBody.setText("");
                break;

        }
    }

    private void insertText(String startText, String endText) {
        int selectionStart = txtBody.getSelectionStart();
        int selectionEnd = txtBody.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }

        if (selectionStart == -1)
            selectionStart = 0;
        if (selectionEnd == -1)
            selectionEnd = 0;
        if (!TextUtils.isEmpty(endText))
            txtBody.getText().insert(selectionEnd, endText);
        txtBody.getText().insert(selectionStart, startText);

    }

    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public class QuoteLoader extends AsyncTask<String, String, Boolean> {


        private String m_QuoteUrl;

        public QuoteLoader(String url) {
            m_QuoteUrl = url;

        }

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        private String m_Quote = "";


        @Override
        protected Boolean doInBackground(String... strings) {
            try {


                String page = Client.getInstance().performGet(m_QuoteUrl);
                Matcher m = Pattern.compile("<textarea name=\"Post\".*?>([\\s\\S]*?)</textarea>").matcher(page);
                if (m.find()) {
                    String quote = m.group(1);
                    if (quote != null)
                        quote = quote.trim();
                    m_Quote = HtmlUtils.modifyHtmlQuote(quote)
                    ;
                }
                return true;
            } catch (Throwable e) {
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }

        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            try {
                progressBar.setVisibility(View.GONE);
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (isCancelled()) return;

            if (success) {
                args.putString(QUOTE_TEXT_KEY, m_Quote);
                txtBody.setText(m_Quote);
                parseQuote();

            } else {
                Log.e(getActivity(), ex, new Runnable() {
                    @Override
                    public void run() {
                        new QuoteLoader(m_QuoteUrl).execute();
                    }
                });
            }
        }
    }
}
