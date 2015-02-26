package org.softeg.slartus.forpdaplus.controls.imageview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;

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
        AlertDialog.Builder builder= new AlertDialogBuilder(getActivity())
                .setTitle(mTitle)
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Полная версия", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
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
                });
        View v = getActivity().getLayoutInflater().inflate(R.layout.image_view_dialog, null);
        m_PhotoView=(PhotoView)v.findViewById(R.id.iv_photo);
        m_PhotoView.setMaximumScale(10f);
        m_ProgressView=v.findViewById(R.id.progressBar);
        builder.setView(v);

        return builder.create();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new ImageViewFragment.DownloadImageTask(m_ProgressView, m_PhotoView, mPreviewUrl).execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PREVIEW_URL_KEY, mPreviewUrl);
        outState.putString(URL_KEY, mUrl);
        outState.putString(TITLE_KEY, mTitle);
    }
}
