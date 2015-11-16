package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.classes.DevDbDevice;
import org.softeg.slartus.forpdaplus.classes.LazyGallery.LazyAdapter;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:05
 */
public class DevDbDeviceActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    private String m_DeviceId;
    private Gallery gallery;
    private DevDbDevice m_DevDbDevice;
    private static final String DEVICE_ID_KEY = "DeviceId";
    private List<String> keyInfoList;
    private List<String> valueInfoList;
    private ArrayList<String> imgUrls;
    private static final String[] KEYS = { "line1", "line2" };
    private static final int[] IDS = { R.id.dev_db_title, R.id.dev_db_sub_title };
    private static final int LAYOUT = R.layout.dev_item_x;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_db_activity_x);
//        pnlRating = (LinearLayout) findViewById(R.id.pnlRating);
        gallery = (Gallery) findViewById(R.id.gallery);


//        infoTable = (TableLayout) findViewById(R.id.infoTable);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        assert extras != null;
        m_DeviceId = extras.getString(DEVICE_ID_KEY);

    }

    @Override
    public void onStart() {
        super.onStart();
        loadPage();
    }


    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(DEVICE_ID_KEY, m_DeviceId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            m_DeviceId = savedInstanceState.getString(DEVICE_ID_KEY, m_DeviceId);
        }
    }

    public static void showDevice(Context context, String deviceId) {
        Intent intent = new Intent(context, DevDbDeviceActivity.class);
        intent.putExtra(DEVICE_ID_KEY, deviceId);

        context.startActivity(intent);
    }

    public void loadPage() {
        new LoadPageTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Браузер")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(m_DeviceId));

                        DevDbDeviceActivity.this.startActivity(Intent.createChooser(marketIntent, "Выберите"));
                        return true;
                    }
                });

        return true;
    }

    @Override
    public void onDestroy() {
        if (gallery != null)
            gallery.setAdapter(null);
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return true;
    }

    private void initUI() throws IOException {
        getSupportActionBar().setTitle(m_DevDbDevice.getInfo().Model);

        imgUrls = new ArrayList<>(m_DevDbDevice.getScreenshotUrls());
        keyInfoList = new ArrayList<>(m_DevDbDevice.getKeyInfo());
        valueInfoList = new ArrayList<>(m_DevDbDevice.getValueInfo());

        List<Map<String, String>> oneList = new ArrayList<>();
        Map<String, String> map;
        for (int i = 0; i < valueInfoList.size(); i++) {
            map = new HashMap<>();
            map.put("line1", valueInfoList.get(i));
            map.put("line2", keyInfoList.get(i));
            oneList.add(map);
        }

        LinearLayout scrollView = (LinearLayout) findViewById(R.id.dev_db_btn_con);
        List<String> btnControls = m_DevDbDevice.getControls();
        if (btnControls != null) {
            for (int i = 1; i < btnControls.size(); i++) //начинается с одного, потому что кнока "характеристики" нам не нужна
            {
                String titleControl = btnControls.get(i);
                Button control = new Button(this);
                control.setText(titleControl);
                control.setId(i + 1);
                control.setOnClickListener(controlClick);
                control.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                scrollView.addView(control);
            }
        }

        ListView list = (ListView) findViewById(R.id.list);
        SimpleAdapter simpleAdapter = new SimpleAdapter(DevDbDeviceActivity.this, oneList, LAYOUT, KEYS, IDS);
        list.setAdapter(simpleAdapter);

        LazyAdapter adapter = new LazyAdapter(DevDbDeviceActivity.this,
                m_DevDbDevice.getScreenshotUrls().toArray(new String[m_DevDbDevice.getScreenshotUrls().size()]));
        gallery.setAdapter(adapter);

        /*
        перенес сюда, ибо в onCreate imgUrls return null.
        Зачем так сделал? startActivity может принять список ссылок и viewpager вроде как должен раюотать.
        На деле хрен с посным маслом.
         */
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageViewActivity.startActivity(DevDbDeviceActivity.this, imgUrls, adapterView.getSelectedItemPosition());
            }
        });

        CardView con1 = (CardView) findViewById(R.id.dev_db_activity_con1);
        con1.setVisibility(View.VISIBLE);

        LinearLayout con = (LinearLayout) findViewById(R.id.dev_db_activity_con);
        con.setVisibility(View.VISIBLE);

        TextView priceTV = (TextView) findViewById(R.id.dev_db_activity_price_tv);
        priceTV.setText(m_DevDbDevice.getInfo().Price);

    }

    private View.OnClickListener controlClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case 1:
//                    // not use
                    break;
                case 2:
                    showTest(m_DeviceId + "#comments");
                    break;
                case 3:
                    showTest(m_DeviceId + "#discussions");
                    break;
                case 4:
                    showTest(m_DeviceId + "#reviews");
                    break;
                case 5:
                    showTest(m_DeviceId + "#firmware");
                    break;
                case 6:
                    showTest(m_DeviceId + "#prices");
                    break;
                default:
                    break;
            }
        }
    };

    private void showTest(String link) {
        ExtUrl.showSelectActionDialog(mHandler, DevDbDeviceActivity.this, link);
    }

