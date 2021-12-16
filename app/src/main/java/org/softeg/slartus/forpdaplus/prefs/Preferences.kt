package org.softeg.slartus.forpdaplus.prefs

import android.net.Uri
import android.text.TextUtils
import org.softeg.slartus.forpdaapi.ClientPreferences
import org.softeg.slartus.forpdacommon.Connectivity.isConnectedWifi
import org.softeg.slartus.forpdacommon.ExtPreferences
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo
import java.io.File
import java.util.*

object Preferences {
    @JvmStatic
    private val isLoadImages: Boolean
        get() {
            val loadImagesType =
                ExtPreferences.parseInt(App.getInstance().preferences, "news.list.loadimages", 1)
            return if (loadImagesType == 2) {
                isConnectedWifi(App.getContext())
            } else loadImagesType == 1
        }

    @JvmStatic
    var isHideFab: Boolean?
        get() = App.getInstance().preferences.getBoolean("fab.hide", true)
        set(hide) {
            App.getInstance().preferences.edit().putBoolean("fab.hide", hide!!).apply()
        }

    @JvmStatic
    var isHideArrows: Boolean?
        get() = App.getInstance().preferences.getBoolean("hideArrows", false)
        set(hide) {
            App.getInstance().preferences.edit().putBoolean("hideArrows", hide!!).apply()
        }

    @JvmStatic
    fun getFontSize(prefix: String): Int {
        val res = ExtPreferences.parseInt(App.getInstance().preferences, "$prefix.FontSize", 16)
        return Math.max(Math.min(res, 72), 1)
    }

    @JvmStatic
    fun setFontSize(prefix: String, value: Int) {
        App.getInstance().preferences.edit().putString("$prefix.FontSize", Integer.toString(value))
            .apply()
    }

    object Lists {
        @JvmStatic
        val scrollByButtons: Boolean
            get() = App.getInstance().preferences.getBoolean("lists.scroll_by_buttons", false)

        @JvmStatic
        var lastSelectedList: String?
            get() = App.getInstance().preferences.getString(
                "list.last_selected_list",
                NewsPagerBrickInfo().name
            )
            set(listName) {
                App.getInstance().preferences.edit().putString("list.last_selected_list", listName)
                    .apply()
            }

        @JvmStatic
        fun addLastAction(name: String) {
            val lastActions = App.getInstance().preferences.getString("lists.last_actions", "")!!
                .split("\\|").toTypedArray()
            val newValue = StringBuilder("$name|")
            var max = 5
            for (nm in lastActions) {
                if (TextUtils.isEmpty(nm)) continue
                if (nm == name) continue
                newValue.append(nm).append("|")
                max--
                if (max == 0) break
            }
            App.getInstance().preferences.edit()
                .putString("lists.last_actions", newValue.toString()).apply()
        }

        @JvmStatic
        val lastActions: Array<String>
            get() = App.getInstance().preferences.getString("lists.last_actions", "")!!.split("\\|")
                .toTypedArray()
        @JvmStatic
        val isRefresh: Boolean
            get() = App.getInstance().preferences.getBoolean("lists.refresh", true)
        @JvmStatic
        val isRefreshOnTab: Boolean
            get() = App.getInstance().preferences.getBoolean("lists.refresh_on_tab", true)
    }

    object List {
        @JvmStatic
        fun setListSort(listName: String, value: String?) {
            App.getInstance().preferences.edit().putString("$listName.list.sort", value).apply()
        }

        @JvmStatic
        fun defaultListSort(): String {
            return "sortorder.asc"
        }

        @JvmStatic
        fun getListSort(listName: String, defaultValue: String?): String? {
            return App.getInstance().preferences.getString("$listName.list.sort", defaultValue)
        }

        object Favorites {
            @JvmStatic
            val isLoadFullPagesList: Boolean
                get() = App.getInstance().preferences.getBoolean("lists.favorites.load_all", false)
        }
    }

    object Forums {
        @JvmStatic
        val isShowImages: Boolean
            get() = App.getInstance().preferences.getBoolean("forum.list.show_images", true)
    }

