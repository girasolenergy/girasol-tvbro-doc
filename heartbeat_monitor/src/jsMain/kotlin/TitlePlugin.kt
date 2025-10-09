package hello

import kotlinx.browser.document

object TitlePlugin : Plugin {
    override fun apply() {
        document.title = "Kanban Bro Heartbeat Monitor"
        UiContainers.topbarRightContainer.prepend(
            document.createElement("div").also { titleDiv ->
                titleDiv.className = "topbar-title"
                titleDiv.textContent = document.title
            },
        )
    }
}
