package org.softeg.slartus.forpdaplus

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.entities.MentionsCount
import org.softeg.slartus.forpdaplus.core.entities.QmsCount
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppMainScope
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

@AndroidEntryPoint
class QmsWidgetProvider : AppWidgetProvider() {
    private val scope = AppMainScope()

    @Inject
    lateinit var qmsService: QmsService

    @Inject
    lateinit var qmsCountParser: Parser<QmsCount>

    @Inject
    lateinit var mentionsCountParser: Parser<MentionsCount>

    @Inject
    lateinit var httpClient: AppHttpClient

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_qms).apply {
            setOnClickPendingIntent(R.id.root, pendingIntent)
            setViewVisibility(R.id.progress_view, View.VISIBLE)
        }
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                views.setImageViewResource(R.id.info_icon, getUserIconRes())
            }.onFailure {
                views.setViewVisibility(R.id.progress_view, View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)

                Log.e("QmsWidgetProvider", it.toString())
                it.printStackTrace()
            }.onSuccess {
                views.setViewVisibility(R.id.progress_view, View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private suspend fun getUserIconRes(): Int {
        val url = "https://${HostHelper.host}/about"
        val pageBody = httpClient.performGet(url)

        val qmsCount = qmsCountParser.parse(pageBody)?.count
        val mentionsCount = mentionsCountParser.parse(pageBody)?.count

        val logined = Client.getInstance().logined

        return when {
            qmsCount != null && qmsCount > 0 -> R.drawable.message_text
            mentionsCount != null && mentionsCount > 0 -> R.drawable.message_text
            qmsCount == 0 && mentionsCount == 0 -> R.drawable.account
            logined -> R.drawable.account
            else -> R.drawable.account_outline
        }
    }

    companion object {
        @JvmStatic
        fun sendUpdateIntent(context: Context) {
            val intent = Intent(context, QmsWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(
                    context,
                    QmsWidgetProvider::class.java
                )
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
