package org.softeg.slartus.forpdaplus.devdb.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.LazyGallery.LazyAdapter;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.model.SpecModel;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by radiationx on 05.01.16.
 */
public class SpecFragment extends BaseDevDbFragment implements FLifecycleUtil {
    private ArrayList<String> imgUrls;
    View view;
    private static final int LAYOUT = R.layout.dev_db_activity_x;

    private SpecModel data;

    public static SpecFragment newInstance(Context context, SpecModel data) {
        SpecFragment f = new SpecFragment();
        f.setContext(context);
        f.setTitle(App.getContext().getString(R.string.specifications));
        f.setData(data);
        return f;
    }
    public void setData(SpecModel data){
        this.data = data;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        if (data!=null) {
            try {
                initUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    private int convertToDp(int p){
        return (int) (p*getResources().getDisplayMetrics().density + 0.5f);
    }

    private void initUI() throws IOException {
        imgUrls =data.getGalleryLinks();
        LinearLayout specList = view.findViewById(R.id.spec_list);
        for(Element element:data.getSpecTable()){
            if(element.text().isEmpty()) continue;
            TextView title = new TextView(getContext());
            title.setPadding(convertToDp(16), convertToDp(16), convertToDp(16), convertToDp(0));
            title.setTextSize(20);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setText(element.select(".specifications-title").first().text());
            specList.addView(title);
            for(Element row:element.select(".specifications-row")){
                TextView rowText = new TextView(getContext());
                rowText.setPadding(convertToDp(16), convertToDp(4), convertToDp(16), convertToDp(8));
                rowText.setText(Html.fromHtml(row.select("dt").first().text() + " <b>" + row.select("dd").first().text() + "</b>"));
                specList.addView(rowText);
            }
        }

        Gallery gallery = view.findViewById(R.id.gallery);
        LazyAdapter adapter = new LazyAdapter(getActivity(), data.getGalleryImages().toArray(new String[data.getGalleryImages().size()]));
        gallery.setAdapter(adapter);

        /*
        перенес сюда, ибо в onCreate imgUrls return null.
        Зачем так сделал? startActivity может принять список ссылок и viewpager вроде как должен раюотать.
        На деле хрен с посным маслом.
         */
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                ImageViewActivity.startActivity(getActivity(), imgUrls, adapterView.getSelectedItemPosition());
                ImgViewer.startActivity(getActivity(), imgUrls, adapterView.getSelectedItemPosition());
            }
        });
        /*HorizontalScrollView con1 = (HorizontalScrollView) getView().findViewById(R.id.dev_db_activity_con1);
        con1.setVisibility(View.VISIBLE);*/

        /*LinearLayout con = (LinearLayout) getView().findViewById(R.id.dev_db_activity_con);
        con.setVisibility(View.VISIBLE);*/

        if(data.getPrice()!=null){
            TextView priceTV = view.findViewById(R.id.price);
            priceTV.setVisibility(View.VISIBLE);
            priceTV.setText(data.getPrice()+" Р");
        }


    }

/*    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onViewCreated(view, savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);


    }*/

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
    }

    @Override
    public void onDestroy() {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onDestroy();
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        data = null;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
