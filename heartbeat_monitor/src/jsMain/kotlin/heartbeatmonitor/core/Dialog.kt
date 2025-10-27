package heartbeatmonitor.core

import heartbeatmonitor.util.createDivElement
import kotlinx.browser.document
import mirrg.kotlin.event.EmittableEventRegistry
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.emit
import mirrg.kotlin.event.once
import org.w3c.dom.Element

fun showDialog(initializer: (Element, EmittableEventRegistry<Unit, Unit, Unit>) -> Unit) {
    val onClosed = EventRegistry<Unit, Unit>()

    document.body!!.append(
        document.createDivElement().also { overlayDiv ->
            overlayDiv.className = "dialog-overlay"

            overlayDiv.addEventListener("click", { e ->
                if (e.target === overlayDiv) onClosed.emit()
            })
            onClosed.once.register {
                document.body!!.removeChild(overlayDiv)
            }

            overlayDiv.append(
                document.createDivElement().also { frameDiv ->
                    frameDiv.className = "dialog-frame"
                    frameDiv.append(
                        document.createDivElement().also { containerDiv ->
                            containerDiv.className = "dialog-container"
                            initializer(containerDiv, onClosed)
                        },
                    )
                },
            )
        },
    )
}
