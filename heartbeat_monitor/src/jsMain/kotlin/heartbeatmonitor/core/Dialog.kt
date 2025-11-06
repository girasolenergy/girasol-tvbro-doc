package heartbeatmonitor.core

import heartbeatmonitor.util.createButtonElement
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.gap
import kotlinx.browser.document
import mirrg.kotlin.event.EmittableEventRegistry
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.emit
import mirrg.kotlin.event.once
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement

interface DialogContext {
    val frame: Element
    val onClosed: EmittableEventRegistry<Unit, Unit, Unit>
}

fun showDialog(initializer: DialogContext.() -> Unit) {
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
                    initializer(object : DialogContext {
                        override val frame get() = frameDiv
                        override val onClosed get() = onClosed
                    })
                },
            )
        },
    )
}

context(context: DialogContext)
fun frame(block: Element.() -> Unit = {}) {
    block(context.frame)
}

context(context: DialogContext, parent: Element)
fun element(element: Element) {
    parent.append(element)
}

context(context: DialogContext, parent: Element)
fun container(block: HTMLDivElement.() -> Unit = {}) {
    element(document.createDivElement().also { div ->
        div.className = "dialog-container"
        block(div)
    })
}

context(context: DialogContext, parent: Element)
fun actions(block: HTMLDivElement.() -> Unit = {}) {
    element(document.createDivElement().also { div ->
        div.style.display = "flex"
        div.style.justifyContent = "end"
        div.style.gap = "8px"
        block(div)
    })
}

context(context: DialogContext, parent: Element)
fun leftRight(leftBlock: HTMLDivElement.() -> Unit = {}, rightBlock: HTMLDivElement.() -> Unit = {}) {
    element(document.createDivElement().also { div ->
        div.style.display = "flex"
        div.style.gap = "8px"
        div.style.alignItems = "center"
        div.append(
            document.createDivElement().also { leftDiv ->
                leftDiv.style.display = "flex"
                leftDiv.style.gap = "8px"
                leftDiv.style.alignItems = "center"
                leftBlock(leftDiv)
            },
            document.createDivElement().also { rightDiv ->
                rightDiv.style.marginLeft = "auto"
                rightDiv.style.display = "flex"
                rightDiv.style.gap = "8px"
                rightDiv.style.alignItems = "center"
                rightBlock(rightDiv)
            },
        )
    })
}

context(context: DialogContext, parent: Element)
fun title(title: String) {
    element(document.createDivElement().also { div ->
        div.textContent = title
        div.style.fontWeight = "700"
    })
}

context(context: DialogContext, parent: Element)
fun textButton(title: String, block: HTMLButtonElement.() -> Unit = {}) {
    element(document.createButtonElement().also { button ->
        button.type = "button"
        button.classList.add("dialog-button")
        button.textContent = title
        block(button)
    })
}

context(context: DialogContext, parent: Element)
fun textTransparentButton(title: String, block: HTMLButtonElement.() -> Unit = {}) {
    element(document.createButtonElement().also { button ->
        button.type = "button"
        button.classList.add("dialog-transparent-button")
        button.textContent = title
        block(button)
    })
}

context(context: DialogContext, parent: Element)
fun closeButton(title: String = "Close") {
    textButton(title) {
        onClick {
            context.onClosed.emit()
        }
    }
}

context(context: DialogContext, parent: Element)
fun onClick(block: () -> Unit = {}) {
    parent.addEventListener("click", { block() })
}
