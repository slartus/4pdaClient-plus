package org.softeg.slartus.forpdaplus.controls.imageview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dmitriy.tarasov.android.intents.IntentUtils;
import com.goka.flickableview.FlickableImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.Transformation;

import org.apache.http.HttpResponse;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.HttpHelper;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.utils.SystemBarTintManager;
//import org.softeg.slartus.forpdaplus.utils.ui.SystemUiHelper;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

//import uk.co.senab.photoview.PhotoView;

/*
 * Created by slartus on 14.10.2014.
 */
public class ImageViewActivity extends AppCompatActivity {
    private static final String IMAGE_URLS_KEY = "IMAGE_URLS_KEY";
    private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";

    private SystemBarTintManager mTintManager;
    private ArrayList<String> urls = new ArrayList<>();
    private SamplePagerAdapter mPageAdapter;
    private static final String ISLOCKED_ARG = "isLocked";
    private ViewPager mViewPager;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            flickableIV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


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
//        super.setTheme(R.style.ImageViewTheme);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.image_view_activity);

        mVisible = true;
        initViewPager(savedInstanceState);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            mTintManager = new SystemBarTintManager(this);
            if (mTintManager.isNavBarTintEnabled()) {
                mTintManager.setNavigationBarTintColor(this.getResources().getColor(R.color.background_toolbar));
            }

            if (mTintManager.isStatusBarTintEnabled()) {
                mTintManager.setStatusBarTintColor(this.getResources().getColor(R.color.background_toolbar));
            }
        }


//        ImageViewFragment fragment =
//                (ImageViewFragment) getSupportFragmentManager().findFragmentById(R.id.image_view_fragment);


//        ArrayList<String> urls = new ArrayList<>();

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(IMAGE_URLS_KEY))
            urls = getIntent().getExtras().getStringArrayList(IMAGE_URLS_KEY);
        else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY))
            urls = savedInstanceState.getStringArrayList(IMAGE_URLS_KEY);

        int index = 0;

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY))
            index = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(SELECTED_INDEX_KEY))
            index = getIntent().getExtras().getInt(SELECTED_INDEX_KEY);

        if (urls.size() > 0) {
            showImage(urls, index);
        }

