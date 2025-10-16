package heartbeatmonitor.core

import kotlinx.browser.document

object UiContainers {
    val topbarLeftContainer = document.getElementById("topbar-left-container")!!
    val topbarRightContainer = document.getElementById("topbar-right-container")!!
    val cards = document.getElementById("cards")!!
}
