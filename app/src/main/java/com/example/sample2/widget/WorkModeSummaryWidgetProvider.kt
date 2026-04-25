package com.example.sample2.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.sample2.MainActivity
import com.example.sample2.R
import com.example.sample2.data.MessageV2
import com.example.sample2.model.JournalJsonStorage
import java.util.Calendar

class WorkModeSummaryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WorkModeSummaryWidgetProvider::class.java)
            val appWidgetIds = manager.getAppWidgetIds(componentName)
            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, manager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val summary = WorkModeSummary.fromMessages(JournalJsonStorage.loadMessages(context))
            val views = RemoteViews(context.packageName, R.layout.widget_work_mode_summary).apply {
                setTextViewText(R.id.widget_delegate_count, summary.delegate.toString())
                setTextViewText(R.id.widget_challenge_count, summary.challenge.toString())
                setTextViewText(R.id.widget_breakdown_count, summary.breakdown.toString())
                setTextViewText(R.id.widget_instruct_count, summary.instruct.toString())
                setTextViewText(R.id.widget_quick_action_count, summary.quickAction.toString())

                val launchIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

private data class WorkModeSummary(
    val delegate: Int,
    val challenge: Int,
    val breakdown: Int,
    val instruct: Int,
    val quickAction: Int
) {
    companion object {
        fun fromMessages(messages: List<MessageV2>): WorkModeSummary {
            val todayMessages = messages.filter { it.isToday() }
            return WorkModeSummary(
                delegate = todayMessages.count { it.flags.delegate },
                challenge = todayMessages.count { it.flags.challenge },
                breakdown = todayMessages.count { it.flags.breakdown },
                instruct = todayMessages.count { it.flags.instruct },
                quickAction = todayMessages.count { it.flags.quickAction }
            )
        }
    }
}

private fun MessageV2.isToday(): Boolean {
    val messageCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val todayCalendar = Calendar.getInstance()
    return messageCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
}
