package heartbeatmonitor.core

import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.jsObjectOf
import kotlinx.browser.document
import kotlinx.browser.window
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.emit
import mirrg.kotlin.event.once

fun showToast(message: String) {
    val onClosed = EventRegistry<Unit, Unit>()

    val container = UiContainers.toastContainer

    container.append(
        document.createDivElement().also { toastDiv ->
            toastDiv.className = "toast"
            toastDiv.textContent = message
            window.requestAnimationFrame { toastDiv.classList.add("show") }

            onClosed.once.register {
                toastDiv.classList.remove("show")
                toastDiv.addEventListener("transitionend", {
                    container.removeChild(toastDiv)
                }, jsObjectOf("once" to true))
            }

            val timer = window.setTimeout({ onClosed.emit() }, 5000)
            toastDiv.addEventListener("click", {
                window.clearTimeout(timer)
                onClosed.emit()
            })
        },
    )

    console.log(message)
}
