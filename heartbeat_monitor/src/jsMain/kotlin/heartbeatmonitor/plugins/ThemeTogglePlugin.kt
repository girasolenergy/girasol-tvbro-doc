package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Theme
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.util.createButtonElement
import kotlinx.browser.document
import mirrg.kotlin.event.initialEmit

object ThemeTogglePlugin : AbstractPlugin("ThemeTogglePlugin") {
    override suspend fun apply() {
        UiContainers.topbarRightContainer.prepend(
            document.createButtonElement().also { button ->
                button.type = "button"
                Theme.onActualThemeChanged.initialEmit.register {
                    button.textContent = "Theme: ${Theme.currentThemeOverride.value?.title ?: "Auto"}"
                }
                button.addEventListener("click", { _ ->
                    Theme.currentThemeOverride.value = when (Theme.currentThemeOverride.value) {
                        null -> Theme.LIGHT
                        Theme.LIGHT -> Theme.DARK
                        Theme.DARK -> null
                    }
                })
            },
        )
    }
}
