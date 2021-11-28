package org.softeg.slartus.forpdaplus.fragments.topic;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.hosthelper.HostHelper;

/**
 * Created by radiationx on 15.11.15.
 */
public class ThemeCurator {
    private final FragmentActivity mTopicActivity;
    private CuratorFragment context;
    private final String topicId;

    public ThemeCurator(FragmentActivity topicActivity, String topicId) {
        mTopicActivity = topicActivity;
        this.topicId = topicId;
    }
    public ThemeCurator(FragmentActivity topicActivity, CuratorFragment context, String topicId) {
        mTopicActivity = topicActivity;
        this.context = context;
        this.topicId = topicId;
    }

    static String mNums = "500";
    static String mRating = "0";
    public static void showMmodDialog(final Activity activity, final Fragment fragment, final String topicId) {


        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.mmod_dialog, null);

        Spinner num_spinner = view.findViewById(R.id.num_spinner);
        String[] data = new String[]{"100", "500", "1000", "5000", activity.getString(R.string.all)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        num_spinner.setAdapter(adapter);
        // заголовок
        num_spinner.setPrompt(activity.getString(R.string.posts_per_page));
        num_spinner.setSelection(1);
        num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mNums = "100";
                        break;
                    case 1:
                        mNums = "500";
                        break;
                    case 2:
                        mNums = "1000";
                        break;
                    case 3:
                        mNums = "5000";
                        break;
                    case 4:
                        mNums = "0";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner rating_spinner = view.findViewById(R.id.rating_spinner);
        data = new String[]{activity.getString(R.string.not_important), "0", "-1", "-2", "-5"};
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rating_spinner.setAdapter(adapter);
        // заголовок
        rating_spinner.setPrompt(activity.getString(R.string.rating_less));
        rating_spinner.setSelection(0);
        rating_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mRating = "0";
                        break;
                    case 1:
                        mRating = "1";
                        break;
                    case 2:
                        mRating = "2";
                        break;
                    case 3:
                        mRating = "3";
                        break;
                    case 4:
                        mRating = "6";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        new MaterialDialog.Builder(activity)
                .title(R.string.multimoderation)
                .customView(view,true)
                .cancelable(true)

                .positiveText(R.string.open)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            String url = String
                                    .format("https://"+ HostHelper.getHost() +"/forum/index.php?act=mmod&t=%s&num=%s&rating=%s",
                                            topicId, mNums, mRating);
                            if(fragment instanceof CuratorFragment)
                                ((CuratorFragment)fragment).load(url, topicId);
                            else
                                CuratorFragment.showSpecial(url, topicId);

                        } catch (Throwable ex) {
                            AppLog.e(activity, ex);
                        }
                    }
                })
                .negativeText(R.string.cancel).show();

    }
}