    object Topic {
        @JvmStatic
        val readersAndWriters: Boolean
            get() = App.getInstance().preferences.getBoolean("theme.ShowReadersAndWriters", false)

        @JvmStatic
        val spoilFirstPost: Boolean
            get() = App.getInstance().preferences.getBoolean("theme.SpoilFirstPost", true)

        @JvmStatic
        var confirmSend: Boolean
            get() = App.getInstance().preferences.getBoolean("theme.ConfirmSend", true) ?: true
            set(value) {
                App.getInstance().preferences.edit().putBoolean("theme.ConfirmSend", value)
                    .apply()
            }

        @JvmStatic
        val isShowAvatars: Boolean
            get() {
                val loadImagesType = showAvatarsOpt
                return if (loadImagesType == 1) {
                    isConnectedWifi(App.getContext())
                } else loadImagesType == 0
            }

        @JvmStatic
        var showAvatarsOpt: Int
            get() = ExtPreferences.parseInt(
                App.getInstance().preferences,
                "topic.show_avatar_opt",
                0
            )
            set(value) {
                App.getInstance().preferences
                    .edit().putInt("topic.show_avatar_opt", value).apply()
            }

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("theme")

        @JvmStatic
        val historyLimit: Int
            get() = ExtPreferences.parseInt(App.getInstance().preferences, "topic.history_limit", 5)

        object Post {
            @JvmStatic
            var enableEmotics: Boolean?
                get() = App.getInstance().preferences.getBoolean("topic.post.enableemotics", true)
                set(value) {
                    App.getInstance().preferences.edit()
                        .putBoolean("topic.post.enableemotics", value!!).apply()
                }

            @JvmStatic
            var enableSign: Boolean?
                get() = App.getInstance().preferences.getBoolean("topic.post.enablesign", true)
                set(value) {
                    App.getInstance().preferences.edit()
                        .putBoolean("topic.post.enablesign", value!!).apply()
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
                App.getInstance().preferences.edit()
                    .putStringSet("topic.post.emotics_favorites", newlist).apply()
            }

            @JvmStatic
            val emoticFavorites: Set<String>?
                get() = try {
                    App.getInstance().preferences.getStringSet(
                        "topic.post.emotics_favorites",
                        HashSet()
                    )
                } catch (ignored: Throwable) {
                    HashSet()
                }
        }
    }

    object System {
        @JvmStatic
        val webviewCompatMode: Boolean
            get() = App.getInstance().preferences.getBoolean("webviewCompatMode", false)

        @JvmStatic
        var systemDir: String?
            get() {
                var dir = App.getInstance().filesDir
                if (dir == null) dir = App.getInstance().getExternalFilesDir(null)
                assert(dir != null)
                var res = App.getInstance().preferences.getString("path.system_path", dir!!.path)
                if (!res!!.endsWith(File.separator)) res = res + File.separator
                return res
            }
            set(value) {
                App.getInstance().preferences.edit().putString("path.system_path", value).apply()
            }

        /**
         * Показывать скролл в браузере
         * Снимите галочку, если программа падает при вызове темы. Например, для Nook Simple Touch
         */
        @JvmStatic
        val isShowWebViewScroll: Boolean
            get() = App.getInstance().preferences.getBoolean("system.WebViewScroll", true)

        @JvmStatic
        fun isScrollUpButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollUp", "24")
        }

        @JvmStatic
        fun isScrollDownButton(keyCode: Int): Boolean {
            return isPrefsButton(keyCode, "keys.scrollDown", "25")
        }

        fun isPrefsButton(keyCode: Int, prefKey: String?, defaultValue: String?): Boolean {
            val scrollUpKeys = "," + App.getInstance().preferences
                .getString(prefKey, defaultValue)!!.replace(" ", "") + ","
            return scrollUpKeys.contains(",$keyCode,")
        }

        @JvmStatic
        val isDevSavePage: Boolean
            get() = App.getInstance().preferences.getBoolean("system.developerSavePage", false)
        @JvmStatic
        val isDevStyle: Boolean
            get() = App.getInstance().preferences.getBoolean("system.developerStyle", false)
        @JvmStatic
        val isDevGrid: Boolean
            get() = App.getInstance().preferences.getBoolean("system.developerGrid", false)
        @JvmStatic
        val isDevBounds: Boolean
            get() = App.getInstance().preferences.getBoolean("system.developerBounds", false)
        @JvmStatic
        val isCurator: Boolean
            get() = App.getInstance().preferences.getBoolean("system.curator", false)

