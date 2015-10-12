package org.softeg.slartus.forpdaplus.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 12.10.15.
 */
public class DeviceDeleteDialog {
    static MaterialDialog dialog;
    static String URL;
    static Context mContext;
    public DeviceDeleteDialog(Context context,String url){
        URL=url;
        mContext = context;
        new getDevice().execute(url);
    }
    public static void showDialog(final Context context, final String url) {
        final DeviceDeleteDialog loginDialog = new DeviceDeleteDialog(context,url);
        dialog = new MaterialDialog.Builder(context)
                .title("Предупреждение")
                .positiveText("Удалить")
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        new deleteDevice().execute();
                    }
                })
                .build();

    }
    public class getDevice extends AsyncTask<String, Void, Void> {
        String res;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                res = Client.getInstance().performGet(URL);
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Matcher m = Pattern.compile("<strong>([\\S\\s]*)<\\/strong>")
                    .matcher(res);
            if(m.find()){
                dialog.setContent("Удалить устройство \""+m.group(1)+"\" из списка ваших устройств?");
                dialog.show();
            }
        }
    }
    public static class deleteDevice extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, String> additionalHeaders = new HashMap<String, String>();
                additionalHeaders.put("auth_key", Client.getInstance().getAuthKey());
                additionalHeaders.put("dodel", "%D3%E4%E0%EB%E8%F2%FC");
                Client.getInstance().performPost(URL,additionalHeaders);
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(mContext,"Устройство удалено",Toast.LENGTH_SHORT).show();
            ((ProfileWebViewFragment)((ProfileWebViewActivity)mContext).getSupportFragmentManager().findFragmentByTag("profileFragment")).startLoadData();
        }
    }
}
