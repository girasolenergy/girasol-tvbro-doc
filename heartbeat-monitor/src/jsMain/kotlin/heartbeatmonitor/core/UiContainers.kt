package heartbeatmonitor.core

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.asList

object UiContainers {
    val topbarLeftContainer = document.getElementById("topbar-left-container")!!
    val topbarRightContainer = document.getElementById("topbar-right-container")!!
    val cards = document.getElementById("cards").unsafeCast<CardsElement>()
    val toastContainer = document.getElementById("toast-container")!!
}

abstract external class CardsElement : HTMLDivElement

val CardsElement.cardElements get() = this.children.asList().map { it.unsafeCast<CardElement>() }
