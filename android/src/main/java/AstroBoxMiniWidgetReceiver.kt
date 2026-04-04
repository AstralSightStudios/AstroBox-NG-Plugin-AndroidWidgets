package moe.astralsight.astrobox.plugin.android_widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews

class AstroBoxMiniWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val TAG = "AstroBoxWidgets"

        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
        ) {
            val snapshot = AstroBoxWidgetStore.read(context)
            Log.i(
                TAG,
                "update mini widgets: count=${appWidgetIds.size}, updatedAt=${snapshot.updatedAt}, connected=${snapshot.connectedDevicePanel.connected}"
            )

            val panel = snapshot.connectedDevicePanel
            val views = RemoteViews(context.packageName, R.layout.widget_mini).apply {
                // Status dot color
                setImageViewResource(
                    R.id.status_dot,
                    if (panel.connected) R.drawable.status_dot_connected else R.drawable.status_dot_disconnected
                )

                // Connection status text
                setTextViewText(
                    R.id.widget_status,
                    if (panel.connected) "Connected" else "Disconnected"
                )

                // Device name
                setTextViewText(R.id.widget_device_name, panel.deviceName)

                // Device icon based on device type
                val deviceIconRes = when (panel.deviceType.lowercase()) {
                    "round" -> R.drawable.ic_widget_device_round
                    "band" -> R.drawable.ic_widget_device_band
                    else -> R.drawable.ic_widget_device_round
                }
                setImageViewResource(R.id.widget_device_icon, deviceIconRes)

                // System version
                setTextViewText(R.id.widget_system_version, panel.systemVersion)

                // Click to open app
                setOnClickPendingIntent(R.id.widget_root, buildLaunchPendingIntent(context))
            }

            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun buildLaunchPendingIntent(context: Context): PendingIntent {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(context.packageName, "${context.packageName}.MainActivity")
                }

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            return PendingIntent.getActivity(
                context,
                1001,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
