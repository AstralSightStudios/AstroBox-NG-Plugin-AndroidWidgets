package moe.astralsight.astrobox.plugin.android_widgets

import android.content.Context

object AstroBoxWidgetUpdater {
    fun refreshAll(context: Context) {
        AstroBoxUnifiedWidgetReceiver.updateAllWidgets(context)
    }
}
