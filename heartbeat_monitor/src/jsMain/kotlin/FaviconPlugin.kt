package hello

import kotlinx.browser.document

object FaviconPlugin : Plugin {
    override fun apply() {
        document.head!!.append(
            document.createElement("link").also { link ->
                link.asDynamic().rel = "icon"
                link.asDynamic().href = "../icon.png"
            },
        )
    }
}
