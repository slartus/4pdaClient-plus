package org.softeg.slartus.forpdaplus.profile;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.classes.LoginForm;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 11.10.15.
 */
public class DeviceEditDialog {
    View mView;
    String requestText="";
    final ArrayList<String> deviceNames = new ArrayList<String>();
    final ArrayList<String> deviceIds = new ArrayList<String>();
    final AutoCompleteTextView autoComplete;
    ArrayAdapter<String> autoCompleteAdapter;

    final EditText accessories;
    final Spinner spinner;
    final ProgressBar loadingResult;
    final ProgressBar loadingContent;
    final LinearLayout content;
    static String url = "";


    String deviceId = "";
    String deviceName="";
    String accessoriesText = "";
    int deviceStatus=1;

    private Context mContext;

    public DeviceEditDialog(Context context, boolean isEdit) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.device_edit, null);

        final int[] selectedId = new int[1];

        final long[] lastTimeStamp = {System.currentTimeMillis()};

        autoComplete = (AutoCompleteTextView) mView.findViewById(R.id.autoComplete);
        accessories = (EditText) mView.findViewById(R.id.accessories);
        spinner = (Spinner) mView.findViewById(R.id.spinner);
        loadingResult = (ProgressBar) mView.findViewById(R.id.loadingResult);
        loadingContent = (ProgressBar) mView.findViewById(R.id.loadingContent);
        content = (LinearLayout) mView.findViewById(R.id.content);

        String[] data = {"Владею", "Был", "Продаю", "Продано"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Статус");
        spinner.setSelection(1);

        if(isEdit) {
            new getInfo().execute();
        }else {
            content.setVisibility(View.VISIBLE);
            loadingContent.setVisibility(View.GONE);
        }

        autoCompleteAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, deviceNames);
        autoComplete.setAdapter(autoCompleteAdapter);
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (System.currentTimeMillis() - lastTimeStamp[0] > 1000) {
                    if (s.length() > 1) {
                        requestText=autoComplete.getText().toString();
                        new getDevices().execute();
                        lastTimeStamp[0] = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId[0] = position;
            }
        });
        autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager inputMethodManager = (InputMethodManager)  mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    protected View getView() {
        return mView;
    }

    public static void showDialog(final Context context, final String s, final boolean isEdit) {
        url = s;
        final String title = (isEdit ? "Изменить":"Добавить")+" устройство";
        final DeviceEditDialog loginDialog = new DeviceEditDialog(context,isEdit);
        new MaterialDialog.Builder(context)
                .title(title)
                .customView(loginDialog.getView(),true)
                .positiveText("Применить")
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                    }
                })
                .show();
    }

    public class getDevices extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingResult.setVisibility(View.VISIBLE);
            autoComplete.dismissDropDown();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Matcher m = Pattern.compile("(\\d+)\\|\\|(.*)(\\n|$)")
                        .matcher(Client.getInstance().performGet("http://4pda.ru/forum/index.php?act=profile-xhr&action=dev-autocomplete&q=" + requestText.replace(" ", "+") + "&limit=150"));
                deviceIds.clear();
                deviceNames.clear();
                while (m.find()) {
                    deviceIds.add(m.group(1));
                    deviceNames.add(m.group(2));
                }
            } catch (Exception e) {}
            return null;
        }

        // can use UI thread here



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("kekos", deviceNames.toString());
            autoCompleteAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, deviceNames);

            autoComplete.setAdapter(autoCompleteAdapter);
            autoComplete.showDropDown();
            loadingResult.setVisibility(View.INVISIBLE);
        }

    }
    public class getInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String result = Client.getInstance().performGet(url);
                Matcher m = Pattern.compile("<text.*>(.*)<[\\S\\s]*getDev\\((\\d+)").matcher(result);

                if(m.find()){
                    accessoriesText = m.group(1);
                    deviceId = m.group(2);

                    m = Pattern.compile("value=\"(\\d)\" selected").matcher(result);
                    if(m.find()) {
                        deviceStatus = Integer.valueOf(m.group(1));
                    }

                    result = Client.getInstance().performGet("http://4pda.ru/forum/index.php?act=profile-xhr&action=dev-id-mod&dev_id=" + deviceId + "&dev_mod=");
                    m = Pattern.compile("\\d+\\|\\|(.*)").matcher(result);
                    if (m.find()) deviceName = m.group(1);
                }
            } catch (Exception e) {
                AppLog.e(mContext, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            autoComplete.setText(deviceName);
            accessories.setText(accessoriesText);
            spinner.setSelection(deviceStatus);
            content.setVisibility(View.VISIBLE);
            loadingContent.setVisibility(View.GONE);
        }
    }
}
