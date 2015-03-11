package org.softeg.browser.pageviewcontrol.htmloutinterfaces;/*
 * Created by slinkin on 09.07.2014.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.dmitriy.tarasov.android.intents.IntentUtils;

import org.softeg.slartus.common.FileUtils;
import org.softeg.slartus.yarportal.App;
import org.softeg.slartus.yarportal.AppLog;

import java.io.File;
import java.io.FileWriter;
import java.lang.ref.WeakReference;


public class Developer implements IHtmlOut {
    public static final String NAME = "DEVOUT";
    private WeakReference<IHtmlOutListener> control;

    public Developer(WeakReference<IHtmlOutListener> control) {
        this.control = control;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean hasRequestCode(int requestCode) {
        return requestCode == FILECHOOSER_RESULTCODE;
    }

    private Context getContext() {
        return control.get().getContext();
    }

    private Activity getActivity() {
        return control.get().getActivity();
    }

    private final static int FILECHOOSER_RESULTCODE = App.getInstance().getUniqueIntValue();

    @JavascriptInterface
    public void showChooseCssDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    control.get().getFragment().startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
    }


    @JavascriptInterface
    public void saveHtml(final String html) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        Toast.makeText(getContext(), "Внешнее хранилище недоступно!", Toast.LENGTH_SHORT).show();
                        getActivity().startActivity(IntentUtils.sendEmail("","Исходник",html));
                        return;
                    }


                    File file = new File(App.getInstance().getExternalFilesDir(null), "Topic.txt");
                    FileWriter out = new FileWriter(file);
                    out.write(html);
                    out.close();
                    Uri uri = Uri.fromFile(file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/plain");
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    AppLog.e(getActivity(), e);
                }
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getContext(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");

            control.get().getWebView().evalJs("window['HtmlInParseLessContent']('" + cssData + "');");

        }
    }


}

