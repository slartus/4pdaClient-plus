package org.softeg.slartus.forpdaplus.prefs;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.ClientPreferences;
import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdacommon.Connectivity;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by slartus on 23.02.14.
 */
public class Preferences {
    public static Boolean isLoadShortUserInfo() {
        return App.getInstance().getPreferences().getBoolean("isLoadShortUserInfo", false);
    }

    public static Boolean isLoadImagesFromWeb(String listName) {
        return isLoadImages(listName + ".LoadsImages");
    }

    public static Boolean isLoadImages(String prefsKey) {
        int loadImagesType = ExtPreferences.parseInt(App.getInstance().getPreferences(), prefsKey, 1);
        if (loadImagesType == 2) {
            return Connectivity.isConnectedWifi(App.getContext());
        }

        return loadImagesType == 1;
    }

    public static Boolean isHideActionBar() {
        return App.getInstance().getPreferences().getBoolean("actionbar.hide", true);
    }
    public static void setHideActionBar(Boolean hide) {
        App.getInstance().getPreferences().edit().putBoolean("actionbar.hide", hide).apply();
    }

    public static Boolean isHideFab() {
        return App.getInstance().getPreferences().getBoolean("fab.hide", true);
    }
    public static void setHideFab(Boolean hide) {
        App.getInstance().getPreferences().edit().putBoolean("fab.hide", hide).apply();
    }

    public static Boolean isBrowserView() {
        return App.getInstance().getPreferences().getBoolean("theme.BrowserStyle", true);
    }
    public static void setBrowserView(Boolean view) {
        App.getInstance().getPreferences().edit().putBoolean("theme.BrowserStyle", view).apply();
    }

    public static Boolean notifyBetaVersions() {
        return App.getInstance().getPreferences().getBoolean("notify.beta_version", false);
    }

    public static void setHideArrows(Boolean hide) {
        App.getInstance().getPreferences().edit().putBoolean("hideArrows", hide).apply();
    }

    public static Boolean isHideArrows() {
        return App.getInstance().getPreferences().getBoolean("hideArrows", false);
    }

    public static int getFontSize(String prefix) {
        int res = ExtPreferences.parseInt(App.getInstance().getPreferences(), prefix + ".FontSize", 16);
        return Math.max(Math.min(res, 72), 1);
    }

    public static void setFontSize(String prefix, int value) {
        App.getInstance().getPreferences().edit().putString(prefix + ".FontSize", Integer.toString(value)).apply();
    }

    public static class Lists {

        public static Boolean getScrollByButtons() {
            return App.getInstance().getPreferences().getBoolean("lists.scroll_by_buttons", false);
        }

        public static void setLastSelectedList(String listName) {
            App.getInstance().getPreferences().edit().putString("list.last_selected_list", listName).apply();
        }

        public static String getLastSelectedList() {
            return App.getInstance().getPreferences().getString("list.last_selected_list", new NewsPagerBrickInfo().getName());
        }

        public static void addLastAction(String name) {
            String[] lastActions = App.getInstance().getPreferences().getString("lists.last_actions", "").split("\\|");

            String newValue = name + "|";
            int max = 5;
            for (String nm : lastActions) {
                if (TextUtils.isEmpty(nm)) continue;
                if (nm.equals(name)) continue;

                newValue += nm + "|";
                max--;
                if (max == 0) break;
            }
            App.getInstance().getPreferences().edit().putString("lists.last_actions", newValue).apply();
        }

        public static String[] getLastActions() {
            return App.getInstance().getPreferences().getString("lists.last_actions", "").split("\\|");
        }


        public static boolean isRefresh() {
            return App.getInstance().getPreferences().getBoolean("lists.refresh", true);
        }
        public static boolean isRefreshOnTab() {
            return App.getInstance().getPreferences().getBoolean("lists.refresh_on_tab", true);
        }
    }

    public static class List {
        public static class Favorites {
            public static Boolean isLoadFullPagesList() {
                return App.getInstance().getPreferences().getBoolean("lists.favorites.load_all", false);
            }
        }

        public static void setListSort(String listName, String value) {
            App.getInstance().getPreferences().edit().putString(listName + ".list.sort", value).apply();
        }

        public static String defaultListSort() {
            return "sortorder.asc";
        }

        public static String getListSort(String listName, String defaultValue) {
            return App.getInstance().getPreferences().getString(listName + ".list.sort", defaultValue);
        }

        public static String getStartForumId() {
            return App.getInstance().getPreferences().getString(ForumBrickInfo.NAME + ".start_forum_id", null);
        }

        public static Forum getStartForum() {
            String id = App.getInstance().getPreferences().getString(ForumBrickInfo.NAME + ".start_forum_id", null);
            if (TextUtils.isEmpty(id))
                return null;
            String title = App.getInstance().getPreferences().getString(ForumBrickInfo.NAME + ".start_forum_title", null);
            if (TextUtils.isEmpty(title))
                return null;
            return new Forum(id, title);
        }

