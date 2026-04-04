package moe.astralsight.astrobox.plugin.android_widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import kotlin.math.roundToInt

class AstroBoxUnifiedWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        private const val TAG = "AstroBoxWidgets"
        // 阈值：宽度超过 200dp 认为是中号布局
        private const val PANEL_MODE_MIN_WIDTH_DP = 200

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val isPanelMode = minWidth >= PANEL_MODE_MIN_WIDTH_DP

            Log.i(TAG, "update widget id=$appWidgetId, minWidth=$minWidth, isPanelMode=$isPanelMode")

            val views = if (isPanelMode) {
                buildPanelViews(context)
            } else {
                buildMiniViews(context)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AstroBoxUnifiedWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            Log.i(TAG, "update all widgets: count=${appWidgetIds.size}")

            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun buildMiniViews(context: Context): RemoteViews {
            val snapshot = AstroBoxWidgetStore.read(context)
            val panel = snapshot.connectedDevicePanel

            return RemoteViews(context.packageName, R.layout.widget_mini).apply {
                // Status dot
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

                // Updated at
                setTextViewText(R.id.widget_updated_at, "Updated ${snapshot.updatedAt}")

                // Click to open app
                setOnClickPendingIntent(R.id.widget_root, buildLaunchPendingIntent(context))
            }
        }

        private fun buildPanelViews(context: Context): RemoteViews {
            val snapshot = AstroBoxWidgetStore.read(context)
            val panel = snapshot.connectedDevicePanel

            return RemoteViews(context.packageName, R.layout.widget_panel).apply {
                // === Left Column ===

                // Storage
                setTextViewText(R.id.widget_storage_value, panel.freeStorage)
                setProgressBar(
                    R.id.widget_storage_progress,
                    100,
                    panel.freeStorageProgress.toInt(),
                    false
                )

                // Battery
                setTextViewText(R.id.widget_battery_value, "${panel.battery}%")
                setTextViewText(R.id.widget_last_charge, panel.lastCharge)
                setImageViewResource(
                    R.id.battery_icon,
                    batteryIconRes(panel.battery, panel.isCharing)
                )

                // === Center Column ===

                // Status dot
                setImageViewResource(
                    R.id.status_dot,
                    if (panel.connected) R.drawable.status_dot_connected else R.drawable.status_dot_disconnected
                )

                // Connection status
                setTextViewText(
                    R.id.widget_status,
                    if (panel.connected) "Connected" else "Disconnected"
                )

                // Device icon and name
                val deviceIconRes = when (panel.deviceType.lowercase()) {
                    "round" -> R.drawable.ic_widget_device_round
                    "band" -> R.drawable.ic_widget_device_band
                    else -> R.drawable.ic_widget_device_round
                }
                setImageViewResource(R.id.widget_device_icon, deviceIconRes)
                setTextViewText(R.id.widget_device_name, panel.deviceName)

                // System version
                setTextViewText(R.id.widget_system_version, panel.systemVersion)

                // === Right Column ===

                // Watchfaces
                setImageViewResource(R.id.widget_watchface_icon, R.drawable.ic_widget_watchface)
                setTextViewText(R.id.widget_watchfaces_value, panel.watchfaceCount.toString())

                // Quick apps
                setTextViewText(R.id.widget_quick_apps_value, panel.quickAppCount.toString())

                // Updated at
                setTextViewText(R.id.widget_updated_at, "Updated ${snapshot.updatedAt}")

                // Click to open app
                setOnClickPendingIntent(R.id.widget_root, buildLaunchPendingIntent(context))
            }
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
                1003,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
