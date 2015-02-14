package org.softeg.slartus.forpdaplus.topicview;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.Log;

/*
 * Created by slinkin on 24.07.2014.
 */
public class Curator {
    private ThemeActivity mTopicActivity;

    public Curator(ThemeActivity topicActivity) {
        mTopicActivity = topicActivity;
    }

    private String mNums = "500";
    private String mRating = "0";

    public void showMmodDialog() {
        LayoutInflater inflater = mTopicActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.mmod_dialog, null);

        Spinner num_spinner = (Spinner) view.findViewById(R.id.num_spinner);
        String[] data = new String[]{"100", "500", "1000", "5000", "Все"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mTopicActivity, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        num_spinner.setAdapter(adapter);
        // заголовок
        num_spinner.setPrompt("Постов на страницу");
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

        Spinner rating_spinner = (Spinner) view.findViewById(R.id.rating_spinner);
        data = new String[]{"Неважно", "0", "-1", "-2", "-5"};
        adapter = new ArrayAdapter<String>(mTopicActivity, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rating_spinner.setAdapter(adapter);
        // заголовок
        rating_spinner.setPrompt("Рейтинг ниже");
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

        new AlertDialogBuilder(mTopicActivity)
                .setTitle("Мультимодерация")
                .setView(view)
                .setCancelable(true)

                .setPositiveButton("Открыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            dialogInterface.dismiss();
                            String url = String
                                    .format("http://4pda.ru/forum/index.php?act=idx&autocom=mmod&t=%s&num=%s&rating=%s",
                                            mTopicActivity.getTopic().getId(), mNums, mRating);
                            ExtUrl.showInBrowser(mTopicActivity, url);
                        } catch (Throwable ex) {
                            Log.e(mTopicActivity, ex);
                        }

                    }
                }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();

    }
}