//    private void fill() throws IOException {
//        LazyAdapter adapter = new LazyAdapter(DevDbDeviceActivity.this,
//                m_DevDbDevice.getScreenshotUrls().toArray(new String[m_DevDbDevice.getScreenshotUrls().size()]));
//        gallery.setAdapter(adapter);
//
//        getSupportActionBar().setTitle(m_DevDbDevice.getInfo().Model);
//
//        List<String> testTest = m_DevDbDevice.getShortInfo();
//
////        fillRating();
//        TableLayout.LayoutParams rowparamsbig = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        rowparamsbig.setMargins(0, 12, 0, 0);
//        TableLayout.LayoutParams rowparams1 = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        rowparams1.setMargins(0, 4, 0, 0);
//        TableLayout.LayoutParams rowparams2 = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        rowparams2.setMargins(0, 0, 0, 4);
//        LayoutParams textviewparams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//
//        for (int i = 0; i < testTest.size(); i++) {
////            if (group.size() == 0) continue;
//            String text = testTest.get(i);
//            Log.i(TAG, "row ......" + text);
//            TableRow row = new TableRow(this);
//
//            TextView textView = createStyledTextView();
////            textView.setText(group.Title.trim());
//            textView.setText(text);
//            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
//            textView.setTypeface(Typeface.DEFAULT_BOLD);
//            textView.setTextSize(20);
//
//            row.addView(textView, textviewparams);
//            row.addView(new TextView(this), textviewparams);
//            infoTable.addView(row, rowparamsbig);
//
//            for (int j = 0; j < testObjs.length; j++) {
//                String text1 = testObjs[j];
//                TableRow row1 = new TableRow(this);
//                TableRow row2 = new TableRow(this);
//
//                TextView textView1 = createStyledTextView();
//                textView1.setText(text1);
//
//                textView1.setTextAppearance(this, android.R.attr.textAppearanceSmall);
//                textView1.setTypeface(Typeface.DEFAULT_BOLD);
//
//                row1.addView(textView1, textviewparams);
//
//                TextView textView2 = createStyledTextView();
//                textView2.setText(text1);
//
//                textView2.setEllipsize(null);
////                textView2.setOnClickListener(new View.OnClickListener() {
////                    public void onClick(View view) {
////                        Pattern p = Pattern.compile("http://(.*?)\"");
////                        Matcher m = p.matcher(item.Value);
////                        if (m.find()) {
////                            String url = "http://" + m.group(1);
////                            IntentActivity.tryShowUrl(DevDbDeviceActivity.this, mHandler, url, true, false);
////                        }
////                    }
////                });
////                textView2.setOnLongClickListener(new View.OnLongClickListener() {
////                    @Override
////                    public boolean onLongClick(View view) {
////                        Pattern p = Pattern.compile("http://(.*?)\"");
////                        Matcher m = p.matcher(item.Value);
////                        if (m.find()) {
////                            String url = "http://" + m.group(1);
////                            ExtUrl.showSelectActionDialog(mHandler, DevDbDeviceActivity.this, url);
////                        }
////                        return true;
////                    }
////                });
//                textView2.setTextAppearance(this, android.R.attr.textAppearanceSmall);
//
//                row2.addView(textView2, textviewparams);
//
//                infoTable.addView(row1, rowparams1);
//                infoTable.addView(row2, rowparams2);
//            }
//        }
//
//        if (!TextUtils.isEmpty(m_DevDbDevice.getInfo().Price)) {
//            TableRow row = new TableRow(this);
//
//
//            TextView textView = createStyledTextView();
//            textView.setText("Средняя цена:");
//            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
//            textView.setTypeface(Typeface.DEFAULT_BOLD);
//            textView.setTextColor(getResources().getColor(R.color.devdb_group_color));
//            row.addView(textView, textviewparams);
//
//            textView = createStyledTextView();
//            textView.setText(m_DevDbDevice.getInfo().Price);
//            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
//            textView.setTypeface(Typeface.DEFAULT_BOLD);
//            textView.setTextColor(getResources().getColor(R.color.devdb_group_color));
//            row.addView(textView, textviewparams);
//
//            infoTable.addView(row, rowparams1);
//        }
//
//    }

    private TextView createStyledTextView() {
        return (TextView) getLayoutInflater().inflate(R.layout.note_first_textview, null);
    }

//    private void fillRating() {
//        double rating = m_DevDbDevice.getInfo().Rating;
//        if (rating == -1) return;
//        for (int i = 0; i < 5; i++) {
//            ImageView img = new ImageView(this);
//            if (rating >= (i + 1)) {
//                img.setImageResource(R.drawable.star_full);
//            } else if (rating > i)
//                img.setImageResource(R.drawable.star_half);
//            else
//                img.setImageResource(R.drawable.star_none);
//            pnlRating.addView(img);
//        }
//    }

    public class LoadPageTask extends AsyncTask<String, String, Boolean> {


        private final MaterialDialog dialog;

        public LoadPageTask(Context context) {

            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .cancelable(false)
                    .content("Загрузка")
                    .build();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setContent(progress[0]);
        }

        private Throwable ex;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_DevDbDevice = new DevDbDevice();
                String pageBody = Client.getInstance().performGet(m_DeviceId);
                m_DevDbDevice.parse(pageBody);
                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
                this.cancel(true);
            }
        }

        protected void onCancelled() {
            super.onCancelled();

        }


        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                try {
//                    fill();
                    initUI();
                } catch (IOException e) {
                    AppLog.e(DevDbDeviceActivity.this, ex);
                }
            } else {
                if (ex != null)
                    AppLog.e(DevDbDeviceActivity.this, ex);
            }
        }

    }

}
