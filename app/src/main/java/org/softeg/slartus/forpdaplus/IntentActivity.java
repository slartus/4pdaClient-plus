package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaapi.devdb.DevDbApi;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.UrlExtensions;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.Email;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewActivity;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.fragments.NewsFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.DevDbCatalogFragment;
import org.softeg.slartus.forpdaplus.listfragments.DevDbModelsFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.DevDbCatalogBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.DevDbModelsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.FavoritesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;
import org.softeg.slartus.forpdaplus.post.EditPostActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.profile.DeviceDeleteDialog;
import org.softeg.slartus.forpdaplus.profile.DeviceEditDialog;
import org.softeg.slartus.forpdaplus.profile.ProfileEditActivity;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;
import org.softeg.slartus.forpdaplus.qms.QmsChatActivity;
import org.softeg.slartus.forpdaplus.qms.QmsContactThemesActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchActivity;
import org.softeg.slartus.forpdaplus.topicview.ThemeActivity;
import org.softeg.slartus.forpdaplus.video.PlayerActivity;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 17.01.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class IntentActivity extends BaseFragmentActivity implements BricksListDialogFragment.IBricksListDialogCaller {
    public static final String ACTION_SELECT_TOPIC = "org.softeg.slartus.forpdaplus.SELECT_TOPIC";
    public static final String RESULT_TOPIC_ID = "org.softeg.slartus.forpdaplus.RESULT_TOPIC_ID";


    @Override
    public void onBricksListDialogResult(DialogInterface dialog, String dialogId,
                                         BrickInfo brickInfo, Bundle args) {
        dialog.dismiss();
        ListFragmentActivity.showListFragment(this, brickInfo.getName(), args);
    }


    public static boolean checkSendAction(BricksListDialogFragment.IBricksListDialogCaller activity, final Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            Bundle extras = intent.getExtras();
            // логи только в тему программы!
            try {
                if (extras != null && extras.containsKey(Intent.EXTRA_EMAIL)
                        && extras.get(Intent.EXTRA_EMAIL) != null && Email.EMAIL.equals(extras.getStringArray(Intent.EXTRA_EMAIL)[0])) {


                    Toast.makeText(activity.getContext(), "Сообщение об ошибке отправлять только на email!", Toast.LENGTH_LONG).show();

                    return true;
                }
            } catch (Throwable ignored) {

            }
            BricksListDialogFragment.showDialog(activity, BricksListDialogFragment.CREATE_POST_ID,
                    ListCore.getBricksNames(ListCore.getCreatePostBricks()), extras);

            return true;
        }
        return false;
    }

    public static Boolean isNews(String url) {

        final Pattern pattern = PatternExtensions.compile("4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+");
        final Pattern pattern1 = PatternExtensions.compile("4pda.ru/\\w+/(?:older|newer)/\\d+");

        return pattern.matcher(url).find() || pattern1.matcher(url).find();
    }

    public static Boolean isYoutube(String url) {
        return PlayerActivity.isYoutube(url);
    }

    public static Boolean tryShowYoutube(Activity context, String url, Boolean finish) {
        if (!isYoutube(url)) return false;
        PlayerActivity.showYoutubeChoiceDialog(context, url);
        if (finish)
            context.finish();
        return true;
    }

    public static Boolean isNewsList(String url) {
        String[] patterns = {"4pda.ru/tag/.*", "4pda.ru/page/(\\d+)/", "4pda.ru/?$",
                "4pda.ru/news", "4pda.ru/articles", "4pda.ru/software", "4pda.ru/games", "4pda.ru/reviews"};
        for (String pattern : patterns) {
            if (PatternExtensions.compile(pattern).matcher(url).find())
                return true;
        }
        return false;
    }

    public static Boolean tryShowNewsList(Activity context, String url, Boolean finish) {
        if (isNewsList(url)) {
            Bundle args = new Bundle();
            args.putString(NewsListFragment.NEWS_LIST_URL_KEY, url);
            ListFragmentActivity.showListFragment(context, new NewsBrickInfo("quick").getName(), args);

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static Boolean tryShowNews(Activity context, String url, Boolean finish) {
        if (isNews(url)) {
            //NewsActivity.shownews(context, url);
            //NewsFragment.newInstance(context, url);
            //Intent eIntent = new Intent(context, MainActivity.class);
            //context.startActivity(eIntent);
            //((MainActivity) context).addTabByIntent(url, NewsFragment.newInstance(context, url));

            return true;
        }
        return false;
    }

    public static boolean isTheme(Uri uri) {
        if (!"4pda.ru".equals(uri.getHost()))
            return false;
        return
                uri.getQueryParameter("showtopic") != null ||
                        ("findpost".equals(uri.getQueryParameter("act")) && uri.getQueryParameter("pid") != null) ||
                        (uri.getPathSegments() != null && uri.getPathSegments().contains("lofiversion") && uri.getQuery() != null && uri.getQuery().matches("t\\d+(?:-\\d+)?.html"));

    }

    public static String normalizeThemeUrl(String url) {
        if (TextUtils.isEmpty(url))
            return url;

        url = TopicApi.toMobileVersionUrl(url);
        Matcher m = PatternExtensions.compile("4pda.ru.*?act=boardrules").matcher(url);// переброс с правил форума на графический вариант
        if (m.find()) {
            return "http://4pda.ru/forum/index.php?showtopic=296875";
        }

        return url;
    }

    public static boolean tryShowSearch(Activity context, String url, Boolean finish) {

        Matcher m = PatternExtensions.compile("4pda.ru.*?act=search").matcher(url);
        if (m.find()) {
            SearchActivity.startForumSearch(context,
                    SearchSettings.parse(url));

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryReputation(Activity context, Handler handler, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if (!"rep".equals(uri.getQueryParameter("act")))
            return false;

        // история репутации
        if ("history".equals(uri.getQueryParameter("view"))
                && !TextUtils.isEmpty(uri.getQueryParameter("mid"))) {
            UserReputationFragment.showActivity(context, uri.getQueryParameter("mid"),
                    "from".equals(uri.getQueryParameter("mode")));
            if (finish)
                context.finish();
            return true;
        }

        // изменение репутации
        if (!TextUtils.isEmpty(uri.getQueryParameter("mid"))
                && !TextUtils.isEmpty(uri.getQueryParameter("view"))) {

            switch (uri.getQueryParameter("view")) {
                case "win_add":
                    if (!TextUtils.isEmpty(uri.getQueryParameter("p"))) {
                        ForumUser.startChangeRep(context, handler,
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("p"), "add", "Поднять репутацию");
                    } else {
                        UserReputationFragment.plusRep(context, handler, uri.getQueryParameter("mid"), uri.getQueryParameter("mid"));
                    }

                    break;
                case "win_minus":
                    if (!TextUtils.isEmpty(uri.getQueryParameter("p"))) {
                        ForumUser.startChangeRep(context, handler,
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("p"), "minus", "Опустить репутацию");
                    } else {
                        UserReputationFragment.minusRep(context, handler, uri.getQueryParameter("mid"), uri.getQueryParameter("mid"));
                    }
                    break;

                default:
                    return false;
            }


            if (finish)
                context.finish();
            return true;
        }

        return false;
    }


    public static boolean tryShowClaim(Activity context, Handler handler, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if (!"report".equals(uri.getQueryParameter("act")))
            return false;

        if (!TextUtils.isEmpty(uri.getQueryParameter("t"))
                && !TextUtils.isEmpty(uri.getQueryParameter("p"))) {
            org.softeg.slartus.forpdaplus.classes.Post.claim(context, handler,
                    uri.getQueryParameter("t"), uri.getQueryParameter("p"));

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }


    public static boolean tryProfile(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if ("profile".equals(uri.getQueryParameter("act"))
                && !TextUtils.isEmpty(uri.getQueryParameter("id"))) {
            ProfileWebViewActivity.startActivity(context, uri.getQueryParameter("id"));

            if (finish)
                context.finish();
            return true;
        }

        if (!TextUtils.isEmpty(uri.getQueryParameter("showuser"))) {
            ProfileWebViewActivity.startActivity(context, uri.getQueryParameter("showuser"));

            if (finish)
                context.finish();
            return true;
        }

        return false;
    }

    public static boolean tryEditProfile(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if (!"usercp".equals(uri.getQueryParameter("act")))
            return false;

        if ("01".equals(uri.getQueryParameter("code"))) {
            ProfileEditActivity.startActivity(context);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryEditDevice(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if ("profile-xhr".equals(uri.getQueryParameter("act"))) {
            if("device".equals(uri.getQueryParameter("action")))
                DeviceEditDialog.showDialog(context,uri.toString(),!TextUtils.isEmpty(uri.getQueryParameter("md_id")));

            if("dev-del".equals(uri.getQueryParameter("action")))
                DeviceDeleteDialog.showDialog(context, uri.toString());



            if (finish)
                context.finish();
            return true;
        }


        return false;
    }

    public static boolean tryShowForum(Activity context, String url, Boolean finish) {
        String[] patterns = {"4pda.ru.*?showforum=(\\d+)$", "4pda.ru/forum/lofiversion/index.php\\?f(\\d+)\\.html",
                "4pda.ru/forum/index.php.*?act=idx"};
        for (String pattern : patterns) {
            Matcher m = PatternExtensions.compile(pattern).matcher(url);
            if (m.find()) {
                String id = m.groupCount() > 0 ? m.group(1) : null;
                ForumFragment.showActivity(context, id, null);
                if (finish)
                    context.finish();
                return true;
            }

        }

        return false;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity) {
        return tryShowUrl(context, handler, url, showInDefaultBrowser, finishActivity, null);
    }


    private static CharSequence getRedirect(CharSequence url) {
        Matcher m = PatternExtensions.compile("4pda\\.ru/pages/go/\\?u=(.*?)$").matcher(url);
        if (m.find()) {
            try {
                return UrlExtensions.decodeUrl(m.group(1));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity, String authKey) {
        url = getRedirect(url).toString();
        Uri uri = Uri.parse(url.toLowerCase());

        if (uri.getHost() != null && (uri.getHost().toLowerCase().contains("4pda.ru")
                || uri.getHost().toLowerCase().contains("4pda.to")
                || uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com")
                || uri.getHost().toLowerCase().contains("mzstatic.com"))) {
            if (isTheme(uri)) {
                showTopic(context, url);
                if (finishActivity)
                    context.finish();
                return true;
            }

            if (tryShowNews(context, url, finishActivity)) {
                return true;
            }

            if (tryShowNewsList(context, url, finishActivity)) {
                return true;
            }

            if (tryShowFile(context, Uri.parse(url), finishActivity)) {
                return true;
            }

            if (tryProfile(context, uri, finishActivity)) {
                return true;
            }

            if (tryEditProfile(context, uri, finishActivity)) {
                return true;
            }

            if (tryEditDevice(context, uri, finishActivity)) {
                return true;
            }

            if (tryShowForum(context, url, finishActivity)) {
                return true;
            }

            if (tryReputation(context, handler, uri, finishActivity))
                return true;

            if (tryShowClaim(context, handler, uri, finishActivity))
                return true;

            if (tryShowQms(context, uri,finishActivity))
                return true;

            if (tryFav(context, url, finishActivity))
                return true;

            if (tryFavorites(context, url, finishActivity))
                return true;

            if (tryShowEditPost(context, uri, authKey, finishActivity))
                return true;

            if (tryShowTopicWriters(context, uri, finishActivity))
                return true;

            if (tryShowSearch(context, url, finishActivity))
                return true;

            if (tryShowTopicAttaches(context, uri, finishActivity))
                return true;
        }

        if (tryDevdb(context, url, finishActivity))
            return true;

        if (tryShowYoutube(context, url, finishActivity))
            return true;

        if (showInDefaultBrowser)
            showInDefaultBrowser(context, url);


        if (finishActivity)
            context.finish();
        return false;
    }

    public static void showTopic(Activity context, String url) {
        Intent themeIntent = new Intent(context, ThemeActivity.class);
        themeIntent.setData(Uri.parse(url));
        context.startActivity(themeIntent);
    }

    private static boolean tryFavorites(Activity context, String url, Boolean finishActivity) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?autocom=favtopics").matcher(url);
        if (m.find()) {
            ListFragmentActivity.showListFragment(context, new FavoritesBrickInfo().getName(), null);

            if (finishActivity)
                context.finish();
            return true;
        }
        return false;
    }

    private static boolean tryFav(Activity context, String url, Boolean finishActivity) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?act=fav").matcher(url);
        if (m.find()) {
            ListFragmentActivity.showListFragment(context, new FavoritesBrickInfo().getName(), null);

            if (finishActivity)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryDevdb(Activity context, String url, Boolean finish) {
        if (DevDbApi.isCatalogUrl(url)) {
            Bundle args = new Bundle();
            args.putString(DevDbCatalogFragment.URL_KEY, url);
            ListFragmentActivity.showListFragment(context, new DevDbCatalogBrickInfo().getName(), args);

            if (finish)
                context.finish();
            return true;
        }
        if (DevDbApi.isDevicesListUrl(url)) {
            Bundle args = new Bundle();
            args.putString(DevDbModelsFragment.BRAND_URL_KEY, url);
            ListFragmentActivity.showListFragment(context, new DevDbModelsBrickInfo().getName(), args);

            if (finish)
                context.finish();
            return true;
        }
        if (DevDbApi.isDeviceUrl(url)) {
            DevDbDeviceActivity.showDevice(context, url);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowEditPost(Activity context, Uri uri, String authKey, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if (!"post".equals(uri.getQueryParameter("act")))
            return false;
        if (!"edit_post".equals(uri.getQueryParameter("do")))
            return false;
        if (TextUtils.isEmpty(uri.getQueryParameter("f")) ||
                TextUtils.isEmpty(uri.getQueryParameter("t")) ||
                TextUtils.isEmpty(uri.getQueryParameter("p")))
            return false;

        EditPostActivity.editPost(context, uri.getQueryParameter("f"), uri.getQueryParameter("t"), uri.getQueryParameter("p"), authKey);

        if (finish)
            context.finish();
        return true;
    }

    public static boolean tryShowTopicAttaches(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        if (!"attach".equals(uri.getQueryParameter("act")))
            return false;
        if (!"showtopic".equals(uri.getQueryParameter("code")))
            return false;
        if (TextUtils.isEmpty(uri.getQueryParameter("tid")))
            return false;

        TopicAttachmentListFragment.showActivity(context, uri.getQueryParameter("tid"));
        if (finish)
            context.finish();
        return true;
    }

    public static boolean tryShowQms(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;
        String mid;
        String tid;
        if ("qms".equals(uri.getQueryParameter("act")) ||
                "qms".equals(uri.getQueryParameter("autocom")) ||
                "msg".equals(uri.getQueryParameter("act"))) {
            mid = uri.getQueryParameter("mid");
            tid = uri.getQueryParameter("t");
        } else
            return false;
        if (!TextUtils.isEmpty(mid)) {
            if (!TextUtils.isEmpty(tid)) {
                QmsChatActivity.openChat(context, mid, null, tid, null);
            } else {
                QmsContactThemesActivity.showThemes(context, mid, "");
            }

        } else {
            ListFragmentActivity.showListFragment(context, QmsContactsBrickInfo.NAME, null);
        }
        if (finish)
            context.finish();
        return true;
    }

    public static boolean tryShowTopicWriters(Activity context, Uri uri, Boolean finish) {
        if (uri.getHost() != null && !uri.getHost().contains("4pda.ru"))
            return false;

        if (!"stats".equals(uri.getQueryParameter("act")))
            return false;
        if (!"who".equals(uri.getQueryParameter("code")))
            return false;
        String tid = uri.getQueryParameter("t");
        if (TextUtils.isEmpty(tid))
            return false;


        Bundle args=new Bundle();
        args.putString(TopicWritersListFragment.TOPIC_ID_KEY, tid);
        ListFragmentActivity.showListFragment(context, TopicWritersBrickInfo.NAME, args);
        if (finish)
            context.finish();
        return true;
    }

    public static boolean tryShowFile(final Activity activity, final Uri uri, final Boolean finish) {
        if (uri.getHost() != null && !(uri.getHost().toLowerCase().contains("4pda.ru")
                || uri.getHost().toLowerCase().contains("4pda.to")
                || uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com")
                || uri.getHost().toLowerCase().contains("mzstatic.com")))
            return false;
        boolean isFile = PatternExtensions.compile("http://4pda.ru/forum/dl/post/\\d+/[^\"]*")
                .matcher(uri.toString()).find() ||
                PatternExtensions.compile("http://st.4pda.ru/wp-content/uploads/[^\"]*")
                        .matcher(uri.toString()).find()
                ||
                ("attach".equals(uri.getQueryParameter("act")) && !TextUtils.isEmpty(uri.getQueryParameter("id")));

        final Pattern imagePattern = PatternExtensions.compile("http://.*?\\.(png|jpg|jpeg|gif)$");
        if (isFile) {
            if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(activity, new Client.OnUserChangedListener() {
                    public void onUserChanged(String user, Boolean success) {
                        if (success) {
                            if (imagePattern.matcher(uri.toString()).find()) {
                                showImage(activity, uri.toString());
                                if (finish)
                                    activity.finish();
                            } else
                                downloadFileStart(activity, uri.toString(), finish);
                        } else if (finish)
                            activity.finish();

                    }
                });
            } else {
                if (imagePattern.matcher(uri.toString()).find()) {
                    showImage(activity, uri.toString());
                    if (finish)
                        activity.finish();
                } else
                    downloadFileStart(activity, uri.toString(), finish);
            }

            return true;
        }
        if (imagePattern.matcher(uri.toString()).find()
                ||(uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com"))) {
            showImage(activity, uri.toString());
            if (finish)
                activity.finish();
            return true;
        }
        return false;
    }

    private static void showImage(Context context, String url) {
        ImageViewActivity.startActivity(context, url);
    }

    public static void downloadFileStart(final Activity activity, final String url, final Boolean finish) {

        if (Preferences.Files.isConfirmDownload()) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.send_post_confirm_dialog, null);
            assert view != null;
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.chkConfirmationSend);
            final TextView message = (TextView) view.findViewById(R.id.textView);
            message.setText("Начать закачку файла?");
            checkBox.setText("Подтверждать скачивание");
            new MaterialDialog.Builder(activity)
                    .title("Подтвердите действие")
                    .customView(view,true)
                    .positiveText("ОК")
                    .negativeText("Отмена")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            //dialogInterface.dismiss();
                            if (!checkBox.isChecked())
                                Preferences.Files.setConfirmDownload(false);
                            DownloadsService.download(activity, url,finish);
                        }
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            if (finish)
                                activity.finish();
                        }
                    })
                    .show();

        } else {
            DownloadsService.download(activity, url,finish);
            if (finish)
                activity.finish();
        }

    }

    private static Boolean is4pdaUrl(String url) {
        return PatternExtensions.compile("4pda\\.ru").matcher(url).find();
    }

    public static void showInDefaultBrowser(Context context, String url) {
        try {

            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));
            if (is4pdaUrl(url))
                context.startActivity(Intent.createChooser(marketIntent, "Открыть с помощью"));
            else
                context.startActivity(marketIntent);
        } catch (Exception ex) {
            AppLog.e(context, new NotReportException("Не найдено ни одно приложение для ссылки: " + url));
        }
    }
}