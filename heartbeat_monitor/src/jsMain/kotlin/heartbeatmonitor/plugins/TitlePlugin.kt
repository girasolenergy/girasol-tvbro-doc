package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.util.createDivElement
import kotlinx.browser.document

object TitlePlugin : AbstractPlugin("TitlePlugin") {
    override suspend fun apply() {
        document.title = "KanbanBro Heartbeat Monitor"
        UiContainers.topbarRightContainer.prepend(
            document.createDivElement().also { titleDiv ->
                titleDiv.className = "topbar-title"
                titleDiv.textContent = document.title
            },
        )
    }
}
