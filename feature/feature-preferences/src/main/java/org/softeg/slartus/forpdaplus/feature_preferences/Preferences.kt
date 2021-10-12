package org.softeg.slartus.forpdaplus.feature_preferences

import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import org.softeg.slartus.forpdacommon.Connectivity.isConnectedWifi
import org.softeg.slartus.forpdacommon.ExtPreferences
import org.softeg.slartus.forpdaplus.core_ui.AppTheme
import org.softeg.slartus.forpdaplus.feature_preferences.App.getInstance
import org.softeg.slartus.forpdaplus.feature_preferences.App.getPreferences
import org.softeg.slartus.forpdaplus.feature_preferences.ClientPreferences.Notifications.SilentMode.getStartTime
import org.softeg.slartus.forpdaplus.feature_preferences.ClientPreferences.Notifications.SilentMode.getTime
import org.softeg.slartus.forpdaplus.feature_preferences.ClientPreferences.Notifications.SilentMode.setTime
import org.softeg.slartus.forpdaplus.feature_preferences.ClientPreferences.Notifications.getSound
import org.softeg.slartus.forpdaplus.feature_preferences.ClientPreferences.Notifications.setSound
import java.io.File
import java.util.*

object Preferences {
    private const val DEFAULT_FONT_SIZE = 16
    private const val MAX_FONT_SIZE = 72

    private val appCookiesPath: String
        get() = System.systemDir + "4pda_cookies"

    @JvmStatic
    val cookieFilePath: String
        get() {
            var res = getPreferences()?.getString("cookies.path", "") ?: ""
            if (TextUtils.isEmpty(res)) res = appCookiesPath
            return res.replace("/", File.separator)
        }

    @JvmStatic
    var isHideFab: Boolean by appPreference("fab.hide", true)

    @JvmStatic
    var isHideArrows: Boolean by appPreference("hideArrows", false)

    @JvmStatic
    fun getFontSize(prefix: String): Int {
        val res = ExtPreferences.parseInt(getPreferences(), "$prefix.FontSize", DEFAULT_FONT_SIZE)
        return res.coerceIn(1, MAX_FONT_SIZE)
    }

    @JvmStatic
    fun setFontSize(prefix: String, value: Int) {
        getPreferences()
            ?.edit()?.putString("$prefix.FontSize", value.toString())?.apply()
    }

    object Common {
        object Overall {
            val DEFAULT_ACCENT_COLOR = Color.rgb(2, 119, 189)
            val DEFAULT_ACCENT_COLOR_PRESSED = Color.rgb(0, 89, 159)
            private const val DEFAULT_MAIN_ACCENT_COLOR = "pink"

            @JvmStatic
            var mainAccentColor: String by appPreference(
                "mainAccentColor",
                DEFAULT_MAIN_ACCENT_COLOR
            )

            @JvmStatic
            var accentColor: Int by appPreference(
                "accentColor",
                DEFAULT_ACCENT_COLOR.toString().toLong(10).toInt()
            )

            @JvmStatic
            var accentColorPressed: Int by appPreference(
                "accentColorPressed",
                DEFAULT_ACCENT_COLOR_PRESSED.toString().toLong(10).toInt()
            )

            @JvmStatic
            var accentColorEdited: Boolean by appPreference("accentColorEdited", false)

            @JvmStatic
            var webViewFont: Int by appPreference("webViewFont", 0)

            @JvmStatic
            var webViewFontName: String by appPreference("webViewFontName", "")

            @JvmStatic
            var appStyle: String by appPreference("appstyle", AppTheme.DEFAULT_APP_THEME.toString())

            @JvmStatic
            var userInfoBg: String by appPreference("userInfoBg", "")

            @JvmStatic
            var isUserBackground: Boolean by appPreference("isUserBackground", false)
        }
    }

    object Lists {

        private const val MAX_LAST_ACTIONS_COUNT = 5

        @JvmStatic
        val scrollByButtons: Boolean by appPreference("lists.scroll_by_buttons", false)

        @JvmStatic
        var lastSelectedList: String by appPreference("list.last_selected_list", "News_Pages")

        @JvmStatic
        fun addLastAction(name: String) {
            val lastActions =
                listOf(name) +
                        (getPreferences()
                            ?.getString("lists.last_actions", "")
                            ?.split("|")
                            ?.filter { it != name }
                            ?.take(MAX_LAST_ACTIONS_COUNT - 1) ?: emptyList())

            getPreferences()?.edit()?.putString("lists.last_actions", lastActions.joinToString("|"))
                ?.apply()
        }

        @JvmStatic
        val lastActions: Array<String>
            get() = getPreferences()!!
                .getString("lists.last_actions", "")!!.split("|").toTypedArray()

        @JvmStatic
        val isRefresh: Boolean by appPreference("lists.refresh", true)

        @JvmStatic
        val isRefreshOnTab: Boolean by appPreference("lists.refresh_on_tab", true)
    }

    object List {
        private const val DEFAULT_LIST_SORT = "sortorder.asc"

        @JvmStatic
        fun setListSort(listName: String, value: String?) {
            getPreferences()!!
                .edit().putString("$listName.list.sort", value).apply()
        }

        @JvmStatic
        fun defaultListSort(): String {
            return DEFAULT_LIST_SORT
        }

        @JvmStatic
        fun getListSort(listName: String, defaultValue: String?): String? {
            return getPreferences()!!
                .getString("$listName.list.sort", defaultValue)
        }

        val startForumId: String? by appPreference("Forum.start_forum_id", null)

