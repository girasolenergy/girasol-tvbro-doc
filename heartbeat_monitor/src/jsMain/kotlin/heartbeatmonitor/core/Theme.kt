package hello.heartbeatmonitor.core

import hello.KanbanBro
import hello.heartbeatmonitor.util.getValue
import hello.heartbeatmonitor.util.property
import hello.heartbeatmonitor.util.setValue
import hello.heartbeatmonitor.util.xmap
import hello.mirrg.kotlin.event.EventRegistry
import hello.mirrg.kotlin.event.ObservableValue
import hello.mirrg.kotlin.event.emit
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window

enum class Theme(val slug: String, val title: String) {
    LIGHT("light", "Light"),
    DARK("dark", "Dark"),
    ;

    companion object {
        private const val LOCAL_STORAGE_KEY = "kanbanbro.theme"
        private val reverseMap = entries.associateBy { it.slug }
        var storage by localStorage.property(LOCAL_STORAGE_KEY).xmap({ reverseMap[it] }, { it?.slug })

        val currentThemeOverride = ObservableValue<Theme?>(null)

        private val isWindowDarkMode = window.matchMedia("(prefers-color-scheme: dark)")
        fun getActualTheme() = currentThemeOverride.value ?: if (isWindowDarkMode.matches) DARK else LIGHT
        val onActualThemeChanged = EventRegistry<Unit, Unit>()

        fun init() {

            // Theme Override
            KanbanBro.event.addEventListener("pluginLoaded", {
                currentThemeOverride.value = storage
            })
            currentThemeOverride.register {
                storage = currentThemeOverride.value
            }

            // Actual Theme
            onActualThemeChanged.register {
                val actualTheme = getActualTheme()
                document.documentElement!!.setAttribute("data-theme", actualTheme.slug)
                document.documentElement.asDynamic().style.colorScheme = actualTheme.slug
            }
            isWindowDarkMode.addEventListener("change", {
                onActualThemeChanged.emit()
            })
            currentThemeOverride.register {
                onActualThemeChanged.emit()
            }

        }
    }
}
