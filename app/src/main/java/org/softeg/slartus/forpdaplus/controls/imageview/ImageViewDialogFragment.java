package org.softeg.slartus.forpdaplus.controls.imageview;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.hosthelper.HostHelper;

import java.net.URI;
import java.net.URISyntaxException;

import uk.co.senab.photoview.PhotoView;

/*
 * Created by slinkin on 19.02.2015.
 */
public class ImageViewDialogFragment extends DialogFragment {
    public static final String PREVIEW_URL_KEY = "PREVIEW_URL_KEY";
    public static final String URL_KEY = "URL_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";

    private PhotoView m_PhotoView;
    private View m_ProgressView;
    private String mPreviewUrl;
    private String mUrl;
    private String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        if (getArguments() != null){
            mPreviewUrl=getArguments().getString(PREVIEW_URL_KEY);
            mUrl=getArguments().getString(URL_KEY);
            mTitle=getArguments().getString(TITLE_KEY);
        }

        else if (savedInstanceState != null) {
            mPreviewUrl=savedInstanceState.getString(PREVIEW_URL_KEY);
            mUrl=savedInstanceState.getString(URL_KEY);
            mTitle=savedInstanceState.getString(TITLE_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.image_view_dialog, null);
        m_PhotoView= v.findViewById(R.id.iv_photo);
        //m_PhotoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        m_PhotoView.setMaximumScale(10f);
        m_ProgressView=v.findViewById(R.id.progressBar);
        MaterialDialog builder= new MaterialDialog.Builder(getActivity())
                .title(mTitle)
                .customView(v,false)
                .negativeText(R.string.close)
                .positiveText(R.string.full_size)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String url = mUrl;
                        try {
                            URI uri = new URI(mUrl);
                            if (!uri.isAbsolute())
                                url = "https://"+ HostHelper.getHost() + url;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
//                        ImageViewActivity.startActivity(getActivity(), url);
                        ImgViewer.startActivity(getActivity(), url);
                    }
                }).build();

        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        builder.show();
        return builder;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try{
            m_ProgressView.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(mPreviewUrl, m_PhotoView, new SimpleImageLoadingListener(){
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    m_ProgressView.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    m_ProgressView.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    m_ProgressView.setVisibility(View.GONE);
                }
            });
        }catch (Throwable ex){
            AppLog.e(getActivity(),ex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PREVIEW_URL_KEY, mPreviewUrl);
        outState.putString(URL_KEY, mUrl);
        outState.putString(TITLE_KEY, mTitle);
    }
}
