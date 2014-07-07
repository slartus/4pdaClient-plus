package org.softeg.slartus.forpdaplus.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;

import org.softeg.slartus.forpdaplus.MyApp;

/**
 * Created by slinkin on 17.12.13.
 */
public class AppProgressDialog extends ProgressDialog {
    public AppProgressDialog(Context context) {
        super(new ContextThemeWrapper(context, MyApp.getInstance().getThemeStyleResID()));

    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

    }

}
