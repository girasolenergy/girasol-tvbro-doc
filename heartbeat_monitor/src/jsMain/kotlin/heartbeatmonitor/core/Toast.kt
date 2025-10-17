package heartbeatmonitor.core

import heartbeatmonitor.util.jsObjectOf
import heartbeatmonitor.util.new
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun showToast(message: String) {
    val toastEvent = new(window.asDynamic().EventTarget)

    val container = UiContainers.toastContainer

    container.append(
        document.createElement("div").also { toastDiv ->
            toastDiv.className = "toast"
            toastDiv.textContent = message
            window.requestAnimationFrame { toastDiv.classList.add("show") }

            toastEvent.addEventListener("dismiss", {
                toastDiv.classList.remove("show")
                toastDiv.addEventListener("transitionend", {
                    container.removeChild(toastDiv)
                }, jsObjectOf("once" to true))
            }, jsObjectOf("once" to true))

            val timer = window.setTimeout({ toastEvent.dispatchEvent(Event("dismiss")) }, 5000)
            toastDiv.addEventListener("click", {
                window.clearTimeout(timer)
                toastEvent.dispatchEvent(Event("dismiss"))
            })
        },
    )

    console.log(message)
}
