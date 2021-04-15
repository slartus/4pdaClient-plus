package org.softeg.slartus.forpdaplus.controls.imageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dmitriy.tarasov.android.intents.IntentUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.utils.SystemBarTintManager;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by isanechek on 24.01.16.
 */
public class ImgViewer extends AppCompatActivity implements PullBackLayout.Callback {
    private static final int LAYOUT = R.layout.img_viewer;
    private static final String IMAGE_URLS_KEY = "IMAGE_URLS_KEY";
    private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";
    private ArrayList<String> urls = new ArrayList<>();
    private SystemBarTintManager tintManager;
    private int index = 0;
    private ShareActionProvider shareActionProvider;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private boolean mVisible;
    private PullBackLayout backLayout;
    private HackyViewPager pager;
    private ImgAdapter adapter;
    private LinearLayout statusBar;

    public static void startActivity(Context context,
                                     String imageUrl) {
        Intent intent = new Intent(context, ImgViewer.class);
        ArrayList<String> urls = new ArrayList<>();
        urls.add(imageUrl);
        intent.putExtra(IMAGE_URLS_KEY, urls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

    }

    public static void startActivity(Context context,
                                     ArrayList<String> imageUrls, int selectedIndex) {
        Intent intent = new Intent(context, ImgViewer.class);
        intent.putExtra(IMAGE_URLS_KEY, imageUrls);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade, 0);
        setTheme(R.style.ImageViewTheme);
        setContentView(LAYOUT);

        backLayout = findViewById(R.id.image_viewer_pullBack);
        backLayout.setCallback(this);

        if (DevDbUtils.isKitKat()) {
            statusBar = findViewById(R.id.img_viewer_statusBar);
            statusBar.setVisibility(View.VISIBLE);
            statusBar.setMinimumHeight(getStatusBarHeight());
            statusBar.setBackgroundColor(ContextCompat.getColor(App.getContext(), R.color.background_toolbar));

            tintManager = new SystemBarTintManager(ImgViewer.this);
            if (tintManager.isNavBarTintEnabled()) {
                tintManager.setNavigationBarTintColor(ContextCompat.getColor(App.getContext(), R.color.background_toolbar));
            }

            if (tintManager.isStatusBarTintEnabled()) {
                tintManager.setStatusBarTintColor(ContextCompat.getColor(App.getContext(), R.color.background_toolbar));
            }
        }

        if (DevDbUtils.isAndroid5()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setNavigationBarColor(ContextCompat.getColor(App.getContext(), R.color.background_toolbar));
        }


        mVisible = true;


        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(IMAGE_URLS_KEY)) {
            urls = getIntent().getExtras().getStringArrayList(IMAGE_URLS_KEY);
        } else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY)) {
            urls = savedInstanceState.getStringArrayList(IMAGE_URLS_KEY);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY)) {
            index = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        } else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(SELECTED_INDEX_KEY)) {
            index = getIntent().getExtras().getInt(SELECTED_INDEX_KEY);
        }
        initUI();
        showImage(urls, index);
    }

    private void initUI() {
        pager = findViewById(R.id.img_viewer_pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateSubtitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter = new ImgAdapter();
        pager.setAdapter(adapter);
        pager.setCurrentItem(index);
        pager.setClipChildren(false);
        updateSubtitle(index);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(IMAGE_URLS_KEY, urls);
        outState.putInt(SELECTED_INDEX_KEY, pager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPullStart() {
        hide();
    }

    @Override
    public void onPull(@PullBackLayout.Direction int direction, float progress) {
        //
//        Animation animation = new AlphaAnimation(1, 0);
//        animation.setInterpolator(new AccelerateInterpolator());
//        animation.setStartOffset(1000);
//        animation.setDuration(1000);
//
//        AnimationSet set = new AnimationSet(false);
//        set.addAnimation(animation);
    }

    @Override
    public void onPullCancel(@PullBackLayout.Direction int direction) {
        //
    }

    @Override
    public void onPullComplete(@PullBackLayout.Direction int direction) {
        finish();
        overridePendingTransition(R.anim.fade, direction == PullBackLayout.DIRECTION_DOWN ? R.anim.activity_slide_down : R.anim.activity_slide_up);
    }

    private void updateSubtitle(int selectedPageIndex) {
        index = selectedPageIndex;
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(String.format("%d \\ %d", selectedPageIndex + 1, urls.size()));
        adapter.showImage(selectedPageIndex);
    }

    public void showImage(ArrayList<String> urls, int index) {
        this.urls = urls;
        this.index = index;
        adapter.notifyDataSetChanged();
        updateSubtitle(this.index);
    }

    class ImgAdapter extends PagerAdapter {
        SparseArray<View> views = new SparseArray<>();
        private final LayoutInflater inflater;
        private PhotoView photoView;
        private ProgressBar progress;
        private final ImageLoader imageLoader;
        private final DisplayImageOptions options;

        public ImgAdapter() {
            this.inflater = LayoutInflater.from(ImgViewer.this);
            imageLoader = ImageLoader.getInstance();

            options = App.getDefaultOptionsUIL()
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .considerExifParams(true)
                    .build();
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View imageLayout = inflater.inflate(R.layout.img_view_page, container, false);
            assert imageLayout != null;
            views.put(position, imageLayout);
            if (position == index)
                loadImage(imageLayout);

            container.addView(imageLayout, 0);
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

        // это костыль, который позже перепишу.
        // просто еще непонятно в каком виде этот класс будет жить дальше
//        @Override
//        public int getItemPosition(Object object) {
//            return POSITION_NONE;
//        }

        public void showImage(int position) {
            if (views.size() == 0)
                return;
            loadImage(views.get(position));
        }

        private void loadImage(View imageLayout) {
            assert imageLayout != null;
            progress = imageLayout.findViewById(R.id.progress);
            photoView = imageLayout.findViewById(R.id.photo_view);
            imageLoader.displayImage(urls.get(index), photoView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    super.onLoadingStarted(imageUri, view);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    progress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    progress.setVisibility(View.INVISIBLE);
                    delayedHide(1000);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    super.onLoadingCancelled(imageUri, view);
                    progress.setVisibility(View.INVISIBLE);
                }
            });

            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    toggle();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter.views != null) {
            for (int i = 0; i < adapter.views.size(); i++) {
                if (adapter.views.get(i) == null) continue;
                ((ImageView) adapter.views.get(i).findViewById(R.id.photo_view)).setImageBitmap(null);
            }
        }
        System.gc();
    }

    private String getCurrentUrl() {
        return urls.get(pager.getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_view, menu);

        MenuItem item = menu.findItem(R.id.share_it);
        shareActionProvider = new ShareActionProvider(ImgViewer.this);
        MenuItemCompat.setActionProvider(item, shareActionProvider);
        // Fetch and store ShareActionProvider

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                try {
                    DownloadsService.download(ImgViewer.this, getCurrentUrl(), false);

                } catch (Throwable ex) {
                    AppLog.e(ImgViewer.this, ex);
                }
                return true;

            case R.id.close:
                finish();
                return true;
            case R.id.share_it:
                Intent intent = IntentUtils.shareText(getString(R.string.link), urls.get(index));
                if (shareActionProvider != null) {
                    shareActionProvider.setShareIntent(intent);
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    private void toggle() {
        if (mVisible) {
            hide();
            if (statusBar != null) statusBar.animate().alpha(0);
        } else {
            show();
            if (statusBar != null) statusBar.animate().alpha(1);
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
        backLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            backLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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


    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
            if (statusBar != null) statusBar.animate().alpha(0);
        }
    };
}
