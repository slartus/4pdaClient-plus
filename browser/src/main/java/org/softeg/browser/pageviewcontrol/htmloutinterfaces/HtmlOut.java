package org.softeg.browser.pageviewcontrol.htmloutinterfaces;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.softeg.slartus.yarportal.common.selectPageDialog;
import org.softeg.slartus.yarportal.imageview.ImageViewActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Created by slinkin on 06.10.2014.
 */
public class HtmlOut implements IHtmlOut {
    public static final String NAME = "HTMLOUT";
    private WeakReference<IHtmlOutListener> mControl;

    public HtmlOut(WeakReference<IHtmlOutListener> control) {

        mControl = control;
    }

    protected Context getContext() {
        return mControl.get().getContext();
    }

    protected FragmentActivity getActivity() {
        return mControl.get().getActivity();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean hasRequestCode(int requestCode) {
        return false;
    }

    @JavascriptInterface
    public void openImagesList(final String htmlText, final int selectedIndex) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(htmlText==null)
                    return;
                Matcher m= Pattern.compile("<a [^>]*href=\"([^\"]*(?:png|jpg|jpeg|gif))\"",Pattern.CASE_INSENSITIVE)
                        .matcher(htmlText.toString());
                ArrayList<String> images=new ArrayList<String>();
                while (m.find()){
                    images.add(m.group(1));
                }
                if(images.size()>0){
                    ImageViewActivity.startActivity(getActivity(), images, selectedIndex);
                }
                else{
                    Toast.makeText(getActivity(),"Не найдены ссылки на изображения в тексте",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mControl.get().nextPage();
            }
        });
    }


    @JavascriptInterface
    public void prevPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mControl.get().prevPage();
            }
        });
    }

    @JavascriptInterface
    public void firstPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mControl.get().firstPage();
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mControl.get().lastPage();
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selectPageDialog.show(getContext(),mControl.get().getCurrentPage(),mControl.get().getPagesCount(),new selectPageDialog.OnSelectPageListener() {
                    @Override
                    public void onSelectPage(int page) {
                        mControl.get().loadPage(page);
                    }
                });

            }
        });

    }

}
