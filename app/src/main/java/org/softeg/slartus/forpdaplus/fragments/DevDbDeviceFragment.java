package org.softeg.slartus.forpdaplus.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.classes.DevDbDevice;
import org.softeg.slartus.forpdaplus.classes.LazyGallery.LazyAdapter;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewActivity;
import org.softeg.slartus.forpdaplus.devdb.ParentFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by radiationx on 16.11.15.
 */
public class DevDbDeviceFragment extends GeneralFragment {
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
    View view;

    private String title;

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public View getView(){
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeArrow();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dev_db_activity_x, container, false);
        //        pnlRating = (LinearLayout) findViewById(R.id.pnlRating);
        gallery = (Gallery) view.findViewById(R.id.gallery);
        setHasOptionsMenu(true);
//        infoTable = (TableLayout) findViewById(R.id.infoTable);
        Bundle extras = getArguments();

        assert extras != null;
        m_DeviceId = extras.getString(DEVICE_ID_KEY);
        loadPage();
        return view;
    }


    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(DEVICE_ID_KEY, m_DeviceId);
        super.onSaveInstanceState(outState);
    }
/*
    @Override
    protected void onRestoreInstanceState(android.os.Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            m_DeviceId = savedInstanceState.getString(DEVICE_ID_KEY, m_DeviceId);
        }
    }
    */
    public static DevDbDeviceFragment newInstance(Bundle args){
        DevDbDeviceFragment fragment = new DevDbDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public static void showDevice(String deviceId) {
        Bundle args = new Bundle();
        args.putString(DEVICE_ID_KEY, deviceId);
        MainActivity.addTab(deviceId, newInstance(args));
    }

    public void loadPage() {
        new LoadPageTask(getContext()).execute();
    }
    Menu menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add("Браузер")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(m_DeviceId));

                        startActivity(Intent.createChooser(marketIntent, "Выберите"));
                        return true;
                    }
                });

        this.menu = menu;
    }


    @Override
    public void onDestroy() {
        if (gallery != null)
            gallery.setAdapter(null);
        super.onDestroy();
    }

    private int convertToDp(int p){
        return (int) (p*getResources().getDisplayMetrics().density + 0.5f);
    }
    private void initUI() throws IOException {
        title = m_DevDbDevice.getInfo().Model;
        getMainActivity().setTitle(title);
        App.getInstance().getTabByTag(getTag()).setTitle(m_DevDbDevice.getInfo().Model);
        TabDrawerMenu.notifyDataSetChanged();

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

        LinearLayout scrollView = (LinearLayout) getView().findViewById(R.id.dev_db_btn_con);
        List<String> btnControls = m_DevDbDevice.getControls();
        if (btnControls != null) {
            for (int i = 1; i < btnControls.size(); i++) //начинается с одного, потому что кнока "характеристики" нам не нужна
            {
                String titleControl = btnControls.get(i);
                Button control = new Button(getContext());
                control.setText(titleControl);
                control.setId(i + 1);
                control.setOnClickListener(controlClick);
                control.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                scrollView.addView(control);
            }
        }


        LinearLayout specList = (LinearLayout) getView().findViewById(R.id.spec_list);
        for(Map list:oneList){
            LinearLayout item = new LinearLayout(getContext());
            TextView line1 = new TextView(getContext());
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(convertToDp(16), convertToDp(12), convertToDp(16), convertToDp(12));
            line1.setText(list.get("line1")+" "+list.get("line2").toString());
            item.addView(line1);
            specList.addView(item);
        }
        //ListView list = (ListView) getView().findViewById(R.id.list);
        //SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), oneList, LAYOUT, KEYS, IDS);
        //list.setAdapter(simpleAdapter);

        LazyAdapter adapter = new LazyAdapter(getMainActivity(),
                m_DevDbDevice.getScreenshotUrls().toArray(new String[m_DevDbDevice.getScreenshotUrls().size()]));
        gallery.setAdapter(adapter);

        /*
        перенес сюда, ибо в onCreate imgUrls return null.
        Зачем так сделал? startActivity может принять список ссылок и viewpager вроде как должен раюотать.
        На деле хрен с посным маслом.
         */
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageViewActivity.startActivity(getMainActivity(), imgUrls, adapterView.getSelectedItemPosition());
            }
        });
        HorizontalScrollView con1 = (HorizontalScrollView) getView().findViewById(R.id.dev_db_activity_con1);
        con1.setVisibility(View.VISIBLE);

        LinearLayout con = (LinearLayout) getView().findViewById(R.id.dev_db_activity_con);
        con.setVisibility(View.VISIBLE);

        TextView priceTV = (TextView) getView().findViewById(R.id.dev_db_activity_price_tv);
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
//                    showTest(m_DeviceId + "#comments");
                    showFrag(m_DeviceId, title, 0);
                    break;
                case 3:
//                    showTest(m_DeviceId + "#discussions");
                    showFrag(m_DeviceId, title, 1);
                    break;
                case 4:
//                    showTest(m_DeviceId + "#reviews");
                    showFrag(m_DeviceId, title, 2);
                    break;
                case 5:
//                    showTest(m_DeviceId + "#firmware");
                    showFrag(m_DeviceId, title, 3);
                    break;
                case 6:
//                    showTest(m_DeviceId + "#prices");
                    showFrag(m_DeviceId, title, 4);
                    break;
                default:
                    break;
            }
        }
    };

    private void showTest(String link) {
        ExtUrl.showSelectActionDialog(mHandler, getMainActivity(), link);
    }

    private void showFrag(String id, String title, int position) {
        ParentFragment.showDevice1(id, title, position);
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
                    AppLog.e(getMainActivity(), ex);
                }
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
            }
        }

    }

}
