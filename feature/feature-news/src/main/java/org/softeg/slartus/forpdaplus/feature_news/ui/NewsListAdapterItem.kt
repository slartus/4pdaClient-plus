package org.softeg.slartus.forpdaplus.feature_news.ui

import android.text.Spanned
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdacommon.isToday
import org.softeg.slartus.forpdacommon.isYesterday
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListItem
import java.text.SimpleDateFormat
import java.util.*

data class UiNewsListItem(
    val id: String?,
    val url: String?,
    val title: String?,
    val description: Spanned?,
    val authorId: String?,
    val author: String?,
    val date: String?,
    val imgUrl: String?,
    val commentsCount: String?,
) {

    companion object {
        private val displayDateFormat by lazy { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }
        private val displayTodayTimeFormat by lazy {
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )
        }
        private val displayYesterdayTimeFormat by lazy {
            SimpleDateFormat(
                "'вчера в' HH:mm",
                Locale.getDefault()
            )
        }

        fun NewsListItem.map(): UiNewsListItem {
            return UiNewsListItem(
                this.id,
                this.url,
                this.title,
                this.description.fromHtml(),
                this.authorId,
                this.author,
                dateToDisplay(this.date),
                this.imgUrl,
                this.commentsCount?.toString()
            )
        }

        private fun dateToDisplay(date: Date?): String? {
            return try {
                when {
                    date == null -> null
//                else->DateUtils.getRelativeTimeSpanString(date.time)?.toString()
                    date.isToday -> displayTodayTimeFormat.format(date)
                    date.isYesterday -> displayYesterdayTimeFormat.format(date)
                    else -> displayDateFormat.format(date)
                }
            } catch (ex: Exception) {
                date.toString()
            }
        }
    }
}
