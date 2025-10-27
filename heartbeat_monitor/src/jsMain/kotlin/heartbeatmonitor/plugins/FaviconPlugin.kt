package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.util.createLinkElement
import kotlinx.browser.document

object FaviconPlugin : AbstractPlugin("FaviconPlugin") {
    override suspend fun apply() {
        document.head!!.append(
            document.createLinkElement().also { link ->
                link.asDynamic().rel = "icon"
                link.asDynamic().href = "../icon.png"
            },
        )
    }
}
