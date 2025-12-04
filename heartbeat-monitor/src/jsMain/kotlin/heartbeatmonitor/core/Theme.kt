package heartbeatmonitor.core

import heartbeatmonitor.util.colorScheme
import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.property
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.ObservableValue
import mirrg.kotlin.event.emit
import onPluginLoaded
import org.w3c.dom.HTMLElement

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
            onPluginLoaded.register {
                currentThemeOverride.value = storage
            }
            currentThemeOverride.register {
                storage = currentThemeOverride.value
            }

            // Actual Theme
            onActualThemeChanged.register {
                val actualTheme = getActualTheme()
                document.documentElement!!.setAttribute("data-theme", actualTheme.slug)
                document.documentElement.unsafeCast<HTMLElement>().style.colorScheme = actualTheme.slug
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
