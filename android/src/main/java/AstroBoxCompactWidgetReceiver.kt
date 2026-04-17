package moe.astralsight.astrobox.plugin.android_widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

class AstroBoxCompactWidgetReceiver : AppWidgetProvider() {
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
                "update compact widgets: count=${appWidgetIds.size}, updatedAt=${snapshot.updatedAt}, connected=${snapshot.connectedDevicePanel.connected}"
            )

            val panel = snapshot.connectedDevicePanel
            val views = RemoteViews(context.packageName, R.layout.widget_compact).apply {
                // Status dot
                setImageViewResource(
                    R.id.status_dot,
                    if (panel.connected) R.drawable.status_dot_connected else R.drawable.status_dot_disconnected
                )

                // Connection status text
                setTextViewText(
                    R.id.widget_status,
                    context.getString(
                        if (panel.connected) R.string.widget_status_connected else R.string.widget_status_disconnected
                    )
                )

                // Device name
                setTextViewText(R.id.widget_device_name, panel.deviceName)

                // Device icon based on device type
                val deviceIconRes = when (panel.deviceType.lowercase()) {
                    "band" -> R.drawable.ic_widget_device_band
                    "bandpro" -> R.drawable.ic_widget_device_bandpro
                    "redmiwatch" -> R.drawable.ic_widget_device_redmiwatch
                    "watchs" -> R.drawable.ic_widget_device_watchs
                    "round" -> R.drawable.ic_widget_device_round
                    else -> R.drawable.ic_widget_device_round
                }
                setImageViewResource(R.id.widget_device_icon, deviceIconRes)

                // Battery icon and value
                setImageViewResource(
                    R.id.battery_icon,
                    batteryIconRes(panel.battery, panel.isCharing)
                )
                setTextViewText(R.id.widget_battery_value, "${panel.battery}%")

                // Battery text color: green when charging, white otherwise
                val batteryTextColor = if (panel.isCharing) 0xFF33C759.toInt() else 0xFFFFFFFF.toInt()
                setTextColor(R.id.widget_battery_value, batteryTextColor)

                // Click to open app
                setOnClickPendingIntent(R.id.widget_root, buildLaunchPendingIntent(context))
            }

            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AstroBoxCompactWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            updateWidgets(context, appWidgetManager, appWidgetIds)
        }

        private fun batteryIconRes(battery: Int, isCharging: Boolean): Int {
            if (isCharging) return R.drawable.ic_widget_battery_charging

            val clampedBattery = battery.coerceIn(0, 100)
            return when {
                clampedBattery == 100 -> R.drawable.ic_widget_battery_full
                clampedBattery >= 85 -> R.drawable.ic_widget_battery_90
                clampedBattery >= 68 -> R.drawable.ic_widget_battery_75
                clampedBattery >= 51 -> R.drawable.ic_widget_battery_50
                clampedBattery >= 34 -> R.drawable.ic_widget_battery_25
                clampedBattery >= 17 -> R.drawable.ic_widget_battery_20
                clampedBattery >= 1 -> R.drawable.ic_widget_battery_10
                else -> R.drawable.ic_widget_battery_0
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
                1004,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
