package moe.astralsight.astrobox.plugin.android_widgets

import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Test

class WidgetLayoutInflationTest {
    @Test
    fun miniWidgetLayoutDoesNotUseGenericViewTags() {
        assertNoGenericViewTags("src/main/res/layout/widget_mini.xml")
    }

    @Test
    fun panelWidgetLayoutDoesNotUseGenericViewTags() {
        assertNoGenericViewTags("src/main/res/layout/widget_panel.xml")
    }

    private fun assertNoGenericViewTags(relativePath: String) {
        val xmlPath = Paths.get(relativePath)
        val document = Files.newInputStream(xmlPath).use { input ->
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
        }

        val viewNodes = document.getElementsByTagName("View")
        assertFalse("Generic <View> tags are unsafe in RemoteViews layouts", viewNodes.length > 0)
    }
}