        public static void setStartForum(String id, String title) {
            App.getInstance().getPreferences().edit()
                    .putString(ForumBrickInfo.NAME + ".start_forum_id", id)
                    .putString(ForumBrickInfo.NAME + ".start_forum_title", title)
                    .apply();
        }
    }

    public static class Forums {

        public static Boolean isShowImages() {
            return App.getInstance().getPreferences().getBoolean("forum.list.show_images", true);
        }
    }

    public static class Topic {

        public static Boolean getReadersAndWriters() {
            return App.getInstance().getPreferences().getBoolean("theme.ShowReadersAndWriters", false);
        }

        public static Boolean getSpoilFirstPost() {
            return App.getInstance().getPreferences().getBoolean("theme.SpoilFirstPost", true);
        }

        public static Boolean getConfirmSend() {
            return App.getInstance().getPreferences().getBoolean("theme.ConfirmSend", true);
        }

        public static void setConfirmSend(Boolean value) {
            App.getInstance().getPreferences().edit().putBoolean("theme.ConfirmSend", value).apply();
        }

        public static Boolean isShowAvatars() {
            int loadImagesType = getShowAvatarsOpt();
            if (loadImagesType == 1) {
                return Connectivity.isConnectedWifi(App.getContext());
            }

            return loadImagesType == 0;
        }

        public static int getShowAvatarsOpt() {
            return ExtPreferences.parseInt(App.getInstance().getPreferences(), "topic.show_avatar_opt", 0);
        }

        public static void setShowAvatarsOpt(int value) {
            App.getInstance().getPreferences()
                    .edit().putInt("topic.show_avatar_opt", value).commit();
        }

        public static int getFontSize() {
            return Preferences.getFontSize("theme");
        }

        public static void setFontSize(int value) {
            Preferences.setFontSize("theme", value);
        }

        public static int getHistoryLimit() {
            return ExtPreferences.parseInt(App.getInstance().getPreferences(), "topic.history_limit", 5);
        }

        public static void setHistoryLimit(int value) {
            App.getInstance().getPreferences()
                    .edit().putInt("topic.history_limit", value).commit();
        }


        public static class Post {


            public static void setEnableEmotics(Boolean value) {
                App.getInstance().getPreferences().edit().putBoolean("topic.post.enableemotics", value).apply();
            }

            public static boolean getEnableEmotics() {
                return App.getInstance().getPreferences().getBoolean("topic.post.enableemotics", true);
            }

            public static void setEnableSign(Boolean value) {
                App.getInstance().getPreferences().edit().putBoolean("topic.post.enablesign", value).apply();
            }

            public static boolean getEnableSign() {
                return App.getInstance().getPreferences().getBoolean("topic.post.enablesign", true);
            }

            public static void addEmoticToFavorites(String name) {
                name = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("]", "&#93;").replace("[", "&#91;");
                
                Set<String> favoritesEmotics = getEmoticFavorites();
                HashSet<String> newlist = new HashSet<>();
                newlist.add(name);

                int max = 9;
                for (String nm : favoritesEmotics) {
                    if (TextUtils.isEmpty(nm)) continue;
                    if (nm.equals(name)) continue;

                    newlist.add(nm);
                    max--;
                    if (max == 0) break;
                }
                App.getInstance().getPreferences().edit().putStringSet("topic.post.emotics_favorites", newlist).apply();
            }

            public static Set<String> getEmoticFavorites() {
                
                try {
                    return App.getInstance().getPreferences().getStringSet("topic.post.emotics_favorites", new HashSet<String>());
                } catch (Throwable ignored) {
                    return new HashSet<>();
                }
            }
        }
    }

    public static class System {

        public static boolean getWebviewCompatMode() {
            return App.getInstance().getPreferences().getBoolean("webviewCompatMode", false);
        }

        public static void setSystemDir(String value) {
            App.getInstance().getPreferences().edit().putString("path.system_path", value).apply();
        }

        public static String getSystemDir() {
            File dir = App.getInstance().getFilesDir();
            if (dir == null)
                dir = App.getInstance().getExternalFilesDir(null);
            
            String res = App.getInstance().getPreferences().getString("path.system_path", dir.getPath());
            if (!res.endsWith(File.separator))
                res = res.concat(File.separator);
            return res;
        }


        /**
         * Показывать скролл в браузере
         * Снимите галочку, если программа падает при вызове темы. Например, для Nook Simple Touch
         */
        public static Boolean isShowWebViewScroll() {
            return App.getInstance().getPreferences().getBoolean("system.WebViewScroll", true);
        }

        public static boolean isScrollUpButton(int keyCode) {
            return isPrefsButton(keyCode, "keys.scrollUp", "24");
        }

        public static boolean isScrollDownButton(int keyCode) {
            return isPrefsButton(keyCode, "keys.scrollDown", "25");
        }

