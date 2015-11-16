package org.softeg.slartus.forpdaplus.controls.imageview;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;

/*
 * Created by slartus on 14.10.2014.
 */
public class ImageViewActivity extends AppCompatActivity{
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
        super.setTheme(R.style.ImageViewTheme);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
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
