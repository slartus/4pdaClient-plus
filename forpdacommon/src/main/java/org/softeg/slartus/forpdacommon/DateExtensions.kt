package org.softeg.slartus.forpdacommon

import android.text.format.DateUtils
import java.util.*

fun Date.toForumDate() = Functions.getForumDateTime(this)
val Date.isToday: Boolean
    get() = DateUtils.isToday(this.time)

val Date.isYesterday: Boolean
    get() = DateUtils.isToday(this.time + DateUtils.DAY_IN_MILLIS)

/**
 * Created by slinkin on 17.12.13.
 */
object DateExtensions {
    @JvmStatic
    fun getDaysBetween(date1: Date, date2: Date): Int {
        return ((date1.time - date2.time)
                / (1000 * 60 * 60 * 24)).toInt()
    }

    @JvmStatic
    fun getHoursBetween(date1: Date, date2: Date): Int {
        return ((date1.time - date2.time)
                / (1000 * 60 * 60)).toInt()
    }

    @JvmStatic
    val timeOutForNextMinute: Long
        get() {
            val nextMinute = GregorianCalendar.getInstance()[Calendar.MINUTE] + 1
            val nextCalendar: Calendar = GregorianCalendar()
            nextCalendar.time = GregorianCalendar.getInstance().time
            nextCalendar[Calendar.MINUTE] = nextMinute
            nextCalendar[Calendar.SECOND] = 0
            nextCalendar[Calendar.MILLISECOND] = 0
            return nextCalendar.time.time - GregorianCalendar.getInstance().time.time
        }
}