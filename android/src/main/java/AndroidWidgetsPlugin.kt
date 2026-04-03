package moe.astralsight.astrobox.plugin.android_widgets

import android.app.Activity
import android.util.Log
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.Plugin

@TauriPlugin
class AndroidWidgetsPlugin(private val activity: Activity) : Plugin(activity) {
    companion object {
        private const val TAG = "AstroBoxWidgets"
    }

    @Command
    fun syncWidgetSnapshot(invoke: Invoke) {
        val args = invoke.parseArgs(SyncWidgetSnapshotArgs::class.java)
        Log.i(TAG, "syncWidgetSnapshot: size=${args.json.length}")
        AstroBoxWidgetStore.write(activity, args.json)
        AstroBoxWidgetUpdater.refreshAll(activity)
        invoke.resolve()
    }
}