//        fragment.showImage(urls, index);
    }

    private void initViewPager(Bundle savedInstanceState) {
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                updateSubtitle(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mPageAdapter = new SamplePagerAdapter();
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(mSelectedIndex);
        updateSubtitle(mSelectedIndex);
        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }
    }

    private int mSelectedIndex = 0;

    private void updateSubtitle(int selectedPageIndex) {
        mSelectedIndex = selectedPageIndex;
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(String.format("%d \\ %d", selectedPageIndex + 1, urls.size()));
        mPageAdapter.showImage(selectedPageIndex);
    }

    private FlickableImageView flickableIV;

    public void showImage(ArrayList<String> urls, int index) {
        this.urls = urls;
        mSelectedIndex = index;
        mPageAdapter.notifyDataSetChanged();
        updateSubtitle(mSelectedIndex);
    }

    private class SamplePagerAdapter extends PagerAdapter {
        SparseArray<View> views = new SparseArray<>();
        private LayoutInflater inflater;
//        private SystemUiHelper mUiHelper = new SystemUiHelper(ImageViewActivity.this, SystemUiHelper.FLAG_IMMERSIVE_STICKY, 0);
        private static final int HIDE_DELAY = 300;

        public SamplePagerAdapter() {
            inflater = LayoutInflater.from(ImageViewActivity.this);
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public View instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.image_view_page, view, false);
            assert imageLayout != null;

            views.put(position, imageLayout);
            if (position == mSelectedIndex)
                loadImage(imageLayout);

            view.addView(imageLayout, 0);
            views.put(position, imageLayout);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }



        public void showImage(int position) {
            if (views.size() == 0)
                return;
            View imageLayout = views.get(position);
            loadImage(imageLayout);

        }

        private void loadImage(final View imageLayout) {
            if (flickableIV != null) {
                PicassoTools.clearCache(Picasso.with(inflater.getContext()));
                flickableIV.setImageDrawable(null);
            }

            assert imageLayout != null;
            final ProgressBar progressView = (ProgressBar) imageLayout.findViewById(R.id.progressBar);
            flickableIV = (FlickableImageView) imageLayout.findViewById(R.id.fiv);

            flickableIV.setOnFlickListener(new FlickableImageView.OnFlickableImageViewFlickListener() {
                @Override
                public void onStartFlick() {
                    if (getSupportActionBar().isShowing()) {
                        hide();
                    }
                }

                @Override
                public void onFinishFlick() {
                    flickableIV.setVisibility(View.GONE);
                    finish();
                    overridePendingTransition(0, 0);
                }
            });

            flickableIV.setOnSingleTapListener(new FlickableImageView.OnFlickableImageViewSingleTapListener() {
                @Override
                public void onSingleTapConfirmed() {
//                    mUiHelper.toggle();
                    toggle();
                }
            });

            flickableIV.setOnZoomListener(new FlickableImageView.OnFlickableImageViewZoomListener() {
                @Override
                public void onStartZoom() {

                }

                @Override
                public void onBackFromMinScale() {
                }
            });


            Transformation transformation = new Transformation() {

                @Override
                public Bitmap transform(Bitmap source) {
                    double maxSize = getMaxTextureSize()*0.75;
                    int imageWidth = source.getWidth();
                    int imageHeight = source.getHeight();
                    double scale = 1;
                    if(imageWidth>maxSize){
                        scale = maxSize/imageWidth;
                        imageWidth = (int)(imageWidth*scale);
                        imageHeight = (int)(imageHeight*scale);
                    }
                    if(imageHeight>maxSize){
                        scale = maxSize/imageHeight;
                        imageWidth = (int)(imageWidth*scale);
                        imageHeight = (int)(imageHeight*scale);
                    }
                    Bitmap result = Bitmap.createScaledBitmap(source, imageWidth, imageHeight, false);
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle();
                    }
                    return result;
                }

                @Override
                public String key() {
                    return "transformation" + " desiredWidth";
                }
            };

            Picasso.Builder builder = new Picasso.Builder(App.getInstance());
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    progressView.setVisibility(View.GONE);
                    Toast.makeText(App.getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
            builder.downloader(new Downloader() {
                @Override
                public Response load(Uri uri, int networkPolicy) throws IOException {
                    HttpResponse httpResponse = new HttpHelper().getDownloadResponse(uri.toString(), 0);


                    return new Response(httpResponse.getEntity().getContent(), false, httpResponse.getEntity().getContentLength());
                }

                @Override
                public void shutdown() {

                }
            });
            builder.build()
                    .load(urls.get(mSelectedIndex))
                    .transform(transformation)
                    .error(R.drawable.no_image)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(flickableIV, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressView.setVisibility(View.GONE);
                            MaterialImageLoading.animate(flickableIV).setDuration(2000).start();
//                            mUiHelper.delayHide(HIDE_DELAY);
                            delayedHide(HIDE_DELAY);

                        }

                        @Override
                        public void onError() {
                        }
                    });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flickableIV.setImageDrawable(null);
    }
    public static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(IMAGE_URLS_KEY, urls);
        outState.putInt(SELECTED_INDEX_KEY, mViewPager.getCurrentItem());
    }


    private String getCurrentUrl() {
        return urls.get(mViewPager.getCurrentItem());
    }

    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_view, menu);

        MenuItem item = menu.findItem(R.id.share_it);
        mShareActionProvider = new ShareActionProvider(ImageViewActivity.this);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
        // Fetch and store ShareActionProvider

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.download:
                try {
                    DownloadsService.download(ImageViewActivity.this, getCurrentUrl(), false);

                } catch (Throwable ex) {
                    AppLog.e(ImageViewActivity.this, ex);
                }
                return true;

            case R.id.close:
                finish();
                return true;
            case R.id.share_it:
                Intent intent = IntentUtils.shareText("Ссылка", urls.get(mSelectedIndex));
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(intent);
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        flickableIV.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
