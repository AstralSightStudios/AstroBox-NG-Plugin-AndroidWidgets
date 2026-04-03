package moe.astralsight.astrobox.plugin.android_widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AstroBoxWidgetSnapshotParserTest {
    @Test
    fun parseSnapshotReadsConnectedDevicePayload() {
        val json = """
            {
              "updatedAt": "09:41",
              "connectedDevicePanel": {
                "connected": true,
                "deviceName": "Xiaomi Watch S4",
                "deviceType": "round",
                "freeStorage": "1.3GB",
                "freeStorageProgress": 40.0,
                "lastCharge": "2 days ago",
                "isCharing": true,
                "battery": 88,
                "systemVersion": "HyperOS 3.0",
                "watchfaceCount": 17,
                "quickAppCount": 5
              }
            }
        """.trimIndent()

        val snapshot = AstroBoxWidgetSnapshotParser.parse(json)

        assertEquals("09:41", snapshot.updatedAt)
        assertTrue(snapshot.connectedDevicePanel.connected)
        assertEquals("Xiaomi Watch S4", snapshot.connectedDevicePanel.deviceName)
        assertEquals("round", snapshot.connectedDevicePanel.deviceType)
        assertEquals(88, snapshot.connectedDevicePanel.battery)
    }

    @Test
    fun parseSnapshotFallsBackToDisconnectedDefaults() {
        val snapshot = AstroBoxWidgetSnapshotParser.parse("{}")

        assertEquals("--:--", snapshot.updatedAt)
        assertFalse(snapshot.connectedDevicePanel.connected)
        assertEquals("N/A", snapshot.connectedDevicePanel.deviceName)
        assertEquals("round", snapshot.connectedDevicePanel.deviceType)
        assertEquals(0, snapshot.connectedDevicePanel.watchfaceCount)
    }
}
