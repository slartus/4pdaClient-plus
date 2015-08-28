package org.softeg.slartus.forpdaplus.controls.imageview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dmitriy.tarasov.android.intents.IntentUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.Transformation;

import org.apache.http.HttpResponse;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BaseFragment;
import org.softeg.slartus.forpdaplus.HttpHelper;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import uk.co.senab.photoview.PhotoView;

//import android.widget.ShareActionProvider;

/*
 * Created by slinkin on 19.02.2015.
 */
public class ImageViewFragment extends BaseFragment {
    private static final String IMAGE_URLS_KEY = "IMAGE_URLS_KEY";
    private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";


    private ArrayList<String> urls = new ArrayList<>();
    private SamplePagerAdapter mPageAdapter;
    private static final String ISLOCKED_ARG = "isLocked";

    private ViewPager mViewPager;

    public static void startActivity(Context context,
                                     String imageUrl) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        ArrayList<String> urls = new ArrayList<>();
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


    public Boolean onBackPressed() {
        if (m_PhotoView != null && m_PhotoView.getScale() > 1) {
            m_PhotoView.setScale(1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_view_fragment, container, false);
        assert v != null;

        mViewPager = (HackyViewPager) v.findViewById(R.id.view_pager);


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

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (urls.size() > 0 && getArguments() != null) {
            showImage(urls, mSelectedIndex);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        urls.clear();
        if (getArguments() != null && getArguments().containsKey(IMAGE_URLS_KEY))
            urls = getArguments().getStringArrayList(IMAGE_URLS_KEY);
        else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY))
            urls = savedInstanceState.getStringArrayList(IMAGE_URLS_KEY);
        // Any implementation of ImageView can be used!

        mSelectedIndex = 0;

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY))
            mSelectedIndex = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        else if (getArguments() != null && getArguments().containsKey(SELECTED_INDEX_KEY))
            mSelectedIndex = getArguments().getInt(SELECTED_INDEX_KEY);


    }

    private int mSelectedIndex = 0;

    private void updateSubtitle(int selectedPageIndex) {
        mSelectedIndex = selectedPageIndex;
        if (getActivity().getActionBar() != null)
            getActivity().getActionBar().setSubtitle(String.format("%d \\ %d", selectedPageIndex + 1, urls.size()));
        mPageAdapter.showImage(selectedPageIndex);
    }

    private PhotoView m_PhotoView;

    public void showImage(ArrayList<String> urls, int index) {
        this.urls = urls;
        mSelectedIndex = index;
        mPageAdapter.notifyDataSetChanged();
        updateSubtitle(mSelectedIndex);
    }


    private class SamplePagerAdapter extends PagerAdapter {
        SparseArray<View> views = new SparseArray<>();
        private LayoutInflater inflater;

        public SamplePagerAdapter() {
            inflater = LayoutInflater.from(getActivity());

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
            if (m_PhotoView != null) {
                PicassoTools.clearCache(Picasso.with(inflater.getContext()));
                m_PhotoView.setImageDrawable(null);
            }
            assert imageLayout != null;
            final View progressView = imageLayout.findViewById(R.id.progressBar);
            m_PhotoView = (PhotoView) imageLayout.findViewById(R.id.iv_photo);

            m_PhotoView.setMaximumScale(10f);

            progressView.setVisibility(View.VISIBLE);

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
                    .into(m_PhotoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressView.setVisibility(View.GONE);
                        }
                    });
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        m_PhotoView.setImageBitmap(null);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.image_view, menu);

        MenuItem item = menu.findItem(R.id.share_it);
        mShareActionProvider = new ShareActionProvider(getActivity());
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
        // Fetch and store ShareActionProvider


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.download:
                try {
                    DownloadsService.download(getActivity(), getCurrentUrl(), false);

                } catch (Throwable ex) {
                    AppLog.e(getActivity(), ex);
                }
                return true;

            case R.id.close:


                getActivity().finish();
                return true;
            case R.id.share_it:
                Intent intent = IntentUtils.shareText("Ссылка", urls.get(mSelectedIndex));
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(intent);
                }
                return true;
        }
        return false;
    }



}

