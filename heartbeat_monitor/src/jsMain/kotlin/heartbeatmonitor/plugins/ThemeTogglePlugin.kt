package heartbeatmonitor.plugins

import hello.UiContainers
import hello.heartbeatmonitor.core.AbstractPlugin
import hello.heartbeatmonitor.core.Theme
import hello.mirrg.kotlin.event.initialEmit
import kotlinx.browser.document

object ThemeTogglePlugin : AbstractPlugin("ThemeTogglePlugin") {
    override suspend fun apply() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("button").also { button ->
                button.asDynamic().type = "button"
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
