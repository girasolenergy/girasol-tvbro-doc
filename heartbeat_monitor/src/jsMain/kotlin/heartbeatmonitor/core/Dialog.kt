package heartbeatmonitor.core

import heartbeatmonitor.util.jsObjectOf
import heartbeatmonitor.util.new
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

fun showDialog(initializer: (Element, dynamic) -> Unit) {
    val dialogEvent = new(window.asDynamic().EventTarget)

    document.body!!.append(
        document.createElement("div").also { overlayDiv ->
            overlayDiv.className = "dialog-overlay"

            overlayDiv.addEventListener("click", { e ->
                if (e.target === overlayDiv) dialogEvent.dispatchEvent(Event("close"))
            })
            dialogEvent.addEventListener("close", {
                document.body!!.removeChild(overlayDiv)
            }, jsObjectOf("once" to true))

            overlayDiv.append(
                document.createElement("div").also { frameDiv ->
                    frameDiv.className = "dialog-frame"
                    frameDiv.append(
                        document.createElement("div").also { containerDiv ->
                            containerDiv.className = "dialog-container"
                            initializer(containerDiv, dialogEvent)
                        },
                    )
                },
            )
        },
    )
}
