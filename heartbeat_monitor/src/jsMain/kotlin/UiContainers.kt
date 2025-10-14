package hello

import kotlinx.browser.document

object UiContainers {
    val topbarLeftContainer = document.getElementById("topbar-left-container")!!
    val topbarRightContainer = document.getElementById("topbar-right-container")!!
}
