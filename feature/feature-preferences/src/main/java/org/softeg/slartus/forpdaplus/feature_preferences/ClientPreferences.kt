package org.softeg.slartus.forpdaplus.feature_preferences

import android.media.RingtoneManager
import android.net.Uri
import java.util.*

/*
 * Created by slinkin on 18.07.2014.
 */
object ClientPreferences {
    object Notifications {
        @JvmStatic
        fun useSound(): Boolean {
            val prefs = App.getPreferences()
            return prefs?.getBoolean("notifiers.service.use_sound", true) ?: true
        }

        @JvmStatic
        fun setSound(soundUri: Uri?) {
            val prefs = App.getPreferences()
            prefs?.edit()?.putString("notifiers.service.sound", soundUri?.toString())?.commit()
        }

        @JvmStatic
        fun getSound(): Uri? {
            val prefs = App.getPreferences()
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val soundString = prefs?.getString("notifiers.service.sound", defaultUri?.toString())
                ?: return null
            return try {
                Uri.parse(soundString)
            } catch (ex: Throwable) {
                null
            }
        }

        @JvmStatic
        fun isDefaultSound(): Boolean {
            val prefs = App.getPreferences()
            return prefs?.getBoolean("notifiers.service.is_default_sound", true) ?: true
        }

        object SilentMode {
            @JvmStatic
            fun getStartTime(): Calendar {
                return getTime("notifiers.silent_mode.start_time")
            }

            @JvmStatic
            fun setStartTime(hourOfDay: Int, minute: Int) {
                setTime("notifiers.silent_mode.start_time", hourOfDay, minute)
            }

            @JvmStatic
            fun getEndTime(): Calendar {
                return getTime("notifiers.silent_mode.end_time")
            }

            fun setEndTime(hourOfDay: Int, minute: Int) {
                setTime("notifiers.silent_mode.end_time", hourOfDay, minute)
            }

            @JvmStatic
            fun getTime(key: String?): Calendar {
                val prefs = App.getPreferences()
                val date = Date(prefs?.getLong(key, 0) ?: 0)
                val cal = Calendar.getInstance()
                cal.time = date
                cal[Calendar.SECOND] = 0
                return cal
            }

            @JvmStatic
            fun setTime(key: String?, hourOfDay: Int, minute: Int) {
                val cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = hourOfDay
                cal[Calendar.MINUTE] = minute
                val prefs = App.getPreferences()
                prefs?.edit()?.putLong(key, cal.timeInMillis)?.apply()
            }

            @JvmStatic
            fun isEnabled(): Boolean {
                val prefs = App.getPreferences()
                return prefs?.getBoolean("notifiers.silent_mode.enabled", false) ?: false
            }
        }
    }
}