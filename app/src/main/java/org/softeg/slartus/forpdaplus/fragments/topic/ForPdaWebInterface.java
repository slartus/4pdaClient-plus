package org.softeg.slartus.forpdaplus.fragments.topic;

/*
 * Created by radiationx on 28.10.15.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.TopicAttaches;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.TopicReadersBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;
import org.softeg.slartus.hosthelper.HostHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 23.06.2015.
 */
public class ForPdaWebInterface {
    public static final String NAME = "HTMLOUT";

    private final WeakReference<ThemeFragment> contextRef;

    ForPdaWebInterface(ThemeFragment context) {
        this.contextRef = new WeakReference<>(context);
    }

    private ThemeFragment getContext() {
        return contextRef.get();
    }

    private FragmentActivity getMainActivity() {
        return contextRef.get().getMainActivity();
    }


    @JavascriptInterface
    public void showImgPreview(final String title, final String previewUrl, final String fullUrl) {
        run(() -> ThemeFragment.showImgPreview(getMainActivity(), title, previewUrl, fullUrl));
    }

    @JavascriptInterface
    public void quote(final String forumId, final String topicId, final String postId, final String postDate, final String userId, final String userNick) {
        run(() -> getContext().quote(forumId, topicId, postId, postDate, userId, userNick)
        );
    }

    @JavascriptInterface
    public void checkBodyAndReload(final String postBody) {
        run(() -> getContext().checkBodyAndReload(postBody));

    }

