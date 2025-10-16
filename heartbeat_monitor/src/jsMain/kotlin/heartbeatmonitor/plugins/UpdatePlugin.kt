package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.UiContainers
import kotlinx.browser.document

object UpdatePlugin : AbstractPlugin("UpdatePlugin") {
    override suspend fun apply() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("button").also { button ->
                button.asDynamic().type = "button"
                button.textContent = "Update"
                button.addEventListener("click", { Card.scheduleUpdate() })
            },
        )
    }
}
