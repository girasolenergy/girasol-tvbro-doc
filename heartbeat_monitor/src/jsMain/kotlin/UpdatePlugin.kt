package hello

import kotlinx.browser.document
import kotlinx.browser.window

object UpdatePlugin : Plugin {
    override fun apply() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("button").also { button ->
                button.asDynamic().type = "button";
                button.textContent = "Update";
                button.addEventListener("click",  { window.asDynamic().scheduleUpdate() })
            },
        )
    }
}
