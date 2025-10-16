package heartbeatmonitor.plugins

import hello.heartbeatmonitor.core.AbstractPlugin
import hello.heartbeatmonitor.core.UiContainers
import kotlinx.browser.document

object TitlePlugin : AbstractPlugin("TitlePlugin") {
    override suspend fun apply() {
        document.title = "Kanban Bro Heartbeat Monitor"
        UiContainers.topbarRightContainer.prepend(
            document.createElement("div").also { titleDiv ->
                titleDiv.className = "topbar-title"
                titleDiv.textContent = document.title
            },
        )
    }
}
