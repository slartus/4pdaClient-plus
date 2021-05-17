package org.softeg.slartus.forpdaplus.classes.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 27.10.12
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class ExtUrl {

    public static void showContextDialog(final Context context, final String title, final List<MenuListDialog> list) {
        final List<String> names = new ArrayList<>();
        for (MenuListDialog menuListDialog : list)
            names.add(menuListDialog.getTitle());

        new MaterialDialog.Builder(context)
                .title(title)
                .items(names.toArray(new CharSequence[names.size()]))
                .itemsCallback((dialog, itemView, which, text) -> list.get(which).getRunnable().run())
                .show();
    }

    public static void openNewTab(Context context, Handler handler, String url) {
        if (!IntentActivity.tryShowUrl((Activity) context, handler, url, false, false))
            Toast.makeText(context, R.string.links_not_supported, Toast.LENGTH_SHORT).show();
    }

    public static void showInBrowser(Context context, String url) {
        url = url.replaceAll("^\\/\\/4pda", "https://4pda");
        if (!url.startsWith("http:") && !url.startsWith("https:")) {
            url = "https:" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "text/html");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose)));
    }

    public static void shareItUrl(Context context, String url) {
        url = url.replaceAll("^\\/\\/4pda", "https://4pda");
        shareIt(context, url);
    }

    public static void shareIt(Context context, String text) {
        Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendMailIntent.setType("text/plain");

        context.startActivity(Intent.createChooser(sendMailIntent, context.getString(R.string.chare_via)));
    }

    public static void copyLinkToClipboard(Context context, String link) {
        link = link.replaceAll("^\\/\\/4pda", "https://4pda");
        StringUtils.copyToClipboard(context, link);
        Toast.makeText(context, R.string.link_copied_to_buffer, Toast.LENGTH_SHORT).show();
    }

    public static void addUrlSubMenu(final android.os.Handler handler, final Context context, List<MenuListDialog> list, final String url
            , final CharSequence id, final String title) {
        addUrlMenu(handler, context, list, url, id, title);
    }

    public static void addUrlMenu(final android.os.Handler handler, final Context context, List<MenuListDialog> list, final String url, final String title) {
        addTopicUrlMenu(handler, context, list, title, url, "", "", "", "", "", "");
    }

    public static void addTopicUrlMenu(final android.os.Handler handler, final Context context, List<MenuListDialog> list,
                                       final String title, final String url, final String body, final CharSequence topicId, final String topic,
                                       final String postId, final String userId, final String user) {

        list.add(new MenuListDialog(context.getString(R.string.open_in_browser), new Runnable() {
            @Override
            public void run() {
                showInBrowser(context, url);
            }
        }));

        list.add(new MenuListDialog(context.getString(R.string.share_link), new Runnable() {
            @Override
            public void run() {
                shareItUrl(context, url);
            }
        }));
        list.add(new MenuListDialog(context.getString(R.string.copy_link), new Runnable() {
            @Override
            public void run() {
                copyLinkToClipboard(context, url);
            }
        }));


        if (!TextUtils.isEmpty(topicId)) {
            list.add(new MenuListDialog(context.getString(R.string.create_note), new Runnable() {
                @Override
                public void run() {
                    NoteDialog.showDialog(handler, context,
                            title, body, url, topicId, topic,
                            postId, userId, user);
                }
            }));
        }
    }

    public static void showSelectActionDialog(final Context context,
                                              final String title,
                                              final String url) {

        CharSequence[] titles = {context.getString(R.string.open_in_browser), context.getString(R.string.share_link), context.getString(R.string.copy_link)};
        new MaterialDialog.Builder(context)
                .title(title)
                .items(titles)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            showInBrowser(context, url);
                            break;
                        case 1:
                            shareItUrl(context, url);
                            break;
                        case 2:
                            copyLinkToClipboard(context, url);
                            break;
                    }
                })
                .cancelable(true)
                .show();


    }

    public static void addUrlMenu(final android.os.Handler handler, final Context context, List<MenuListDialog> menu, final String url,
                                  final CharSequence id, final String title) {
        addTopicUrlMenu(handler, context, menu, title, url, url, id, title, "", "", "");
    }

    public static void showSelectActionDialog(final android.os.Handler handler, final Context context,
                                              final String title, final String body, final String url, final String topicId, final String topic,
                                              final String postId, final String userId, final String user) {
        ArrayList<String> titles =
                new ArrayList<>(Arrays.asList(
                        context.getString(R.string.open_in_new_tab),
                        context.getString(R.string.open_in),
                        context.getString(R.string.share_link),
                        context.getString(R.string.copy_link),
                        context.getString(R.string.create_note),
                        context.getString(R.string.save)));
        new MaterialDialog.Builder(context)
                .content(url)
                .items(titles)
                .itemsCallback((dialog, view, i, titles1) -> {
                    switch (i) {
                        case 0:
                            openNewTab(context, handler, url);
                            break;
                        case 1:
                            showInBrowser(context, url);
                            break;
                        case 2:
                            shareItUrl(context, url);
                            break;
                        case 3:
                            copyLinkToClipboard(context, url);
                            break;
                        case 4:
                            NoteDialog.showDialog(handler, context,
                                    title, body, url, topicId, topic,
                                    postId, userId, user);
                            break;
                        case 5:
                            DownloadsService.download(((MainActivity) context), url, false);
                            break;
                        default:
                            break;
                    }
                })
                .cancelable(true)
                .show();
    }

    public static void showSelectActionDialog(final android.os.Handler handler, final Context context, final String url) {
        showSelectActionDialog(handler, context, "", "", url, "", "", "", "", "");
    }


    public static void showImageSelectActionDialog(final android.os.Handler handler, final Context context, final String url) {
        try {
            CharSequence[] titles = new CharSequence[]{context.getString(R.string.open), context.getString(R.string.open_in), context.getString(R.string.copy_link), context.getString(R.string.save)};
            handler.post(() -> {
                try {
                    new MaterialDialog.Builder(context)
                            .content(url)
                            .items(titles)
                            .itemsCallback((dialog, view, i, titles1) -> {
                                switch (i) {
                                    case 0:
//                                ImageViewActivity.startActivity(context, url);
                                        ImgViewer.startActivity(context, url);
                                        break;
                                    case 1:
                                        showInBrowser(context, url);
                                        break;
                                    case 2:
                                        copyLinkToClipboard(context, url);
                                        break;
                                    case 3:
                                        DownloadsService.download((Activity) context, url, false);
                                        break;
                                }
                            })
                            .cancelable(true)
                            .show();
                } catch (Throwable ex1) {
                    AppLog.e(ex1);
                }
            });


        } catch (Throwable ex) {
            AppLog.e(ex);
        }


    }
}
