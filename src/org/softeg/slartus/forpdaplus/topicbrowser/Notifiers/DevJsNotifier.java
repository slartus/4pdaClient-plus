package org.softeg.slartus.forpdaplus.topicbrowser.Notifiers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class DevJsNotifier implements NotifyActivityRegistrator.NotifyClass {

    static final String jsFuncPrefix = "HTMLDEV.";

    private Context mContext;
    private WebView mWebView;

    private int chooseFileDialogRequestCode;
    private String chooseFileDialogOk;

    /** Instantiate the interface and set the context */
    public DevJsNotifier(Context c, WebView w) {
        mContext = c;
        mWebView = w;

    }

    public void register(NotifyActivityRegistrator notifyRegistrator) {
        chooseFileDialogRequestCode = notifyRegistrator.registerRequestCode(this);
    }

    @JavascriptInterface
    public void alert ( String text ) {
        new AlertDialog.Builder(mContext).setTitle(mWebView.getTitle()).setMessage(text).setPositiveButton("Ok",null).create().show();
    }

    @JavascriptInterface
    public Boolean chooseFileDialog(int save, String onOk) {
        chooseFileDialogOk = onOk;
        Intent chooseFile = new Intent(save == 1 ? Intent.ACTION_PICK : Intent.ACTION_GET_CONTENT);
        if ( save == 1 ) {
            chooseFile.setData(Uri.parse("folder://" + mContext.getFilesDir().getPath()));
            chooseFile.putExtra(Intent.EXTRA_TITLE, "A Custom Title"); //optional
        }
        else {
            chooseFile.setType("file/*");
        }
        Intent intent = Intent.createChooser(chooseFile, "Choose a file");
        ((Activity) mContext).startActivityForResult(intent, chooseFileDialogRequestCode);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == chooseFileDialogRequestCode ) {
            String filePath = "";
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                filePath = uri.getPath();
            }
            mWebView.loadUrl ( "javascript:" + jsFuncPrefix + chooseFileDialogOk + "('" + filePath.replaceAll("['\"\\n\\r]", "\\$0") + "')" );
        }
    }
}