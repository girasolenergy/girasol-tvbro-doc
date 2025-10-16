package hello.heartbeatmonitor.modules

import hello.KanbanBro
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun initTheme() {

    KanbanBro.theme = localStorage.getItem("kanbanbro.theme")

    KanbanBro.event.addEventListener("themeChanged", {
        val theme = if (KanbanBro.theme == null) {
            if (window.matchMedia("(prefers-color-scheme: dark)").matches) "dark" else "light"
        } else {
            KanbanBro.theme as String
        }
        document.documentElement!!.setAttribute("data-theme", theme)
        document.documentElement.asDynamic().style.colorScheme = theme
    })

    window.asDynamic().setTheme = { theme: String? ->
        KanbanBro.theme = theme
        if (theme == null) {
            localStorage.removeItem("kanbanbro.theme")
        } else {
            localStorage.setItem("kanbanbro.theme", theme)
        }
        KanbanBro.event.dispatchEvent(Event("themeChanged"))
    }

    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", {
        KanbanBro.event.dispatchEvent(Event("themeChanged"))
    })
    KanbanBro.event.addEventListener("pluginLoaded", {
        KanbanBro.event.dispatchEvent(Event("themeChanged"))
    })

}