        @JvmStatic
        val showSubMain: Boolean by appPreference("showSubMain", false)

        fun setStartForum(id: String?, title: String?) {
            getPreferences()?.edit()
                ?.putString("Forum.start_forum_id", id)
                ?.putString("Forum.start_forum_title", title)
                ?.apply()
        }

        object Favorites {
            @JvmStatic
            val isLoadFullPagesList: Boolean by appPreference("lists.favorites.load_all", false)
        }
    }

    object Forums {
        @JvmStatic
        val isShowImages: Boolean by appPreference("forum.list.show_images", true)
    }

    object Topic {
        private const val DEFAULT_HISTORY_LIMIT = 5

        @JvmStatic
        val readersAndWriters: Boolean by appPreference("theme.ShowReadersAndWriters", false)

        @JvmStatic
        val spoilFirstPost: Boolean by appPreference("theme.SpoilFirstPost", true)

        @JvmStatic
        var confirmSend: Boolean by appPreference("theme.ConfirmSend", true)

        @JvmStatic
        val isShowAvatars: Boolean
            get() {
                val loadImagesType = showAvatarsOpt
                return if (loadImagesType == 1) {
                    isConnectedWifi(getInstance())
                } else loadImagesType == 0
            }

        @JvmStatic
        var showAvatarsOpt: Int by appPreference("topic.show_avatar_opt", 0)

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("theme")

        @JvmStatic
        val historyLimit: Int by appPreference("topic.history_limit", DEFAULT_HISTORY_LIMIT)

        object Post {
            private const val MAX_FAV_EMOTICS = 9

            @JvmStatic
            var enableEmotics: Boolean by appPreference("topic.post.enableemotics", true)

            @JvmStatic
            var enableSign: Boolean by appPreference("topic.post.enablesign", true)

            @JvmStatic
            fun addEmoticToFavorites(name1: String) {
                val name = name1
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("]", "&#93;")
                    .replace("[", "&#91;")

                val favoritesEmotics =
                    listOf(name) +
                            (emoticFavorites
                                ?.filter { it != name }
                                ?.take(MAX_FAV_EMOTICS - 1) ?: emptyList())
                getPreferences()!!
                    .edit()
                    .putStringSet("topic.post.emotics_favorites", favoritesEmotics.toHashSet())
                    .apply()
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

        val onlyCustomScript: Boolean by appPreference("only_custom_script", false)

        val isAccelerateGif: Boolean by appPreference("isAccelerateGif", false)

        @JvmStatic
        val webviewCompatMode: Boolean by appPreference("webviewCompatMode", false)

        @JvmStatic
        var systemDir: String
            get() {
                var dir = getInstance().filesDir
                if (dir == null) dir = getInstance().getExternalFilesDir(null)
                assert(dir != null)
                var res = getPreferences()
                    ?.getString("path.system_path", dir?.path) ?: dir?.path
                if (!res!!.endsWith(File.separator)) res += File.separator
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
        val isShowWebViewScroll: Boolean by appPreference("system.WebViewScroll", true)

        @JvmStatic
        fun isScrollUpButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollUp", "24")
        }

        @JvmStatic
        fun isScrollDownButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollDown", "25")
        }

        private fun isPrefsButton(keyCode: Int, prefKey: String?, defaultValue: String?): Boolean {
            val scrollUpKeys = "," + getPreferences()
                ?.getString(prefKey, defaultValue)?.replace(" ", "") + ","
            return scrollUpKeys.contains(",$keyCode,")
        }

        @JvmStatic
        val isDevSavePage: Boolean by appPreference("system.developerSavePage", false)

        @JvmStatic
        val isDevStyle: Boolean by appPreference("system.developerStyle", false)

        @JvmStatic
        val isDevGrid: Boolean by appPreference("system.developerGrid", false)

        @JvmStatic
        val isDevBounds: Boolean by appPreference("system.developerBounds", false)

        @JvmStatic
        val isCurator: Boolean by appPreference("system.curator", false)

        @JvmStatic
        val drawerMenuPosition: String? by appPreference(
            "system.drawermenuposition", "left"
        )

        @JvmStatic
        var isEvaluateJavascriptEnabled: Boolean by appPreference(
            "system.EvaluateJavascriptEnabled", true
        )

        const val DEFAULT_LANG = "default"

        @JvmStatic
        val lang: String by appPreference("lang", DEFAULT_LANG)
    }

    object News {
        @JvmStatic
        var lastSelectedSection: Int by appPreference("news.lastselectedsection", 0)

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("news")

        object List {
            @JvmStatic
            val isLoadImages: Boolean
                get() {
                    val loadImagesType =
                        ExtPreferences.parseInt(getPreferences(), "news.list.loadimages", 1)
                    return if (loadImagesType == 2) {
                        isConnectedWifi(getInstance())
                    } else loadImagesType == 1
                }

            @JvmStatic
            val listView: String by appPreference("news.list.view", "full")
        }
    }

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
        var isConfirmDownload by AppPreferenceBoolean("files.ConfirmDownload", true)
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
            val isReadDone: Boolean by appPreference("refreshQMSData", false)
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

        var remoteUrl by appPreference("notes.remote.url", "")
    }

    fun getInt(key: String, defaultValue: Int): Int = appPreference(key, defaultValue).value
    fun getFloat(key: String, defaultValue: Float): Float = appPreference(key, defaultValue).value
    fun getString(key: String, defaultValue: String?): String? =
        appPreference(key, defaultValue).value
}
