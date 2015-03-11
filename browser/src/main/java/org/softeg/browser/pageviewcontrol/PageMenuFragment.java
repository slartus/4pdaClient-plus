package org.softeg.browser.pageviewcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.softeg.slartus.api.TopicApi;
import org.softeg.slartus.common.AppAssetsManager;
import org.softeg.slartus.yarportal.AppLog;
import org.softeg.slartus.yarportal.R;
import org.softeg.slartus.yarportal.preferences.AppPreferences;

import java.io.File;
import java.util.ArrayList;

/*
 * Created by slinkin on 16.12.2014.
 */
public class PageMenuFragment extends Fragment {
    public static void showStyleDialog(final PageFragment fragment) {
        try {
            Context context = fragment.getActivity();
            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
            newStyleNames.add("Стандартный");
            newStyleNames.add("Режим разработчика");
            final ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();
            newstyleValues.add(AppAssetsManager.getAssetsPath()+"forum/css/style.css");
            newstyleValues.add(TopicApi.CSS_DEVELOPER);
            File file = new File(context.getExternalFilesDir(null).getPath() + File.separator + "css");
            getStylesList(newStyleNames, newstyleValues, file, ".css");

            new AlertDialog.Builder(fragment.getActivity())
                    .setTitle("Выбор стиля")
                    .setSingleChoiceItems(
                            newStyleNames.toArray(new CharSequence[newStyleNames.size()]),
                            newstyleValues.indexOf(AppPreferences.Topic.getStyleCssPath()), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    AppPreferences.Topic.setStyleCssPath(newstyleValues.get(i).toString());

                                    fragment.refreshPage();
                                }
                            })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } catch (Throwable ex) {
            AppLog.e(ex);
        }
    }

    public static void showFontSizeDialog(final PageFragment fragment) {
        View v = fragment.getActivity().getLayoutInflater().inflate(R.layout.font_size_dialog, null);

        assert v != null;
        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
        seekBar.setProgress(AppPreferences.getWebViewFontSize());
        final TextView textView = (TextView) v.findViewById(R.id.value_textview);
        textView.setText((seekBar.getProgress() + 1) + "");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                fragment.getWebView().getSettings().setDefaultFontSize(i + 1);
                textView.setText((i + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle("Размер шрифта")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        AppPreferences.setWebViewFontSize(seekBar.getProgress() + 1);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        fragment.getWebView().getSettings().setDefaultFontSize(AppPreferences.getWebViewFontSize());
                    }
                })
                .create().show();

    }

    private static void getStylesList(ArrayList<CharSequence> newStyleNames, ArrayList<CharSequence> newstyleValues,
                                      File file, String ext) {

        if (file.exists()) {
            File[] cssFiles = file.listFiles();
            assert cssFiles != null;
            for (File cssFile : cssFiles) {
                if (cssFile.isDirectory()) {
                    getStylesList(newStyleNames, newstyleValues, cssFile, ext);
                    continue;
                }
                String cssPath = cssFile.getPath();
                if (!cssPath.toLowerCase().endsWith(ext)) continue;

                newStyleNames.add(cssFile.getName());
                newstyleValues.add(cssPath);

            }
        }
    }
}
