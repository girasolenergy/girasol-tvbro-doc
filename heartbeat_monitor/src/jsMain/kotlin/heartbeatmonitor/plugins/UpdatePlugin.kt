package heartbeatmonitor.plugins

import hello.AbstractPlugin
import hello.UiContainers
import kotlinx.browser.document
import kotlinx.browser.window

object UpdatePlugin : AbstractPlugin("UpdatePlugin") {
    override suspend fun applyImpl() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("button").also { button ->
                button.asDynamic().type = "button"
                button.textContent = "Update"
                button.addEventListener("click", { window.asDynamic().scheduleUpdate() })
            },
        )
    }
}
