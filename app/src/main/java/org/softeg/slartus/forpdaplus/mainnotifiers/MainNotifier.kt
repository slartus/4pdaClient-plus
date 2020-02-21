package org.softeg.slartus.forpdaplus.mainnotifiers

import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.DateExtensions
import org.softeg.slartus.forpdacommon.ExtPreferences
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.BuildConfig
import java.util.*

/*
 * Created by slartus on 03.06.2014.
 */
abstract class MainNotifier(private val notifiersManager: NotifiersManager, var name: String, private val periodInHours: Int) {
    fun addToStack(materialDialog: MaterialDialog?) {
        notifiersManager.addNotifyDialog(materialDialog!!)
    }

    protected val isTime: Boolean
        get() {
            if (periodInHours == 0) return true
            val lastShowpromoCalendar = GregorianCalendar()
            val prefs = App.getInstance().preferences
            val lastCheckDate = ExtPreferences.getDateTime(prefs, "notifier.$name", null)
            if (lastCheckDate == null) {
                saveTime()
                return true
            }
            lastShowpromoCalendar.time = lastCheckDate
            val calendar = GregorianCalendar()
            calendar.time = Date()
            val hours = DateExtensions.getHoursBetween(calendar.time, lastShowpromoCalendar.time)
            return hours >= periodInHours
        }

    protected fun saveTime() {
        val editor = App.getInstance().preferences.edit()
        ExtPreferences.putDateTime(editor, "notifier.$name", Date())
        editor.apply()
    }

    companion object {
        @JvmStatic
        protected val appVersion: String
            get() = BuildConfig.VERSION_NAME
    }

}