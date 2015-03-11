package org.softeg.browser.pageviewcontrol;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.yarportal.AppLog;
import org.softeg.slartus.yarportal.FragmentBase;
import org.softeg.slartus.yarportal.R;
import org.softeg.slartus.yarportal.common.UrlExtensions;
import org.softeg.slartus.yarportal.pageviewcontrol.htmloutinterfaces.Developer;
import org.softeg.slartus.yarportal.pageviewcontrol.htmloutinterfaces.HtmlOutManager;
import org.softeg.slartus.yarportal.pageviewcontrol.htmloutinterfaces.IHtmlOutListener;
import org.softeg.slartus.yarportal.preferences.AppPreferences;

import java.io.File;
import java.util.ArrayList;


/*
 * Created by slinkin on 01.10.2014.
 */
public abstract class PageFragment extends PageViewFragment implements
        IHtmlOutListener, IWebViewClientListener,
        FragmentBase.OnBackPressedListener {
    public static final String ID = "ru.yarportal.topic.PageFragment";
    public static final String SCROLL_Y_KEY = "PageFragment.SCROLL_Y_KEY";
    private static final String TAG = "PageFragment";
    protected HtmlOutManager mHtmlOutManager;
    protected int mScrollY = 0;

    protected void initHtmlOutManager() {
        mHtmlOutManager = new HtmlOutManager(this);
        mHtmlOutManager.registerInterfaces(mWebView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initHtmlOutManager();


        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_Y_KEY)) {
            mScrollY = savedInstanceState.getInt(SCROLL_Y_KEY);
        }
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mWebView.setWebViewClient(new AppWebViewClient(this));
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showUrlChoice(hitTestResult.getExtra());
            }
        }
    }

    public void showUrlChoice(String url){
        UrlExtensions.showChoiceDialog(getActivity(), url,
                new UrlExtensions.UrlAction() {
                    @Override
                    public String getActionTitle() {
                        return "Добавить закладку";
                    }

                    @Override
                    public void run(String url) {
                        showAddBookmarkDialog(url);
                    }
                });
    }

    private void showAddBookmarkDialog(final String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(url);

// Set up the input
        final EditText input = new EditText(getActivity());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Заголовок закладки");
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String m_Text = input.getText().toString();
                if (TextUtils.isEmpty(m_Text)) {
                    Toast.makeText(getActivity(), "Необходимо ввести заголовок", Toast.LENGTH_SHORT).show();
                    showSelectStyleDialog();
                    return;
                }
                try {
                    //BookmarksTable.addBookmark(m_Text, null, url);
                    Toast.makeText(getActivity(), "Закладка добавлена", Toast.LENGTH_SHORT).show();

                } catch (Throwable ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCROLL_Y_KEY, mWebView.getScrollY());
    }

    public void loadData(String url) {
        loadData(url, getCurrentPage());
    }

    protected void setLoading(boolean loading) {
        try {
            if (getActivity() == null) return;

            mPullToRefreshLayout.setRefreshing(loading);
        } catch (Throwable ignore) {

            android.util.Log.e("TAG", ignore.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mHtmlOutManager.onActivityResult(requestCode, resultCode, data);
    }

    public void saveHtml() {
        try {
            mWebView.evalJs("window." + Developer.NAME + ".saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    public abstract void nextPage();

    public abstract void prevPage();

    public abstract void firstPage();

    public abstract void lastPage();

    protected void showFontSizeDialog() {
        View v = getActivity().getLayoutInflater().inflate(R.layout.font_size_dialog, null);

        assert v != null;
        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
        seekBar.setProgress(AppPreferences.getWebViewFontSize());
        final TextView textView = (TextView) v.findViewById(R.id.value_textview);
        textView.setText((seekBar.getProgress() + 1) + "");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getWebView().getSettings().setDefaultFontSize(i + 1);
                textView.setText((i + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialog.Builder(getActivity())
                .setTitle("Размер шрифта")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        AppPreferences.setWebViewFontSize(seekBar.getProgress() + 1);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getWebView().getSettings().setDefaultFontSize(AppPreferences.getWebViewFontSize());
                    }
                })
                .create().show();

    }

    protected void showSelectStyleDialog() {
        try {
//            Context context = getActivity();
//            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
//            newStyleNames.add("Стандартный");
//            newStyleNames.add("Режим разработчика");
//            final ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();
//            newstyleValues.add(AppAssetsManager.getAssetsPath() + "forum/css/style.css");
//            newstyleValues.add(TopicApi.CSS_DEVELOPER);
//            File file = new File(context.getExternalFilesDir(null).getPath() + File.separator + "css");
//            getStylesList(newStyleNames, newstyleValues, file, ".css");
//
//            new AlertDialog.Builder(getActivity())
//                    .setTitle("Выбор стиля")
//                    .setSingleChoiceItems(
//                            newStyleNames.toArray(new CharSequence[newStyleNames.size()]),
//                            newstyleValues.indexOf(AppPreferences.Topic.getStyleCssPath()), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.dismiss();
//                                    AppPreferences.Topic.setStyleCssPath(newstyleValues.get(i).toString());
//                                    refreshPage();
//                                }
//                            })
//                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    }).create().show();
        } catch (Throwable ex) {
            AppLog.e(ex);
        }
    }

    public abstract void refreshPage();

    private static void getStylesList(ArrayList<CharSequence> newStyleNames, ArrayList<CharSequence> newstyleValues,
                                      File file, String ext) {

        if (file.exists()) {
            File[] cssFiles = file.listFiles();
            assert cssFiles != null;
            for (File cssFile : cssFiles) {
                if (cssFile.isDirectory()) {
                    getStylesList(newStyleNames, newstyleValues, cssFile, ext);
                    continue;
                }
                String cssPath = cssFile.getPath();
                if (!cssPath.toLowerCase().endsWith(ext)) continue;

                newStyleNames.add(cssFile.getName());
                newstyleValues.add(cssPath);

            }
        }
    }
}