        @JvmStatic
        val drawerMenuPosition: String?
            get() = App.getInstance().preferences.getString("system.drawermenuposition", "left")

        @JvmStatic
        var isEvaluateJavascriptEnabled: Boolean
            get() = App.getInstance().preferences
                .getBoolean("system.EvaluateJavascriptEnabled", true)
            set(value) {
                App.getInstance().preferences.edit()
                    .putBoolean("system.EvaluateJavascriptEnabled", value).apply()
            }
    }

    object News {
        @JvmStatic
        var lastSelectedSection: Int
            get() = App.getInstance().preferences.getInt("news.lastselectedsection", 0)
            set(section) {
                App.getInstance().preferences.edit().putInt("news.lastselectedsection", section)
                    .apply()
            }

        @JvmStatic
        val fontSize: Int
            get() = getFontSize("news")

        object List {
            @JvmStatic
            val isLoadImages: Boolean
                get() = Preferences.isLoadImages

            @JvmStatic
            val newsListViewId: Int
                get() = if ("medium" == App.getInstance().preferences.getString(
                        "news.list.view",
                        "full"
                    )
                ) {
                    R.layout.item_news_medium
                } else R.layout.item_news
        }
    }

    class Menu
    object Notice {
        @JvmStatic
        fun setNoticed(id: String?) {
            App.getInstance().preferences.edit().putBoolean("notice.$id", true).apply()
        }
        @JvmStatic
        fun isNoticed(id: String?): Boolean {
            return App.getInstance().preferences.getBoolean("notice.$id", false)
        }
    }

    object Files {
        @JvmStatic
        var isConfirmDownload: Boolean
            get() = App.getInstance().preferences.getBoolean("files.ConfirmDownload", true)
            set(b) {
                App.getInstance().preferences.edit().putBoolean("files.ConfirmDownload", b).apply()
            }
    }

    object Notifications {
        var sound: Uri?
            get() = ClientPreferences.Notifications.getSound(App.getContext())
            set(soundUri) {
                ClientPreferences.Notifications.setSound(App.getContext(), soundUri)
            }

        internal object SilentMode {
            val startTime: Calendar
                get() = ClientPreferences.Notifications.SilentMode.getStartTime(App.getContext())

            fun setStartTime(hourOfDay: Int, minute: Int) {
                ClientPreferences.Notifications.SilentMode.setTime(
                    App.getContext(),
                    "notifiers.silent_mode.start_time",
                    hourOfDay,
                    minute
                )
            }

            val endTime: Calendar
                get() = ClientPreferences.Notifications.SilentMode.getTime(
                    App.getContext(),
                    "notifiers.silent_mode.end_time"
                )

            fun setEndTime(hourOfDay: Int, minute: Int) {
                ClientPreferences.Notifications.SilentMode.setTime(
                    App.getContext(),
                    "notifiers.silent_mode.end_time",
                    hourOfDay,
                    minute
                )
            }
        }

        object Qms {
            @JvmStatic
            fun readQmsDone() {
                App.getInstance().preferences.edit().putBoolean("refreshQMSData", true).apply()
            }
            @JvmStatic
            val isReadDone: Boolean
                get() = App.getInstance().preferences.getBoolean("refreshQMSData", false)
        }
    }

    object Notes {
        @JvmStatic
        val isLocal: Boolean
            get() = App.getInstance().preferences.getString("notes.placement", "local") != "remote"

        @JvmStatic

        fun setPlacement(value: String?) {
            App.getInstance().preferences.edit().putString("notes.placement", value).apply()
        }

        @JvmStatic
        var remoteUrl: String?
            get() = App.getInstance().preferences.getString("notes.remote.url", "")
            set(value) {
                App.getInstance().preferences.edit().putString("notes.remote.url", value).apply()
            }
    }
}