package hello

import kotlinx.browser.document
import kotlinx.browser.window

object ThemeTogglePlugin : AbstractPlugin() {
    override suspend fun applyImpl() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("button").also { button ->
                button.asDynamic().type = "button"
                KanbanBro.event.addEventListener("themeChanged", { _ ->
                    button.textContent = when (val theme = KanbanBro.theme) {
                        null -> "Theme: Auto"
                        "light" -> "Theme: Light"
                        "dark" -> "Theme: Dark"
                        else -> "Theme: $theme"
                    }
                })
                button.addEventListener("click", { _ ->
                    val oldTheme = KanbanBro.theme
                    val newTheme = if (oldTheme == null) "light" else if (oldTheme == "light") "dark" else null
                    window.asDynamic().setTheme(newTheme)
                })
            },
        )
    }
}
