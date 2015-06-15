package org.softeg.slartus.forpdaplus.controls.imageview;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;


/*
 * Created by slartus on 14.10.2014.
 */
public class ImageViewActivity extends ActionBarActivity {
    private static final String IMAGE_URLS_KEY = "IMAGE_URLS_KEY";
    private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";


    public static void startActivity(Context context,
                                     String imageUrl) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(imageUrl);
        intent.putExtra(IMAGE_URLS_KEY, urls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

    }

    public static void startActivity(Context context,
                                     ArrayList<String> imageUrls, int selectedIndex) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.putExtra(IMAGE_URLS_KEY, imageUrls);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(App.getInstance().getThemeStyleResID());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("statusbarTransparent",false)) {
            if (Integer.valueOf(android.os.Build.VERSION.SDK) > 19) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }else{
            if (Integer.valueOf(android.os.Build.VERSION.SDK) == 19) {
                SystemBarTintManager tintManager = new SystemBarTintManager(this);
                tintManager.setStatusBarTintEnabled(true);
                if (App.getInstance().getCurrentThemeName().equals("white")) {
                    tintManager.setTintColor(getResources().getColor(R.color.statusBar_wh));
                } else if (App.getInstance().getCurrentThemeName().equals("black")) {
                    tintManager.setTintColor(getResources().getColor(R.color.statusBar_bl));
                }
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
        setContentView(R.layout.image_view_activity);

        ImageViewFragment fragment =
                (ImageViewFragment) getSupportFragmentManager().findFragmentById(R.id.image_view_fragment);


        ArrayList<String> urls = new ArrayList<>();

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(IMAGE_URLS_KEY))
            urls = getIntent().getExtras().getStringArrayList(IMAGE_URLS_KEY);
        else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY))
            urls = savedInstanceState.getStringArrayList(IMAGE_URLS_KEY);

        int index = 0;

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY))
            index = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(SELECTED_INDEX_KEY))
            index = getIntent().getExtras().getInt(SELECTED_INDEX_KEY);

        fragment.showImage(urls, index);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
