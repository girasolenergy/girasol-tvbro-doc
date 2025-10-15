package hello

import kotlinx.browser.document

object TitlePlugin : AbstractPlugin() {
    override suspend fun applyImpl() {
        document.title = "Kanban Bro Heartbeat Monitor"
        UiContainers.topbarRightContainer.prepend(
            document.createElement("div").also { titleDiv ->
                titleDiv.className = "topbar-title"
                titleDiv.textContent = document.title
            },
        )
    }
}
