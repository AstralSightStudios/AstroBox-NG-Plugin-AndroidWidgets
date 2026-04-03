package moe.astralsight.astrobox.plugin.android_widgets

import android.content.Context
import android.util.Log
import java.io.File

object AstroBoxWidgetStore {
    private const val FILE_NAME = "widget-data.json"
    private const val TAG = "AstroBoxWidgets"

    fun write(context: Context, json: String) {
        val output = File(context.filesDir, FILE_NAME)
        output.writeText(json)
        Log.i(TAG, "write snapshot -> ${output.absolutePath}")
    }

    fun read(context: Context): AstroBoxWidgetSnapshot {
        val input = File(context.filesDir, FILE_NAME)
        if (!input.exists()) {
            Log.w(TAG, "read snapshot: file missing, use placeholder")
            return AstroBoxWidgetSnapshot()
        }

        val raw = input.readText()
        Log.i(TAG, "read snapshot: size=${raw.length}")
        return AstroBoxWidgetSnapshotParser.parse(raw)
    }
}
