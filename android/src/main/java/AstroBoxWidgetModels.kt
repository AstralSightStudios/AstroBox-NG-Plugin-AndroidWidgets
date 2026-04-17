package moe.astralsight.astrobox.plugin.android_widgets

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
data class SyncWidgetSnapshotArgs(
    val json: String = "{}"
)

@Keep
data class AstroBoxConnectedDevicePanel(
    val connected: Boolean = false,
    val deviceName: String = "N/A",
    val deviceType: String = "round",
    val freeStorage: String = "N/A",
    val freeStorageProgress: Float = 0f,
    val lastCharge: String = "N/A",
    val isCharing: Boolean = false,
    val battery: Int = 0,
    val systemVersion: String = "N/A",
    val watchfaceCount: Int = 0,
    val quickAppCount: Int = 0,
)

@Keep
data class AstroBoxWidgetSnapshot(
    val updatedAt: String = "--:--",
    val connectedDevicePanel: AstroBoxConnectedDevicePanel = AstroBoxConnectedDevicePanel(),
)

object AstroBoxWidgetSnapshotParser {
    fun parse(rawJson: String?): AstroBoxWidgetSnapshot {
        if (rawJson.isNullOrBlank()) {
            return AstroBoxWidgetSnapshot()
        }

        return runCatching {
            val root = JSONObject(rawJson)
            val panel = root.optJSONObject("connectedDevicePanel")

            AstroBoxWidgetSnapshot(
                updatedAt = root.optString("updatedAt", "--:--").ifBlank { "--:--" },
                connectedDevicePanel = AstroBoxConnectedDevicePanel(
                    connected = panel?.optBoolean("connected") ?: false,
                    deviceName = panel?.optString("deviceName", "N/A").orDefault("N/A"),
                    deviceType = panel?.optString("deviceType", "round").orDefault("round"),
                    freeStorage = panel?.optString("freeStorage", "N/A").orDefault("N/A"),
                    freeStorageProgress = (panel?.optDouble("freeStorageProgress") ?: 0.0).toFloat(),
                    lastCharge = panel?.optString("lastCharge", "N/A").orDefault("N/A"),
                    isCharing = panel?.optBoolean("isCharing") ?: false,
                    battery = panel?.optInt("battery") ?: 0,
                    systemVersion = panel?.optString("systemVersion", "N/A").orDefault("N/A"),
                    watchfaceCount = panel?.optInt("watchfaceCount") ?: 0,
                    quickAppCount = panel?.optInt("quickAppCount") ?: 0,
                ),
            )
        }.getOrElse {
            AstroBoxWidgetSnapshot()
        }
    }

    private fun String?.orDefault(fallback: String): String {
        return if (this.isNullOrBlank()) fallback else this
    }
}
