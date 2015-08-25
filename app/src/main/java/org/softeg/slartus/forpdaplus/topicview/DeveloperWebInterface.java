package org.softeg.slartus.forpdaplus.topicview;/*
 * Created by slinkin on 09.07.2014.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.common.AppLog;

public class DeveloperWebInterface {
    public static final String NAME = "DEVOUT";
    private ThemeActivity context;

    public DeveloperWebInterface(ThemeActivity context) {
        this.context = context;
    }

    private ThemeActivity getContext() {
        return context;
    }

    public final static int FILECHOOSER_RESULTCODE = App.getInstance().getUniqueIntValue();

    @JavascriptInterface
    public void showChooseCssDialog() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    getContext().startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getContext(), ex);
                }
            }
        });
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        new SaveHtml(getContext(),html,"Topic");
    }


    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            if(data.getData()==null)
                return;
            String attachFilePath = FileUtils.getRealPathFromURI(App.getInstance(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");

            getContext().getWebView().evalJs("window['HtmlInParseLessContent']('" + cssData + "');");

        }
    }
}