        public static boolean isPrefsButton(int keyCode, String prefKey, String defaultValue) {
            String scrollUpKeys = "," + App.getInstance().getPreferences()
                    .getString(prefKey, defaultValue).replace(" ", "") + ",";
            return (scrollUpKeys.contains("," + Integer.toString(keyCode) + ","));
        }

        public static boolean isDevSavePage() {
            return App.getInstance().getPreferences().getBoolean("system.developerSavePage", false);
        }

        public static boolean isDevInterface() {
            return App.getInstance().getPreferences().getBoolean("system.developerInterface", false);
        }

        public static boolean isDevStyle() {
            return App.getInstance().getPreferences().getBoolean("system.developerStyle", false);
        }

        public static boolean isDevGrid() {
            return App.getInstance().getPreferences().getBoolean("system.developerGrid", false);
        }

        public static boolean isDevBounds() {
            return App.getInstance().getPreferences().getBoolean("system.developerBounds", false);
        }

        public static boolean isCurator() {
            return App.getInstance().getPreferences().getBoolean("system.curator", false);
        }

        public static String getDrawerMenuPosition() {
            return App.getInstance().getPreferences().getString("system.drawermenuposition", "left");
        }

        public static String getDownloadFilesDir() {
            String res = App.getInstance().getPreferences().getString("downloads.path", DownloadsService.getDownloadDir(App.getContext()));
            if (TextUtils.isEmpty(res))
                return res;
            if (!res.endsWith(File.separator))
                res += File.separator;
            return res;
        }

        public static void setEvaluateJavascriptEnabled(boolean value) {
            App.getInstance().getPreferences().edit().putBoolean("system.EvaluateJavascriptEnabled", value).apply();
        }

        public static boolean isEvaluateJavascriptEnabled() {
            return App.getInstance().getPreferences()
                    .getBoolean("system.EvaluateJavascriptEnabled", true);
        }
    }

    public static class News {
        public static int getLastSelectedSection() {
            return App.getInstance().getPreferences().getInt("news.lastselectedsection", 0);
        }

        public static void setLastSelectedSection(int section) {
            App.getInstance().getPreferences().edit().putInt("news.lastselectedsection", section).apply();
        }

        public static int getFontSize() {
            return Preferences.getFontSize("news");
        }

        public static class List {
            public static Boolean isLoadImages() {
                return Preferences.isLoadImages("news.list.loadimages");
            }

            public static int getNewsListViewId() {
                
                switch (App.getInstance().getPreferences().getString("news.list.view", "full")) {
                    case "medium":
                        return R.layout.item_news_medium;
                    default:
                        return R.layout.item_news;
                }
            }
        }
    }

    public static class Menu {
        public static boolean getGroupExpanded(int groupIndex) {
            return App.getInstance().getPreferences().getBoolean("menu.GroupExpanded." + groupIndex, true);
        }

        public static void setGroupExpanded(int groupIndex, Boolean expanded) {
            App.getInstance().getPreferences().edit().putBoolean("menu.GroupExpanded." + groupIndex, expanded).apply();
        }
    }

    public static class Search {
    }

    public static class Attention {
        public static void setAttentionId(String value) {
            App.getInstance().getPreferences().edit().putString("attention.id", value).apply();
        }

        public static String getAttentionId() {
            return App.getInstance().getPreferences().getString("attention.id", null);
        }
    }

    public static class Files {
        public static Boolean isConfirmDownload() {
            return App.getInstance().getPreferences().getBoolean("files.ConfirmDownload", true);
        }

        public static void setConfirmDownload(boolean b) {
            App.getInstance().getPreferences().edit().putBoolean("files.ConfirmDownload", b).apply();
        }
    }

    public static class Notifications {
        public static void setSound(Uri soundUri) {
            ClientPreferences.Notifications.setSound(App.getContext(), soundUri);
        }

        public static Uri getSound() {
            return ClientPreferences.Notifications.getSound(App.getContext());
        }

        public static boolean isDefaultSound() {
            return ClientPreferences.Notifications.isDefaultSound(App.getContext());
        }

        public static class SilentMode {
            public static Calendar getStartTime() {
                return ClientPreferences.Notifications.SilentMode.getStartTime(App.getContext());
            }

            public static void setStartTime(int hourOfDay, int minute) {
                ClientPreferences.Notifications.SilentMode.setTime(App.getContext(), "notifiers.silent_mode.start_time", hourOfDay, minute);
            }

            public static Calendar getEndTime() {
                return ClientPreferences.Notifications.SilentMode.getTime(App.getContext(), "notifiers.silent_mode.end_time");
            }

            public static void setEndTime(int hourOfDay, int minute) {
                ClientPreferences.Notifications.SilentMode.setTime(App.getContext(), "notifiers.silent_mode.end_time", hourOfDay, minute);
            }
        }

        public static class Qms {

            public static void readQmsDone() {
                App.getInstance().getPreferences().edit().putBoolean("refreshQMSData", true).apply();
            }

            public static boolean isReadDone() {
                return App.getInstance().getPreferences().getBoolean("refreshQMSData", false);
            }

        }
    }
}
