package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaapi.devdb.NewDevDbApi;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.UrlExtensions;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.devdb.ParentFragment;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.fragments.ForumFragment;
import org.softeg.slartus.forpdaplus.fragments.ForumRulesFragment;
import org.softeg.slartus.forpdaplus.fragments.NewsFragment;
import org.softeg.slartus.forpdaplus.fragments.SpecialView;
import org.softeg.slartus.forpdaplus.fragments.profile.DeviceDelete;
import org.softeg.slartus.forpdaplus.fragments.profile.DeviceEdit;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileEditFragment;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes;
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment;
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.DevDbCatalogFragment;
import org.softeg.slartus.forpdaplus.listfragments.DevDbModelsFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.DevDbCatalogBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.DevDbModelsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.FavoritesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NewsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;
import org.softeg.slartus.hosthelper.HostHelper;
import org.softeg.slartus.hosthelper.HostHelperKt;

import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentActivity extends MainActivity implements BricksListDialogFragment.IBricksListDialogCaller {
    @Override
    public void onBricksListDialogResult(DialogInterface dialog, String dialogId,
                                         BrickInfo brickInfo, Bundle args) {
        dialog.dismiss();
        MainActivity.showListFragment(brickInfo.getName(), args);
    }

    public static String getRedirectUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String u = uri.getQueryParameter("u");

            if (url.contains(App.Host + "/pages/go") && !TextUtils.isEmpty(u))
                return URLDecoder.decode(u, "UTF-8");
        } catch (Throwable ignore) {

        }

        return url;
    }

    public static Boolean isNews(String url) {

        url = IntentActivity.getRedirectUrl(url);
        final Pattern pattern = PatternExtensions.compile(App.Host + "/\\d{4}/\\d{2}/\\d{2}/\\d+");
        final Pattern pattern1 = PatternExtensions.compile(App.Host + "/\\w+/(?:older|newer)/\\d+");

        return pattern.matcher(url).find() || pattern1.matcher(url).find();
    }

    public static Boolean isYoutube(String url) {
        url = IntentActivity.getRedirectUrl(url);
        return PatternExtensions.compile("youtube.com/(?:watch|v|e|embed)|youtu.be").matcher(url).find();
    }

    public static Boolean tryShowYoutube(Activity context, String url) {
        url = IntentActivity.getRedirectUrl(url);
        if (!isYoutube(url)) return false;
        IntentActivity.showInDefaultBrowser(context, url);
        return true;
    }

    public static Boolean isNewsList(String url) {
        url = IntentActivity.getRedirectUrl(url);
        String[] patterns = {
                HostHelper.getHostPattern() + "/tag/.*",
                HostHelper.getHostPattern() + "/page/(\\d+)/",
                HostHelper.getHostPattern() + "/?$",
                HostHelper.getHostPattern() + "/news",
                HostHelper.getHostPattern() + "/articles",
                HostHelper.getHostPattern() + "/software",
                HostHelper.getHostPattern() + "/games",
                HostHelper.getHostPattern() + "/reviews"};
        for (String pattern : patterns) {
            if (PatternExtensions.compile(pattern).matcher(url).find())
                return true;
        }
        return false;
    }

    public static Boolean tryShowNewsList(String url) {
        if (isNewsList(url)) {
            Bundle args = new Bundle();
            args.putString(NewsListFragment.NEWS_LIST_URL_KEY, url);
            MainActivity.showListFragment(new NewsBrickInfo().getName(), args);

            return true;
        }
        return false;
    }

    public static Boolean tryShowNews(String url) {
        if (isNews(url)) {
            MainActivity.addTab(url, NewsFragment.newInstance(url));
            return true;
        }
        return false;
    }

    public static Boolean tryShowSpecial(String url) {
        if (url.contains("special")) {
            SpecialView.showSpecial(url);
            return true;
        }
        return false;
    }

    public static boolean isTheme(Uri uri) {
        if (!HostHelperKt.is4pdaHost(uri.getHost()))
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
        Matcher m = PatternExtensions.compile(App.Host + ".*?act=boardrules").matcher(url);// переброс с правил форума на графический вариант
        if (m.find()) {
            return "https://" + App.Host + "/forum/index.php?showtopic=296875";
        }

        return url;
    }

    public static boolean tryShowSearch(String url) {

        Matcher m = PatternExtensions.compile(App.Host + ".*?act=search").matcher(url);
        if (m.find()) {
            MainActivity.startForumSearch(SearchSettings.parse(url));
            return true;
        }
        return false;
    }

    public static boolean tryReputation(Activity context, Handler handler, Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if (!"rep".equals(uri.getQueryParameter("act")))
            return false;

        // история репутации
        if ("history".equals(uri.getQueryParameter("view"))
                && !TextUtils.isEmpty(uri.getQueryParameter("mid"))) {
            UserReputationFragment.showActivity(uri.getQueryParameter("mid"),
                    "from".equals(uri.getQueryParameter("mode")));
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
                                uri.getQueryParameter("p"), "add", context.getString(R.string.increase_reputation));
                    } else {
                        UserReputationFragment.plusRep(context, handler, uri.getQueryParameter("mid"), uri.getQueryParameter("mid"));
                    }

                    break;
                case "win_minus":
                    if (!TextUtils.isEmpty(uri.getQueryParameter("p"))) {
                        ForumUser.startChangeRep(context, handler,
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("mid"),
                                uri.getQueryParameter("p"), "minus", context.getString(R.string.decrease_reputation));
                    } else {
                        UserReputationFragment.minusRep(context, handler, uri.getQueryParameter("mid"), uri.getQueryParameter("mid"));
                    }
                    break;

                default:
                    return false;
            }
            return true;
        }

        return false;
    }


    public static boolean tryShowClaim(Activity context, Handler handler, Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if (!"report".equals(uri.getQueryParameter("act")))
            return false;

        if (!TextUtils.isEmpty(uri.getQueryParameter("t"))
                && !TextUtils.isEmpty(uri.getQueryParameter("p"))) {
            org.softeg.slartus.forpdaplus.classes.Post.claim(context, handler,
                    uri.getQueryParameter("t"), uri.getQueryParameter("p"));

            return true;
        }
        return false;
    }


    public static boolean tryProfile(Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if ("profile".equals(uri.getQueryParameter("act"))
                && !TextUtils.isEmpty(uri.getQueryParameter("id"))) {
            ProfileFragment.showProfile(uri.getQueryParameter("id"), uri.getQueryParameter("id"));


            return true;
        }

        if (!TextUtils.isEmpty(uri.getQueryParameter("showuser"))) {
            ProfileFragment.showProfile(uri.getQueryParameter("showuser"), uri.getQueryParameter("showuser"));


            return true;
        }

        return false;
    }

    public static boolean tryEditProfile(Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if (!"usercp".equals(uri.getQueryParameter("act")))
            return false;

        if ("01".equals(uri.getQueryParameter("code"))) {
            ProfileEditFragment.editProfile();
            return true;
        }
        return false;
    }

    public static boolean tryEditDevice(Activity context, Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if ("profile-xhr".equals(uri.getQueryParameter("act"))) {
            if ("device".equals(uri.getQueryParameter("action")))
                new DeviceEdit(context, uri.toString(), !TextUtils.isEmpty(uri.getQueryParameter("md_id")),
                        TabsManager.getInstance().getCurrentFragmentTag());

            if ("dev-del".equals(uri.getQueryParameter("action")))
                new DeviceDelete(context, uri.toString(), TabsManager.getInstance().getCurrentFragmentTag());
            return true;
        }


        return false;
    }

    public static boolean tryShowForum(String url) {
        String[] patterns = {App.Host + ".*?showforum=(\\d+)$", App.Host + "/forum/lofiversion/index.php\\?f(\\d+)\\.html",
                App.Host + "/forum/index.php.*?act=idx"};
        for (String pattern : patterns) {
            Matcher m = PatternExtensions.compile(pattern).matcher(url);
            if (m.find()) {
                String id = m.groupCount() > 0 ? m.group(1) : null;
                ForumFragment.Companion.showActivity(id, null);
                return true;
            }

        }

        return false;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity) {
        return tryShowUrl(context, handler, url, showInDefaultBrowser, finishActivity, null);
    }

    public static boolean tryShowRules(Uri uri) {
        if ("announce".equals(uri.getQueryParameter("act")) | "boardrules".equals(uri.getQueryParameter("act"))) {
            ForumRulesFragment.showRules(uri.toString());
            return true;
        }
        return false;
    }


    private static CharSequence getRedirect(String url) {
        try {
            Uri uri = Uri.parse(url);
            CharSequence u = Uri.parse(url).getQueryParameter("u");
            if (!TextUtils.isEmpty(u)) {
                u = UrlExtensions.decodeUrl(u);
                StringBuilder redirectUrl = new StringBuilder(u);
                // for urls like: https://4pda.to/pages/go/?u=https%3A%2F%2Falreader.com%2Fdownloads%2FOTHER%2FAlReaderXPro.apk&e=83423404
                boolean first = true;
                for (String queryParameterName : uri.getQueryParameterNames()) {
                    if ("u".equals(queryParameterName.toLowerCase(Locale.ENGLISH))) continue;
                    if (first)
                        redirectUrl.append("?").append(queryParameterName).append("=");
                    else
                        redirectUrl.append("&").append(queryParameterName).append("=");
                    redirectUrl.append(uri.getQueryParameter(queryParameterName));
                    first = false;
                }
                return redirectUrl.toString();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity, String authKey) {
        if (url.startsWith("//")) {
            url = "https:".concat(url);
        }
        url = url.replace("&amp;", "&").replace("\"", "").trim();
        url = getRedirect(url).toString();
        url = url.trim();

        if (HostHelperKt.is4pdaHost(url) & !url.contains("http://") & !url.contains("https://"))
            url = "https://" + url;
        Uri uri = Uri.parse(url.toLowerCase());


        if (uri.getHost() != null && (HostHelperKt.is4pdaHost(uri.getHost())
                || uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com")
                || uri.getHost().toLowerCase().contains("cs3-2.4pda.to")
                || uri.getHost().toLowerCase().contains("mzstatic.com")
                || uri.getHost().toLowerCase().contains("savepice.ru"))) {
            if (isTheme(uri)) {
                showTopic(url);
                return true;
            }
            if (tryShowRules(uri)) {
                return true;
            }

            if (tryShowNews(url)) {
                return true;
            }

            if (tryShowNewsList(url)) {
                return true;
            }

            if (tryShowSpecial(url)) {
                return true;
            }

            if (tryShowFile(context, Uri.parse(url), finishActivity)) {
                return true;
            }

            if (tryProfile(uri)) {
                return true;
            }

            if (tryEditProfile(uri)) {
                return true;
            }

            if (tryEditDevice(context, uri)) {
                return true;
            }

            if (tryShowForum(url)) {
                return true;
            }

            if (tryReputation(context, handler, uri))
                return true;

            if (tryShowClaim(context, handler, uri))
                return true;

            if (tryShowQms(uri))
                return true;

            if (tryFav(url))
                return true;

            if (tryFavorites(url))
                return true;

            if (tryShowEditPost(context, uri, authKey))
                return true;

            if (tryShowTopicWriters(uri))
                return true;

            if (tryShowSearch(url))
                return true;

            if (tryShowTopicAttaches(uri))
                return true;
        }

        if (tryDevdb(url))
            return true;

        if (tryShowYoutube(context, url))
            return true;

        if (showInDefaultBrowser)
            showInDefaultBrowser(context, url);


        /*if (finishActivity)
            context.finish();*/
        return false;
    }

    public static void showTopic(String url) {
        MainActivity.addTab(url, ThemeFragment.newInstance(url));
    }

    private static boolean tryFavorites(String url) {
        Matcher m = PatternExtensions.compile(App.Host + ".*?autocom=favtopics").matcher(url);
        if (m.find()) {
            MainActivity.showListFragment(new FavoritesBrickInfo().getName(), null);
            return true;
        }
        return false;
    }

    private static boolean tryFav(String url) {
        Matcher m = PatternExtensions.compile(App.Host + ".*?act=fav").matcher(url);
        if (m.find()) {
            MainActivity.showListFragment(new FavoritesBrickInfo().getName(), null);
            return true;
        }
        return false;
    }

    public static boolean tryDevdb(String url) {
        if (NewDevDbApi.isCatalogUrl(url)) {
            Bundle args = new Bundle();
            args.putString(DevDbCatalogFragment.URL_KEY, url);
            MainActivity.showListFragment(new DevDbCatalogBrickInfo().getName(), args);
            return true;
        }
        if (NewDevDbApi.isDevicesListUrl(url)) {
            Bundle args = new Bundle();
            args.putString(DevDbModelsFragment.BRAND_URL_KEY, url);
            MainActivity.showListFragment(new DevDbModelsBrickInfo().getName(), args);
            return true;
        }
        if (NewDevDbApi.isDeviceUrl(url)) {
            ParentFragment.showDevice(url);
            return true;
        }
        return false;
    }

    public static boolean tryShowEditPost(Activity context, Uri uri, String authKey) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if (!"post".equals(uri.getQueryParameter("act")))
            return false;
        if (!"edit_post".equals(uri.getQueryParameter("do")))
            return false;
        if (TextUtils.isEmpty(uri.getQueryParameter("f")) ||
                TextUtils.isEmpty(uri.getQueryParameter("t")) ||
                TextUtils.isEmpty(uri.getQueryParameter("p")))
            return false;

        EditPostFragment.Companion.editPost(context, uri.getQueryParameter("f"),
                uri.getQueryParameter("t"), uri.getQueryParameter("p"), authKey,
                TabsManager.getInstance().getCurrentFragmentTag());
        return true;
    }

    public static boolean tryShowTopicAttaches(Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;
        if (!"attach".equals(uri.getQueryParameter("act")))
            return false;
        if (!"showtopic".equals(uri.getQueryParameter("code")))
            return false;
        if (TextUtils.isEmpty(uri.getQueryParameter("tid")))
            return false;

        TopicAttachmentListFragment.showActivity(uri.getQueryParameter("tid"));
        return true;
    }

    public static boolean tryShowQms(Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
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
                //QmsChatActivity.openChat(context, mid, null, tid, null);
                QmsChatFragment.Companion.openChat(mid, null, tid, null);
            } else {
                //QmsContactThemesActivity.showThemes(context, mid, "");
                QmsContactThemes.showThemes(mid, "");
            }

        } else {
            //ListFragmentActivity.showListFragment(context, QmsContactsBrickInfo.NAME, null);
            QmsContactsBrickInfo brickInfo = new QmsContactsBrickInfo();
            MainActivity.addTab(brickInfo.getTitle(), brickInfo.getName(), brickInfo.createFragment());
        }
        return true;
    }

    public static boolean tryShowTopicWriters(Uri uri) {
        if (uri.getHost() != null && !HostHelperKt.is4pdaHost(uri.getHost()))
            return false;

        if (!"stats".equals(uri.getQueryParameter("act")))
            return false;
        if (!"who".equals(uri.getQueryParameter("code")))
            return false;
        String tid = uri.getQueryParameter("t");
        if (TextUtils.isEmpty(tid))
            return false;


        Bundle args = new Bundle();
        args.putString(TopicWritersListFragment.TOPIC_ID_KEY, tid);
        MainActivity.showListFragment(TopicWritersBrickInfo.NAME, args);
        return true;
    }

    public static boolean tryShowFile(final Activity activity, final Uri uri, final Boolean finish) {
        if (uri.getHost() != null && !(HostHelperKt.is4pdaHost(uri.getHost())
                || uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com")
                || uri.getHost().toLowerCase().contains("cs3-2.4pda.to")
                || uri.getHost().toLowerCase().contains("mzstatic.com")
                || uri.getHost().toLowerCase().contains("savepice.ru")))
            return false;
        boolean isFile = PatternExtensions.compile("https?://" + App.Host + "/forum/dl/post/\\d+/[^\"]*")
                .matcher(uri.toString()).find() ||
                PatternExtensions.compile("https?://st." + App.Host + "/wp-content/uploads/[^\"]*")
                        .matcher(uri.toString()).find()
                ||
                ("attach".equals(uri.getQueryParameter("act")) && !TextUtils.isEmpty(uri.getQueryParameter("id")));

        final Pattern imagePattern = PatternExtensions.compile("https?://.*?\\.(png|jpg|jpeg|gif)$");
        if (isFile) {
            if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(activity);
            } else {
                if (imagePattern.matcher(uri.toString()).find()) {
//                    showImage(activity, uri.toString());
                    showImage(activity, uri.toString());
                    if (finish)
                        activity.finish();
                } else
                    downloadFileStart(activity, uri.toString(), finish);
            }

            return true;
        }
        if (imagePattern.matcher(uri.toString()).find()
                || (uri.getHost().toLowerCase().contains("ggpht.com")
                || uri.getHost().toLowerCase().contains("googleusercontent.com")
                || uri.getHost().toLowerCase().contains("windowsphone.com")
                || uri.getHost().toLowerCase().contains("savepice.ru"))) {
//            showImage(activity, uri.toString());
            showImage(activity, uri.toString());
            if (finish)
                activity.finish();
            return true;
        }
        return false;
    }


    private static void showImage(Context context, String url) {
//        ImageViewActivity.startActivity(context, url);
        ImgViewer.startActivity(context, url);
    }

    public static void downloadFileStart(final Activity activity, final String url, final Boolean finish) {

        if (Preferences.Files.isConfirmDownload()) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.send_post_confirm_dialog, null);
            assert view != null;
            final CheckBox checkBox = view.findViewById(R.id.chkConfirmationSend);
            final TextView message = view.findViewById(R.id.textView);
            message.setText(R.string.ask_start_download_file);
            checkBox.setText(R.string.confirm_download);
            new MaterialDialog.Builder(activity)
                    .title(R.string.confirm_action)
                    .customView(view, true)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog, which) -> {
                        if (!checkBox.isChecked())
                            Preferences.Files.setConfirmDownload(false);
                        DownloadsService.download(activity, url, finish);
                    })
                    .onNegative((dialog, which) -> {
                        if (finish)
                            activity.finish();
                    })
                    .show();

        } else {
            DownloadsService.download(activity, url, finish);
            if (finish)
                activity.finish();
        }

    }

    private static Boolean is4pdaUrl(String url) {
        return PatternExtensions.compile(HostHelper.getHostPattern()).matcher(url).find();
    }

    public static void showInDefaultBrowser(Context context, String url) {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));
            if (is4pdaUrl(url))
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_in)));
            else {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException ex) {
            StringUtils.copyToClipboard(context, url);
            Toast.makeText(context, context.getString(R.string.link_copied_to_buffer), Toast.LENGTH_SHORT).show();
            AppLog.e(context, new NotReportException(context.getString(R.string.no_app_for_link) + ": " + url));
        } catch (Throwable ex) {
            AppLog.e(context, ex);
        }
    }
}