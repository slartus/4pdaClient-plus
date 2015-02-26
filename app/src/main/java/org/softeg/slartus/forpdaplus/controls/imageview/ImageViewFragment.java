package org.softeg.slartus.forpdaplus.controls.imageview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.softeg.slartus.forpdaplus.BaseFragment;
import org.softeg.slartus.forpdaplus.HttpHelper;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

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

        private void loadImage(View imageLayout) {
            if (m_PhotoView != null) {
                m_PhotoView.setImageDrawable(null);
            }
            assert imageLayout != null;
            final View progressView = imageLayout.findViewById(R.id.progressBar);
            m_PhotoView = (PhotoView) imageLayout.findViewById(R.id.iv_photo);
            m_PhotoView.setMaximumScale(10f);

            new DownloadImageTask(progressView, m_PhotoView, urls.get(mSelectedIndex)).execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(IMAGE_URLS_KEY, urls);
        outState.putInt(SELECTED_INDEX_KEY, mViewPager.getCurrentItem());
    }


    private String getCurrentUrl() {
        return urls.get(mViewPager.getCurrentItem());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.image_view, menu);
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
        }
        return false;
    }


    public static class DownloadImageTask extends AsyncTask<String, String, String> {

        private View mProgressView;
        private ImageView mImageView;
        private String mImageUrl;

        public DownloadImageTask(View progressView, ImageView imageView, String imageUrl) {

            mProgressView = progressView;
            mImageView = imageView;
            mImageUrl = imageUrl;
        }

        @Override
        protected String doInBackground(String... forums) {
            try {
                return downloadImage(mImageUrl);
            } catch (Throwable e) {

                ex = e;
                return null;
            }
        }


        protected void onPreExecute() {
            try {
                mProgressView.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }

        private Throwable ex;

        protected void onPostExecute(final String success) {
            try {
                mProgressView.setVisibility(View.GONE);
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
            if (ex != null) {
                AppLog.e(null, ex);
                return;
            }
            if (isCancelled())
                return;
            if (!TextUtils.isEmpty(success))
                mImageView.setImageURI(Uri.parse(success));

        }

        private String downloadImage(String imageUrl) throws Exception {
            HttpHelper httpHelper = new HttpHelper();
            try {
                File file = File.createTempFile("temp_image", ".tmp");


                long total = 0;


                HttpEntity entity = httpHelper.getDownloadResponse(imageUrl, total);


                int count;


                InputStream in = entity.getContent();
                FileOutputStream output = new FileOutputStream(file, true);

                byte data[] = new byte[1024];
                try {
                    while ((count = in.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                } finally {
                    output.flush();
                    output.close();
                    in.close();
                }
                return file.getPath();
            } finally {
                httpHelper.close();
            }

        }
    }
}

