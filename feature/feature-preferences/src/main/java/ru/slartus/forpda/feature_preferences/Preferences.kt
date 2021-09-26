package ru.slartus.forpda.feature_preferences

import android.net.Uri
import android.text.TextUtils
import ru.slartus.forpda.feature_preferences.App.getPreferences
import org.softeg.slartus.forpdacommon.Connectivity.isConnectedWifi
import ru.slartus.forpda.feature_preferences.App.getInstance
import ru.slartus.forpda.feature_preferences.ClientPreferences.Notifications.setSound
import ru.slartus.forpda.feature_preferences.ClientPreferences.Notifications.getSound
import ru.slartus.forpda.feature_preferences.ClientPreferences.Notifications.SilentMode.getStartTime
import ru.slartus.forpda.feature_preferences.ClientPreferences.Notifications.SilentMode.setTime
import ru.slartus.forpda.feature_preferences.ClientPreferences.Notifications.SilentMode.getTime
import org.softeg.slartus.forpdacommon.ExtPreferences
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Preferences {
    private val isLoadImages: Boolean
        get() {
            val loadImagesType =
                ExtPreferences.parseInt(getPreferences(), "news.list.loadimages", 1)
            return if (loadImagesType == 2) {
                isConnectedWifi(getInstance())
            } else loadImagesType == 1
        }

    @JvmStatic
    var isHideFab: Boolean?
        get() = getPreferences()!!
            .getBoolean("fab.hide", true)
        set(hide) {
            getPreferences()!!
                .edit().putBoolean("fab.hide", hide!!).apply()
        }

    @JvmStatic
    var isHideArrows: Boolean?
        get() = getPreferences()!!
            .getBoolean("hideArrows", false)
        set(hide) {
            getPreferences()!!
                .edit().putBoolean("hideArrows", hide!!).apply()
        }

    @JvmStatic
    fun getFontSize(prefix: String): Int {
        val res = ExtPreferences.parseInt(getPreferences(), "$prefix.FontSize", 16)
        return Math.max(Math.min(res, 72), 1)
    }

    @JvmStatic
    fun setFontSize(prefix: String, value: Int) {
        getPreferences()!!
            .edit().putString("$prefix.FontSize", Integer.toString(value)).apply()
    }

    object Lists {
        @JvmStatic
        val scrollByButtons: Boolean
            get() = getPreferences()!!
                .getBoolean("lists.scroll_by_buttons", false)

        @JvmStatic
        var lastSelectedList: String?
            get() = getPreferences()!!
                .getString("list.last_selected_list", "News_Pages")
            set(listName) {
                getPreferences()!!
                    .edit().putString("list.last_selected_list", listName).apply()
            }

        @JvmStatic
        fun addLastAction(name: String) {
            val lastActions = getPreferences()!!
                .getString("lists.last_actions", "")!!.split("\\|").toTypedArray()
            val newValue = StringBuilder("$name|")
            var max = 5
            for (nm in lastActions) {
                if (TextUtils.isEmpty(nm)) continue
                if (nm == name) continue
                newValue.append(nm).append("|")
                max--
                if (max == 0) break
            }
            getPreferences()!!
                .edit().putString("lists.last_actions", newValue.toString()).apply()
        }

        @JvmStatic
        val lastActions: Array<String>
            get() = getPreferences()!!
                .getString("lists.last_actions", "")!!.split("\\|").toTypedArray()

        @JvmStatic
        val isRefresh: Boolean
            get() = getPreferences()!!
                .getBoolean("lists.refresh", true)

        @JvmStatic
        val isRefreshOnTab: Boolean
            get() = getPreferences()!!
                .getBoolean("lists.refresh_on_tab", true)
    }

    object List {
        @JvmStatic
        fun setListSort(listName: String, value: String?) {
            getPreferences()!!
                .edit().putString("$listName.list.sort", value).apply()
        }

        @JvmStatic
        fun defaultListSort(): String {
            return "sortorder.asc"
        }

        @JvmStatic
        fun getListSort(listName: String, defaultValue: String?): String? {
            return getPreferences()!!
                .getString("$listName.list.sort", defaultValue)
        }

        val startForumId: String?
            get() = getPreferences()!!
                .getString("Forum.start_forum_id", null)

        fun setStartForum(id: String?, title: String?) {
            getPreferences()!!.edit()
                .putString("Forum.start_forum_id", id)
                .putString("Forum.start_forum_title", title)
                .apply()
        }

        object Favorites {
            @JvmStatic
            val isLoadFullPagesList: Boolean
                get() = getPreferences()!!
                    .getBoolean("lists.favorites.load_all", false)
        }
    }

    object Forums {
        val isShowImages: Boolean
            get() = getPreferences()!!
                .getBoolean("forum.list.show_images", true)
    }

    object Topic {
        @JvmStatic
        val readersAndWriters: Boolean
            get() = getPreferences()!!
                .getBoolean("theme.ShowReadersAndWriters", false)

        @JvmStatic
        val spoilFirstPost: Boolean
            get() = getPreferences()!!
                .getBoolean("theme.SpoilFirstPost", true)

        @JvmStatic
        var confirmSend: Boolean
            get() = getPreferences()!!
                .getBoolean("theme.ConfirmSend", true)
            set(value) {
                getPreferences()!!
                    .edit().putBoolean("theme.ConfirmSend", value).apply()
            }

        @JvmStatic
        val isShowAvatars: Boolean
            get() {
                val loadImagesType = showAvatarsOpt
                return if (loadImagesType == 1) {
                    isConnectedWifi(getInstance())
                } else loadImagesType == 0
            }

        @JvmStatic
        var showAvatarsOpt: Int
            get() = ExtPreferences.parseInt(getPreferences(), "topic.show_avatar_opt", 0)
            set(value) {
                getPreferences()
                    ?.edit()?.putInt("topic.show_avatar_opt", value)?.apply()
            }

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("theme")

        @JvmStatic
        val historyLimit: Int
            get() = ExtPreferences.parseInt(getPreferences(), "topic.history_limit", 5)

        object Post {
            @JvmStatic
            var enableEmotics: Boolean?
                get() = getPreferences()!!
                    .getBoolean("topic.post.enableemotics", true)
                set(value) {
                    getPreferences()!!
                        .edit().putBoolean("topic.post.enableemotics", value!!).apply()
                }

            @JvmStatic
            var enableSign: Boolean?
                get() = getPreferences()!!
                    .getBoolean("topic.post.enablesign", true)
                set(value) {
                    getPreferences()!!
                        .edit().putBoolean("topic.post.enablesign", value!!).apply()
                }

            @JvmStatic
            fun addEmoticToFavorites(name: String) {
                var name = name
                name = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("]", "&#93;").replace("[", "&#91;")
                val favoritesEmotics = emoticFavorites
                val newlist = HashSet<String>()
                newlist.add(name)
                var max = 9
                for (nm in favoritesEmotics!!) {
                    if (TextUtils.isEmpty(nm)) continue
                    if (nm == name) continue
                    newlist.add(nm)
                    max--
                    if (max == 0) break
                }
                getPreferences()!!
                    .edit().putStringSet("topic.post.emotics_favorites", newlist).apply()
            }

            @JvmStatic
            val emoticFavorites: Set<String>?
                get() = try {
                    getPreferences()!!
                        .getStringSet("topic.post.emotics_favorites", HashSet())
                } catch (ignored: Throwable) {
                    HashSet()
                }
        }
    }

    object System {
        @JvmStatic
        val webviewCompatMode: Boolean
            get() = getPreferences()!!
                .getBoolean("webviewCompatMode", false)

        @JvmStatic
        var systemDir: String?
            get() {
                var dir = getInstance().filesDir
                if (dir == null) dir = getInstance().getExternalFilesDir(null)
                assert(dir != null)
                var res = getPreferences()!!
                    .getString("path.system_path", dir!!.path)
                if (!res!!.endsWith(File.separator)) res = res + File.separator
                return res
            }
            set(value) {
                getPreferences()!!
                    .edit().putString("path.system_path", value).apply()
            }

        /**
         * Показывать скролл в браузере
         * Снимите галочку, если программа падает при вызове темы. Например, для Nook Simple Touch
         */
        @JvmStatic
        val isShowWebViewScroll: Boolean
            get() = getPreferences()!!
                .getBoolean("system.WebViewScroll", true)

        @JvmStatic
        fun isScrollUpButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollUp", "24")
        }

        @JvmStatic
        fun isScrollDownButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollDown", "25")
        }

        fun isPrefsButton(keyCode: Int, prefKey: String?, defaultValue: String?): Boolean {
            val scrollUpKeys = "," + getPreferences()
                ?.getString(prefKey, defaultValue)?.replace(" ", "") + ","
            return scrollUpKeys.contains(",$keyCode,")
        }

        @JvmStatic
        val isDevSavePage: Boolean
            get() = getPreferences()!!
                .getBoolean("system.developerSavePage", false)

        @JvmStatic
        val isDevStyle: Boolean
            get() = getPreferences()!!
                .getBoolean("system.developerStyle", false)

        @JvmStatic
        val isDevGrid: Boolean
            get() = getPreferences()!!
                .getBoolean("system.developerGrid", false)

        @JvmStatic
        val isDevBounds: Boolean
            get() = getPreferences()!!
                .getBoolean("system.developerBounds", false)

        @JvmStatic
        val isCurator: Boolean
            get() = getPreferences()!!
                .getBoolean("system.curator", false)

        @JvmStatic
        val drawerMenuPosition: String?
            get() = getPreferences()!!
                .getString("system.drawermenuposition", "left")

        @JvmStatic
        var isEvaluateJavascriptEnabled: Boolean
            get() = getPreferences()
                ?.getBoolean("system.EvaluateJavascriptEnabled", true) ?: true
            set(value) {
                getPreferences()!!
                    .edit().putBoolean("system.EvaluateJavascriptEnabled", value).apply()
            }
    }

    object News {
        @JvmStatic
        var lastSelectedSection: Int
            get() = getPreferences()!!
                .getInt("news.lastselectedsection", 0)
            set(section) {
                getPreferences()!!
                    .edit().putInt("news.lastselectedsection", section).apply()
            }

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("news")

        object List {
            //            public static int getNewsListViewId() {
            @JvmStatic
            val isLoadImages: Boolean
                get() = Preferences.isLoadImages
            //
            //                if ("medium".equals(App.getPreferences().getString("news.list.view", "full"))) {
            //                    return R.layout.item_news_medium;
            //                }
            //                return R.layout.item_news;
            //            }
        }
    }

    class Menu
    object Notice {
        fun setNoticed(id: String?) {
            getPreferences()
                ?.edit()?.putBoolean("notice.$id", true)?.apply()
        }

        fun isNoticed(id: String?): Boolean {
            return getPreferences()
                ?.getBoolean("notice.$id", false) ?: false
        }
    }

    object Files {
        @JvmStatic
        var isConfirmDownload: Boolean
            get() = getPreferences()!!
                .getBoolean("files.ConfirmDownload", true)
            set(b) {
                getPreferences()!!
                    .edit().putBoolean("files.ConfirmDownload", b).apply()
            }
    }

    object Notifications {
        var sound: Uri?
            get() = getSound()
            set(soundUri) {
                setSound(soundUri)
            }

        object SilentMode {
            val startTime: Calendar
                get() = getStartTime()

            fun setStartTime(hourOfDay: Int, minute: Int) {
                setTime("notifiers.silent_mode.start_time", hourOfDay, minute)
            }

            val endTime: Calendar
                get() = getTime("notifiers.silent_mode.end_time")

            fun setEndTime(hourOfDay: Int, minute: Int) {
                setTime("notifiers.silent_mode.end_time", hourOfDay, minute)
            }
        }

        object Qms {
            @JvmStatic
            fun readQmsDone() {
                getPreferences()!!
                    .edit().putBoolean("refreshQMSData", true).apply()
            }

            @JvmStatic
            val isReadDone: Boolean
                get() = getPreferences()!!
                    .getBoolean("refreshQMSData", false)
        }
    }

    object Notes {
        val isLocal: Boolean
            get() = getPreferences()!!
                .getString("notes.placement", "local") != "remote"

        fun setPlacement(value: String?) {
            getPreferences()!!
                .edit().putString("notes.placement", value).apply()
        }

        var remoteUrl: String? by PreferenceStringDelegate("notes.remote.url","")
//            get() = getPreferences()!!
//                .getString("notes.remote.url", "")
//            set(value) {
//                getPreferences()!!
//                    .edit().putString("notes.remote.url", value).apply()
//            }
    }

    data class PreferenceStringDelegate(
        private val key: String,
        private val defaultValue: String
    ) : ReadWriteProperty<String?, String?> {

        private var trimmedValue: String = ""

        operator fun getValue(
            thisRef: String?,
            property: KProperty<*>
        ): String {
            return getPreferences()
                ?.getString(key, defaultValue) ?: defaultValue
        }

        operator fun setValue(
            thisRef: String?,
            property: KProperty<*>, value: String?
        ) {
            getPreferences()?.edit()?.putString(key, value)?.apply()
        }

    }
}