    @JavascriptInterface
    public void showTopicAttaches(final String postBody) {
        run(() -> {
            final TopicAttaches topicAttaches = new TopicAttaches();
            topicAttaches.parseAttaches(postBody);
            if (topicAttaches.size() == 0) {
                Toast.makeText(getMainActivity(), R.string.no_attachment_on_page, Toast.LENGTH_SHORT).show();
                return;
            }
            final boolean[] selection = new boolean[topicAttaches.size()];
            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.attachments)
                    .items(topicAttaches.getList())
                    .itemsCallbackMultiChoice(null, (materialDialog, which, charSequence) -> {
                        for (Integer integer : which) {
                            selection[integer] = true;
                        }
                        return true;
                    })
                    .alwaysCallMultiChoiceCallback()
                    .positiveText(R.string.do_download)
                    .onPositive((dialog, which) -> {
                        if (!Client.getInstance().getLogined()) {
                            new MaterialDialog.Builder(getMainActivity())
                                    .title(R.string.attention)
                                    .content(R.string.need_login_for_download)
                                    .positiveText(R.string.ok)
                                    .show();
                            return;
                        }
                        for (int j = 0; j < selection.length; j++) {
                            if (!selection[j]) continue;
                            DownloadsService.download((getMainActivity()), topicAttaches.get(j).getUri(), false);
                            selection[j] = false;
                        }
                    })
                    .negativeText(R.string.cancel)
                    .show();
        });
    }

    @JavascriptInterface
    public void showPostLinkMenu(final String postId) {
        run(() -> getContext().showLinkMenu(Post.getLink(getContext().getTopic().getId(), postId), postId))
        ;
    }

    @JavascriptInterface
    public void postVoteBad(final String postId) {
        run(() -> new MaterialDialog.Builder(getMainActivity())
                .title(R.string.confirm_action)
                .content(R.string.vote_bad)
                .positiveText(R.string.do_vote_bad)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> Post.minusOne(getMainActivity(), new Handler(), postId))
                .show())
        ;
    }

    @JavascriptInterface
    public void postVoteGood(final String postId) {
        run(() -> new MaterialDialog.Builder(getMainActivity())
                .title(R.string.confirm_action)
                .content(R.string.vote_good)
                .positiveText(R.string.do_vote_good)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> Post.plusOne(getMainActivity(), new Handler(), postId))
                .show())
        ;
    }

    @JavascriptInterface
    public void showReadingUsers() {
        run(() -> {
            try {
                Bundle args = new Bundle();
                args.putString(TopicReadersListFragment.TOPIC_ID_KEY, getContext().getTopic().getId());
                MainActivity.showListFragment(getContext().getTopic().getId(), TopicReadersBrickInfo.NAME, args);
            } catch (ActivityNotFoundException e) {
                AppLog.e(getMainActivity(), e);
            }
        });

    }

    @JavascriptInterface
    public void showWriters() {
        run(() -> {
            if (getContext().getTopic() == null) {
                Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
            } else {
                Bundle args = new Bundle();
                args.putString(TopicWritersListFragment.TOPIC_ID_KEY, getContext().getTopic().getId());
                MainActivity.showListFragment(getContext().getTopic().getId(), TopicWritersBrickInfo.NAME, args);

            }
        });
    }

    @JavascriptInterface
    public void showUserMenu(final String postId, final String userId, final String userNick) {
        run(() -> ForumUser.showUserQuickAction(getMainActivity(), getContext().getTopic().getId(), postId, userId, userNick,
                this::insertTextToPost
        ));
    }

    @JavascriptInterface
    public void insertTextToPost(final String text) {
        Runnable runnable = () -> new Handler().post(() -> getContext().insertTextToPost(text, -1));
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            run(runnable);
        } else {
            getMainActivity().runOnUiThread(runnable);
        }
    }

    @JavascriptInterface
    public void showPostMenu(final String postId, final String postDate,
                             final String userId, final String userNick,
                             final String canEdit, final String canDelete) {
        run(() -> getContext().openActionMenu(postId, postDate, userId, userNick, "1".equals(canEdit), "1".equals(canDelete)));
    }

    @JavascriptInterface
    public void post() {
        run(() -> getContext().post());
    }

    @JavascriptInterface
    public void nextPage() {
        run(() -> getContext().nextPage());
    }

    @JavascriptInterface
    public void prevPage() {
        run(() -> getContext().prevPage());

    }

    @JavascriptInterface
    public void firstPage() {
        run(() -> getContext().firstPage());
    }

    @JavascriptInterface
    public void lastPage() {
        run(() -> getContext().lastPage());
    }

    @JavascriptInterface
    public void jumpToPage() {
        run(() -> {
            try {

                final CharSequence[] pages = new CharSequence[getContext().getTopic().getPagesCount()];

                final int postsPerPage = getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());

                final String page = getMainActivity().getString(R.string.page_short);

                for (int p = 0; p < getContext().getTopic().getPagesCount(); p++) {
                    pages[p] = page + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                }

                LayoutInflater inflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                View view = inflater.inflate(R.layout.select_page_layout, null);

                assert view != null;
                final ListView listView = view.findViewById(R.id.lstview);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getMainActivity(),
                        R.layout.simple_list_item_single_choice, pages);
                // присваиваем адаптер списку
                listView.setAdapter(adapter);

                final EditText txtNumberPage = view.findViewById(R.id.txtNumberPage);
                txtNumberPage.setText(Integer.toString(getContext().getTopic().getCurrentPage()));
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
                            AppLog.e(getMainActivity(), ex);
                        } finally {
                            listView.setTag(true);
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                listView.setOnItemClickListener((adapterView, view1, i, l) -> {
                    if (listView.getTag() != null && !((Boolean) listView.getTag()))
                        return;
                    txtNumberPage.setTag(false);
                    try {
                        txtNumberPage.setText(Integer.toString((int) l + 1));
                    } catch (Throwable ex) {
                        AppLog.e(getMainActivity(), ex);
                    } finally {
                        txtNumberPage.setTag(true);
                    }
                });

                listView.setItemChecked(getContext().getTopic().getCurrentPage() - 1, true);
                listView.setSelection(getContext().getTopic().getCurrentPage() - 1);

                MaterialDialog dialog = new MaterialDialog.Builder(getMainActivity())
                        .title(R.string.jump_to_page)
                        .customView(view, false)
                        .positiveText(R.string.jump)
                        .onPositive((dialog1, which) -> getContext().openFromSt(listView.getCheckedItemPosition() * postsPerPage))
                        .negativeText(R.string.cancel)
                        .cancelable(true)
                        .show();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            } catch (Throwable ex) {
                AppLog.e(getMainActivity(), ex);
            }
        });

    }

    @JavascriptInterface
    public void plusRep(final String postId, final String userId, final String userNick) {
        run(() -> getContext().showChangeRep(postId, userId, userNick, "add", getContext().getString(R.string.increase_reputation)));
    }

    @JavascriptInterface
    public void minusRep(final String postId, final String userId, final String userNick) {
        run(() -> getContext().showChangeRep(postId, userId, userNick, "minus", getContext().getString(R.string.increase_reputation)));
    }

    @JavascriptInterface
    public void claim(final String postId) {
        run(() -> Post.claim(getMainActivity(), new Handler(), getContext().getTopic().getId(), postId));
    }

    @JavascriptInterface
    public void showRepMenu(final String postId, final String userId, final String userNick, final String canPlus, final String canMinus) {
        run(() -> {
            boolean minus = "1".equals(canMinus);
            boolean plus = "1".equals(canPlus);
            if (!minus && !plus) {
                getContext().showRep(userId);
                return;
            }

            List<String> items = new ArrayList<>();

            int i = 0;

            int plusRepPosition = -1;
            int showRepPosition;
            int minusRepPosition = -1;
            if (plus) {
                items.add(getContext().getString(R.string.do_vote_good) + " (+1)");
                plusRepPosition = i;
                i++;
            }

            items.add(App.getInstance().getString(R.string.look));
            showRepPosition = i;
            i++;

            if (minus) {
                items.add(getContext().getString(R.string.do_vote_bad) + " (-1)");
                minusRepPosition = i;
            }

            if (items.size() == 0) return;

            final int finalMinusRepPosition = minusRepPosition;
            final int finalShowRepPosition = showRepPosition;
            final int finalPlusRepPosition = plusRepPosition;
            new MaterialDialog.Builder(getMainActivity())
                    .title(App.getInstance().getString(R.string.reputation) + " " + userNick)
                    .items(items.toArray(new CharSequence[items.size()]))
                    .itemsCallback((materialDialog, view, i1, charSequence) -> {
                        if (i1 == finalMinusRepPosition) {
                            UserReputationFragment.minusRep(getMainActivity(), new Handler(), postId, userId, userNick);
                        } else if (i1 == finalShowRepPosition) {
                            getContext().showRep(userId);
                        } else if (i1 == finalPlusRepPosition) {
                            UserReputationFragment.plusRep(getMainActivity(), new Handler(), postId, userId, userNick);
                        }
                    })
                    .show();
        });

    }

    @JavascriptInterface
    public void go_gadget_show() {
        run(() -> {

            String url = "https://"+ HostHelper.getHost() +"/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&mode=show&poll_open=true&st=" +
                    getContext().getTopic().getCurrentPage() * getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
            getContext().showTheme(url);
        });

    }

    @JavascriptInterface
    public void go_gadget_vote() {
        run(() -> {
            String url = "https://"+ HostHelper.getHost() +"/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&poll_open=true&st=" +
                    getContext().getTopic().getCurrentPage() * getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
            getContext().showTheme(url);
        });

    }

    @JavascriptInterface
    public void sendPostsAttaches(final String openImageUrl, final String json) {
        run(() -> {
            for (JsonElement s : new JsonParser().parse(json).getAsJsonArray()) {
                ArrayList<String> list1 = new ArrayList<>();
                for (JsonElement a : s.getAsJsonArray()) {
                    String url = a.getAsString();
                    if (!url.contains("http")) {
                        url = "https:".concat(url);
                    }
                    list1.add(url);
                }
                getContext().imageAttaches.add(list1);
                getContext().showImage(openImageUrl);
            }
        });

    }

    @JavascriptInterface
    public void setHistoryBody(final String index, final String body) {
        run(() -> getContext().setHistoryBody(Integer.parseInt(index), body.replaceAll("data-block-init=\"1\"", "")));
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(() -> new SaveHtml(getMainActivity(), html, "Topic"));
    }

    /**
     * Инфо о посте с селектнутым текстом
     */
    @JavascriptInterface
    public void selectionPostInfo(final String postId, final String postDate, final String userId,
                                  String userNick, String selection) {
        if (!TextUtils.isEmpty(postId) && !TextUtils.isEmpty(postDate) && !TextUtils.isEmpty(userNick))
            getContext().insertQuote(postId, Functions.getForumDateTime(Functions.parseForumDateTime(postDate)), userNick, selection);
    }

    public void run(final Runnable runnable) {
        //Почему-то перестало работать как раньше
        /*if (Build.VERSION.SDK_INT < 17) {
            runnable.run();
        } else {
            getMainActivity().runOnUiThread(runnable);
        }*/
        getMainActivity().runOnUiThread(runnable);
    }
}