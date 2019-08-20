package org.softeg.slartus.forpdaplus.fragments.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 07.11.15.
 */
public class DeviceDelete {
    static MaterialDialog dialog;
    static String URL;
    static Context mContext;
    private String parentTag = "";
    public DeviceDelete(Context context, String url, String tag){
        URL=url;
        mContext = context;
        parentTag = tag;
        new getDevice().execute(url);
        dialog = new MaterialDialog.Builder(context)
                .title(R.string.warning)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
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
                res = Client.getInstance().performGet(URL).getResponseBody();
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
    public class deleteDevice extends AsyncTask<String, Void, Void> {
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
            Toast.makeText(mContext, "Устройство удалено", Toast.LENGTH_SHORT).show();
            ((ProfileFragment)((MainActivity)mContext)
                    .getSupportFragmentManager()
                    .findFragmentByTag(parentTag)).startLoadData();
        }
    }
}
