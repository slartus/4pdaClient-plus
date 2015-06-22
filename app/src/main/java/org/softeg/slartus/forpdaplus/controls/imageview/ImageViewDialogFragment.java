package org.softeg.slartus.forpdaplus.controls.imageview;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import org.apache.http.HttpResponse;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.HttpHelper;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.topicview.HtmloutWebInterface;
import org.softeg.slartus.forpdaplus.topicview.HtmloutWebInterfaceForOld;

import java.io.IOException;
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
        m_PhotoView=(PhotoView)v.findViewById(R.id.iv_photo);
        //m_PhotoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        m_PhotoView.setMaximumScale(10f);
        if(android.os.Build.VERSION.SDK_INT < 17){
            m_PhotoView.setZoomable(false);
        }
        m_ProgressView=v.findViewById(R.id.progressBar);
        MaterialDialog builder= new MaterialDialog.Builder(getActivity())
                .title(mTitle)
                .customView(v,false)
                .negativeText("Закрыть")
                .positiveText("Полная версия")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String url = mUrl;
                        try {
                            URI uri = new URI(mUrl);
                            if (!uri.isAbsolute())
                                url = "http://4pda.ru" + url;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        ImageViewActivity.startActivity(getActivity(), url);
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
            PicassoTools.clearCache(Picasso.with(App.getInstance()));
            m_ProgressView.setVisibility(View.VISIBLE);

            Picasso.Builder builder = new Picasso.Builder(App.getInstance());
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    m_ProgressView.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();

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
                    .load(mPreviewUrl)
                    .error(R.drawable.no_image)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(m_PhotoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            m_ProgressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
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
