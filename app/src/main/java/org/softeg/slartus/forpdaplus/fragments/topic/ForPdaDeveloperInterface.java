package org.softeg.slartus.forpdaplus.fragments.topic;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.common.AppLog;

/**
 * Created by radiationx on 28.10.15.
 */
public class ForPdaDeveloperInterface {
    public static final String NAME = "DEVOUT";
    private FragmentActivity activity;
    private ThemeFragment context;

    public ForPdaDeveloperInterface(ThemeFragment context) {
        this.context = context;
        this.activity = context.getActivity();
    }
    private ThemeFragment getContext(){
        return context;
    }
    private FragmentActivity getActivity() {
        return activity;
    }

    public final static int FILECHOOSER_RESULTCODE = App.getInstance().getUniqueIntValue();

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
                    getActivity().startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        new SaveHtml(getActivity(),html,"Topic");
    }


    public void onActivityResult(int requestCode, int resultCode,
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