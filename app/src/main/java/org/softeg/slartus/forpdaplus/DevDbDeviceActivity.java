package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.classes.DevDbDevice;
import org.softeg.slartus.forpdaplus.classes.LazyGallery.LazyAdapter;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewActivity;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:05
 */
public class DevDbDeviceActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    private String m_DeviceId;
    private Gallery gallery;
    private TableLayout infoTable;
    private DevDbDevice m_DevDbDevice;
    private TextView txtModel;
    private LinearLayout pnlRating;
    private static final String DEVICE_ID_KEY = "DeviceId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dev_db_device_activity);
        pnlRating = (LinearLayout) findViewById(R.id.pnlRating);
        gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String imgUrl = adapterView.getItemAtPosition(i).toString();
                ImageViewActivity.startActivity(DevDbDeviceActivity.this, imgUrl.replace("p.jpg", "n.jpg"));
            }
        });
        txtModel = (TextView) findViewById(R.id.txtModel);
        infoTable = (TableLayout) findViewById(R.id.infoTable);
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
    private void fill() throws IOException {
        LazyAdapter adapter = new LazyAdapter(DevDbDeviceActivity.this,
                m_DevDbDevice.getScreenshotUrls().toArray(new String[m_DevDbDevice.getScreenshotUrls().size()]));
        gallery.setAdapter(adapter);

        txtModel.setText(m_DevDbDevice.getInfo().Model);
        fillRating();
        TableLayout.LayoutParams rowparams = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        rowparams.setMargins(5, 5, 5, 5);
        LayoutParams textviewparams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        for (DevDbDevice.InfoGroup group : m_DevDbDevice.getInfo()) {
            if (group.size() == 0) continue;
            TableRow row = new TableRow(this);

            TextView textView = createStyledTextView();
            textView.setText(group.Title.trim());
            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            row.addView(textView, textviewparams);
            row.addView(new TextView(this), textviewparams);
            infoTable.addView(row, rowparams);

            for (final DevDbDevice.InfoItem item : group) {
                TableRow row1 = new TableRow(this);

                TextView textView1 = createStyledTextView();
                textView1.setText(Html.fromHtml(item.Title.trim()));

                textView1.setTextAppearance(this, android.R.attr.textAppearanceSmall);

                row1.addView(textView1, textviewparams);

                TextView textView2 = createStyledTextView();
                textView2.setText(Html.fromHtml(item.Value));

                textView2.setEllipsize(null);
                textView2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Pattern p = Pattern.compile("http://(.*?)\"");
                        Matcher m = p.matcher(item.Value);
                        if (m.find()) {
                            String url = "http://" + m.group(1);
                            IntentActivity.tryShowUrl(DevDbDeviceActivity.this, mHandler, url, true, false);
                        }
                    }
                });
                textView2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Pattern p = Pattern.compile("http://(.*?)\"");
                        Matcher m = p.matcher(item.Value);
                        if (m.find()) {
                            String url = "http://" + m.group(1);
                            ExtUrl.showSelectActionDialog(mHandler, DevDbDeviceActivity.this, url);
                        }
                        return true;
                    }
                });
                textView2.setTextAppearance(this, android.R.attr.textAppearanceSmall);
                textView2.setTypeface(Typeface.DEFAULT_BOLD);
                row1.addView(textView2, textviewparams);

                infoTable.addView(row1, rowparams);
            }
        }

        if (!TextUtils.isEmpty(m_DevDbDevice.getInfo().Price)) {
            TableRow row = new TableRow(this);


            TextView textView = createStyledTextView();
            textView.setText("Средняя цена:");
            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(getResources().getColor(R.color.devdb_group_color));
            row.addView(textView, textviewparams);

            textView = createStyledTextView();
            textView.setText(m_DevDbDevice.getInfo().Price);
            textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(getResources().getColor(R.color.devdb_group_color));
            row.addView(textView, textviewparams);

            infoTable.addView(row, rowparams);
        }

    }

    private TextView createStyledTextView() {
        return (TextView) getLayoutInflater().inflate(R.layout.themed_textview, null);
    }

    private void fillRating() {
        double rating = m_DevDbDevice.getInfo().Rating;
        if (rating == -1) return;
        for (int i = 0; i < 5; i++) {
            ImageView img = new ImageView(this);
            if (rating >= (i + 1)) {
                img.setImageResource(R.drawable.star_full);
            } else if (rating > i)
                img.setImageResource(R.drawable.star_half);
            else
                img.setImageResource(R.drawable.star_none);
            pnlRating.addView(img);
        }
    }

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
                    fill();
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
