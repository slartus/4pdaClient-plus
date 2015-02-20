package org.softeg.slartus.forpdaplus.topicbrowser.Notifiers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment;
import org.softeg.slartus.forpdaplus.topicbrowser.TopicFragment;

/*
 * Created by slartus on 05.06.2014.
 */
public class TopicNotifier implements NotifyActivityRegistrator.NotifyClass {
    private TopicFragment topicFragment;

    public TopicNotifier(TopicFragment topicFragment) {

        this.topicFragment = topicFragment;
    }

    @Override
    public void register(NotifyActivityRegistrator registrator) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void runOnUiThread(Runnable runnable) {

        topicFragment.getActivity().runOnUiThread(runnable);
    }

    @JavascriptInterface
    public void nextPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topicFragment.loadTopic(topicFragment.getTopic().getNextPageUrl());
            }
        });
    }

    @JavascriptInterface
    public void prevPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topicFragment.loadTopic(topicFragment.getTopic().getPrevPageUrl());
            }
        });

    }

    @JavascriptInterface
    public void firstPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topicFragment.loadTopic(topicFragment.getTopic().getFirstPageUrl());
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topicFragment.loadTopic(topicFragment.getTopic().getLastPageUrl());
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    final CharSequence[] pages = new CharSequence[topicFragment.getTopic().getPagesCount()];

                    final int postsPerPage = topicFragment.getTopic().getPostsPerPage();

                    for (int p = 0; p < topicFragment.getTopic().getPagesCount(); p++) {
                        pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                    }

                    LayoutInflater inflater = (LayoutInflater) App.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.select_page_layout, null);

                    assert view != null;
                    final ListView listView = (ListView) view.findViewById(R.id.lstview);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(App.getInstance(),
                            android.R.layout.simple_list_item_single_choice, pages);
                    // присваиваем адаптер списку
                    listView.setAdapter(adapter);

                    final EditText txtNumberPage = (EditText) view.findViewById(R.id.txtNumberPage);
                    txtNumberPage.setText(Integer.toString(topicFragment.getTopic().getCurrentPage()));
                    txtNumberPage.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            if (txtNumberPage.getTag() != null && !((Boolean) txtNumberPage.getTag()))
                                return;
                            if (TextUtils.isEmpty(charSequence)) return;
                            try {
                                int value = Integer.parseInt(charSequence.toString());
                                value = Math.min(pages.length - 1, value - 1);
                                listView.setTag(false);
                                listView.setItemChecked(value, true);
                                listView.setSelection(value);
                            } catch (Throwable ex) {
                                AppLog.e(topicFragment.getActivity(), ex);
                            } finally {
                                listView.setTag(true);
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (listView.getTag() != null && !((Boolean) listView.getTag()))
                                return;
                            txtNumberPage.setTag(false);
                            try {
                                txtNumberPage.setText(Integer.toString((int) l + 1));
                            } catch (Throwable ex) {
                                AppLog.e(topicFragment.getActivity(), ex);
                            } finally {
                                txtNumberPage.setTag(true);
                            }
                        }
                    });

                    listView.setItemChecked(topicFragment.getTopic().getCurrentPage() - 1, true);
                    listView.setSelection(topicFragment.getTopic().getCurrentPage() - 1);

                    new AlertDialogBuilder(topicFragment.getActivity())
                            .setTitle("Перейти к странице")
                            .setView(view)
                            .setPositiveButton("Перейти", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    topicFragment.loadTopic(topicFragment.getTopic().getPageUrl(listView.getCheckedItemPosition() + 1));
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .setCancelable(true)
                            .create()
                            .show();
                } catch (Throwable ex) {
                    AppLog.e(topicFragment.getActivity(), ex);
                }
            }
        });

    }

    @JavascriptInterface
    public void showImgPreview(final String title, final String previewUrl, final String fullUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showImgPreview(topicFragment.getActivity(), title, previewUrl, fullUrl);
            }
        });
    }

    public static void showImgPreview(final FragmentActivity context, String title, String previewUrl, final String fullUrl) {
        ImageViewDialogFragment fragment = new ImageViewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ImageViewDialogFragment.PREVIEW_URL_KEY, previewUrl);
        args.putString(ImageViewDialogFragment.URL_KEY, fullUrl);
        args.putString(ImageViewDialogFragment.TITLE_KEY, title);
        fragment.setArguments(args);
        fragment.show(context.getSupportFragmentManager(), "dlg1");
    }

}
